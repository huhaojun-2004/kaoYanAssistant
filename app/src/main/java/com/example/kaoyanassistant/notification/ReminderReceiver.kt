package com.example.kaoyanassistant.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.kaoyanassistant.KaoYanAssistantApplication
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val app = context.applicationContext as KaoYanAssistantApplication
        val pendingResult = goAsync()
        app.container.applicationScope.launch {
            try {
                when (intent?.action) {
                    ReminderScheduler.ACTION_CHECK_IN_REMINDER -> handleCheckInReminder(app)
                    ReminderScheduler.ACTION_TODO_REMINDER -> handleTodoReminder(app)
                }
                val settings = app.container.settingsRepository.getSettings()
                app.container.reminderScheduler.rescheduleAll(settings)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleCheckInReminder(app: KaoYanAssistantApplication) {
        val today = com.example.kaoyanassistant.util.DateTimeUtils.todayKey()
        val record = app.container.checkInRepository.getByDate(today)
        if (record?.isRestDay == true || record?.isCheckIn == true) return

        app.container.notificationHelper.showReminder(
            notificationId = NotificationHelper.CHECK_IN_NOTIFICATION_ID,
            title = app.getString(com.example.kaoyanassistant.R.string.check_in_reminder_title),
            content = "还没有打卡，今天的坚持从按下一次开始。",
        )
    }

    private suspend fun handleTodoReminder(app: KaoYanAssistantApplication) {
        val today = com.example.kaoyanassistant.util.DateTimeUtils.todayKey()
        val remaining = app.container.todoRepository.actualIncompleteCountForDate(today)
        if (remaining <= 0) return

        app.container.notificationHelper.showReminder(
            notificationId = NotificationHelper.TODO_NOTIFICATION_ID,
            title = app.getString(com.example.kaoyanassistant.R.string.todo_reminder_title),
            content = "还有${remaining}个待办未完成",
        )
    }
}
