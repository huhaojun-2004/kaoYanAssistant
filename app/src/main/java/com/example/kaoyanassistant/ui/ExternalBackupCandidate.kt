package com.example.kaoyanassistant.ui

import com.example.kaoyanassistant.util.AppBackupPayload

data class ExternalBackupCandidate(
    val sourceName: String,
    val json: String,
    val payload: AppBackupPayload,
)
