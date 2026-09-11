package com.zavedahmad.yaHabit.ui.mainPage


import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.materialkolor.Contrast
import com.materialkolor.dynamicColorScheme
import com.materialkolor.dynamiccolor.ColorSpec
import com.zavedahmad.yaHabit.Screen
import com.zavedahmad.yaHabit.database.entities.HabitEntity
import com.zavedahmad.yaHabit.database.entities.RoutineEntity
import com.zavedahmad.yaHabit.database.repositories.HabitRepository
import com.zavedahmad.yaHabit.database.utils.getAmoledThemeMode
import com.zavedahmad.yaHabit.database.utils.getFirstDayOfWeek
import com.zavedahmad.yaHabit.database.utils.getShowActive
import com.zavedahmad.yaHabit.database.utils.getShowArchive
import com.zavedahmad.yaHabit.database.utils.getTheme
import com.zavedahmad.yaHabit.ui.components.CardMyStyle
import com.zavedahmad.yaHabit.ui.mainPage.habitItemReorderable.HabitItemReorderableNew
import com.zavedahmad.yaHabit.ui.theme.ComposeTemplateTheme
import com.zavedahmad.yaHabit.widgets.overviewWidget.MyAppWidget
import kotlinx.coroutines.channels.Channel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainPageReorderable(backStack: SnapshotStateList<NavKey>, viewModel: MainPageViewModel) {
    val listUpdatedChannel = remember { Channel<Unit>() }
    val habits = viewModel.habits.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)
    val isReorderableMode = viewModel.isReorderableMode.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    LaunchedEffect(habits.value) {
        listUpdatedChannel.trySend(Unit)
    }
    val allPreferences = viewModel.allPreferences.collectAsStateWithLifecycle().value
    val showFloatingActionButton = remember { mutableStateOf(true) }
    val previousHeightOffsetOfTopAppBar =
        remember { mutableStateOf(scrollBehavior.state.heightOffset) }
    BackHandler(enabled = isReorderableMode.value) {
        viewModel.changeReorderableMode(false)
    }
    LaunchedEffect(                                            // todo this can have some perfromacnce issues
        lazyListState.lastScrolledBackward,
        lazyListState.lastScrolledForward,
        scrollBehavior.state.heightOffset
    ) {
        if (lazyListState.lastScrolledBackward && !showFloatingActionButton.value) {
            showFloatingActionButton.value = true
        }
        if (lazyListState.lastScrolledForward && showFloatingActionButton.value) {
            showFloatingActionButton.value = false
        }
        if (previousHeightOffsetOfTopAppBar.value != scrollBehavior.state.heightOffset) {
            if (previousHeightOffsetOfTopAppBar.value < scrollBehavior.state.heightOffset) {
                showFloatingActionButton.value = true
            } else {
                showFloatingActionButton.value = false
            }
            previousHeightOffsetOfTopAppBar.value = scrollBehavior.state.heightOffset
        }
    }

    if (allPreferences.isEmpty()) {
        ComposeTemplateTheme("system") {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            )
        }
    } else {


        Scaffold(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection),


            topBar = {
                MainPageTopAppBar(
                    viewModel = viewModel,
                    backStack = backStack,
                    scrollBehavior = scrollBehavior
                )
            },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = !isReorderableMode.value && showFloatingActionButton.value,
                    enter = slideInVertically { it -> it * 30 / 20 } + fadeIn(),
                    exit = slideOutVertically { it -> it * 30 / 20 } + fadeOut()
                ) {
                    MediumFloatingActionButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .border(
                                shape = FloatingActionButtonDefaults.shape,
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(
                                    0.5f
                                ).compositeOver(MaterialTheme.colorScheme.surface))
                            ),
                        shape = FloatingActionButtonDefaults.shape,
                        containerColor = MaterialTheme.colorScheme.primary.copy(0.3f).compositeOver(
                            MaterialTheme.colorScheme.surface),
                        contentColor = MaterialTheme.colorScheme.primary,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 4.dp
                        )
                    ) {
                        Box(
                            Modifier
                                .clip(MaterialShapes.Cookie12Sided.toShape())
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(0.7f)
                                )
                                .border(
                                    border = BorderStroke(
                                        width = 2.dp,
                                        brush = SolidColor(
                                            MaterialTheme.colorScheme.primary.copy(
                                            )
                                        )
                                    ), shape = MaterialShapes.Cookie12Sided.toShape()
                                )
                                .padding(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                "add", modifier = Modifier.size(30.dp), tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            BottomSheetForFiltersAndSorting(viewModel)
            val currentHabits = habits.value
            val filteredHabits by remember(allPreferences, currentHabits) {
                derivedStateOf {
                    filterHabitsList(
                        allPreferences.getShowArchive(),
                        allPreferences.getShowActive(),
                        currentHabits
                    )

                }
            }
            val routines = viewModel.routines.collectAsStateWithLifecycle().value
            val filteredRoutines by remember(allPreferences, routines) {
                derivedStateOf { filterRoutinesList(allPreferences.getShowArchive(), allPreferences.getShowActive(), routines) }
            }

            val darkTheme = when (allPreferences.getTheme()) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }
            // Precompute each habit's color scheme once, instead of generating a
            // fresh materialkolor scheme inside every list item on every scroll.
            val habitColorSchemes: Map<Int, ColorScheme> = remember(
                filteredHabits,
                allPreferences.getTheme(),
                allPreferences.getAmoledThemeMode(),
                isSystemInDarkTheme()
            ) {
                filteredHabits.associate { habit ->
                    habit.id to dynamicColorScheme(
                        primary = habit.color,
                        isDark = darkTheme,
                        isAmoled = allPreferences.getAmoledThemeMode(),
                        specVersion = ColorSpec.SpecVersion.SPEC_2025,
                        contrastLevel = Contrast.Medium.value
                    )
                }
            }
            val routineColorSchemes: Map<Int, ColorScheme> = remember(
                filteredRoutines,
                allPreferences.getTheme(),
                allPreferences.getAmoledThemeMode(),
                isSystemInDarkTheme()
            ) {
                filteredRoutines.associate { routine ->
                    routine.id to dynamicColorScheme(
                        primary = routine.color,
                        isDark = darkTheme,
                        isAmoled = allPreferences.getAmoledThemeMode(),
                        specVersion = ColorSpec.SpecVersion.SPEC_2025,
                        contrastLevel = Contrast.Medium.value
                    )
                }
            }


            val reorderableLazyListState =
                rememberReorderableLazyListState(
                    lazyListState,

                    ) { from, to ->
                    listUpdatedChannel.tryReceive()
                    //println("from: key ${from.key} index ${from.index}  \n to:   key ${to.key} index ${to.index}")
                    val realFromIndex = filteredHabits[from.index - 1].index
                    val realToIndex = filteredHabits[to.index - 1].index
                    viewModel.move(realFromIndex, realToIndex)
                    listUpdatedChannel.receive()
                }


            Box(
                modifier = Modifier
                    .fillMaxSize()

            ) {


                if (filteredHabits.isEmpty()) {
                    NoHabitsPage(Modifier.padding(innerPadding))
                } else {
                    Box {

                        //HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            state = lazyListState,
//                            contentPadding = PaddingValues(top = 1.dp, start = 10.dp, end = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            item { }  // spacing
                            stickyHeader(key = "routines-header") { SectionHeader("Routines", filteredRoutines.size) }
                            items(filteredRoutines, key = { "routine-${it.id}" }) { routine ->
                                val scheme = routineColorSchemes[routine.id]
                                if (scheme != null) {
                                    MaterialTheme(colorScheme = scheme) {
                                        RoutineItemReorderable(
                                            backStack = backStack,
                                            viewModel = viewModel,
                                            routine = routine,
                                            isReorderableMode = isReorderableMode.value
                                        )
                                    }
                                } else {
                                    RoutineItemReorderable(
                                        backStack = backStack,
                                        viewModel = viewModel,
                                        routine = routine,
                                        isReorderableMode = isReorderableMode.value
                                    )
                                }
                            }
                            stickyHeader(key = "habits-header") { SectionHeader("Habits", filteredHabits.size) }
                            items(filteredHabits, key = { it.id }) { habit ->
                                ReorderableItem(
                                    reorderableLazyListState,
                                    key = habit.id
                                ) { isDragging ->
                                    val habitCompletions by remember(habit.id) {
                                        viewModel.habitRepository.getAllHabitCompletionsByIdFlow(habit.id)
                                    }.collectAsStateWithLifecycle(initialValue = emptyList())
                                    val scheme = habitColorSchemes[habit.id]
                                    if (scheme != null) {
                                        MaterialTheme(colorScheme = scheme) {
                                            HabitItemReorderableNew(
                                                backStack = backStack,
                                                viewModel = viewModel,
                                                habit = habit,
                                                habitCompletions = habitCompletions.orEmpty(),
                                                reorderableListScope = this,
                                                isDragging = isDragging,
                                                isReorderableMode = isReorderableMode.value,
                                                firstDayOfWeek = allPreferences.getFirstDayOfWeek()
                                            )
                                        }
                                    }
                                }
                            }

                            item { Spacer(Modifier.height(70.dp)) }
                        }
                    }
                }
            }
        }
        if (showAddDialog) {
            Dialog(onDismissRequest = { showAddDialog = false }) {
                CardMyStyle(modifier = Modifier.padding(16.dp)) {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Add new", style = MaterialTheme.typography.titleLarge)
                        androidx.compose.material3.Button(
                            onClick = { showAddDialog = false; backStack.add(Screen.AddHabitPageRoute()) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Habit") }
                        androidx.compose.material3.Button(
                            onClick = { showAddDialog = false; backStack.add(Screen.AddRoutinePageRoute()) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Routine") }
                        androidx.compose.material3.TextButton(onClick = { showAddDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
                    }
                }
            }
        }
    }

}

@Composable
private fun SectionHeader(title: String, count: Int) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Text("$count", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

private fun filterHabitsList(
    showArchived: Boolean,
    showActive: Boolean,
    habits: List<HabitEntity>
): List<HabitEntity> {
    return habits.filter { showArchived && it.isArchived || showActive && !it.isArchived }
        .sortedBy { it.index }

}

private fun filterRoutinesList(
    showArchived: Boolean,
    showActive: Boolean,
    routines: List<RoutineEntity>
): List<RoutineEntity> {
    return routines.filter { showArchived && it.isArchived || showActive && !it.isArchived }
        .sortedBy { it.index }
}


