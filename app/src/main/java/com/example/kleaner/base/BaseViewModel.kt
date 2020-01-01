package com.example.kleaner.base

import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.CompositeDisposable

abstract class BaseViewModel : ViewModel() {
    protected val TAG = javaClass.simpleName
    private var disposable: CompositeDisposable? = null

    init {
        this.onCreate()
    }

    override fun onCleared() {
        super.onCleared()
        onDestroy()
    }

    protected fun <T> request(callback: RequestCallback<T>) {
        if (disposable == null) {
            disposable = CompositeDisposable()
        }
        disposable!!.add(
            Observable.create(ObservableOnSubscribe<T> {
                //todo 进行网络请求
            }).subscribe(
                { t -> callback.succeed(t) },
                { throwable -> callback.failed(throwable) })
        )
    }

    abstract fun onCreate()

    abstract fun onDestroy()

    protected interface RequestCallback<T> {
        fun succeed(data: T)
        fun failed(throwable: Throwable)
    }
}