package com.example.safehome

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import com.example.safehome.data.repo.DeviceRepository
import com.example.safehome.presentation.main.MainActivity

@AndroidEntryPoint
class FirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var deviceRepository: DeviceRepository
    private val serviceScope = CoroutineScope(SupervisorJob())

    companion object {
        private const val TAG = "SafeHomeFirebaseMsgService"
    }

    override fun onNewToken(token: String) {
        Timber.d("Refreshed token: $token")
        serviceScope.launch {
            deviceRepository.saveFcmToken(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Timber.d("From: ${remoteMessage.from}")
        Timber.d("Message data payload: ${remoteMessage.data}")
        Timber.d("Message notification: ${remoteMessage.notification?.title}, ${remoteMessage.notification?.body}")

        if (remoteMessage.data.isNotEmpty()) {
            if (isLongRunningJob(remoteMessage.data)) {
                scheduleJob()
            } else {
                handleNow()
            }
        }

        remoteMessage.notification?.let {
            sendNotification(it.body ?: "", it.title ?: "Safe Home")
        } ?: run {
            val title = remoteMessage.data["title"] ?: "Safe Home"
            val body = remoteMessage.data["body"] ?: ""
            sendNotification(body, title)
        }
    }

    private fun isLongRunningJob(data: Map<String, String>): Boolean {
        return false
    }

    private fun scheduleJob() {
        Timber.d("Scheduled WorkManager job")
    }

    private fun handleNow() {
        Timber.d("Handled data payload immediately")
    }

    private fun sendNotification(messageBody: String, messageTitle: String) {
        val requestCode = System.currentTimeMillis().toInt()
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "safe_home_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notify)
            .setContentTitle(messageTitle)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Safe Home Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
            Timber.d("Notification channel created: $channelId")
        }

        notificationManager.notify(requestCode, notificationBuilder.build())
    }
}