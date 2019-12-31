package com.example.kleaner.main

import android.util.Log
import com.example.kleaner.base.BaseViewModel

class MainViewModel : BaseViewModel() {
    override fun onCreate() {
        Log.e(TAG, "onCreate")
    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy")
    }
}