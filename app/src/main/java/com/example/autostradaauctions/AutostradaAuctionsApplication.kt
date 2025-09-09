package com.example.autostradaauctions

import android.app.Application
import com.example.autostradaauctions.di.AppContainer

class AutostradaAuctionsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize the dependency injection container
        AppContainer.initialize(this)
    }
}
