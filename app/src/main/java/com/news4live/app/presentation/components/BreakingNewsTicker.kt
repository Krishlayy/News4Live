package com.news4live.app.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.news4live.app.domain.model.Article
import com.news4live.app.presentation.theme.BreakingRed
import kotlinx.coroutines.delay

@Composable
fun BreakingNewsTicker(
    breakingArticles: List<Article>,
    onArticleClick: (Article) -> Unit,
    modifier: Modifier = Modifier
) {
    if (breakingArticles.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(breakingArticles.size) {
        if (breakingArticles.size > 1) {
            while (true) {
                delay(4000)
                currentIndex = (currentIndex + 1) % breakingArticles.size
            }
        }
    }

    val currentArticle = breakingArticles.getOrNull(currentIndex) ?: return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(BreakingRed.copy(alpha = 0.12f))
            .clickable { onArticleClick(currentArticle) }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Breaking Badge
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(BreakingRed)
                .padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "ब्रेकिंग",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Animated Headline
        AnimatedContent(
            targetState = currentArticle,
            transitionSpec = {
                slideInVertically { height -> height } togetherWith slideOutVertically { height -> -height }
            },
            modifier = Modifier.weight(1f),
            label = "breaking_headline_anim"
        ) { article ->
            Text(
                text = article.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
