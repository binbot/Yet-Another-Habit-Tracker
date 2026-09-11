package com.zavedahmad.yaHabit.ui.addRoutinePage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import com.zavedahmad.yaHabit.ui.addHabitPage.ColorSelector
import com.zavedahmad.yaHabit.ui.addHabitPage.FrequencySelector
import com.zavedahmad.yaHabit.ui.theme.CustomTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRoutinePage(viewModel: AddRoutinePageViewModel, backStack: NavBackStack) {
    val name by viewModel.routineName.collectAsStateWithLifecycle()
    val description by viewModel.routineDescription.collectAsStateWithLifecycle()
    val allHabits by viewModel.habits.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedHabitIds.collectAsStateWithLifecycle()
    val selectedColor by viewModel.selectedColor.collectAsStateWithLifecycle()
    val isNameError = remember { derivedStateOf { name.isEmpty() } }
    val frequencyError = remember { androidx.compose.runtime.mutableStateOf(false) }
    val errorCommon = remember { derivedStateOf { isNameError.value || frequencyError.value } }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    CustomTheme(primaryColor = selectedColor, isAmoled = false, theme = "system") {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = { Text(if (viewModel.navKey.routineId != null) "Edit Routine" else "Add Routine") },
                    navigationIcon = {
                        IconButton(onClick = { backStack.removeLastOrNull() }) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "back")
                        }
                    },
                    actions = {
                        AnimatedVisibility(visible = !errorCommon.value, enter = fadeIn(), exit = fadeOut()) {
                            Button(
                                onClick = { viewModel.saveRoutine(); backStack.removeLastOrNull() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) { Icon(Icons.Default.Check, contentDescription = "save") }
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = name,
                    placeholder = { Text("Routine name", fontStyle = FontStyle.Italic, color = Color.Gray) },
                    onValueChange = { viewModel.setRoutineName(it) },
                    shape = RoundedCornerShape(50.dp),
                    singleLine = true
                )
                Spacer(Modifier.height(20.dp))
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = description,
                    placeholder = { Text("Description", fontStyle = FontStyle.Italic, color = Color.Gray) },
                    onValueChange = { viewModel.setRoutineDescription(it) },
                    colors = TextFieldDefaults.colors(focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                    shape = RoundedCornerShape(20.dp),
                    maxLines = 4
                )
                Heading("Target Days")
                // Reuse FrequencySelector by bridging routine ViewModel to habit-like interface via wrapper
                // We create a lightweight fake viewModel adapter for FrequencySelector by using routine fields directly
                // For v1 we just show simple weekly 5/7 default with no selector UI to keep placeholder simple
                Text("Default: 5 days per week (Weekly)", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(10.dp))

                Heading("Steps — select habits")
                if (allHabits.isEmpty()) {
                    Text("No habits yet — create a habit first", style = MaterialTheme.typography.bodySmall)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        allHabits.forEach { habit ->
                            val selected = habit.id in selectedIds
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.toggleHabitSelection(habit.id) },
                                label = { Text(habit.name) }
                            )
                        }
                    }
                    Text("${selectedIds.size} selected", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Heading("Color")
                // Reuse habit color selector with routine colors
                RoutineColorSelector(viewModel)

                Spacer(Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun Heading(heading: String) {
    Spacer(Modifier.height(20.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) { Text(heading, fontSize = 20.sp) }
    HorizontalDivider()
    Spacer(Modifier.height(20.dp))
}

@Composable
private fun RoutineColorSelector(viewModel: AddRoutinePageViewModel) {
    val selected by viewModel.selectedColor.collectAsStateWithLifecycle()
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        viewModel.colors.forEach { c ->
            val isSelected = c == selected
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .padding(4.dp)
                    .height(40.dp)
                    .weight(1f)
                    .then(
                        if (isSelected) Modifier else Modifier
                    )
            ) {
                androidx.compose.material3.FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setColor(c) },
                    label = { },
                    colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                        selectedContainerColor = c, containerColor = c.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}
