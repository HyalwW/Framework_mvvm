package com.example.net

import android.util.Log
import io.reactivex.observers.DisposableObserver

class MySubscribe<T>(var callback: Requester.RequestCallback<T>) : DisposableObserver<T>() {
    override fun onStart() {
        super.onStart()
        Log.e("wwh", "request start")
    }

    override fun onNext(t: T) {
        callback.onSuccess(t)
    }

    override fun onError(e: Throwable) {
        callback.onFailed(e)
    }

    override fun onComplete() {
        callback.onComplete()
    }
}