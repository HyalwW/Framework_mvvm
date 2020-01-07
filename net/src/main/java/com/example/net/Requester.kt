package com.example.net

import com.example.net.beans.BaseResponse
import com.example.net.beans.WeatherEntity
import com.example.net.services.ApiService
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

class Requester {
    companion object Statics {
        private val sGson: Gson = Gson()
        private val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaTypeOrNull()

        private val manager: RequestManager = RequestManager.getInstance()
        fun queryWeather(city: String, callback: RequestCallback<WeatherEntity>) {
            val subscriber: MySubscribe<WeatherEntity> = MySubscribe(callback)
            val observable = manager.getService(ApiService::class.java, Url.baseUrl())
                .requestTest("http://wthrcdn.etouch.cn/weather_mini", city)
            observable.throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber)
        }

        /**
         * 请求json对象，自己解析，传键值对
         */
        fun requestJson(
            url: String,
            headers: MutableMap<String, String>?,
            params: MutableMap<String, Any>,
            callback: RequestCallback<JsonObject>
        ) {
            if (headers == null) {
                params["Content-Type"] = "application/json"
            }
            requestJson(url, headers, map2Json(params), callback)
        }

        /**
         * 请求json对象，自己解析，传特殊类型requestBody
         */
        fun requestJson(
            url: String,
            headers: MutableMap<String, String>?,
            body: RequestBody,
            callback: RequestCallback<JsonObject>
        ) {
            val header: MutableMap<String, String> = headers ?: HashMap()
            val subscriber: MySubscribe<JsonObject> = MySubscribe(callback)
            val observable = manager.getService(ApiService::class.java, Url.baseUrl()).post(url, header, body)
            observable.throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber)
        }

        /**
         * 请求实体类,类型为 T
         */
        fun <T> requestBean(
            url: String,
            headers: MutableMap<String, String>?,
            body: RequestBody,
            callback: RequestCallback<T>
        ) {
            requestJson(url, headers, body, object : RequestCallback<JsonObject> {
                override fun onSuccess(t: JsonObject) {
                    try {
                        val argument: Type = if (callback is RequestCallback) {
                            val parameterizedType = callback::class.java.genericInterfaces[0] as ParameterizedType
                            parameterizedType.actualTypeArguments[0]
                        } else {
                            val parameterizedType = callback::class.java.genericSuperclass as ParameterizedType
                            parameterizedType.actualTypeArguments[0]
                        }
                        val response: BaseResponse<T> = json2Data(t, argument)
                        if (response.code == ResponseCodes.SUCCESS) {
                            callback.onSuccess(response.t!!)
                        } else {
                            callback.onFailed(Throwable(response.msg))
                        }
                    } catch (e: Exception) {
                        callback.onFailed(e)
                    }
                }

                override fun onFailed(throwable: Throwable) {
                    callback.onFailed(throwable)
                }

                override fun onComplete() {
                    callback.onComplete()
                }

            })
        }

        /**
         * 将键值对转换为json
         */
        fun map2Json(map: MutableMap<String, *>): RequestBody {
            return sGson.toJson(map).toRequestBody(MEDIA_TYPE_JSON)
        }

        /**
         * 将json对象转换为BaseResponse<T>， T已被解析
         */
        fun <T> json2Data(`object`: JsonObject?, arg: Type): BaseResponse<T> {
            val type: Type = ParameterizedTypeImpl(BaseResponse::class.java, arrayOf(arg))
            return sGson.fromJson(`object`, type)
        }
    }

    interface RequestCallback<T> {
        fun onSuccess(t: T)
        fun onFailed(throwable: Throwable)
        fun onComplete()
    }
}