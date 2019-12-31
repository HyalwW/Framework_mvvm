package com.example.kleaner.utils

import android.content.Context

class Utils {
    companion object Static {
        lateinit var context: Context
        fun init(context: Context) {
            this.context = context
        }
    }
}