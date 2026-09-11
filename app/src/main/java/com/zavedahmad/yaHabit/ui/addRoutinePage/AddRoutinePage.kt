package com.zavedahmad.yaHabit.ui.addRoutinePage

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
fun AddRoutinePage(viewModel: AddRoutinePageViewModel, backStack: NavBackStack) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.navKey.routineId != null) "Edit Routine" else "Add Routine") },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Text("Routine form placeholder — will list habits multi-select + color/frequency")
        }
    }
}
