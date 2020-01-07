package com.example.net

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class RequestManager private constructor() {

    private val READ_TIMEOUT: Long = 5
    private val WRITE_TIMEOUT: Long = 5
    private val CONNECT_TIMEOUT: Long = 5

    companion object Statics {
        fun getInstance(): RequestManager {
            return RetrofitHolder.requestManager
        }

        private val serviceMap: ConcurrentHashMap<String, Any> = ConcurrentHashMap()
    }

    private object RetrofitHolder {
        val requestManager: RequestManager = RequestManager()
    }

    private fun <T> createRetrofit(clz: Class<T>, baseUrl: String): T {
        //添加请求头
        val logging = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Log.e("wwh", "RetrofitLog---->$message")
            }
        })
        logging.level = HttpLoggingInterceptor.Level.BODY
        val client: OkHttpClient = OkHttpClient.Builder()
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(clz)
    }

    public fun <T> getService(clz: Class<T>, url: String): T {
        var t: T
        if (!serviceMap.containsKey(clz as Any)) {
            t = createRetrofit(clz, url)
            serviceMap[clz.simpleName] = t as Any
        } else {
            t = serviceMap[clz.simpleName] as T
        }
        return t
    }


}