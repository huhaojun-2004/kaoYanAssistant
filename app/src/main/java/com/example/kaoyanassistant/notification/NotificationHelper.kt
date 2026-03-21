package com.example.kaoyanassistant.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.kaoyanassistant.MainActivity
import com.example.kaoyanassistant.R

class NotificationHelper(
    private val context: Context,
) {
    private val notificationManager: NotificationManager =
        context.getSystemService(NotificationManager::class.java)
    private val defaultNotificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    private val notificationAudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val timerChannel = NotificationChannel(
            TIMER_CHANNEL_ID,
            "学习计时",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "显示当前学习计时状态"
            setShowBadge(false)
            enableVibration(true)
            enableLights(true)
            setSound(defaultNotificationSound, notificationAudioAttributes)
        }

        val reminderChannel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            "学习提醒",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "打卡与待办提醒"
            enableVibration(true)
            enableLights(true)
            setSound(defaultNotificationSound, notificationAudioAttributes)
        }

        notificationManager.createNotificationChannels(listOf(timerChannel, reminderChannel))
    }

    fun buildTimerNotification(subjectName: String, startedAt: Long): Notification {
        val contentIntent = PendingIntent.getActivity(
            context,
            TIMER_NOTIFICATION_ID,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val compactView = buildTimerRemoteViews(
            layoutId = R.layout.notification_timer_compact,
            subjectName = subjectName,
            startedAt = startedAt,
        )
        val expandedView = buildTimerRemoteViews(
            layoutId = R.layout.notification_timer_expanded,
            subjectName = subjectName,
            startedAt = startedAt,
        )
        return NotificationCompat.Builder(context, TIMER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setContentTitle(context.getString(R.string.timer_notification_title))
            .setContentText(context.getString(R.string.timer_notification_text, subjectName))
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(true)
            .setWhen(startedAt)
            .setUsesChronometer(true)
            .setCustomContentView(compactView)
            .setCustomBigContentView(expandedView)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setColorized(true)
            .build()
    }

    fun showReminder(notificationId: Int, title: String, content: String) {
        if (!canPostNotifications()) return
        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(contentIntent)
            .build()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    fun cancel(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun buildTimerRemoteViews(
        layoutId: Int,
        subjectName: String,
        startedAt: Long,
    ): RemoteViews {
        val base = SystemClock.elapsedRealtime() - (System.currentTimeMillis() - startedAt)
        return RemoteViews(context.packageName, layoutId).apply {
            setTextViewText(R.id.timer_subject, subjectName)
            setTextViewText(
                R.id.timer_title,
                if (layoutId == R.layout.notification_timer_expanded) "专注计时" else "专注计时中",
            )
            setChronometer(R.id.timer_chronometer, base, null, true)
        }
    }

    companion object {
        const val TIMER_CHANNEL_ID = "study_timer_v2"
        const val REMINDER_CHANNEL_ID = "study_reminder_v3"
        const val TIMER_NOTIFICATION_ID = 1001
        const val CHECK_IN_NOTIFICATION_ID = 2001
        const val TODO_NOTIFICATION_ID = 2002
    }
}
