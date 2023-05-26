package ru.mirea.ivashechkinav.yandexdriver

import android.app.Application
import com.yandex.mapkit.MapKitFactory


class App : Application() {
    private val MAPKIT_API_KEY = "a3e8eef9-e414-43a9-8891-b3ad8a2afc14"
    override fun onCreate() {
        super.onCreate()
        // Set the api key before calling initialize on MapKitFactory.
        MapKitFactory.setApiKey(MAPKIT_API_KEY)
    }
}