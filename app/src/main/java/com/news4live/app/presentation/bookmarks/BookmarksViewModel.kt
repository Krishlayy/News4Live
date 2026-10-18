package com.news4live.app.presentation.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.news4live.app.domain.model.Bookmark
import com.news4live.app.domain.usecase.BookmarkUseCases
import com.news4live.app.domain.usecase.GetArticleDetailsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookmarksViewModel(
    private val bookmarkUseCases: BookmarkUseCases,
    private val getArticleDetailsUseCase: GetArticleDetailsUseCase
) : ViewModel() {

    val bookmarks: StateFlow<List<Bookmark>> = bookmarkUseCases.getBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeBookmark(articleId: Long) {
        viewModelScope.launch {
            val articleRes = getArticleDetailsUseCase.getById(articleId)
            articleRes.onSuccess { article ->
                bookmarkUseCases.toggleBookmark(article)
            }
        }
    }
}
