package com.news4live.app.util

import android.content.Context
import android.content.Intent
import com.news4live.app.R

object ShareUtils {

    fun shareArticle(context: Context, title: String, canonicalUrl: String) {
        val shareBody = context.getString(R.string.share_text_template, title, canonicalUrl)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, shareBody)
        }
        val chooser = Intent.createChooser(intent, context.getString(R.string.share_article))
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun shareToWhatsApp(context: Context, title: String, canonicalUrl: String, excerpt: String = "") {
        val cleanExcerpt = excerpt.replace(Regex("<.*?>"), "").trim()
        val excerptSnippet = if (cleanExcerpt.isNotBlank()) {
            val truncated = if (cleanExcerpt.length > 120) cleanExcerpt.take(120) + "..." else cleanExcerpt
            "\n📌 *$truncated*\n"
        } else ""

        val shareText = """
            🔥 *${title.trim()}*
            $excerptSnippet
            📖 पूरी खबर पढ़ें:
            $canonicalUrl

            📲 *News 4 Live ऐप डाउनलोड करें:*
            https://apkpure.com/p/com.news4live.app
        """.trimIndent()

        val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(whatsappIntent)
        } catch (e: Exception) {
            // Fallback to WhatsApp Business or general chooser
            try {
                val wbIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    setPackage("com.whatsapp.w4b")
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(wbIntent)
            } catch (e2: Exception) {
                // Fallback to system share chooser
                val generalIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, title)
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(generalIntent, "WhatsApp / अन्य ऐप्स पर शेयर करें"))
            }
        }
    }

    fun shareApp(context: Context) {
        val shareBody = context.getString(R.string.share_app_text)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareBody)
        }
        val chooser = Intent.createChooser(intent, context.getString(R.string.more_share_app))
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
