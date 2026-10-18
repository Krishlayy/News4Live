package com.news4live.app.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.news4live.app.R
import com.news4live.app.domain.model.Article
import com.news4live.app.presentation.components.ArticleCardSkeleton
import com.news4live.app.presentation.components.EmptyStateView
import com.news4live.app.presentation.components.NewsArticleCard
import com.news4live.app.presentation.theme.EditorialRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onArticleClick: (Article) -> Unit,
    modifier: Modifier = Modifier
) {
    val query by viewModel.query.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val searchResults: LazyPagingItems<Article> = viewModel.searchResults.collectAsLazyPagingItems()
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { viewModel.onQueryChanged(it) },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.search_hint),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontSize = 14.sp
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (query.isNotBlank()) {
                                IconButton(onClick = { viewModel.onQueryChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "साफ़ करें",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EditorialRed,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                viewModel.submitSearch(query)
                                focusManager.clearFocus()
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
            // When Query is empty, show Recent Searches
            if (query.isBlank()) {
                if (recentSearches.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.recent_searches),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        TextButton(onClick = { viewModel.clearAllRecentSearches() }) {
                            Text(
                                text = stringResource(R.string.clear_history),
                                style = MaterialTheme.typography.bodySmall.copy(color = EditorialRed)
                            )
                        }
                    }

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(
                            count = recentSearches.size,
                            key = { index -> recentSearches[index] }
                        ) { index ->
                            val item = recentSearches[index]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.submitSearch(item)
                                        focusManager.clearFocus()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = item,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }

                                IconButton(onClick = { viewModel.deleteRecentSearch(item) }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "हटाएं",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    EmptyStateView(
                        title = "खोजने के लिए ऊपर टाइप करें",
                        description = "देश, राजनीति, खेल, मथुरा समाचार या अपनी पसंद का विषय खोजें",
                        icon = Icons.Default.Search
                    )
                }
            } else {
                // Search Results
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                ) {
                    if (searchResults.loadState.refresh is LoadState.Loading) {
                        items(6) {
                            ArticleCardSkeleton()
                        }
                    }

                    if (searchResults.loadState.refresh is LoadState.NotLoading && searchResults.itemCount == 0) {
                        item {
                            EmptyStateView(
                                title = stringResource(R.string.no_search_results),
                                description = "\"$query\" से संबंधित कोई खबर नहीं मिली। कृपया कोई अन्य शब्द खोजें।"
                            )
                        }
                    }

                    items(
                        count = searchResults.itemCount,
                        key = searchResults.itemKey { it.id },
                        contentType = searchResults.itemContentType { "search_article" }
                    ) { index ->
                        val article = searchResults[index]
                        if (article != null) {
                            NewsArticleCard(
                                article = article,
                                onArticleClick = {
                                    viewModel.submitSearch(query)
                                    onArticleClick(article)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
