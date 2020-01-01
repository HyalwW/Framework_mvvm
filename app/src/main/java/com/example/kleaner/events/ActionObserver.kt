package com.example.kleaner.events

class ActionObserver<T> {
    private var listener: Listener<T>? = null

    fun observe(listener: Listener<T>) {
        this.listener = listener
    }

    fun active() {
        listener?.call(null)
    }

    fun setValue(t: T) {
        listener?.call(t)
    }

    interface Listener<T> {
        fun call(t: T?)
    }
}