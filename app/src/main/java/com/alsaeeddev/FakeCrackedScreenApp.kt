package com.alsaeeddev

import android.app.Application
import com.alsaeeddev.di.AppContainer
import com.alsaeeddev.di.DefaultAppContainer

/**
 * Custom Application class maintaining singleton dependencies and initializing background audio assets.
 */
class FakeCrackedScreenApp : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = DefaultAppContainer(this)
    }
}
