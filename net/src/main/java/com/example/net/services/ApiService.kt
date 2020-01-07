package com.example.net.services

import com.example.net.beans.WeatherEntity
import com.google.gson.JsonObject
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {

    @GET
    fun requestTest(@Url url: String, @Query("city") city: String): Observable<WeatherEntity>

    @POST("{path}")
    fun post(
        @Path(
            value = "path",
            encoded = true
        ) path: String, @HeaderMap headers: Map<String, String>, @Body requestBody: RequestBody
    ): Observable<JsonObject>
}