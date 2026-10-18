package com.news4live.app.presentation.article

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.news4live.app.R
import com.news4live.app.audio.HindiAudioNewsManager
import com.news4live.app.domain.model.Article
import com.news4live.app.domain.model.ParsedHtmlBlock
import com.news4live.app.presentation.components.*
import com.news4live.app.presentation.theme.EditorialRed
import com.news4live.app.util.ArticleFontSize
import com.news4live.app.util.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(
    viewModel: ArticleViewModel,
    articleId: Long,
    slug: String = "",
    onBackClick: () -> Unit,
    onRelatedArticleClick: (Article) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val audioManager = viewModel.audioManager
    val audioState by audioManager.state.collectAsState()

    DisposableEffect(articleId) {
        onDispose {
            audioManager.stop()
        }
    }

    var showFontDialog by remember { mutableStateOf(false) }
    var selectedLightboxImageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(articleId, slug) {
        viewModel.loadArticle(articleId, slug)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "वापस जाएं",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (uiState is ArticleUiState.Success) {
                        val article = (uiState as ArticleUiState.Success).article

                        // Font Size Toggle
                        IconButton(onClick = { showFontDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = "फॉन्ट साइज़",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Bookmark Button
                        val isBookmarked = (uiState as ArticleUiState.Success).isBookmarked
                        IconButton(onClick = { viewModel.toggleBookmark() }) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "बुकमार्क",
                                tint = if (isBookmarked) EditorialRed else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // 1-Tap WhatsApp Share Button
                        IconButton(onClick = {
                            ShareUtils.shareToWhatsApp(context, article.title, article.canonicalUrl, article.cleanExcerpt)
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "व्हाट्सएप पर शेयर करें",
                                tint = Color(0xFF25D366)
                            )
                        }

                        // General Share Button
                        IconButton(onClick = {
                            ShareUtils.shareArticle(context, article.title, article.canonicalUrl)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "शेयर करें",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is ArticleUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = EditorialRed)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.loading),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                is ArticleUiState.Error -> {
                    if (state.isNotFound) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Article,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "खबर उपलब्ध नहीं है",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = onBackClick,
                                colors = ButtonDefaults.buttonColors(containerColor = EditorialRed)
                            ) {
                                Text(text = "वापस जाएं")
                            }
                        }
                    } else {
                        ErrorStateView(
                            message = state.message,
                            onRetry = { viewModel.loadArticle(articleId, slug) }
                        )
                    }
                }

                is ArticleUiState.Success -> {
                    val article = state.article

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        // Category Badge
                        Box(
                            modifier = Modifier
                                .padding(start = 16.dp, top = 8.dp, bottom = 6.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(EditorialRed)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = article.categoryName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        // Headline
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = 28.sp
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )

                        // Meta Information: Author & Date
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = article.formattedDate,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            if (article.authorName.isNotBlank()) {
                                Text(
                                    text = " • ${article.authorName}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }

                        // Featured Hero Image (if available)
                        if (!article.featuredImageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(article.featuredImageUrl)
                                    .crossfade(true)
                                    .placeholder(R.drawable.placeholder_news)
                                    .build(),
                                contentDescription = article.title,
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        }

                        // Audio News Player Bar ("खबर सुनें")
                        val bodyParagraphs = remember(state.parsedBlocks) {
                            state.parsedBlocks.filterIsInstance<ParsedHtmlBlock.Paragraph>().map { it.text }
                        }

                        AudioNewsPlayerBar(
                            state = audioState,
                            onTogglePlayPause = {
                                audioManager.togglePlayPause(article.title, bodyParagraphs)
                            },
                            onStop = { audioManager.stop() },
                            onSpeedChange = { speed -> audioManager.setSpeechRate(speed) },
                            modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp)
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )

                        // Rich HTML Body Content
                        HtmlArticleRenderer(
                            blocks = state.parsedBlocks,
                            fontSize = state.fontSize,
                            onImageClick = { url -> selectedLightboxImageUrl = url }
                        )

                        // Ad Slot Bottom
                        AdBannerView(
                            location = AdSlotLocation.ARTICLE_DETAIL_BOTTOM,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        // Viral Share Box at Bottom of Article
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "खबर अच्छी लगी? मित्रों के साथ शेयर करें:",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // WhatsApp 1-Tap Share Button
                                    Button(
                                        onClick = {
                                            ShareUtils.shareToWhatsApp(
                                                context,
                                                article.title,
                                                article.canonicalUrl,
                                                article.cleanExcerpt
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "व्हाट्सएप", color = Color.White, fontWeight = FontWeight.Bold)
                                    }

                                    // Other Apps Share Button
                                    OutlinedButton(
                                        onClick = { ShareUtils.shareArticle(context, article.title, article.canonicalUrl) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "अन्य ऐप्स")
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }

            // Image Lightbox Dialog
            if (selectedLightboxImageUrl != null) {
                ImageLightboxDialog(
                    imageUrl = selectedLightboxImageUrl,
                    onDismiss = { selectedLightboxImageUrl = null }
                )
            }

            // Font Size Selector Modal Dialog
            if (showFontDialog && uiState is ArticleUiState.Success) {
                val currentSize = (uiState as ArticleUiState.Success).fontSize

                AlertDialog(
                    onDismissRequest = { showFontDialog = false },
                    title = {
                        Text(
                            text = stringResource(R.string.font_size),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    text = {
                        Column {
                            ArticleFontSize.entries.forEach { size ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = currentSize == size,
                                        onClick = {
                                            viewModel.setFontSize(size)
                                            showFontDialog = false
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = EditorialRed)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(size.labelRes),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = (16 * size.scaleFactor).sp
                                        )
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showFontDialog = false }) {
                            Text(text = "बंद करें", color = EditorialRed)
                        }
                    }
                )
            }
        }
    }
}
