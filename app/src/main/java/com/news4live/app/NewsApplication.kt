package com.news4live.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.news4live.app.ads.AdManager
import com.news4live.app.di.ServiceLocator
import com.news4live.app.di.ViewModelFactory
import com.news4live.app.notifications.NotificationHelper

class NewsApplication : Application(), ImageLoaderFactory {

    lateinit var serviceLocator: ServiceLocator
        private set

    lateinit var viewModelFactory: ViewModelFactory
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        serviceLocator = ServiceLocator(this)
        viewModelFactory = ViewModelFactory(serviceLocator)

        // Initialize Notification Channels
        NotificationHelper.createNotificationChannels(this)

        // Initialize Ads (if enabled)
        AdManager.initialize(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient {
                com.news4live.app.data.api.ApiClient.createOkHttpClient(this)
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(150L * 1024L * 1024L) // 150 MB disk image cache
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false) // Cache images aggressively even if WordPress headers are missing
            .crossfade(150)
            .build()
    }

    companion object {
        lateinit var instance: NewsApplication
            private set
    }
}
