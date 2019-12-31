package com.example.kleaner

import android.app.Application
import android.util.Log
import com.example.kleaner.data.database.AppDatabase
import com.example.kleaner.data.models.Bean
import com.example.kleaner.utils.Utils

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
        Log.e("wwh", "init")
        AppDatabase.getDatabase().beanDao().insert(Bean(0, " opening me, time : " + System.currentTimeMillis()))
    }

}