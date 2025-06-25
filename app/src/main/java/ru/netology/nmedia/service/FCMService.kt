package ru.netology.nmedia.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.netology.nmedia.R
import kotlin.random.Random

class FCMService: FirebaseMessagingService() {

    private val gson = Gson()
    private val content = "content"


    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }


    override fun onNewToken(token: String) {
        println(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {

        message.data["action"]?.let {
            try {
                when (Action.valueOf(it)) {
                    Action.LIKE -> handleLike(gson.fromJson(message.data[content], Like::class.java))
                    Action.NEW_POST -> handleNewPost(gson.fromJson(message.data[content], NewPost::class.java))
                }
            }
            catch (e: IllegalArgumentException) {
                 handleUnknownNotification()
            }
        }
    }

    private fun handleLike(content: Like) {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.notification_user_liked, content.userName, content.postAuthor))
                .build()
            NotificationManagerCompat.from(this).notify(Random.nextInt(100_000), notification)
        }
    }
    private fun handleNewPost(content: NewPost) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(content.author)
                .setContentText(content.published)
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText(content.content))
                .build()
            NotificationManagerCompat.from(this).notify(Random.nextInt(100_000), notification)
        }
    }

    private fun handleUnknownNotification() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Возможно, у вас есть новые уведомления!")
                .build()
            NotificationManagerCompat.from(this).notify(Random.nextInt(100_000), notification)
        }
    }


    companion object {
        private const val CHANNEL_ID = "notifications"
    }

}

enum class Action {
    LIKE,
    NEW_POST
}

data class NewPost (
    val author: String,
    val content: String,
    val published: String
)

data class Like (
    val userId: Long,
    val userName: String,
    val postId: Long,
    val postAuthor: String
)