package com.news4live.app.di

import com.news4live.app.presentation.article.ArticleViewModel
import com.news4live.app.presentation.bookmarks.BookmarksViewModel
import com.news4live.app.presentation.category.CategoryViewModel
import com.news4live.app.presentation.home.HomeViewModel
import com.news4live.app.presentation.search.SearchViewModel

class ViewModelFactory(private val serviceLocator: ServiceLocator) {

    fun getHomeViewModel(): HomeViewModel {
        return HomeViewModel(
            getHomeFeedUseCase = serviceLocator.getHomeFeedUseCase,
            getCategoriesUseCase = serviceLocator.getCategoriesUseCase,
            getPagedArticlesUseCase = serviceLocator.getPagedArticlesUseCase
        )
    }

    fun getArticleViewModel(): ArticleViewModel {
        return ArticleViewModel(
            getArticleDetailsUseCase = serviceLocator.getArticleDetailsUseCase,
            bookmarkUseCases = serviceLocator.bookmarkUseCases,
            textSizeManager = serviceLocator.textSizeManager,
            audioManager = serviceLocator.hindiAudioNewsManager
        )
    }

    fun getCategoryViewModel(): CategoryViewModel {
        return CategoryViewModel(
            getCategoriesUseCase = serviceLocator.getCategoriesUseCase,
            getPagedArticlesUseCase = serviceLocator.getPagedArticlesUseCase
        )
    }

    fun getSearchViewModel(): SearchViewModel {
        return SearchViewModel(
            searchArticlesUseCase = serviceLocator.searchArticlesUseCase
        )
    }

    fun getBookmarksViewModel(): BookmarksViewModel {
        return BookmarksViewModel(
            bookmarkUseCases = serviceLocator.bookmarkUseCases,
            getArticleDetailsUseCase = serviceLocator.getArticleDetailsUseCase
        )
    }
}
