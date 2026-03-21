package com.example.kaoyanassistant.data.repository

import com.example.kaoyanassistant.data.local.dao.SettingsDao
import com.example.kaoyanassistant.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val settingsDao: SettingsDao,
) {
    val settings: Flow<SettingsEntity> = settingsDao.observe().map { it ?: SettingsEntity() }

    suspend fun ensureDefaults() {
        if (settingsDao.get() == null) {
            settingsDao.insert(SettingsEntity())
        }
    }

    suspend fun getSettings(): SettingsEntity {
        return settingsDao.get() ?: SettingsEntity()
    }

    suspend fun update(settings: SettingsEntity) {
        settingsDao.insert(settings)
    }
}
