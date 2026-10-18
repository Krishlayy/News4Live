package com.news4live.app.presentation.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.news4live.app.BuildConfig
import com.news4live.app.ads.AdManager

enum class AdSlotLocation {
    HOME_FEED_TOP,
    HOME_FEED_BETWEEN,
    ARTICLE_DETAIL_BOTTOM,
    CATEGORY_FEED
}

@Composable
fun AdBannerView(
    location: AdSlotLocation,
    modifier: Modifier = Modifier
) {
    if (!BuildConfig.ENABLE_ADS) {
        return
    }

    var isAdLoaded by remember { mutableStateOf(false) }
    var hasAdFailed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AdManager.bannerAdUnitId
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            isAdLoaded = true
                            hasAdFailed = false
                            Log.d("AdBannerView", "Ad loaded successfully for $location")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            super.onAdFailedToLoad(error)
                            isAdLoaded = false
                            hasAdFailed = true
                            Log.w("AdBannerView", "Ad failed to load: ${error.message}")
                        }
                    }
                    loadAd(AdManager.createAdRequest())
                }
            }
        )

        // Subtle placeholder shown if ad is loading or offline fallback
        if (!isAdLoaded && !hasAdFailed) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "विज्ञापन • News 4 Live",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
