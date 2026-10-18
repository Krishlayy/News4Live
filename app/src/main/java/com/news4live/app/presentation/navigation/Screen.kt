package com.news4live.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.news4live.app.R

sealed class Screen(
    val route: String,
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : Screen(
        route = "home",
        titleRes = R.string.nav_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    data object Categories : Screen(
        route = "categories",
        titleRes = R.string.nav_categories,
        selectedIcon = Icons.Filled.GridView,
        unselectedIcon = Icons.Outlined.GridView
    )

    data object Search : Screen(
        route = "search",
        titleRes = R.string.nav_search,
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    )

    data object Bookmarks : Screen(
        route = "bookmarks",
        titleRes = R.string.nav_bookmarks,
        selectedIcon = Icons.Filled.Bookmark,
        unselectedIcon = Icons.Outlined.BookmarkBorder
    )

    data object More : Screen(
        route = "more",
        titleRes = R.string.nav_more,
        selectedIcon = Icons.Filled.Menu,
        unselectedIcon = Icons.Outlined.Menu
    )

    // Non-bottom-bar destinations
    data object ArticleDetail : Screen(
        route = "article/{articleId}",
        titleRes = R.string.app_name,
        selectedIcon = Icons.AutoMirrored.Filled.Article,
        unselectedIcon = Icons.AutoMirrored.Outlined.Article
    ) {
        fun createRoute(articleId: Long): String {
            return "article/$articleId"
        }
    }

    data object CategoryFeed : Screen(
        route = "category/{categoryId}?categoryName={categoryName}",
        titleRes = R.string.nav_categories,
        selectedIcon = Icons.Filled.Category,
        unselectedIcon = Icons.Outlined.Category
    ) {
        fun createRoute(categoryId: Long, categoryName: String): String {
            return "category/$categoryId?categoryName=$categoryName"
        }
    }

    data object WebPage : Screen(
        route = "web?title={title}&url={url}",
        titleRes = R.string.app_name,
        selectedIcon = Icons.Filled.Language,
        unselectedIcon = Icons.Outlined.Language
    ) {
        fun createRoute(title: String, url: String): String {
            return "web?title=$title&url=$url"
        }
    }
}
