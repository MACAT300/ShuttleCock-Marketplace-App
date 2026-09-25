package com.example.shuttlecock_frontend

import android.app.Application
import coil.Coil
import coil.ImageLoader
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import com.example.shuttlecock_frontend.data.UserSession

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        UserSession.init(this)

        val userAgentInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                            "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                )
                .build()
            chain.proceed(request)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(userAgentInterceptor)
            .build()

        val imageLoader = ImageLoader.Builder(this)
            .okHttpClient(client)
            .build()

        Coil.setImageLoader(imageLoader)
    }
}