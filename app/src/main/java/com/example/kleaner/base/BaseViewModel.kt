package com.example.kleaner.base

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable

abstract class BaseViewModel : ViewModel() {
    protected val TAG = javaClass.simpleName
    private lateinit var disposable: CompositeDisposable

    init {
        this.onCreate()
    }

    override fun onCleared() {
        super.onCleared()
        onDestroy()
    }

    abstract fun onCreate()

    abstract fun onDestroy()

    protected interface RequestCallback<T> {
        fun succeed(data: T)
        fun failed(throwable: Throwable)
    }
}