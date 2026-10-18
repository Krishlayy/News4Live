package com.news4live.app.ads

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.news4live.app.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AdManager {

    private const val TAG = "AdManager"
    val isAdsEnabled: Boolean = BuildConfig.ENABLE_ADS
    val bannerAdUnitId: String = BuildConfig.ADMOB_BANNER_ID

    private var isInitialized = false

    fun initialize(context: Context) {
        if (!isAdsEnabled) {
            Log.d(TAG, "Ads are disabled via BuildConfig")
            return
        }

        if (isInitialized) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                MobileAds.initialize(context) { status ->
                    Log.d(TAG, "Google Mobile Ads initialized: ${status.adapterStatusMap}")
                    isInitialized = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing MobileAds", e)
            }
        }
    }

    fun createAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }
}
