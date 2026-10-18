package com.news4live.app.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

enum class ArticleFontSize(val scaleFactor: Float, val labelRes: Int) {
    SMALL(0.85f, com.news4live.app.R.string.font_size_small),
    MEDIUM(1.0f, com.news4live.app.R.string.font_size_medium),
    LARGE(1.2f, com.news4live.app.R.string.font_size_large),
    EXTRA_LARGE(1.4f, com.news4live.app.R.string.font_size_extra_large)
}

class TextSizeManager(private val context: Context) {

    private val KEY_FONT_SIZE = intPreferencesKey("article_font_size_index")

    val fontSizeFlow: Flow<ArticleFontSize> = context.dataStore.data.map { prefs ->
        val index = prefs[KEY_FONT_SIZE] ?: ArticleFontSize.MEDIUM.ordinal
        ArticleFontSize.entries.getOrElse(index) { ArticleFontSize.MEDIUM }
    }

    suspend fun setFontSize(size: ArticleFontSize) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FONT_SIZE] = size.ordinal
        }
    }
}
