package com.example.kaoyanassistant.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UiPreferencesRepository(
    context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val _showCountdownBadge = MutableStateFlow(
        preferences.getBoolean(KEY_SHOW_COUNTDOWN_BADGE, true),
    )

    val showCountdownBadge: StateFlow<Boolean> = _showCountdownBadge.asStateFlow()

    fun setShowCountdownBadge(show: Boolean) {
        preferences.edit().putBoolean(KEY_SHOW_COUNTDOWN_BADGE, show).apply()
        _showCountdownBadge.value = show
    }

    companion object {
        private const val PREFERENCES_NAME = "ui_preferences"
        private const val KEY_SHOW_COUNTDOWN_BADGE = "show_countdown_badge"
    }
}
