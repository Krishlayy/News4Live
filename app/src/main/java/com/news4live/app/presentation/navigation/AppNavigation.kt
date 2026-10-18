package com.news4live.app.presentation.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.news4live.app.BuildConfig
import com.news4live.app.di.ViewModelFactory
import com.news4live.app.presentation.article.ArticleScreen
import com.news4live.app.presentation.bookmarks.BookmarksScreen
import com.news4live.app.presentation.category.CategoryFeedScreen
import com.news4live.app.presentation.category.CategoryScreen
import com.news4live.app.presentation.home.HomeScreen
import com.news4live.app.presentation.more.MoreScreen
import com.news4live.app.presentation.more.WebPageScreen
import com.news4live.app.presentation.search.SearchScreen

@Composable
fun AppNavigation(
    viewModelFactory: ViewModelFactory,
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. Home Screen
            composable(route = Screen.Home.route) {
                val homeViewModel = remember { viewModelFactory.getHomeViewModel() }
                HomeScreen(
                    viewModel = homeViewModel,
                    onArticleClick = { article ->
                        navController.navigate(Screen.ArticleDetail.createRoute(article.id))
                    },
                    onSearchClick = {
                        navController.navigate(Screen.Search.route)
                    },
                    onShareAppClick = {
                        navController.navigate(Screen.More.route)
                    }
                )
            }

            // 2. Categories Screen (Grid)
            composable(route = Screen.Categories.route) {
                val categoryViewModel = remember { viewModelFactory.getCategoryViewModel() }
                CategoryScreen(
                    viewModel = categoryViewModel,
                    onCategoryClick = { category ->
                        navController.navigate(Screen.CategoryFeed.createRoute(category.id, category.name))
                    }
                )
            }

            // 3. Category Feed Screen (Dedicated Category List)
            composable(
                route = Screen.CategoryFeed.route,
                arguments = listOf(
                    navArgument("categoryId") { type = NavType.LongType },
                    navArgument("categoryName") {
                        type = NavType.StringType
                        defaultValue = "समाचार"
                    }
                )
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: 0L
                val categoryName = backStackEntry.arguments?.getString("categoryName") ?: "समाचार"
                val categoryViewModel = remember(categoryId) { viewModelFactory.getCategoryViewModel() }

                CategoryFeedScreen(
                    viewModel = categoryViewModel,
                    categoryId = categoryId,
                    categoryName = categoryName,
                    onArticleClick = { article ->
                        navController.navigate(Screen.ArticleDetail.createRoute(article.id))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 4. Search Screen
            composable(route = Screen.Search.route) {
                val searchViewModel = remember { viewModelFactory.getSearchViewModel() }
                SearchScreen(
                    viewModel = searchViewModel,
                    onArticleClick = { article ->
                        navController.navigate(Screen.ArticleDetail.createRoute(article.id))
                    }
                )
            }

            // 5. Bookmarks Screen
            composable(route = Screen.Bookmarks.route) {
                val bookmarksViewModel = remember { viewModelFactory.getBookmarksViewModel() }
                BookmarksScreen(
                    viewModel = bookmarksViewModel,
                    onBookmarkClick = { articleId ->
                        navController.navigate(Screen.ArticleDetail.createRoute(articleId))
                    }
                )
            }

            // 6. More / Settings Screen
            composable(route = Screen.More.route) {
                MoreScreen(
                    onOpenUrl = { title, url ->
                        val encodedUrl = Uri.encode(url)
                        navController.navigate(Screen.WebPage.createRoute(title, encodedUrl))
                    }
                )
            }

            // 7. Article Detail Screen (Direct ID route & deep link)
            composable(
                route = Screen.ArticleDetail.route,
                arguments = listOf(
                    navArgument("articleId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    }
                ),
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern = "https://${BuildConfig.APP_LINK_HOST}/?p={articleId}"
                        action = Intent.ACTION_VIEW
                    }
                )
            ) { backStackEntry ->
                val articleId = backStackEntry.arguments?.getLong("articleId") ?: 0L
                val articleViewModel = remember(articleId) { viewModelFactory.getArticleViewModel() }

                ArticleScreen(
                    viewModel = articleViewModel,
                    articleId = articleId,
                    onBackClick = { navController.popBackStack() },
                    onRelatedArticleClick = { related ->
                        navController.navigate(Screen.ArticleDetail.createRoute(related.id))
                    }
                )
            }

            // 8. Web Page Screen
            composable(
                route = Screen.WebPage.route,
                arguments = listOf(
                    navArgument("title") { type = NavType.StringType },
                    navArgument("url") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val title = backStackEntry.arguments?.getString("title") ?: "News 4 Live"
                val rawUrl = backStackEntry.arguments?.getString("url") ?: BuildConfig.WORDPRESS_BASE_URL
                val decodedUrl = Uri.decode(rawUrl)

                WebPageScreen(
                    title = title,
                    url = decodedUrl,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
