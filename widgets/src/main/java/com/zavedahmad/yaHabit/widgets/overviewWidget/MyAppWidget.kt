package com.zavedahmad.yaHabit.widgets.overviewWidget

import android.content.Context
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
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
import com.materialkolor.Contrast
import com.materialkolor.dynamicColorScheme
import com.materialkolor.dynamiccolor.ColorSpec
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
            val isDark = (context.resources.configuration.uiMode and UI_MODE_NIGHT_MASK) == UI_MODE_NIGHT_YES

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
                            HabitItemsList(habits.value, todayCompletions.value, habitRepository, isDark)
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

private data class WidgetVisuals(
    val bgColor: ColorProvider,
    val textColor: ColorProvider,
    val iconKind: WidgetIconKind
)

private enum class WidgetIconKind { Check, Close, DoubleArrow, Number }

@Composable
private fun resolveWidgetVisuals(state: DayState, habitColor: Color, isDark: Boolean): WidgetVisuals {
    fun cp(c: Color) = ColorProvider(c)

    val scheme = dynamicColorScheme(
        primary = habitColor,
        isDark = isDark,
        isAmoled = false,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
        contrastLevel = Contrast.Medium.value
    )

    return when (state) {
        DayState.Absolute ->
            WidgetVisuals(cp(scheme.primary), cp(scheme.onPrimary), WidgetIconKind.Check)
        DayState.AbsoluteMore ->
            WidgetVisuals(cp(scheme.primaryContainer.copy(alpha = 0.3f)), cp(scheme.primary), WidgetIconKind.Number)
        DayState.Partial ->
            WidgetVisuals(cp(scheme.primaryContainer.copy(alpha = 0.5f)), cp(scheme.primary), WidgetIconKind.Number)
        DayState.NegativeCount ->
            WidgetVisuals(cp(scheme.primaryContainer.copy(alpha = 0.5f)), cp(scheme.primary), WidgetIconKind.Number)
        DayState.NotNeeded ->
            WidgetVisuals(cp(scheme.surfaceVariant.copy(alpha = 0.3f)), cp(scheme.primary.copy(alpha = 0.5f)), WidgetIconKind.Check)
        DayState.Skip ->
            WidgetVisuals(cp(scheme.tertiaryContainer), cp(scheme.onTertiaryContainer), WidgetIconKind.DoubleArrow)
        DayState.Note ->
            WidgetVisuals(cp(scheme.secondaryContainer), cp(scheme.onSecondaryContainer), WidgetIconKind.Close)
        DayState.Failed ->
            WidgetVisuals(cp(Color(0xFFF44336).copy(alpha = 0.2f)), cp(Color(0xFFF44336)), WidgetIconKind.Number)
        DayState.Incomplete ->
            WidgetVisuals(cp(scheme.surfaceVariant), cp(scheme.onSurfaceVariant), WidgetIconKind.Close)
        DayState.Error ->
            WidgetVisuals(cp(scheme.error), cp(scheme.onError), WidgetIconKind.Close)

        // Disabled variants — muted versions
        DayState.AbsoluteDisabled ->
            WidgetVisuals(cp(scheme.primary.copy(alpha = 0.3f)), cp(scheme.onPrimary.copy(alpha = 0.5f)), WidgetIconKind.Check)
        DayState.AbsoluteMoreDisabled ->
            WidgetVisuals(cp(scheme.primaryContainer.copy(alpha = 0.15f)), cp(scheme.primary.copy(alpha = 0.4f)), WidgetIconKind.Number)
        DayState.PartialDisabled ->
            WidgetVisuals(cp(scheme.primaryContainer.copy(alpha = 0.2f)), cp(scheme.primary.copy(alpha = 0.4f)), WidgetIconKind.Number)
        DayState.NegativeCountDisabled ->
            WidgetVisuals(cp(scheme.primaryContainer.copy(alpha = 0.2f)), cp(scheme.primary.copy(alpha = 0.4f)), WidgetIconKind.Number)
        DayState.NotNeededDisabled ->
            WidgetVisuals(cp(scheme.surfaceVariant.copy(alpha = 0.15f)), cp(scheme.primary.copy(alpha = 0.3f)), WidgetIconKind.Check)
        DayState.SkipDisabled ->
            WidgetVisuals(cp(scheme.tertiaryContainer.copy(alpha = 0.4f)), cp(scheme.onTertiaryContainer.copy(alpha = 0.5f)), WidgetIconKind.DoubleArrow)
        DayState.NoteDisabled ->
            WidgetVisuals(cp(scheme.secondaryContainer.copy(alpha = 0.3f)), cp(scheme.onSecondaryContainer.copy(alpha = 0.5f)), WidgetIconKind.Close)
        DayState.FailedDisabled ->
            WidgetVisuals(cp(Color(0xFFF44336).copy(alpha = 0.1f)), cp(Color(0xFFF44336).copy(alpha = 0.3f)), WidgetIconKind.Number)
        DayState.IncompleteDisabled ->
            WidgetVisuals(cp(scheme.surfaceVariant.copy(alpha = 0.1f)), cp(scheme.onSurfaceVariant.copy(alpha = 0.3f)), WidgetIconKind.Close)
    }
}

@Composable
private fun HabitItemsList(
    habits: List<HabitEntity>,
    todayCompletions: Map<Int, HabitCompletionEntity?>,
    habitRepository: HabitRepository,
    isDark: Boolean
) {
    val today = LocalDate.now()

    LazyColumn {
        items(items = habits.filter { !it.isArchived }) { habit ->
            val habitCompletionEntity = todayCompletions[habit.id]
            val coroutineScope = rememberCoroutineScope()
            val state = resolveDayState(habit, habitCompletionEntity, today, today)
            val visuals = resolveWidgetVisuals(state, habit.color, isDark)
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
