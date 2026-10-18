package com.news4live.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.messaging.FirebaseMessaging
import com.news4live.app.presentation.navigation.AppNavigation
import com.news4live.app.presentation.navigation.Screen
import com.news4live.app.presentation.theme.News4LiveTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            subscribeToFcmTopics()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install branded splash screen before super.onCreate()
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Always subscribe to default news topics
        subscribeToFcmTopics()

        val app = application as NewsApplication
        val viewModelFactory = app.viewModelFactory

        setContent {
            News4LiveTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    AppNavigation(
                        viewModelFactory = viewModelFactory,
                        navController = navController
                    )

                    // Handle intent extras from notification tap or deep link
                    LaunchedEffectHandleIntent(navController)
                }
            }
        }
    }

    fun requestNotificationPermissionPrompt() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun subscribeToFcmTopics() {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic("breaking_news")
            FirebaseMessaging.getInstance().subscribeToTopic("all_news")
        } catch (_: Exception) {}
    }

    @Composable
    private fun LaunchedEffectHandleIntent(navController: androidx.navigation.NavHostController) {
        LaunchedEffect(intent) {
            val postId = intent?.getLongExtra("EXTRA_POST_ID", 0L) ?: 0L
            if (postId > 0) {
                navController.navigate(Screen.ArticleDetail.createRoute(postId))
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
