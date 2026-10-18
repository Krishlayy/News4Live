package com.news4live.app.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.news4live.app.domain.model.Article
import com.news4live.app.domain.usecase.SearchArticlesUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchArticlesUseCase: SearchArticlesUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val recentSearches: StateFlow<List<String>> = searchArticlesUseCase.getRecentSearches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val searchResults: Flow<PagingData<Article>> = _query
        .debounce(400)
        .distinctUntilChanged()
        .flatMapLatest { q ->
            if (q.isBlank() || q.length < 2) {
                flowOf(PagingData.empty())
            } else {
                searchArticlesUseCase(q)
            }
        }
        .cachedIn(viewModelScope)

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    fun submitSearch(searchQuery: String) {
        if (searchQuery.isNotBlank()) {
            _query.value = searchQuery
            viewModelScope.launch {
                searchArticlesUseCase.saveQuery(searchQuery)
            }
        }
    }

    fun deleteRecentSearch(query: String) {
        viewModelScope.launch {
            searchArticlesUseCase.deleteQuery(query)
        }
    }

    fun clearAllRecentSearches() {
        viewModelScope.launch {
            searchArticlesUseCase.clearHistory()
        }
    }
}
