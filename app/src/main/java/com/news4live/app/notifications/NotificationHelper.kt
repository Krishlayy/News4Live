package com.news4live.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.news4live.app.BuildConfig
import com.news4live.app.MainActivity
import com.news4live.app.R
import com.news4live.app.presentation.theme.EditorialRed

object NotificationHelper {

    const val CHANNEL_BREAKING_NEWS = "breaking_news_channel"
    const val CHANNEL_DAILY_DIGEST = "daily_digest_channel"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val breakingChannel = NotificationChannel(
                CHANNEL_BREAKING_NEWS,
                context.getString(R.string.channel_breaking_news_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_breaking_news_desc)
                enableVibration(true)
                enableLights(true)
            }

            val digestChannel = NotificationChannel(
                CHANNEL_DAILY_DIGEST,
                context.getString(R.string.channel_daily_digest_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_daily_digest_desc)
            }

            manager.createNotificationChannel(breakingChannel)
            manager.createNotificationChannel(digestChannel)
        }
    }

    suspend fun showBreakingNewsNotification(
        context: Context,
        postId: Long,
        title: String,
        excerpt: String,
        imageUrl: String?,
        canonicalUrl: String?
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_POST_ID", postId)
            if (!canonicalUrl.isNullOrBlank()) {
                data = Uri.parse(canonicalUrl)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            postId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_BREAKING_NEWS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🔴 ब्रेकिंग: $title")
            .setContentText(excerpt)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        // Load image for BigPictureStyle if present
        if (!imageUrl.isNullOrBlank()) {
            val bitmap = fetchImageBitmap(context, imageUrl)
            if (bitmap != null) {
                builder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .setSummaryText(excerpt)
                )
            } else {
                builder.setStyle(NotificationCompat.BigTextStyle().bigText(excerpt))
            }
        } else {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(excerpt))
        }

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(postId.toInt(), builder.build())
        } catch (_: SecurityException) {}
    }

    private suspend fun fetchImageBitmap(context: Context, url: String): Bitmap? {
        return try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .build()
            val result = (loader.execute(request) as? SuccessResult)?.drawable
            (result as? BitmapDrawable)?.bitmap
        } catch (e: Exception) {
            null
        }
    }
}
