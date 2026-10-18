package com.news4live.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.news4live.app.domain.model.Article
import com.news4live.app.domain.model.Category
import com.news4live.app.domain.model.HomeFeed
import com.news4live.app.domain.usecase.GetCategoriesUseCase
import com.news4live.app.domain.usecase.GetHomeFeedUseCase
import com.news4live.app.domain.usecase.GetPagedArticlesUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val homeFeed: HomeFeed) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(
    private val getHomeFeedUseCase: GetHomeFeedUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getPagedArticlesUseCase: GetPagedArticlesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    val categories: StateFlow<List<Category>> = getCategoriesUseCase.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val pagedArticles: Flow<PagingData<Article>> = _selectedCategoryId
        .flatMapLatest { catId ->
            getPagedArticlesUseCase(catId)
        }
        .cachedIn(viewModelScope)

    init {
        loadHomeFeed()
    }

    fun loadHomeFeed(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            // 1. Instant Cache Check (0ms cold start)
            val cached = getHomeFeedUseCase.getCached()
            if (cached != null && !forceRefresh && _uiState.value !is HomeUiState.Success) {
                _uiState.value = HomeUiState.Success(cached)
            } else if (_uiState.value !is HomeUiState.Success) {
                _uiState.value = HomeUiState.Loading
            }

            if (forceRefresh) _isRefreshing.value = true

            // 2. Fetch fresh articles in background
            val result = getHomeFeedUseCase(forceRefresh)
            result.onSuccess { feed ->
                _uiState.value = HomeUiState.Success(feed)
            }.onFailure { error ->
                if (_uiState.value !is HomeUiState.Success) {
                    _uiState.value = HomeUiState.Error(error.message ?: "खबरें लोड करने में समस्या आई")
                }
            }

            _isRefreshing.value = false
        }
    }

    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun refresh() {
        loadHomeFeed(forceRefresh = true)
    }
}
