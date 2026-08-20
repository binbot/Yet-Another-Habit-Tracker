package com.zavedahmad.yaHabit.ui.mainPage.habitItemReorderable


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoubleArrow
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.zavedahmad.yaHabit.database.entities.DayState
import com.zavedahmad.yaHabit.R
import com.zavedahmad.yaHabit.ui.theme.LocalOutlineSizes
import com.zavedahmad.yahabit.common.formatNumber.formatNumberToReadable
import java.time.LocalDate


@Composable
fun DayItem(
    date: LocalDate,
    state: DayState,
    repetitionsOnThisDay: Double,
    incrementHabit: () -> Unit = {},
    deleteHabit: () -> Unit = {},
    skipHabit: () -> Unit,
    unSkipHabit: () -> Unit,
    hasNote: Boolean = false,
    dialogueComposable: @Composable (Boolean, () -> Unit) -> Unit,
    interactive: Boolean = false
) {
    val context = LocalContext.current
    val isDialogVisible = remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme
    val visuals = remember(state, colorScheme) { resolveDayVisuals(state, colorScheme) }
    val bgColor = visuals.bgColor
    val textColor = visuals.textColor
    val borderColor = visuals.borderColor
    val makeToast =
        { Toast.makeText(context, "Cannot modify future data", Toast.LENGTH_SHORT).show() }
    val buttonAction: List<() -> Unit> = when {
        state == DayState.Error -> listOf({}, {})
        state.isDisabled -> listOf(makeToast, makeToast)
        state == DayState.Skip -> listOf(unSkipHabit, { isDialogVisible.value = true })
        else -> listOf(incrementHabit, { isDialogVisible.value = true })
    }
    var iconComposable: (@Composable () -> Unit) = { }
    var dateColor: Color = colorScheme.primary
    dialogueComposable(isDialogVisible.value, { isDialogVisible.value = false })
    val fontSizeForRepetition = listOf(13, 15)
    val formattedNumber = formatNumberToReadable(number = repetitionsOnThisDay)

    iconComposable = {
        when (visuals.iconKind) {
            DayIconKind.Check -> Icon(Icons.Default.Check, "", tint = textColor)
            DayIconKind.Close -> Icon(Icons.Default.Close, "", tint = textColor)
            DayIconKind.DoubleArrow -> Icon(Icons.Default.DoubleArrow, "", tint = textColor)
            DayIconKind.Number -> Text(
                text = formattedNumber,
                textAlign = TextAlign.Center,
                color = textColor,
                maxLines = 1,
                fontSize = if (formattedNumber.length > 3) {
                    fontSizeForRepetition[0].sp
                } else {
                    fontSizeForRepetition[1].sp
                },
                modifier = Modifier.fillMaxSize()
            )
            DayIconKind.None -> {}
        }
    }
   Box(
        modifier =
            Modifier
                .fillMaxWidth()


    ) {
        Box(
            Modifier.clip(   shape = RoundedCornerShape(10.dp),)
                .background(bgColor)
                .border(width = LocalOutlineSizes.current.small, color = borderColor, shape = RoundedCornerShape(10.dp))



            ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                if (hasNote) {
                    Surface(
                        shape = CircleShape,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(5.dp),
                        tonalElevation = 5.dp,
                        shadowElevation = 100.dp,
                        color = MaterialTheme.colorScheme.tertiary
                    ) {}
                }
                Column(
                    modifier =

                        Modifier
                            .fillMaxSize()
                            .combinedClickable(
                                onLongClick = buttonAction[1],

                                onClick = {
                                    buttonAction[0]()
                                    // println("$state This is state")
                                }
                            ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        /* Text(
                             date.dayOfWeek.name.slice(0..2),
                             style = MaterialTheme.typography.labelSmall
                         )*/
                        Text(
                            date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (interactive) {
                                dateColor
                            } else {
                                textColor
                            }
                        )
                        HorizontalDivider(modifier = Modifier.height(5.dp).fillMaxWidth(0.8f), color =  MaterialTheme.colorScheme.outlineVariant.copy(0.5f), thickness = 0.5.dp)
                    }

                    Column(
                        Modifier
                            .padding(5.dp)
                            .height(25.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {

                        iconComposable()
                    }
                }
            }
        }
    }
}

private enum class DayIconKind { Check, Close, DoubleArrow, Number, None }

private data class DayVisuals(
    val bgColor: Color,
    val textColor: Color,
    val borderColor: Color,
    val iconKind: DayIconKind
)

private fun resolveDayVisuals(state: DayState, cs: ColorScheme): DayVisuals = when (state) {
    DayState.AbsoluteMore ->
        DayVisuals(cs.primaryContainer.copy(0.3f), cs.primary, cs.primary.copy(0.5f), DayIconKind.Number)
    DayState.AbsoluteMoreDisabled ->
        DayVisuals(cs.inverseSurface.copy(0.3f), cs.onSurface.copy(0.5f), cs.inverseSurface.copy(0.1f), DayIconKind.Number)
    DayState.Absolute ->
        DayVisuals(cs.primary, cs.onPrimary, cs.primary, DayIconKind.Check)
    DayState.Partial ->
        DayVisuals(cs.primaryContainer.copy(0.5f), cs.primary, cs.primary.copy(0.3f), DayIconKind.Number)
    DayState.NegativeCount ->
        DayVisuals(cs.primaryContainer.copy(0.5f), cs.primary, cs.primary.copy(0.3f), DayIconKind.Number)
    DayState.NegativeCountDisabled ->
        DayVisuals(cs.inverseSurface.copy(0.2f), cs.onSurface.copy(0.3f), cs.inverseSurface.copy(0.1f), DayIconKind.Number)
    DayState.NotNeeded ->
        DayVisuals(cs.surfaceVariant.copy(0.3f), cs.primary.copy(0.5f), cs.primary.copy(0.2f), DayIconKind.Check)
    DayState.NotNeededDisabled ->
        DayVisuals(cs.surfaceVariant.copy(0.15f), cs.onSurface.copy(0.3f), cs.primary.copy(0.1f), DayIconKind.Check)
    DayState.AbsoluteDisabled ->
        DayVisuals(cs.inverseSurface.copy(0.5f), cs.onSurface.copy(0.5f), cs.inverseSurface.copy(0.1f), DayIconKind.Check)
    DayState.PartialDisabled ->
        DayVisuals(cs.inverseSurface.copy(0.2f), cs.onSurface.copy(0.3f), cs.inverseSurface.copy(0.1f), DayIconKind.Number)
    DayState.IncompleteDisabled ->
        DayVisuals(cs.inverseSurface.copy(0.05f), cs.onSurfaceVariant.copy(0.3f), cs.inverseSurface.copy(0.05f), DayIconKind.Close)
    DayState.NoteDisabled ->
        DayVisuals(cs.secondaryContainer.copy(0.3f), cs.onSecondaryContainer.copy(0.7f), cs.secondaryContainer.copy(0.1f), DayIconKind.Close)
    DayState.FailedDisabled ->
        DayVisuals(Color(0xFFF44336).copy(alpha = 0.1f), Color(0xFFF44336).copy(alpha = 0.3f), Color(0xFFF44336).copy(alpha = 0.1f), DayIconKind.Number)
    DayState.Incomplete ->
        DayVisuals(cs.surfaceVariant, cs.onSurfaceVariant, cs.primary.copy(0.5f), DayIconKind.Close)
    DayState.Note ->
        DayVisuals(cs.secondaryContainer, cs.onSecondaryContainer, cs.secondary, DayIconKind.Close)
    DayState.Skip ->
        DayVisuals(cs.tertiaryContainer, cs.onTertiaryContainer, cs.tertiary, DayIconKind.DoubleArrow)
    DayState.SkipDisabled ->
        DayVisuals(cs.tertiaryContainer.copy(0.4f), cs.onTertiaryContainer.copy(0.5f), cs.tertiary, DayIconKind.DoubleArrow)
    DayState.Failed ->
        DayVisuals(Color(0xFFF44336).copy(alpha = 0.2f), Color(0xFFF44336), Color(0xFFF44336), DayIconKind.Number)
    DayState.Error ->
        DayVisuals(cs.error, cs.onError, cs.primary, DayIconKind.None)
}