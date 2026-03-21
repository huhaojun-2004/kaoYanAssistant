package com.example.kaoyanassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.kaoyanassistant.ui.theme.KaoYanAssistantTheme
import com.example.kaoyanassistant.ui.AppViewModel
import com.example.kaoyanassistant.ui.KaoYanAssistantApp

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels { AppViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KaoYanAssistantTheme {
                KaoYanAssistantApp(viewModel = viewModel)
            }
        }
    }
}
