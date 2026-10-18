package com.news4live.app.presentation.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.news4live.app.R
import com.news4live.app.domain.model.ParsedHtmlBlock
import com.news4live.app.presentation.theme.EditorialNavy
import com.news4live.app.presentation.theme.EditorialRed
import com.news4live.app.util.ArticleFontSize

@Composable
fun HtmlArticleRenderer(
    blocks: List<ParsedHtmlBlock>,
    fontSize: ArticleFontSize = ArticleFontSize.MEDIUM,
    onImageClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val baseFontSize = 17.sp * fontSize.scaleFactor
    val baseLineHeight = 28.sp * fontSize.scaleFactor

    SelectionContainer {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            blocks.forEach { block ->
                when (block) {
                    is ParsedHtmlBlock.Paragraph -> {
                        Text(
                            text = block.text,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = baseFontSize,
                                lineHeight = baseLineHeight,
                                fontStyle = if (block.isItalic) FontStyle.Italic else FontStyle.Normal,
                                fontWeight = if (block.isBold) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.padding(bottom = 14.dp)
                        )
                    }

                    is ParsedHtmlBlock.Heading -> {
                        val headingSize = when (block.level) {
                            1 -> 22.sp * fontSize.scaleFactor
                            2 -> 20.sp * fontSize.scaleFactor
                            3 -> 18.sp * fontSize.scaleFactor
                            else -> 17.sp * fontSize.scaleFactor
                        }
                        Text(
                            text = block.text,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = headingSize,
                                lineHeight = (headingSize.value + 8).sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
                        )
                    }

                    is ParsedHtmlBlock.Image -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(block.url)
                                    .crossfade(true)
                                    .placeholder(R.drawable.placeholder_news)
                                    .error(R.drawable.placeholder_news)
                                    .build(),
                                contentDescription = block.alt ?: block.caption ?: "समाचार चित्र",
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onImageClick?.invoke(block.url) }
                            )
                            if (!block.caption.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = block.caption,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontStyle = FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    is ParsedHtmlBlock.Blockquote -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .background(EditorialRed)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = block.text,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = baseFontSize,
                                    lineHeight = baseLineHeight,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    is ParsedHtmlBlock.BulletList -> {
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            block.items.forEach { item ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "• ",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = EditorialRed,
                                            fontSize = baseFontSize
                                        )
                                    )
                                    Text(
                                        text = item,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = baseFontSize,
                                            lineHeight = baseLineHeight,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }

                    is ParsedHtmlBlock.VideoEmbed -> {
                        YouTubePlayerCard(
                            videoUrl = block.videoUrl,
                            youtubeVideoId = block.youtubeVideoId
                        )
                    }

                    is ParsedHtmlBlock.Link -> {
                        Text(
                            text = block.text,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = EditorialNavy,
                                fontSize = baseFontSize
                            ),
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(block.url)).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                }
                        )
                    }

                    is ParsedHtmlBlock.RawHtml -> {
                        Text(
                            text = block.html,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = baseFontSize,
                                lineHeight = baseLineHeight
                            ),
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }
                }
            }
        }
    }
}
