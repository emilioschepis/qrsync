package com.emilioschepis.qrsync.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.emilioschepis.qrsync.R
import com.emilioschepis.qrsync.ui.splash.SplashActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class QSFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val notificationRequestCode = 884
    }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onMessageReceived(message: RemoteMessage?) {
        super.onMessageReceived(message)

        if (message == null) {
            return
        }

        if (message.data.isNotEmpty()) {
            // Payloads are not handled yet.
        }

        message.notification?.let {
            initNotificationChannels()
            showNotification(it.title, it.body)
        }
    }

    private fun initNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.notification_channel_info_id)
            val channelName = getString(R.string.notification_channel_info_name)
            val channelColor = getColor(R.color.colorPrimary)
            val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.lightColor = channelColor
            channel.enableLights(true)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String?, body: String?) {
        val intent = Intent(this, SplashActivity::class.java)
                .apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
        val pending = PendingIntent.getActivity(
                this,
                notificationRequestCode,
                intent,
                PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_qr_sync_logo_trim)
                .setContentTitle(title)
                .setContentText(body)
                .setSound(defaultSoundUri)
                .setAutoCancel(true)
                .setColor(getColor(R.color.colorPrimary))
                .setContentIntent(pending)

        notificationManager.notify(notificationRequestCode, builder.build())
    }
}