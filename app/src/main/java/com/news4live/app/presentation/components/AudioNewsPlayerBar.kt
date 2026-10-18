package com.news4live.app.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.news4live.app.audio.AudioPlayerState
import com.news4live.app.presentation.theme.EditorialNavy
import com.news4live.app.presentation.theme.EditorialRed

@Composable
fun AudioNewsPlayerBar(
    state: AudioPlayerState,
    onTogglePlayPause: () -> Unit,
    onStop: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSpeedMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (state.isPlaying) EditorialNavy else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (state.isPlaying) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Play / Pause Button with animated ring
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (state.isPlaying) EditorialRed else MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (state.isPlaying) "रोकें" else "खबर सुनें",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = if (state.isPlaying) "खबर सुनाई जा रही है..." else if (state.isPaused) "खबर रुकी हुई है" else "खबर सुनें (Audio News)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (state.isPlaying) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    if (state.totalParagraphs > 0 && (state.isPlaying || state.isPaused)) {
                        Text(
                            text = "भाग ${state.currentParagraphIndex + 1} / ${state.totalParagraphs}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (state.isPlaying) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        )
                    } else {
                        Text(
                            text = "हिंदी में समाचार ऑडियो सुनें",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (state.isPlaying) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Controls (Speed & Stop)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speed Chip
                Box {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (state.isPlaying) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                        modifier = Modifier.clickable { showSpeedMenu = true }
                    ) {
                        Text(
                            text = "${state.speechRate}x",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (state.isPlaying) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showSpeedMenu,
                        onDismissRequest = { showSpeedMenu = false }
                    ) {
                        listOf(0.8f, 1.0f, 1.25f, 1.5f).forEach { speed ->
                            DropdownMenuItem(
                                text = { Text("${speed}x") },
                                onClick = {
                                    onSpeedChange(speed)
                                    showSpeedMenu = false
                                }
                            )
                        }
                    }
                }

                if (state.isPlaying || state.isPaused) {
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onStop,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "बंद करें",
                            tint = if (state.isPlaying) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
