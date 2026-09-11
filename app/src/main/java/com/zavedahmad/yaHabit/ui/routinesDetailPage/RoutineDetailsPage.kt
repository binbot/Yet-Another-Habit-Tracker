package com.zavedahmad.yaHabit.ui.routinesDetailPage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDetailsPage(routineId: Int, backStack: NavBackStack) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Routine $routineId") },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Text("Routine details placeholder — future stats + habit steps")
        }
    }
}
