package com.zavedahmad.yaHabit.widgets.overviewWidget

import android.content.Context
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import androidx.glance.material3.ColorProviders
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import com.zavedahmad.yaHabit.database.PreferenceEntity
import com.zavedahmad.yaHabit.database.entities.DayState
import com.zavedahmad.yaHabit.database.entities.HabitCompletionEntity
import com.zavedahmad.yaHabit.database.entities.HabitEntity
import com.zavedahmad.yaHabit.database.entities.resolveDayState
import com.zavedahmad.yaHabit.database.repositories.HabitRepository
import com.zavedahmad.yaHabit.database.repositories.PreferencesRepository
import com.zavedahmad.yaHabit.database.utils.getAmoledThemeMode
import com.zavedahmad.yaHabit.database.utils.getDynamicColor
import com.zavedahmad.yaHabit.database.utils.getTheme
import com.zavedahmad.yaHabit.widgets.R
import com.zavedahmad.yahabit.common.WidgetUpdater
import com.zavedahmad.yahabit.common.formatNumber.formatNumberToReadable
import kotlinx.coroutines.CoroutineScope
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
            // Widget chrome follows the app theme (theme mode, AMOLED, dynamic color) so the
            // widget and the in-app surfaces agree on colors.
            val preferencesRepository: PreferencesRepository = get()
            val preferences: List<PreferenceEntity> =
                preferencesRepository.preferences.collectAsState(initial = emptyList()).value

            val systemIsDark =
                (context.resources.configuration.uiMode and UI_MODE_NIGHT_MASK) == UI_MODE_NIGHT_YES
            val isDark = when (preferences.getTheme()) {
                "light" -> false
                "dark" -> true
                else -> systemIsDark
            }
            val isAmoled = preferences.getAmoledThemeMode()
            val dynamicColor = preferences.getDynamicColor()

            GlanceTheme(
                ColorProviders(widgetChromeScheme(context, isDark, isAmoled, dynamicColor))
            ) {
                Scaffold {
                    val habitRepository: HabitRepository = get()
                    val habits = habitRepository.getHabitsFlowSortedByIndex()
                        .collectAsState(initial = emptyList()).value
                    val todayCompletions = habitRepository.getTodayCompletionsFlow()
                        .collectAsState(initial = emptyMap()).value
                    val activeHabits = habits.filter { !it.isArchived }

                    Column(verticalAlignment = Alignment.CenterVertically) {
                        TitleBarWidget("Habits")
                        if (activeHabits.isNotEmpty()) {
                            HabitItemsList(
                                habits = activeHabits,
                                todayCompletions = todayCompletions,
                                habitRepository = habitRepository,
                                isDark = isDark,
                                isAmoled = isAmoled
                            )
                        } else {
                            Box(GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Nothing Here",
                                    style = WidgetType.Body.copy(
                                        color = GlanceTheme.colors.onSurfaceVariant
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
    @DrawableRes val leadingIcon: Int?,
    val leadingCount: String? = null
)

/** The secondary (corrective) action a row exposes next to the main tap target. */
private data class TrailingAction(
    @DrawableRes val iconRes: Int,
    val description: String
)

private val SkipIcon = R.drawable.outline_keyboard_double_arrow_right_24
private val UndoIcon = R.drawable.outline_remove_24

/** No-op tap action for settled states. */
private val NoAction: () -> Unit = {}

private fun cp(color: Color) = ColorProvider(color)

@Composable
private fun HabitItemsList(
    habits: List<HabitEntity>,
    todayCompletions: Map<Int, HabitCompletionEntity?>,
    habitRepository: HabitRepository,
    isDark: Boolean,
    isAmoled: Boolean
) {
    val today = LocalDate.now()

    // Precompute each habit's accent scheme once, like the app list does, rather than generating a
    // fresh materialkolor scheme for every row on every recomposition.
    val habitSchemes: Map<Int, ColorScheme> = remember(habits, isDark, isAmoled) {
        habits.associate { habit -> habit.id to itemScheme(habit.color, isDark, isAmoled) }
    }

    LazyColumn {
        items(items = habits) { habit ->
            HabitChip(
                habit = habit,
                completion = todayCompletions[habit.id],
                scheme = habitSchemes[habit.id] ?: itemScheme(habit.color, isDark, isAmoled),
                habitRepository = habitRepository,
                today = today
            )
        }
    }
}

@Composable
private fun HabitChip(
    habit: HabitEntity,
    completion: HabitCompletionEntity?,
    scheme: ColorScheme,
    habitRepository: HabitRepository,
    today: LocalDate
) {
    val coroutineScope = rememberCoroutineScope()
    val state = resolveDayState(habit, completion, today, today)
    val repetitions = completion?.repetitionsOnThisDay ?: 0.0
    val (trailing, onTrailingClick) =
        habitTrailing(state, habit, habitRepository, coroutineScope, today)

    ChipSlot {
        WidgetChip(
            label = habit.name,
            leadingDescription = state.name,
            visuals = habitVisuals(state, scheme, repetitions),
            onChipClick = habitTapAction(state, habit, habitRepository, coroutineScope, today),
            trailing = trailing,
            onTrailingClick = onTrailingClick
        )
    }
}

private fun habitVisuals(state: DayState, scheme: ColorScheme, count: Double): WidgetVisuals =
    when (state) {
        DayState.Absolute ->
            WidgetVisuals(cp(scheme.primary), cp(scheme.onPrimary), R.drawable.baseline_check_24)
        DayState.AbsoluteMore ->
            WidgetVisuals(cp(scheme.primaryContainer.copy(alpha = 0.3f)), cp(scheme.primary), null, formatNumberToReadable(count))
        DayState.Partial ->
            WidgetVisuals(cp(scheme.primaryContainer.copy(alpha = 0.5f)), cp(scheme.primary), null, formatNumberToReadable(count))
        DayState.NegativeCount ->
            WidgetVisuals(cp(scheme.primaryContainer.copy(alpha = 0.5f)), cp(scheme.primary), null, formatNumberToReadable(count))
        DayState.NotNeeded ->
            WidgetVisuals(cp(scheme.surfaceVariant.copy(alpha = 0.3f)), cp(scheme.primary.copy(alpha = 0.5f)), R.drawable.baseline_check_24)
        DayState.Skip ->
            WidgetVisuals(cp(scheme.tertiaryContainer), cp(scheme.onTertiaryContainer), SkipIcon)
        DayState.Note ->
            WidgetVisuals(cp(scheme.secondaryContainer), cp(scheme.onSecondaryContainer), R.drawable.outline_close_24)
        DayState.Failed ->
            WidgetVisuals(cp(FailureRed.copy(alpha = 0.2f)), cp(FailureRed), null, formatNumberToReadable(count))
        DayState.Incomplete ->
            WidgetVisuals(cp(scheme.primary), cp(scheme.onPrimary), R.drawable.outline_close_24)
        DayState.Error ->
            WidgetVisuals(cp(scheme.error), cp(scheme.onError), R.drawable.outline_close_24)

        // Disabled variants — muted versions
        DayState.AbsoluteDisabled ->
            WidgetVisuals(cp(scheme.primary.copy(alpha = 0.3f)), cp(scheme.onPrimary.copy(alpha = 0.5f)), R.drawable.baseline_check_24)
        DayState.AbsoluteMoreDisabled ->
            WidgetVisuals(cp(scheme.primaryContainer.copy(alpha = 0.15f)), cp(scheme.primary.copy(alpha = 0.4f)), null, formatNumberToReadable(count))
        DayState.PartialDisabled ->
            WidgetVisuals(cp(scheme.primaryContainer.copy(alpha = 0.2f)), cp(scheme.primary.copy(alpha = 0.4f)), null, formatNumberToReadable(count))
        DayState.NegativeCountDisabled ->
            WidgetVisuals(cp(scheme.primaryContainer.copy(alpha = 0.2f)), cp(scheme.primary.copy(alpha = 0.4f)), null, formatNumberToReadable(count))
        DayState.NotNeededDisabled ->
            WidgetVisuals(cp(scheme.surfaceVariant.copy(alpha = 0.15f)), cp(scheme.primary.copy(alpha = 0.3f)), R.drawable.baseline_check_24)
        DayState.SkipDisabled ->
            WidgetVisuals(cp(scheme.tertiaryContainer.copy(alpha = 0.4f)), cp(scheme.onTertiaryContainer.copy(alpha = 0.5f)), SkipIcon)
        DayState.NoteDisabled ->
            WidgetVisuals(cp(scheme.secondaryContainer.copy(alpha = 0.3f)), cp(scheme.onSecondaryContainer.copy(alpha = 0.5f)), R.drawable.outline_close_24)
        DayState.FailedDisabled ->
            WidgetVisuals(cp(FailureRed.copy(alpha = 0.1f)), cp(FailureRed.copy(alpha = 0.3f)), null, formatNumberToReadable(count))
        DayState.IncompleteDisabled ->
            WidgetVisuals(cp(scheme.primary.copy(alpha = 0.3f)), cp(scheme.onPrimary.copy(alpha = 0.5f)), R.drawable.outline_close_24)
    }

/**
 * Main tap = move the day forward: complete, count up, or un-skip. States that are already
 * "settled" (done, or not needed) deliberately do nothing, so a stray tap can't undo work.
 */
private fun habitTapAction(
    state: DayState,
    habit: HabitEntity,
    habitRepository: HabitRepository,
    scope: CoroutineScope,
    today: LocalDate
): () -> Unit = when (state) {
    DayState.Incomplete, DayState.Partial, DayState.IncompleteDisabled, DayState.PartialDisabled -> {
        { scope.launch(Dispatchers.IO) { habitRepository.applyRepetitionForADate(today, habit.id, habit.repetitionPerDay) } }
    }
    DayState.NegativeCount, DayState.NegativeCountDisabled, DayState.Failed, DayState.FailedDisabled -> {
        { scope.launch(Dispatchers.IO) { habitRepository.incrementRepetitions(today, habit.id) } }
    }
    DayState.Skip, DayState.SkipDisabled -> {
        { scope.launch(Dispatchers.IO) { habitRepository.setSkip(today, habit.id, false) } }
    }
    else -> NoAction
}

/**
 * Trailing corrective icon: un-skip a skipped day, count back down when the habit is over its
 * target, otherwise skip the day.
 */
private fun habitTrailing(
    state: DayState,
    habit: HabitEntity,
    habitRepository: HabitRepository,
    scope: CoroutineScope,
    today: LocalDate
): Pair<TrailingAction?, () -> Unit> {
    val action: TrailingAction
    val click: () -> Unit
    when (state) {
        DayState.Skip, DayState.SkipDisabled -> {
            action = TrailingAction(SkipIcon, "unskip")
            click = { scope.launch(Dispatchers.IO) { habitRepository.setSkip(today, habit.id, false) } }
        }
        DayState.AbsoluteMore, DayState.AbsoluteMoreDisabled,
        DayState.NegativeCount, DayState.NegativeCountDisabled,
        DayState.Failed, DayState.FailedDisabled -> {
            action = TrailingAction(UndoIcon, "decrement")
            click = { scope.launch(Dispatchers.IO) { habitRepository.decrementRepetitions(today, habit.id) } }
        }
        DayState.NotNeeded, DayState.NotNeededDisabled, DayState.Error -> {
            return null to NoAction
        }
        else -> {
            action = TrailingAction(SkipIcon, "skip")
            click = { scope.launch(Dispatchers.IO) { habitRepository.setSkip(today, habit.id, true) } }
        }
    }
    return action to click
}

/**
 * A widget row: a rounded, state-coloured chip with a leading state icon (or count), the row label
 * taking the free space, and an optional trailing corrective icon.
 */
@Composable
private fun WidgetChip(
    label: String,
    leadingDescription: String,
    visuals: WidgetVisuals,
    onChipClick: () -> Unit,
    trailing: TrailingAction?,
    onTrailingClick: () -> Unit
) {
    Row(
        GlanceModifier.background(visuals.bgColor)
            .cornerRadius(WidgetChipRadius)
            .padding(vertical = 5.dp, horizontal = 12.dp)
            .fillMaxWidth()
            .height(40.dp)
            .clickable { onChipClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (visuals.leadingCount != null) {
            Text(
                text = visuals.leadingCount,
                style = WidgetType.RowValue.copy(color = visuals.textColor)
            )
        } else if (visuals.leadingIcon != null) {
            Image(
                modifier = GlanceModifier.size(24.dp),
                provider = ImageProvider(visuals.leadingIcon),
                contentDescription = leadingDescription,
                colorFilter = ColorFilter.tint(visuals.textColor)
            )
        }
        Spacer(GlanceModifier.width(10.dp))
        Text(
            text = label,
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight(),
            style = WidgetType.RowLabel.copy(color = visuals.textColor)
        )
        if (trailing != null) {
            Spacer(GlanceModifier.width(4.dp))
            Image(
                modifier = GlanceModifier.size(24.dp).clickable { onTrailingClick() },
                provider = ImageProvider(trailing.iconRes),
                contentDescription = trailing.description,
                colorFilter = ColorFilter.tint(visuals.textColor)
            )
        }
    }
}

/** Spacing wrapper so all rows share the same vertical rhythm. */
@Composable
private fun ChipSlot(content: @Composable () -> Unit) {
    Column {
        content()
        Spacer(GlanceModifier.height(5.dp))
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
            style = WidgetType.Title.copy(color = GlanceTheme.colors.onSurface)
        )
    }
}

/** Semantic "over the limit" red, matching the app's failed/over-target day cells. */
private val FailureRed = Color(0xFFF44336)
