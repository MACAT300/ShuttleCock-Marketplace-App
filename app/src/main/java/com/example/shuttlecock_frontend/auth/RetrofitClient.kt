package com.example.shuttlecock_frontend.auth

import com.example.shuttlecock_frontend.data.UserSession
import com.example.shuttlecock_frontend.`interface`.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import retrofit2.converter.gson.GsonConverterFactory
object RetrofitClient {

    // 注意:Android 模拟器访问电脑本机 localhost,要用 10.0.2.2,不是 localhost
//        private const val BASE_URL = "http://10.0.2.2:8080/"
    // phone
      private const val BASE_URL = "http://192.168.0.42:8080/"

    // token 存取,可以换成 SharedPreferences 存
    var authToken: String?
        get() = UserSession.token
        set(value) { UserSession.token = value }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor { UserSession.token })
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun baseUrlForImages(): String = BASE_URL.trimEnd('/')
}