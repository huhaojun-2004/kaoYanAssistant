package com.example.kaoyanassistant.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.PunchClock
import androidx.compose.material.icons.outlined.Today
import androidx.compose.ui.graphics.vector.ImageVector

data class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

object AppRoutes {
    const val ROOT = "root"
    const val SUBJECTS = "subjects"
    const val TIMER = "timer"
    const val CHECK_IN = "check_in"
    const val STATISTICS = "statistics"
    const val TODO = "todo"
    const val SETTINGS = "settings"
    const val DAILY_DETAIL = "daily_detail/{date}"
    const val TODO_EDITOR = "todo_editor?todoId={todoId}"

    fun dailyDetail(date: String): String = "daily_detail/$date"
    fun todoEditor(todoId: Long? = null): String = if (todoId == null) "todo_editor" else "todo_editor?todoId=$todoId"
}

val topLevelDestinations = listOf(
    TopLevelDestination(AppRoutes.TIMER, "计时", Icons.Outlined.PunchClock),
    TopLevelDestination(AppRoutes.CHECK_IN, "打卡", Icons.Outlined.Today),
    TopLevelDestination(AppRoutes.STATISTICS, "统计", Icons.Outlined.AutoGraph),
    TopLevelDestination(AppRoutes.TODO, "待办", Icons.Outlined.Checklist),
    TopLevelDestination(AppRoutes.SUBJECTS, "科目", Icons.Outlined.Bookmarks),
)

fun titleForRoute(route: String?): String {
    return when (route) {
        AppRoutes.SUBJECTS -> "科目管理"
        AppRoutes.TIMER -> "专注计时"
        AppRoutes.CHECK_IN -> "日历打卡"
        AppRoutes.STATISTICS -> "学习统计"
        AppRoutes.TODO -> "待办清单"
        AppRoutes.SETTINGS -> "提醒设置"
        AppRoutes.TODO_EDITOR -> "待办编辑"
        AppRoutes.DAILY_DETAIL -> "每日详情"
        else -> "考研助手"
    }
}
