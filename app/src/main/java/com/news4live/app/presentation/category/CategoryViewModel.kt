package com.news4live.app.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.news4live.app.domain.model.Article
import com.news4live.app.domain.model.Category
import com.news4live.app.domain.usecase.GetCategoriesUseCase
import com.news4live.app.domain.usecase.GetPagedArticlesUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface CategoryUiState {
    data object Loading : CategoryUiState
    data class Success(val categories: List<Category>) : CategoryUiState
    data class Error(val message: String) : CategoryUiState
}

class CategoryViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getPagedArticlesUseCase: GetPagedArticlesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CategoryUiState>(CategoryUiState.Loading)
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val categoryArticles: Flow<PagingData<Article>> = _selectedCategoryId
        .filterNotNull()
        .flatMapLatest { catId -> getPagedArticlesUseCase(catId) }
        .cachedIn(viewModelScope)

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = CategoryUiState.Loading
            val result = getCategoriesUseCase.refresh()
            result.onSuccess { list ->
                _uiState.value = CategoryUiState.Success(list)
            }.onFailure {
                getCategoriesUseCase.getCategories().collect { cached ->
                    if (cached.isNotEmpty()) {
                        _uiState.value = CategoryUiState.Success(cached)
                    } else {
                        _uiState.value = CategoryUiState.Error("श्रेणियां लोड नहीं हो सकीं")
                    }
                }
            }
        }
    }

    fun selectCategory(categoryId: Long) {
        _selectedCategoryId.value = categoryId
    }
}
