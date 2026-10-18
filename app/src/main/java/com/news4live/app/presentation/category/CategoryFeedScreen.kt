package com.news4live.app.presentation.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.news4live.app.R
import com.news4live.app.domain.model.Article
import com.news4live.app.presentation.components.ArticleCardSkeleton
import com.news4live.app.presentation.components.EmptyStateView
import com.news4live.app.presentation.components.ErrorStateView
import com.news4live.app.presentation.components.NewsArticleCard
import com.news4live.app.presentation.theme.EditorialRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFeedScreen(
    viewModel: CategoryViewModel,
    categoryId: Long,
    categoryName: String,
    onArticleClick: (Article) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(categoryId) {
        viewModel.selectCategory(categoryId)
    }

    val pagedArticles: LazyPagingItems<Article> = viewModel.categoryArticles.collectAsLazyPagingItems()
    var isRefreshing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "वापस जाएं")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                pagedArticles.refresh()
                isRefreshing = false
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                // Initial Loading
                if (pagedArticles.loadState.refresh is LoadState.Loading) {
                    items(6) {
                        ArticleCardSkeleton()
                    }
                }

                // Refresh Error
                if (pagedArticles.loadState.refresh is LoadState.Error && pagedArticles.itemCount == 0) {
                    item {
                        ErrorStateView(
                            onRetry = { pagedArticles.retry() }
                        )
                    }
                }

                // Empty State
                if (pagedArticles.loadState.refresh is LoadState.NotLoading && pagedArticles.itemCount == 0) {
                    item {
                        EmptyStateView(
                            title = "इस श्रेणी में कोई खबर नहीं है"
                        )
                    }
                }

                // Articles
                items(
                    count = pagedArticles.itemCount,
                    key = pagedArticles.itemKey { it.id },
                    contentType = pagedArticles.itemContentType { "article" }
                ) { index ->
                    val article = pagedArticles[index]
                    if (article != null) {
                        NewsArticleCard(
                            article = article,
                            onArticleClick = onArticleClick
                        )
                    }
                }

                // Load More State
                if (pagedArticles.loadState.append is LoadState.Loading) {
                    item {
                        ArticleCardSkeleton()
                    }
                }
            }
        }
    }
}
