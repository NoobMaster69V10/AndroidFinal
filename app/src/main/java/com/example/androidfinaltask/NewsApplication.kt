package com.example.androidfinaltask

import android.app.Application
import com.google.firebase.FirebaseApp

class NewsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
    }
}


