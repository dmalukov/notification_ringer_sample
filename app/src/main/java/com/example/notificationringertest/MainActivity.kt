package com.example.notificationringertest

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity() {
    private val nm by lazy { NotificationManagerCompat.from(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.start_ringing_btn).setOnClickListener {
            startRinging()
        }

        findViewById<Button>(R.id.stop_ringing_btn).setOnClickListener {
            stopRinging()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == DROP_CALL_ACTION)
            stopRinging()
    }

    private fun startRinging() {
        val notification = createIncomingCallNotification()
        nm.notify(NOTIFICATION_ID, notification)
    }

    private fun stopRinging() {
        nm.cancel(NOTIFICATION_ID)
    }

    private fun createIncomingCallNotification(): Notification {
        val channel = getNotificationChannel()

        val openActivityIntent = PendingIntent.getActivity(
            applicationContext,
            1,
            Intent(applicationContext, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE
        )

        val dropCallPendingIntent = PendingIntent.getActivity(
            applicationContext,
            2,
            Intent(applicationContext, MainActivity::class.java).apply {
                action = DROP_CALL_ACTION
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(applicationContext, channel.id)
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_call,
                    "Answer",
                    dropCallPendingIntent,
                ).build()
            ).addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_call_end,
                    "Reject",
                    dropCallPendingIntent,
                ).build()
            ).setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(openActivityIntent, true)
            .setContentText("Incoming call")
            .setSmallIcon(R.drawable.ic_call)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .build().apply {
                flags = flags.or(NotificationCompat.FLAG_INSISTENT)
            }

    }

    private fun getNotificationChannel(): NotificationChannelCompat {
        val channel = nm.getNotificationChannelCompat(NOTIFICATION_CHANNEL_ID)
        if (channel != null)
            return channel

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
            .build()

        val newChannel = NotificationChannelCompat.Builder(
            NOTIFICATION_CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_MAX
        ).setSound(
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE),
            audioAttributes
        ).setName("Incoming calls")
            .setShowBadge(false)
            .setVibrationEnabled(true)
            .build()

        nm.createNotificationChannel(newChannel)
        return newChannel
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "incoming_calls_channel"
        private const val DROP_CALL_ACTION = "drop_call"
        private const val NOTIFICATION_ID = 100
    }
}