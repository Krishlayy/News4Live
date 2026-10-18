package com.news4live.app.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NewsFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token refreshed; can be sent to backend if per-user targeting is needed in future
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val postId = data["post_id"]?.toLongOrNull() ?: 0L
        val title = data["title"] ?: remoteMessage.notification?.title ?: "ताज़ा खबर"
        val excerpt = data["excerpt"] ?: remoteMessage.notification?.body ?: ""
        val imageUrl = data["image"] ?: remoteMessage.notification?.imageUrl?.toString()
        val canonicalUrl = data["url"]

        if (postId > 0 || title.isNotBlank()) {
            serviceScope.launch {
                NotificationHelper.showBreakingNewsNotification(
                    context = applicationContext,
                    postId = if (postId > 0) postId else System.currentTimeMillis(),
                    title = title,
                    excerpt = excerpt,
                    imageUrl = imageUrl,
                    canonicalUrl = canonicalUrl
                )
            }
        }
    }
}
