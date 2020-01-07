package com.example.kleaner.base

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.net.Requester
import com.example.net.ResponseCodes
import com.example.net.beans.BaseResponse
import com.google.gson.JsonObject
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.CompositeDisposable
import okhttp3.RequestBody
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class BaseViewModel : ViewModel() {
    protected val TAG = javaClass.simpleName
    private var disposable: CompositeDisposable? = null

    init {
        this.onCreate()
    }

    override fun onCleared() {
        super.onCleared()
        if (disposable != null) {
            disposable!!.clear()
            disposable = null
        }
        onDestroy()
    }

    /**
     * 传键值对进行请求
     */
    protected fun <T> request(
        url: String,
        headers: MutableMap<String, String>?,
        params: MutableMap<String, *>,
        callback: Callback<T>
    ) {
        request(url, headers, Requester.map2Json(params), callback)
    }

    /**
     * 传请求体进行请求
     */
    protected fun <T> request(
        url: String,
        headers: MutableMap<String, String>?,
        body: RequestBody,
        callback: Callback<T>
    ) {
        if (disposable == null) {
            disposable = CompositeDisposable()
        }
        disposable!!.add(
            Observable.create(ObservableOnSubscribe<T> { emitter ->
                Requester.requestJson(url, headers, body, object : Requester.RequestCallback<JsonObject> {
                    override fun onSuccess(t: JsonObject) {
                        try {
                            val argument: Type = if (callback is Callback) {
                                val parameterizedType = callback::class.java.genericInterfaces[0] as ParameterizedType
                                parameterizedType.actualTypeArguments[0]
                            } else {
                                val parameterizedType = callback::class.java.genericSuperclass as ParameterizedType
                                parameterizedType.actualTypeArguments[0]
                            }
                            val response: BaseResponse<T> = Requester.json2Data(t, argument)
                            if (response.code == ResponseCodes.SUCCESS) {
                                if (!emitter.isDisposed) {
                                    emitter.onNext(response.t!!)
                                }
                            } else {
                                if (!emitter.isDisposed) {
                                    emitter.onError(java.lang.Exception(response.msg))
                                }
                            }
                        } catch (e: Exception) {
                            if (!emitter.isDisposed) {
                                emitter.onError(e)
                            }
                        }
                    }

                    override fun onFailed(throwable: Throwable) {
                        if (!emitter.isDisposed) {
                            emitter.onError(throwable)
                        }
                    }

                    override fun onComplete() {
                        Log.e(TAG, "request complete")
                    }

                })
            }).subscribe(
                { t -> callback.succeed(t) },
                { throwable -> callback.failed(throwable) })
        )
    }

    abstract fun onCreate()

    abstract fun onDestroy()

    protected interface Callback<T> {
        fun succeed(data: T)
        fun failed(throwable: Throwable)
    }
}