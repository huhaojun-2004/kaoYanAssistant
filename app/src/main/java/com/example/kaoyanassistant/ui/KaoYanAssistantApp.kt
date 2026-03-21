package com.example.kaoyanassistant.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kaoyanassistant.ui.navigation.AppRoutes
import com.example.kaoyanassistant.ui.navigation.titleForRoute
import com.example.kaoyanassistant.ui.navigation.topLevelDestinations
import com.example.kaoyanassistant.ui.screens.CheckInScreen
import com.example.kaoyanassistant.ui.screens.DailyDetailScreen
import com.example.kaoyanassistant.ui.screens.SettingsScreen
import com.example.kaoyanassistant.ui.screens.StatisticsScreen
import com.example.kaoyanassistant.ui.screens.SubjectsScreen
import com.example.kaoyanassistant.ui.screens.TimerScreen
import com.example.kaoyanassistant.ui.screens.TodoEditorScreen
import com.example.kaoyanassistant.ui.screens.TodoScreen
import com.example.kaoyanassistant.ui.theme.Apricot
import com.example.kaoyanassistant.ui.theme.Cream
import com.example.kaoyanassistant.ui.theme.Muted
import com.example.kaoyanassistant.ui.theme.Paper
import com.example.kaoyanassistant.ui.theme.Pine
import com.example.kaoyanassistant.ui.theme.PineDark
import com.example.kaoyanassistant.util.DateTimeUtils

private sealed interface SecondaryScreen {
    val route: String

    data object Settings : SecondaryScreen {
        override val route: String = AppRoutes.SETTINGS
    }

    data class DailyDetail(val date: String) : SecondaryScreen {
        override val route: String = AppRoutes.DAILY_DETAIL
    }

    data class TodoEditor(
        val todoId: Long?,
        val initialDate: String,
    ) : SecondaryScreen {
        override val route: String = AppRoutes.TODO_EDITOR
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KaoYanAssistantApp(
    viewModel: AppViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTopLevelRoute by rememberSaveable { mutableStateOf(AppRoutes.TIMER) }
    var secondaryScreen by remember { mutableStateOf<SecondaryScreen?>(null) }
    val currentRoute = secondaryScreen?.route ?: selectedTopLevelRoute
    val isTopLevel = secondaryScreen == null
    val daysUntilExam = DateTimeUtils.daysUntilExam()
    val contentBrush = Brush.verticalGradient(
        colors = listOf(Apricot.copy(alpha = 0.08f), Cream, Paper),
    )
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {},
    )

    BackHandler(enabled = !isTopLevel) {
        secondaryScreen = null
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = titleForRoute(currentRoute)) },
                navigationIcon = {
                    if (isTopLevel) {
                        CountdownBadge(
                            daysLeft = daysUntilExam,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    } else {
                        IconButton(onClick = { secondaryScreen = null }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回",
                            )
                        }
                    }
                },
                actions = {
                    if (currentRoute != AppRoutes.SETTINGS) {
                        IconButton(onClick = { secondaryScreen = SecondaryScreen.Settings }) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "设置",
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (isTopLevel) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = Paper,
                    tonalElevation = 0.dp,
                    shadowElevation = 10.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        topLevelDestinations.forEach { destination ->
                            val selected = currentRoute == destination.route
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        color = if (selected) Pine.copy(alpha = 0.17f) else Color.Transparent,
                                        shape = RoundedCornerShape(18.dp),
                                    )
                                    .clickable { selectedTopLevelRoute = destination.route }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label,
                                    tint = if (selected) PineDark else Muted,
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(
                                    text = destination.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) PineDark else Muted,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = Cream,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = contentBrush)
                .padding(innerPadding),
        ) {
            when (val screen = secondaryScreen) {
                null -> {
                    when (selectedTopLevelRoute) {
                        AppRoutes.SUBJECTS -> {
                            SubjectsScreen(
                                uiState = uiState,
                                onStartTimerForSubject = { subjectId ->
                                    viewModel.selectTimerSubject(subjectId)
                                    selectedTopLevelRoute = AppRoutes.TIMER
                                },
                                onSaveSubject = viewModel::saveSubject,
                                onDeleteSubject = viewModel::deleteSubject,
                            )
                        }

                        AppRoutes.TIMER -> {
                            TimerScreen(
                                uiState = uiState,
                                onSelectSubject = viewModel::selectTimerSubject,
                                onStartTimer = viewModel::startTimerForSelected,
                                onStartSubject = viewModel::startTimerForSubject,
                                onStopTimer = viewModel::stopTimer,
                            )
                        }

                        AppRoutes.CHECK_IN -> {
                            CheckInScreen(
                                uiState = uiState,
                                onToggleCheckInToday = { checkedIn ->
                                    viewModel.setCheckIn(
                                        DateTimeUtils.todayKey(),
                                        checkedIn,
                                    )
                                },
                                onOpenDailyDetail = { date ->
                                    secondaryScreen = SecondaryScreen.DailyDetail(date)
                                },
                                onSetRestDay = viewModel::setRestDay,
                            )
                        }

                        AppRoutes.STATISTICS -> {
                            StatisticsScreen(
                                uiState = uiState,
                                onDeleteStudyTime = viewModel::deleteStudyTimeInRange,
                            )
                        }

                        AppRoutes.TODO -> {
                            TodoScreen(
                                uiState = uiState,
                                onAddTask = { selectedDate ->
                                    secondaryScreen = SecondaryScreen.TodoEditor(
                                        todoId = null,
                                        initialDate = selectedDate,
                                    )
                                },
                                onEditTask = { todoId ->
                                    secondaryScreen = SecondaryScreen.TodoEditor(
                                        todoId = todoId,
                                        initialDate = DateTimeUtils.todayKey(),
                                    )
                                },
                                onToggleComplete = viewModel::setTodoCompleted,
                                onDeleteTodo = viewModel::deleteTodo,
                            )
                        }
                    }
                }

                is SecondaryScreen.Settings -> {
                    SettingsScreen(
                        settings = uiState.settings,
                        onSave = viewModel::saveSettings,
                        onRequestPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                    )
                }

                is SecondaryScreen.DailyDetail -> {
                    DailyDetailScreen(
                        date = screen.date,
                        uiState = uiState,
                    )
                }

                is SecondaryScreen.TodoEditor -> {
                    TodoEditorScreen(
                        uiState = uiState,
                        todoId = screen.todoId,
                        initialDate = screen.initialDate,
                        onSave = { id, content, subjectId, taskDate ->
                            viewModel.saveTodo(id, content, subjectId, taskDate)
                            secondaryScreen = null
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun CountdownBadge(
    daysLeft: Long,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Pine.copy(alpha = 0.1f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(
                text = "倒计时",
                style = MaterialTheme.typography.labelSmall,
                color = Pine.copy(alpha = 0.84f),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Pine.copy(alpha = 0.16f))
                        .padding(4.dp),
                )
                Text(
                    text = "${daysLeft}天",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = PineDark,
                )
            }
        }
    }
}
