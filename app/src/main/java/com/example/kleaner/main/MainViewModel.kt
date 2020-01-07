package com.example.kleaner.main

import android.util.Log
import com.example.kleaner.base.BaseViewModel
import com.example.net.Requester
import com.example.net.beans.WeatherEntity

class MainViewModel : BaseViewModel() {
    override fun onCreate() {
        Log.e(TAG, "onCreate")
        Requester.queryWeather("深圳", object : Requester.RequestCallback<WeatherEntity> {
            override fun onSuccess(t: WeatherEntity) {
                Log.e(TAG, "success " + t.desc)
            }

            override fun onFailed(throwable: Throwable) {
                Log.e(TAG, "failed " + throwable.message)
            }

            override fun onComplete() {
                Log.e(TAG, "complete")
            }

        })
//        val params: MutableMap<String, Any> = HashMap()
//        params["city"] = "深圳"
//        request("weather_mini", null, params, object : Callback<Weather> {
//            override fun succeed(data: Weather) {
//                Log.e("wwh", "-->(): $data")
//            }
//
//            override fun failed(throwable: Throwable) {
//                Log.e(TAG, "failed " + throwable.message)
//            }
//
//        })
    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy")
    }
}