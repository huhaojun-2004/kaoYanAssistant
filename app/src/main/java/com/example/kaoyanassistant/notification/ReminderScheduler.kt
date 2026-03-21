package com.example.kaoyanassistant.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.os.Build
import android.content.Context
import android.content.Intent
import com.example.kaoyanassistant.data.local.entity.SettingsEntity
import com.example.kaoyanassistant.util.DateTimeUtils

class ReminderScheduler(
    private val context: Context,
) {
    private val alarmManager: AlarmManager =
        context.getSystemService(AlarmManager::class.java)

    fun rescheduleAll(settings: SettingsEntity) {
        runCatching {
            cancelAll()

            if (settings.checkInReminderEnabled) {
                scheduleDaily(
                    action = ACTION_CHECK_IN_REMINDER,
                    requestCode = REQUEST_CODE_CHECK_IN,
                    triggerAtMillis = DateTimeUtils.nextTriggerMillis(
                        settings.checkInReminderHour,
                        settings.checkInReminderMinute,
                    ),
                )
            }

            if (settings.todoReminderEnabled) {
                scheduleDaily(
                    action = ACTION_TODO_REMINDER,
                    requestCode = REQUEST_CODE_TODO,
                    triggerAtMillis = DateTimeUtils.nextTriggerMillis(
                        settings.todoReminderHour,
                        settings.todoReminderMinute,
                    ),
                )
            }
        }.getOrElse {
            // Avoid crashing app startup if a ROM rejects exact-alarm scheduling.
        }
    }

    fun cancelAll() {
        alarmManager.cancel(pendingIntent(ACTION_CHECK_IN_REMINDER, REQUEST_CODE_CHECK_IN))
        alarmManager.cancel(pendingIntent(ACTION_TODO_REMINDER, REQUEST_CODE_TODO))
    }

    private fun scheduleDaily(action: String, requestCode: Int, triggerAtMillis: Long) {
        val pendingIntent = pendingIntent(action, requestCode)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms() -> {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent,
                )
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent,
                )
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent,
                )
            }

            else -> {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent,
                )
            }
        }
    }

    private fun pendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).setAction(action)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val ACTION_CHECK_IN_REMINDER = "com.example.kaoyanassistant.action.CHECK_IN_REMINDER"
        const val ACTION_TODO_REMINDER = "com.example.kaoyanassistant.action.TODO_REMINDER"
        private const val REQUEST_CODE_CHECK_IN = 3001
        private const val REQUEST_CODE_TODO = 3002
    }
}
