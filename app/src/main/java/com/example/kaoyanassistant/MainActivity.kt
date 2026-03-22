package com.example.kaoyanassistant

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.IntentCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.kaoyanassistant.ui.AppViewModel
import com.example.kaoyanassistant.ui.ExternalBackupCandidate
import com.example.kaoyanassistant.ui.KaoYanAssistantApp
import com.example.kaoyanassistant.ui.theme.KaoYanAssistantTheme
import com.example.kaoyanassistant.util.AppBackupSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels { AppViewModel.Factory }
    private var externalBackupCandidate by mutableStateOf<ExternalBackupCandidate?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIncomingImportIntent(intent)
        setContent {
            KaoYanAssistantTheme {
                KaoYanAssistantApp(
                    viewModel = viewModel,
                    externalBackupCandidate = externalBackupCandidate,
                    onDismissExternalBackup = { externalBackupCandidate = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingImportIntent(intent)
    }

    private fun handleIncomingImportIntent(intent: Intent?) {
        lifecycleScope.launch {
            when (val result = readExternalBackupCandidate(intent)) {
                null -> Unit
                else -> result.fold(
                    onSuccess = { candidate ->
                        externalBackupCandidate = candidate
                    },
                    onFailure = { throwable ->
                        Toast.makeText(
                            this@MainActivity,
                            "不是可导入的备份文件：${throwable.message ?: "格式不支持"}",
                            Toast.LENGTH_SHORT,
                        ).show()
                    },
                )
            }
        }
    }

    private suspend fun readExternalBackupCandidate(intent: Intent?): Result<ExternalBackupCandidate>? {
        val action = intent?.action ?: return null
        val streamUri = when (action) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND -> IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
            else -> null
        }
        val sharedText = when (action) {
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            else -> null
        }?.trim().orEmpty()

        if (streamUri == null && sharedText.isBlank()) return null

        return withContext(Dispatchers.IO) {
            runCatching {
                val json = when {
                    streamUri != null -> {
                        contentResolver.openInputStream(streamUri)?.bufferedReader()?.use { it.readText() }
                            ?: error("无法读取文件")
                    }
                    else -> sharedText
                }
                val payload = AppBackupSerializer.parse(json)
                ExternalBackupCandidate(
                    sourceName = streamUri?.let(::queryDisplayName).orEmpty().ifBlank { "外部备份文件" },
                    json = json,
                    payload = payload,
                )
            }
        }
    }

    private fun queryDisplayName(uri: Uri): String {
        return contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
            }
            ?: uri.lastPathSegment
            ?: "外部备份文件"
    }
}
