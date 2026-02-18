package com.firstapp.dogscanai

import android.app.Application
import network.model.AuthManager

class DogScanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Dito natin i-initialize ang AuthManager sa pagsisimula pa lang ng app
        AuthManager.initialize(this)
    }
}