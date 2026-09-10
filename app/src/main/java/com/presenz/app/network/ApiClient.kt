package com.presenz.app.network

import com.presenz.app.util.Prefs
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // Retrofit instance is rebuilt if the configured base URL changes at runtime
    // (e.g. user updates server IP in a real device scenario).
    private var cachedBaseUrl: String? = null
    private var retrofit: Retrofit? = null

    fun service(): ApiService {
        val url = Prefs.baseUrl
        if (retrofit == null || cachedBaseUrl != url) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            cachedBaseUrl = url
        }
        return retrofit!!.create(ApiService::class.java)
    }

    fun teacherBearer(): String =
        "Bearer ${Prefs.teacherToken ?: ""}"

    fun studentBearer(): String =
        "Bearer ${Prefs.studentToken ?: ""}"
}
