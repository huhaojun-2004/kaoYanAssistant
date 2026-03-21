package com.example.kaoyanassistant.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.kaoyanassistant.KaoYanAssistantApplication
import com.example.kaoyanassistant.notification.NotificationHelper

class StudyTimerService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val subjectName = intent.getStringExtra(EXTRA_SUBJECT_NAME).orEmpty()
                val startedAt = intent.getLongExtra(EXTRA_STARTED_AT, System.currentTimeMillis())
                val carriedMillis = intent.getLongExtra(EXTRA_CARRIED_MILLIS, 0L)
                val helper = (application as KaoYanAssistantApplication).container.notificationHelper
                startForeground(
                    NotificationHelper.TIMER_NOTIFICATION_ID,
                    helper.buildTimerNotification(subjectName, startedAt - carriedMillis),
                )
            }

            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    companion object {
        const val ACTION_START = "com.example.kaoyanassistant.action.START_TIMER"
        const val ACTION_STOP = "com.example.kaoyanassistant.action.STOP_TIMER"
        const val EXTRA_SUBJECT_NAME = "extra_subject_name"
        const val EXTRA_STARTED_AT = "extra_started_at"
        const val EXTRA_CARRIED_MILLIS = "extra_carried_millis"
    }
}
