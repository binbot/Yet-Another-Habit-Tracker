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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.zavedahmad.yaHabit.Screen
import com.zavedahmad.yaHabit.database.entities.HabitEntity
import com.zavedahmad.yaHabit.database.repositories.HabitRepository
import com.zavedahmad.yaHabit.database.utils.getAmoledThemeMode
import com.zavedahmad.yaHabit.database.utils.getFirstDayOfWeek
import com.zavedahmad.yaHabit.database.utils.getShowActive
import com.zavedahmad.yaHabit.database.utils.getShowArchive
import com.zavedahmad.yaHabit.database.utils.getTheme
import com.zavedahmad.yaHabit.ui.mainPage.habitItemReorderable.HabitItemReorderableNew
import com.zavedahmad.yaHabit.ui.theme.ComposeTemplateTheme
import com.zavedahmad.yaHabit.ui.theme.CustomTheme
import com.zavedahmad.yaHabit.widgets.overviewWidget.MyAppWidget
import kotlinx.coroutines.channels.Channel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainPageReorderable(backStack: SnapshotStateList<NavKey>, viewModel: MainPageViewModel) {
    val listUpdatedChannel = remember { Channel<Unit>(Channel.CONFLATED) }
    val habits = viewModel.habits.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)
    val isReorderableMode = viewModel.isReorderableMode.collectAsStateWithLifecycle()
    
    // Centralized data collection for performance
    val completionsByHabit by viewModel.completionsByHabit.collectAsStateWithLifecycle()

    LaunchedEffect(habits.value) {
        listUpdatedChannel.trySend(Unit)
    }
    val allPreferences = viewModel.allPreferences.collectAsStateWithLifecycle().value
    val showFloatingActionButton = remember { mutableStateOf(true) }

    BackHandler(enabled = isReorderableMode.value) {
        viewModel.changeReorderableMode(false)
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
        ComposeTemplateTheme(
            theme = allPreferences.getTheme()
        ) {
            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
                            onClick = {
                                backStack.add(Screen.AddHabitPageRoute(null))
                            },
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
                            Icon(Icons.Default.Add, contentDescription = "Add Habit")
                        }
                    }
                }
            ) { innerPadding ->
                val filteredHabits = remember(allPreferences, habits.value) {
                    getSortedHabitList(
                        allPreferences.getShowArchive(),
                        allPreferences.getShowActive(),
                        habits.value
                    )
                }

                val reorderableLazyListState =
                    rememberReorderableLazyListState(
                        lazyListState,
                    ) { from, to ->
                        listUpdatedChannel.tryReceive()
                        val realFromIndex = filteredHabits[from.index].index
                        val realToIndex = filteredHabits[to.index].index
                        viewModel.move(realFromIndex, realToIndex)
                    }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    if (filteredHabits.isEmpty()) {
                        NoHabitsPage(Modifier.padding(innerPadding))
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            state = lazyListState,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            items(filteredHabits, key = { it.id }) { habit ->
                                ReorderableItem(reorderableLazyListState, key = habit.id) { isDragging ->
                                    HabitItemReorderableNew(
                                        backStack = backStack,
                                        viewModel = viewModel,
                                        habit = habit,
                                        habitCompletions = completionsByHabit[habit.id] ?: emptyList(),
                                        reorderableListScope = this,
                                        isDragging = isDragging,
                                        isReorderableMode = isReorderableMode.value,
                                        allPreferences = allPreferences,
                                        firstDayOfWeek = allPreferences.getFirstDayOfWeek()
                                    )
                                }
                            }
                            item { Spacer(Modifier.height(70.dp)) }
                        }
                    }
                }
            }
        }
    }
}

fun getSortedHabitList(
    showArchived: Boolean,
    showActive: Boolean,
    habits: List<HabitEntity>
): List<HabitEntity> {
    return habits.filter { showArchived && it.isArchived || showActive && !it.isArchived }
        .sortedBy { it.index }
}
