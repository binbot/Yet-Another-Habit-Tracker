package com.zavedahmad.yaHabit.ui.habitsDetailPage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.zavedahmad.yaHabit.R
import com.zavedahmad.yaHabit.Screen
import com.zavedahmad.yaHabit.database.entities.HabitCompletionEntity
import com.zavedahmad.yaHabit.database.entities.HabitEntity
import com.zavedahmad.yaHabit.database.utils.getAmoledThemeMode
import com.zavedahmad.yaHabit.database.utils.getFirstDayOfWeek
import com.zavedahmad.yaHabit.database.utils.getTheme
import com.zavedahmad.yaHabit.ui.components.ConfirmationDialog
import com.zavedahmad.yaHabit.ui.mainPage.DialogueForHabit
import com.zavedahmad.yaHabit.ui.theme.ComposeTemplateTheme
import com.zavedahmad.yaHabit.ui.theme.CustomTheme
import com.zavedahmad.yaHabit.ui.theme.LocalOutlineSizes
import com.zavedahmad.yahabit.common.formatNumber.formatNumberToReadable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HabitDetailsPage(viewModel: HabitDetailsPageViewModel, backStack: SnapshotStateList<NavKey>) {
    val habit = viewModel.habitDetails.collectAsStateWithLifecycle().value
    val habitAllData = viewModel.habitAllData.collectAsStateWithLifecycle().value
    val coroutineScope = rememberCoroutineScope()
    val dialogueVisible = rememberSaveable { mutableStateOf(false) }
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val allPreferences = viewModel.allPreferences.collectAsStateWithLifecycle().value


    if (allPreferences.isEmpty()) {
        ComposeTemplateTheme("system") {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            )
        }
    } else {
        if (habitAllData == null || habit == null) {
            Scaffold { innerPadding ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
        } else {
            CustomTheme(
                theme = allPreferences.getTheme(),
                primaryColor = habit.color,
                isAmoled = allPreferences.getAmoledThemeMode()
            ) {
                Scaffold(Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
                    MediumFlexibleTopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { backStack.removeLastOrNull() }) {
                                Icon(
                                    Icons.AutoMirrored.Default.ArrowBack,
                                    contentDescription = "go back"
                                )
                            }
                        },
                        title = {
                            Row {
                                AnimatedVisibility(visible = habit.isArchived) {
                                    val shape = MaterialShapes.Cookie6Sided
                                    Box(
                                        Modifier
                                            .clip(shape.toShape())
                                            .background(
                                                MaterialTheme.colorScheme.primary.copy(0.7f)
                                            ).border(
                                                border = BorderStroke(
                                                    width = 2.dp,
                                                    brush = SolidColor(
                                                        MaterialTheme.colorScheme.primary.copy(
                                                            0.5f
                                                        )
                                                    )
                                                ), shape = shape.toShape()
                                            )
                                            .padding(5.dp)
                                    ) {
                                        Icon(

                                            painter = painterResource(R.drawable.archive_outline),
                                            contentDescription = "archived habit",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Spacer(Modifier.width(10.dp))

                                    }
                                }
                                Text(
                                    habit.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        actions = {
                            HabitDetailActions(habit, viewModel, backStack)
                        },
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.mediumTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }) { innerPadding ->
                    Column(
                        Modifier
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Surface(tonalElevation = 20.dp) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Icon(Icons.Default.Repeat, "")
                                Spacer(Modifier.width(10.dp))

                                Text(
                                    formatHabitFrequency(
                                        habit.streakType, habit.frequency, habit.cycle
                                    )
                                )
                                Spacer(Modifier.width(20.dp))
                                Icon(Icons.Default.Adjust, "")
                                Spacer(Modifier.width(10.dp))
                                Text("Goal: ${formatNumberToReadable(habit.repetitionPerDay)} ${habit.measurementUnit}")
                            }
                        }
                        Column(
                            Modifier.padding(horizontal = 10.dp)


                        ) {
                            if (habit.description != "") {
                                Text(habit.description)
                            }

                            Spacer(Modifier.height(10.dp))

                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable(onClick = { dialogueVisible.value = true })
                            ) {
                                FullDataGridCalender(
                                    habitData = habitAllData,
                                    incrementHabit = { date ->
                                        coroutineScope.launch(
                                            Dispatchers.IO
                                        ) {
                                            val entry = viewModel.getEntryOfCertainHabitIdAndDate(habit.id, date)
                                            val newValue = if (habit.isNegative) {
                                                (entry?.repetitionsOnThisDay ?: 0.0) + 1.0
                                            } else {
                                                habit.repetitionPerDay
                                            }
                                            viewModel.habitRepository.applyRepetitionForADate(
                                                date = date,
                                                habitId = habit.id,
                                                newRepetitionValue = newValue
                                            )
                                        }
                                    },
                                    deleteHabit = { date ->
                                        coroutineScope.launch(
                                            Dispatchers.IO
                                        ) {
                                            viewModel.habitRepository.deleteHabitCompletionEntry(
                                                habitId = habit.id,
                                                date = date
                                            )
                                        }
                                    },
                                    firstDayOfWeek = allPreferences.getFirstDayOfWeek(),
                                    interactive = true,
                                    skipHabit = { date ->
                                        coroutineScope.launch {
                                            viewModel.habitRepository.setSkip(
                                                date = date, habitId = habit.id, skipValue = true
                                            )
                                        }
                                    },
                                    unSkipHabit = { date ->
                                        coroutineScope.launch {
                                            viewModel.habitRepository.setSkip(
                                                date = date, habitId = habit.id, skipValue = false
                                            )
                                        }
                                    },
                                    habitEntity = habit,
                                    dialogueComposable = { visible, onDismiss, habitCompletionEntity, completionDate ->
                                        DialogueForHabit(
                                            isVisible = visible,
                                            onDismissRequest = { onDismiss() },
                                            habitCompletionEntity = habitCompletionEntity,
                                            updateHabitCompletionEntity = { },
                                            habitEntity = habit,
                                            onFinalised = { isRepetitionsChanged, isNotesChanged, userTypedRepetition, userTypedNote ->
                                                if (isRepetitionsChanged && userTypedRepetition.toDoubleOrNull() != null) {
                                                    coroutineScope.launch(Dispatchers.IO) {
                                                        viewModel.habitRepository.applyRepetitionForADate(
                                                            date = completionDate,
                                                            habitId = habit.id,
                                                            newRepetitionValue = userTypedRepetition.toDouble()
                                                        )
                                                        if (isNotesChanged) {
                                                            viewModel.habitRepository.applyNotes(
                                                                date = completionDate,
                                                                habitId = habit.id,
                                                                newNote = userTypedNote
                                                            )
                                                        }
                                                    }
                                                } else if (isNotesChanged) {
                                                    coroutineScope.launch(Dispatchers.IO) {
                                                        viewModel.habitRepository.applyNotes(
                                                            date = completionDate,
                                                            habitId = habit.id,
                                                            newNote = userTypedNote
                                                        )
                                                    }
                                                }
                                            }
                                        )
                                    }
                                )
                            }

                            Spacer(Modifier.height(20.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(20.dp))
                            StatsSummaryCard(habitAllData, habit)
                            Spacer(Modifier.height(20.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(20.dp))

                            FrequencyChart(habitAllData, habit)
                            Spacer(Modifier.height(20.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(20.dp))

                            PieChartDetail(habitAllData, habit)

                            Spacer(Modifier.height(20.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(20.dp))

                            Row(
                                Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start
                            ) {
                                Text(
                                    "Top Streaks",
                                    fontSize = 20.sp,
                                    textDecoration = TextDecoration.Underline
                                )
                            }
                            Spacer(Modifier.height(10.dp))

                            StreakChartWidget(habitAllData, habit)
                            Spacer(Modifier.height(40.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HabitDetailActions(habit: HabitEntity, viewModel: HabitDetailsPageViewModel, backStack: SnapshotStateList<NavKey>) {
    val menuVisible = remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { menuVisible.value = true }) {
            Icon(Icons.Outlined.MoreVert, contentDescription = "Options")
        }
        DropdownMenu(expanded = menuVisible.value, onDismissRequest = { menuVisible.value = false }) {
            DropdownMenuItem(text = {
                Row {
                    Icon(Icons.Default.Edit, "")
                    Spacer(Modifier.width(10.dp))
                    Text("Edit Habit")
                }
            }, onClick = {
                menuVisible.value = false
                backStack.add(Screen.AddHabitPageRoute(habit.id))
            })
            DropdownMenuItem(text = {
                Row {
                    if (habit.isArchived) {
                        Icon(Icons.Default.Unarchive, "")
                        Spacer(Modifier.width(10.dp))
                        Text("Unarchive Habit")
                    } else {
                        Icon(Icons.Default.Archive, "")
                        Spacer(Modifier.width(10.dp))
                        Text("Archive Habit")
                    }
                }
            }, onClick = {
                menuVisible.value = false
                if (habit.isArchived) viewModel.unArchive(habit.id) else viewModel.archive(habit.id)
            })
            DropdownMenuItem(text = {
                Row {
                    Icon(Icons.Default.Delete, "")
                    Spacer(Modifier.width(10.dp))
                    Text("Delete Habit")
                }
            }, onClick = {
                menuVisible.value = false
                showDialog.value = true
            })
        }
    }

    ConfirmationDialog(
        visible = showDialog.value,
        text = "Do you want to delete this Habit?",
        confirmAction = { 
            viewModel.deleteHabitById(habit.id)
            backStack.removeLastOrNull()
        },
        onDismiss = { showDialog.value = false },
        confirmationColor = ButtonDefaults.buttonColors(
            contentColor = MaterialTheme.colorScheme.onError,
            containerColor = MaterialTheme.colorScheme.error
        )
    )
}
