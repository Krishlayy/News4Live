package com.news4live.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.news4live.app.R
import com.news4live.app.domain.model.Article
import com.news4live.app.presentation.components.*
import com.news4live.app.presentation.theme.EditorialRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onArticleClick: (Article) -> Unit,
    onSearchClick: () -> Unit,
    onShareAppClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val selectedCategory by viewModel.selectedCategoryId.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val pagedArticles: LazyPagingItems<Article> = viewModel.pagedArticles.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            NewsTopAppBar(
                onSearchClick = onSearchClick,
                onNotificationClick = { /* Handled via FCM */ },
                onShareAppClick = onShareAppClick
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Horizontal Dynamic Category Tabs
            CategoryChipsRow(
                categories = categories,
                selectedCategoryId = selectedCategory,
                onCategorySelected = { catId -> viewModel.selectCategory(catId) }
            )

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    viewModel.refresh()
                    pagedArticles.refresh()
                },
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = uiState) {
                    is HomeUiState.Loading -> {
                        HomeFeedSkeleton()
                    }

                    is HomeUiState.Error -> {
                        if (pagedArticles.itemCount == 0) {
                            ErrorStateView(
                                message = state.message,
                                onRetry = { viewModel.refresh() }
                            )
                        }
                    }

                    is HomeUiState.Success -> {
                        val feed = state.homeFeed
                        val topStoryIds = remember(feed) {
                            (listOfNotNull(feed.heroArticle) + feed.topStories).map { it.id }.toSet()
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            // Only show Breaking Ticker and Hero if we are on "होम" (All) tab
                            if (selectedCategory == null || selectedCategory == 0L) {
                                // 1. Breaking News Ticker
                                if (feed.breakingNews.isNotEmpty()) {
                                    item(key = "breaking_ticker") {
                                        BreakingNewsTicker(
                                            breakingArticles = feed.breakingNews,
                                            onArticleClick = onArticleClick
                                        )
                                    }
                                }

                                // 2. Featured Story Hero Card
                                if (feed.heroArticle != null) {
                                    item(key = "hero_article") {
                                        HeroArticleCard(
                                            article = feed.heroArticle,
                                            onArticleClick = onArticleClick
                                        )
                                    }
                                }

                                // 3. Top Stories Section Header
                                if (feed.topStories.isNotEmpty()) {
                                    item(key = "top_stories_header") {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = stringResource(R.string.section_top_stories),
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                )
                                            )
                                        }
                                    }

                                    // Top stories items
                                    items(
                                        count = feed.topStories.size,
                                        key = { index -> "top_${feed.topStories[index].id}" }
                                    ) { index ->
                                        NewsArticleCard(
                                            article = feed.topStories[index],
                                            onArticleClick = onArticleClick
                                        )
                                    }
                                }

                                // 4. Ad Slot
                                item(key = "ad_slot_home") {
                                    AdBannerView(location = AdSlotLocation.HOME_FEED_TOP)
                                }

                                // 5. Latest News Header
                                item(key = "latest_news_header") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 6.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.section_latest_news),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        )
                                    }
                                }
                            }

                            // 6. Paginated Infinite Article Feed (Deduplicated on Home tab)
                            items(
                                count = pagedArticles.itemCount,
                                key = pagedArticles.itemKey { it.id },
                                contentType = pagedArticles.itemContentType { "article" }
                            ) { index ->
                                val article = pagedArticles[index]
                                val isDuplicate = (selectedCategory == null || selectedCategory == 0L) && article != null && topStoryIds.contains(article.id)
                                if (article != null && !isDuplicate) {
                                    NewsArticleCard(
                                        article = article,
                                        onArticleClick = onArticleClick
                                    )
                                }
                            }

                            // 7. Paging Load States (Loading & Append Error)
                            when (val appendState = pagedArticles.loadState.append) {
                                is LoadState.Loading -> {
                                    item(key = "append_loading") {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            ArticleCardSkeleton()
                                        }
                                    }
                                }
                                is LoadState.Error -> {
                                    item(key = "append_error") {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            Text(
                                                text = "और खबरें लोड नहीं हो सकीं",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            )
                                            TextButton(onClick = { pagedArticles.retry() }) {
                                                Text(text = "पुनः प्रयास करें", color = EditorialRed)
                                            }
                                        }
                                    }
                                }
                                is LoadState.NotLoading -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}
