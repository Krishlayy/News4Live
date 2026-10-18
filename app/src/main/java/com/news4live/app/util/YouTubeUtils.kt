package com.news4live.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.util.regex.Pattern

object YouTubeUtils {

    private val YOUTUBE_PATTERNS = listOf(
        Pattern.compile("(?:youtu\\.be/|youtube\\.com/(?:embed/|v/|watch\\?v=|watch\\?.+&v=|shorts/))([\\w-]{11})", Pattern.CASE_INSENSITIVE),
        Pattern.compile("^([\\w-]{11})$")
    )

    fun extractVideoId(urlOrId: String?): String? {
        if (urlOrId.isNullOrBlank()) return null

        for (pattern in YOUTUBE_PATTERNS) {
            val matcher = pattern.matcher(urlOrId.trim())
            if (matcher.find()) {
                return matcher.group(1)
            }
        }
        return null
    }

    fun getThumbnailUrl(videoId: String): String {
        return "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
    }

    fun getHighResThumbnailUrl(videoId: String): String {
        return "https://img.youtube.com/vi/$videoId/maxresdefault.jpg"
    }

    fun launchYouTubeVideo(context: Context, videoUrlOrId: String) {
        val videoId = extractVideoId(videoUrlOrId)
        if (videoId != null) {
            // Attempt to launch via YouTube app intent
            val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(appIntent)
                return
            } catch (_: Exception) {}
        }

        // Web browser fallback
        val targetUrl = if (videoId != null) "https://www.youtube.com/watch?v=$videoId" else videoUrlOrId
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(webIntent)
        } catch (_: Exception) {}
    }
}
