package com.zavedahmad.yaHabit.widgets.overviewWidget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.zavedahmad.yaHabit.database.entities.DayState
import com.zavedahmad.yaHabit.database.entities.HabitCompletionEntity
import com.zavedahmad.yaHabit.database.entities.HabitEntity
import com.zavedahmad.yaHabit.database.entities.resolveDayState
import com.zavedahmad.yaHabit.database.repositories.HabitRepository
import com.zavedahmad.yaHabit.widgets.R
import com.zavedahmad.yahabit.common.WidgetUpdater
import com.zavedahmad.yahabit.common.formatNumber.formatNumberToReadable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.time.LocalDate

class HabitWidgetRepository(private val context: Context, val widgetUpdater: WidgetUpdater) {
    suspend fun update() {
        widgetUpdater.updateWidgets()
    }
}

class MyAppWidget : GlanceAppWidget(), KoinComponent {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                Scaffold {
                    val habitRepository: HabitRepository = get()
                    val habits = habitRepository.getHabitsFlowSortedByIndex()
                        .collectAsState(initial = emptyList())
                    val todayCompletions = habitRepository.getTodayCompletionsFlow()
                        .collectAsState(initial = emptyMap())
                    Column(verticalAlignment = Alignment.CenterVertically) {
                        TitleBarWidget("Habits")
                        if (habits.value.isNotEmpty()) {
                            HabitItemsList(habits.value, todayCompletions.value, habitRepository)
                        } else {
                            Box(GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    "Nothing Here",
                                    style = TextStyle(
                                        fontStyle = FontStyle.Italic,
                                        color = GlanceTheme.colors.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private val White = Color.White

private data class WidgetVisuals(
    val bgColor: ColorProvider,
    val textColor: ColorProvider,
    val iconKind: WidgetIconKind
)

private enum class WidgetIconKind { Check, Close, DoubleArrow, Number }

@Composable
private fun resolveWidgetVisuals(state: DayState, habitColor: Color): WidgetVisuals {
    fun cp(c: Color) = ColorProvider(c)

    val bg = when (state) {
        // Disabled states — muted version of habit color
        DayState.AbsoluteDisabled, DayState.AbsoluteMoreDisabled,
        DayState.PartialDisabled, DayState.NegativeCountDisabled,
        DayState.NotNeededDisabled, DayState.SkipDisabled,
        DayState.NoteDisabled, DayState.FailedDisabled,
        DayState.IncompleteDisabled -> cp(habitColor.copy(alpha = 0.35f))
        // All active states — full habit color
        else -> cp(habitColor)
    }

    val iconKind = when (state) {
        DayState.Absolute, DayState.AbsoluteMore,
        DayState.NotNeeded, DayState.NotNeededDisabled -> WidgetIconKind.Check
        DayState.Partial, DayState.NegativeCount,
        DayState.Failed, DayState.Error,
        DayState.AbsoluteMoreDisabled, DayState.PartialDisabled,
        DayState.NegativeCountDisabled, DayState.FailedDisabled -> WidgetIconKind.Number
        DayState.Skip, DayState.SkipDisabled -> WidgetIconKind.DoubleArrow
        else -> WidgetIconKind.Close
    }

    return WidgetVisuals(bg, cp(White), iconKind)
}

@Composable
private fun HabitItemsList(
    habits: List<HabitEntity>,
    todayCompletions: Map<Int, HabitCompletionEntity?>,
    habitRepository: HabitRepository
) {
    val today = LocalDate.now()

    LazyColumn {
        items(items = habits.filter { !it.isArchived }) { habit ->
            val habitCompletionEntity = todayCompletions[habit.id]
            val coroutineScope = rememberCoroutineScope()
            val state = resolveDayState(habit, habitCompletionEntity, today, today)
            val visuals = resolveWidgetVisuals(state, habit.color)
            val repetitionsOnThisDay = habitCompletionEntity?.repetitionsOnThisDay ?: 0.0

            val buttonAction: () -> Unit = when (state) {
                DayState.Error -> {{}}
                DayState.Skip -> {
                    {
                        coroutineScope.launch(Dispatchers.IO) {
                            habitRepository.applyRepetitionForADate(
                                date = today,
                                habitId = habit.id,
                                newRepetitionValue = 0.0
                            )
                            habitRepository.setSkip(
                                date = today,
                                habitId = habit.id,
                                skipValue = false
                            )
                        }
                    }
                }
                DayState.NotNeeded -> {{}}
                // Completed habits — tap to skip (undo mechanism)
                DayState.Absolute, DayState.AbsoluteMore -> {
                    {
                        coroutineScope.launch(Dispatchers.IO) {
                            habitRepository.setSkip(
                                date = today,
                                habitId = habit.id,
                                skipValue = true
                            )
                        }
                    }
                }
                // Incomplete/Partial — tap to complete to target
                DayState.Incomplete, DayState.Partial -> {
                    {
                        coroutineScope.launch(Dispatchers.IO) {
                            habitRepository.applyRepetitionForADate(
                                date = today,
                                habitId = habit.id,
                                newRepetitionValue = habit.repetitionPerDay
                            )
                        }
                    }
                }
                // Negative habits — tap to log an increment
                DayState.NegativeCount, DayState.Failed -> {
                    {
                        coroutineScope.launch(Dispatchers.IO) {
                            habitRepository.incrementRepetitions(
                                date = today,
                                habitId = habit.id
                            )
                        }
                    }
                }
                // Disabled states — no action
                else -> {{}}
            }

            Column {
                Row(
                    GlanceModifier.background(visuals.bgColor)
                        .cornerRadius(10.dp)
                        .padding(vertical = 5.dp, horizontal = 10.dp)
                        .fillMaxWidth()
                        .height(40.dp)
                        .clickable { buttonAction() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (visuals.iconKind) {
                        WidgetIconKind.Check -> {
                            Image(
                                modifier = GlanceModifier.size(24.dp),
                                provider = ImageProvider(R.drawable.baseline_check_24),
                                contentDescription = state.name,
                                colorFilter = ColorFilter.tint(visuals.textColor)
                            )
                        }
                        WidgetIconKind.Close -> {
                            Image(
                                modifier = GlanceModifier.size(24.dp),
                                provider = ImageProvider(R.drawable.outline_close_24),
                                contentDescription = state.name,
                                colorFilter = ColorFilter.tint(visuals.textColor)
                            )
                        }
                        WidgetIconKind.DoubleArrow -> {
                            Image(
                                modifier = GlanceModifier.size(24.dp),
                                provider = ImageProvider(R.drawable.outline_keyboard_double_arrow_right_24),
                                contentDescription = state.name,
                                colorFilter = ColorFilter.tint(visuals.textColor)
                            )
                        }
                        WidgetIconKind.Number -> {
                            Text(
                                text = formatNumberToReadable(repetitionsOnThisDay),
                                style = TextStyle(color = visuals.textColor, fontSize = 15.sp)
                            )
                        }
                    }
                    Spacer(GlanceModifier.width(10.dp))
                    Text(
                        text = habit.name,
                        maxLines = 1,
                        style = TextStyle(
                            color = visuals.textColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                Spacer(GlanceModifier.height(5.dp))
            }
        }
    }
}

@Composable
internal fun TitleBarWidget(title: String) {
    Row(
        GlanceModifier.height(50.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = GlanceModifier.size(24.dp),
            provider = ImageProvider(R.drawable.yahabiticonnobg),
            contentDescription = "app Logo"
        )
        Spacer(GlanceModifier.width(10.dp))
        Text(
            title,
            maxLines = 1,
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
