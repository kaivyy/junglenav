package com.example.junglenav

import android.app.Application
import com.example.junglenav.app.AppContainer
import org.maplibre.android.MapLibre

class JungleNavApp : Application() {
    val appContainer: AppContainer by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this)
    }
}
