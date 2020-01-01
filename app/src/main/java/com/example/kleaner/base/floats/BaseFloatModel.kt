package com.example.kleaner.base.floats

import android.os.Bundle
import android.os.Handler
import com.example.kleaner.events.ActionObserver

abstract class BaseFloatModel {
    internal val TAG: String = javaClass.simpleName
    private val DEFAULT_DELAY: Long = 8000
    internal lateinit var timeControl: ActionObserver<Any>
    internal lateinit var exitAction: ActionObserver<Any>
    private lateinit var timeHandler: Handler
    private lateinit var timeTrigger: Runnable

    fun create() {
        timeControl = ActionObserver()
        exitAction = ActionObserver()
        initTimer()
        onCreate()
    }

    private fun initTimer() {
        timeHandler = Handler()
        timeTrigger = Runnable {
            timeControl.active()
        }
        timeHandler.postDelayed(timeTrigger, DEFAULT_DELAY)
    }

    open fun stopTimer() {
        timeHandler.removeCallbacks(timeTrigger)
    }

    open fun resetTimer() {
        resetTimer(DEFAULT_DELAY)
    }

    open fun resetTimer(millis: Long) {
        stopTimer()
        timeHandler.postDelayed(timeTrigger, millis)
    }

    protected open fun stopSelf() {
        exitAction.active()
    }

    fun destroy() {
        stopTimer()
        onDestroy()
    }

    abstract fun onCreate()

    abstract fun parseIntent(argument: Bundle?)

    abstract fun onDestroy()
}
