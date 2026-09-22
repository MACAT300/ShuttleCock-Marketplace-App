package com.example.shuttlecock_frontend

import android.app.Application
import com.example.shuttlecock_frontend.data.UserSession

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        UserSession.init(this)
    }
}