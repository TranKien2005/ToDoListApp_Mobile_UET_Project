package com.example.todolist.ui.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.todolist.core.model.User
import com.example.todolist.ui.common.TopBarUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String = "Todolist",
    showTopBar: Boolean = false,
    showBottomBar: Boolean = false,
    // Thêm tham số cho TopBarUser
    user: User? = null,
    unreadNotificationCount: Int = 0,
    onNotificationClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    // callbacks for bottom bar actions
    onHome: () -> Unit = {},
    onList: () -> Unit = {},
    onStats: () -> Unit = {},
    onVoice: () -> Unit = {},
    onAdd: () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            if (showTopBar) {
                // Sử dụng TopBarUser mới thay vì TopAppBar mặc định
                TopBarUser(
                    user = user,
                    unreadNotificationCount = unreadNotificationCount,
                    onNotificationClick = onNotificationClick,
                    onSettingsClick = onSettingsClick
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                BottomBar(
                    onHome = onHome,
                    onList = onList,
                    onStats = onStats,
                    onVoice = onVoice,
                    onAdd = onAdd
                )
            }
        },
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onBackground) {
            content(innerPadding)
        }
    }
}
