package com.news4live.app.analytics

import android.content.Context
import android.os.Bundle

interface AnalyticsService {
    fun trackAppOpen()
    fun trackArticleView(articleId: Long, title: String, category: String)
    fun trackCategoryView(categoryId: Long, categoryName: String)
    fun trackSearch(query: String)
    fun trackShare(articleId: Long, title: String)
    fun trackBookmark(articleId: Long, title: String, isAdded: Boolean)
}

class AppAnalytics(private val context: Context) : AnalyticsService {

    override fun trackAppOpen() {
        // Modular analytics tracking
    }

    override fun trackArticleView(articleId: Long, title: String, category: String) {
        val bundle = Bundle().apply {
            putLong("article_id", articleId)
            putString("article_title", title)
            putString("category", category)
        }
        // Logs to Firebase Analytics or custom analytics backend if enabled
    }

    override fun trackCategoryView(categoryId: Long, categoryName: String) {
        val bundle = Bundle().apply {
            putLong("category_id", categoryId)
            putString("category_name", categoryName)
        }
    }

    override fun trackSearch(query: String) {
        val bundle = Bundle().apply {
            putString("search_term", query)
        }
    }

    override fun trackShare(articleId: Long, title: String) {
        val bundle = Bundle().apply {
            putLong("article_id", articleId)
            putString("article_title", title)
        }
    }

    override fun trackBookmark(articleId: Long, title: String, isAdded: Boolean) {
        val bundle = Bundle().apply {
            putLong("article_id", articleId)
            putBoolean("is_added", isAdded)
        }
    }
}
