package com.news4live.app.presentation.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.news4live.app.audio.HindiAudioNewsManager
import com.news4live.app.data.repository.ArticleNotFoundException
import com.news4live.app.domain.model.Article
import com.news4live.app.domain.model.ParsedHtmlBlock
import com.news4live.app.domain.usecase.BookmarkUseCases
import com.news4live.app.domain.usecase.GetArticleDetailsUseCase
import com.news4live.app.domain.usecase.HtmlBlockParserUseCase
import com.news4live.app.util.ArticleFontSize
import com.news4live.app.util.TextSizeManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface ArticleUiState {
    data object Loading : ArticleUiState
    data class Success(
        val article: Article,
        val parsedBlocks: List<ParsedHtmlBlock>,
        val isBookmarked: Boolean,
        val relatedArticles: List<Article> = emptyList(),
        val fontSize: ArticleFontSize = ArticleFontSize.MEDIUM
    ) : ArticleUiState
    data class Error(val message: String, val isNotFound: Boolean = false) : ArticleUiState
}

class ArticleViewModel(
    private val getArticleDetailsUseCase: GetArticleDetailsUseCase,
    private val bookmarkUseCases: BookmarkUseCases,
    private val textSizeManager: TextSizeManager,
    val audioManager: HindiAudioNewsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ArticleUiState>(ArticleUiState.Loading)
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()

    val currentFontSize: StateFlow<ArticleFontSize> = textSizeManager.fontSizeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ArticleFontSize.MEDIUM)

    fun loadArticle(articleId: Long, slug: String = "") {
        viewModelScope.launch {
            // 1. Instant Cache Display (< 10ms)
            if (articleId > 0) {
                val cached = getArticleDetailsUseCase.getCached(articleId)
                if (cached != null) {
                    val rawHtml = cached.contentHtml.ifBlank { cached.excerptHtml.ifBlank { cached.cleanExcerpt } }
                    val parsed = HtmlBlockParserUseCase.parseHtmlToBlocks(rawHtml)
                    val isBookmarked = bookmarkUseCases.isBookmarked(cached.id).firstOrNull() ?: false

                    _uiState.value = ArticleUiState.Success(
                        article = cached,
                        parsedBlocks = parsed,
                        isBookmarked = isBookmarked,
                        fontSize = currentFontSize.value
                    )
                } else {
                    _uiState.value = ArticleUiState.Loading
                }
            } else {
                _uiState.value = ArticleUiState.Loading
            }

            // 2. Fetch fresh details from network
            val result = if (articleId > 0) {
                getArticleDetailsUseCase.getById(articleId)
            } else if (slug.isNotBlank()) {
                getArticleDetailsUseCase.getBySlug(slug)
            } else {
                Result.failure(ArticleNotFoundException("अमान्य खबर आईडी या लिंक"))
            }

            result.onSuccess { article ->
                val rawHtml = article.contentHtml.ifBlank { article.excerptHtml.ifBlank { article.cleanExcerpt } }
                val parsed = HtmlBlockParserUseCase.parseHtmlToBlocks(rawHtml)
                val isBookmarked = bookmarkUseCases.isBookmarked(article.id).firstOrNull() ?: false

                _uiState.value = ArticleUiState.Success(
                    article = article,
                    parsedBlocks = parsed,
                    isBookmarked = isBookmarked,
                    fontSize = currentFontSize.value
                )

                // Observe bookmark state changes
                launch {
                    bookmarkUseCases.isBookmarked(article.id).collect { bookmarked ->
                        val current = _uiState.value
                        if (current is ArticleUiState.Success) {
                            _uiState.value = current.copy(isBookmarked = bookmarked)
                        }
                    }
                }
            }.onFailure { error ->
                // Only show error screen if we don't already have cached content displaying
                if (_uiState.value !is ArticleUiState.Success) {
                    val is404 = error is ArticleNotFoundException
                    _uiState.value = ArticleUiState.Error(
                        message = if (is404) "यह खबर अब उपलब्ध नहीं है या हटा दी गई है।" else (error.message ?: "खबर लोड नहीं हो सकी"),
                        isNotFound = is404
                    )
                }
            }
        }
    }

    fun toggleBookmark() {
        val current = _uiState.value
        if (current is ArticleUiState.Success) {
            viewModelScope.launch {
                bookmarkUseCases.toggleBookmark(current.article)
            }
        }
    }

    fun setFontSize(size: ArticleFontSize) {
        viewModelScope.launch {
            textSizeManager.setFontSize(size)
            val current = _uiState.value
            if (current is ArticleUiState.Success) {
                _uiState.value = current.copy(fontSize = size)
            }
        }
    }
}
