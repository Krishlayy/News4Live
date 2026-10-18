package com.news4live.app.presentation.more

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.news4live.app.BuildConfig
import com.news4live.app.R
import com.news4live.app.presentation.theme.EditorialRed
import com.news4live.app.util.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onOpenUrl: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.nav_more),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
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
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // App Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.app_logo),
                        contentDescription = "News 4 Live Logo",
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "NEWS 4 LIVE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.app_tagline),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.app_version, BuildConfig.VERSION_NAME),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Trust & Verification Badges Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.trust_section_title),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TrustBadgeRow(
                        icon = Icons.Default.VerifiedUser,
                        text = stringResource(R.string.trust_secure),
                        tint = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TrustBadgeRow(
                        icon = Icons.Default.Bolt,
                        text = stringResource(R.string.trust_superfast),
                        tint = Color(0xFFEF6C00)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TrustBadgeRow(
                        icon = Icons.Default.CloudDownload,
                        text = stringResource(R.string.trust_offline),
                        tint = Color(0xFF1565C0)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TrustBadgeRow(
                        icon = Icons.Default.Flag,
                        text = stringResource(R.string.trust_made_in_india),
                        tint = Color(0xFFFF6F00)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation List Items
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    MoreMenuItem(
                        icon = Icons.Default.Share,
                        title = stringResource(R.string.more_share_app),
                        onClick = { ShareUtils.shareApp(context) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    MoreMenuItem(
                        icon = Icons.Default.Star,
                        title = stringResource(R.string.more_rate),
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://apkpure.com/p/com.news4live.app"))
                                context.startActivity(intent)
                            }
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    MoreMenuItem(
                        icon = Icons.Default.Language,
                        title = "वेबसाइट पर जाएं (news4live.com)",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.WORDPRESS_BASE_URL))
                            context.startActivity(intent)
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    MoreMenuItem(
                        icon = Icons.Default.Policy,
                        title = stringResource(R.string.more_privacy),
                        onClick = {
                            onOpenUrl("गोपनीयता नीति", "${BuildConfig.WORDPRESS_BASE_URL}privacy-policy/")
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    MoreMenuItem(
                        icon = Icons.Default.Description,
                        title = stringResource(R.string.more_terms),
                        onClick = {
                            onOpenUrl("नियम और शर्तें", "${BuildConfig.WORDPRESS_BASE_URL}terms-and-conditions/")
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    MoreMenuItem(
                        icon = Icons.AutoMirrored.Filled.ContactSupport,
                        title = stringResource(R.string.more_contact),
                        onClick = {
                            onOpenUrl("संपर्क करें", "${BuildConfig.WORDPRESS_BASE_URL}contact-us/")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun MoreMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp)
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun TrustBadgeRow(
    icon: ImageVector,
    text: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
