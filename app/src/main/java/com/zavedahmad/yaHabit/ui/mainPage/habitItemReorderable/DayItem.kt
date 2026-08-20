package com.zavedahmad.yaHabit.ui.mainPage.habitItemReorderable


import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
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
    var bgColor = MaterialTheme.colorScheme.error
    var textColor = MaterialTheme.colorScheme.onError
    var borderColor = MaterialTheme.colorScheme.primary
    var buttonAction: List<() -> Unit> = listOf({}, {})
    var icon: ImageVector? = null
    var iconComposable: (@Composable () -> Unit) = { }
    var dateColor: Color = MaterialTheme.colorScheme.primary
    dialogueComposable(isDialogVisible.value, { isDialogVisible.value = false })
    val fontSizeForRepetition = listOf(13, 15)
    val formattedNumber = formatNumberToReadable(number = repetitionsOnThisDay)
    val makeToast =
        { Toast.makeText(context, "Cannot modify future data", Toast.LENGTH_SHORT).show() }
    when (state) {
        DayState.AbsoluteMore -> {
            buttonAction = listOf(incrementHabit, { isDialogVisible.value = true })
            bgColor = MaterialTheme.colorScheme.primaryContainer.copy(0.3f)
            textColor = MaterialTheme.colorScheme.primary
            borderColor = MaterialTheme.colorScheme.primary.copy(0.5f)
            icon = Icons.Default.Check
            iconComposable = {
                Text(
                    text = formattedNumber,
                    textAlign = TextAlign.Center,
                    color = textColor,
                    maxLines = 1,
                    fontSize = if (formattedNumber.length > 3) {
                        fontSizeForRepetition[0]
                            .sp
                    } else {
                        fontSizeForRepetition[1].sp
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        DayState.AbsoluteMoreDisabled -> {
            bgColor = MaterialTheme.colorScheme.inverseSurface.copy(0.3f)
            textColor = MaterialTheme.colorScheme.onSurface.copy(0.5f)
            borderColor = MaterialTheme.colorScheme.inverseSurface.copy(0.1f)

            iconComposable = {
                Text(
                    text = formattedNumber,
                    textAlign = TextAlign.Center,
                    color = textColor,
                    maxLines = 1,
                    fontSize = if (formattedNumber.length > 3) {
                        fontSizeForRepetition[0]
                            .sp
                    } else {
                        fontSizeForRepetition[1].sp
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        DayState.Absolute -> {
            buttonAction = listOf(incrementHabit, { isDialogVisible.value = true })
            bgColor = MaterialTheme.colorScheme.primary
            textColor = MaterialTheme.colorScheme.onPrimary
            borderColor = MaterialTheme.colorScheme.primary
            icon = Icons.Default.Check
            iconComposable = { Icon(Icons.Default.Check, "", tint = textColor) }

        }

        DayState.Partial -> {
            buttonAction = listOf(incrementHabit, { isDialogVisible.value = true })
            bgColor = MaterialTheme.colorScheme.primaryContainer.copy(0.5f)
            textColor = MaterialTheme.colorScheme.primary
            borderColor = MaterialTheme.colorScheme.primary.copy(0.3f)
            iconComposable = {
                Text(
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
            }

        }

        DayState.NegativeCount -> {
            buttonAction = listOf(incrementHabit, { isDialogVisible.value = true })
            bgColor = MaterialTheme.colorScheme.primaryContainer.copy(0.5f)
            textColor = MaterialTheme.colorScheme.primary
            borderColor = MaterialTheme.colorScheme.primary.copy(0.3f)
            iconComposable = {
                Text(
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
            }

        }

        DayState.NegativeCountDisabled -> {
            buttonAction = listOf(makeToast, makeToast)
            bgColor = MaterialTheme.colorScheme.inverseSurface.copy(0.2f)
            textColor = MaterialTheme.colorScheme.onSurface.copy(0.3f)
            borderColor = MaterialTheme.colorScheme.inverseSurface.copy(0.1f)
            iconComposable = {
                Text(
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
            }

        }

        DayState.NotNeeded -> {
            buttonAction = listOf(incrementHabit, { isDialogVisible.value = true })
            bgColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f)
            textColor = MaterialTheme.colorScheme.primary.copy(0.5f)
            borderColor = MaterialTheme.colorScheme.primary.copy(0.2f)
            iconComposable = {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "not needed",
                    tint = textColor.copy(alpha = 0.5f)
                )
            }
        }

        DayState.NotNeededDisabled -> {
            buttonAction = listOf(makeToast, makeToast)
            bgColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.15f)
            textColor = MaterialTheme.colorScheme.onSurface.copy(0.3f)
            borderColor = MaterialTheme.colorScheme.primary.copy(0.1f)
            iconComposable = {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "not needed",
                    tint = textColor.copy(alpha = 0.3f)
                )
            }
        }

        DayState.AbsoluteDisabled -> {
            buttonAction = listOf(makeToast, makeToast)
            bgColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f)
            textColor = MaterialTheme.colorScheme.onSurface.copy(0.5f)
            borderColor = MaterialTheme.colorScheme.inverseSurface.copy(0.1f)
            iconComposable = { Icon(Icons.Default.Check, "", tint = textColor) }

        }

        DayState.PartialDisabled -> {
            buttonAction = listOf(makeToast, makeToast)
            bgColor = MaterialTheme.colorScheme.inverseSurface.copy(0.2f)
            textColor = MaterialTheme.colorScheme.onSurface.copy(0.3f)
            borderColor = MaterialTheme.colorScheme.inverseSurface.copy(0.1f)
            iconComposable = {
                Text(
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
            }

        }

        DayState.IncompleteDisabled -> {
            buttonAction = listOf(makeToast, makeToast)
            bgColor = MaterialTheme.colorScheme.inverseSurface.copy(0.05f)
            textColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f)
            borderColor = MaterialTheme.colorScheme.inverseSurface.copy(0.05f)
            iconComposable = { Icon(Icons.Default.Close, "", tint = textColor) }

        }

        DayState.NoteDisabled -> {
            buttonAction = listOf(makeToast, makeToast)
            bgColor = MaterialTheme.colorScheme.secondaryContainer.copy(0.3f)
            textColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(0.7f)
            borderColor = MaterialTheme.colorScheme.secondaryContainer.copy(0.1f)
            iconComposable = { Icon(Icons.Default.Close, "", tint = textColor) }
        }

        DayState.FailedDisabled -> {
            buttonAction = listOf(makeToast, makeToast)
            bgColor = Color(0xFFF44336).copy(alpha = 0.1f)
            textColor = Color(0xFFF44336).copy(alpha = 0.3f)
            borderColor = Color(0xFFF44336).copy(alpha = 0.1f)
            iconComposable = {
                Text(
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
            }
        }

        DayState.Incomplete -> {
            borderColor = MaterialTheme.colorScheme.primary.copy(0.5f)
            buttonAction = listOf(incrementHabit, { isDialogVisible.value = true })
            bgColor = MaterialTheme.colorScheme.surfaceVariant
            textColor = MaterialTheme.colorScheme.onSurfaceVariant
            iconComposable = { Icon(Icons.Default.Close, "", tint = textColor) }

        }

        DayState.Note -> {
            borderColor = MaterialTheme.colorScheme.secondary
            buttonAction = listOf(incrementHabit, { isDialogVisible.value = true })
            bgColor = MaterialTheme.colorScheme.secondaryContainer
            textColor = MaterialTheme.colorScheme.onSecondaryContainer
            iconComposable = { Icon(Icons.Default.Close, "", tint = textColor) }
        }

        DayState.Skip -> {
            borderColor = MaterialTheme.colorScheme.tertiary
            buttonAction = listOf(unSkipHabit, { isDialogVisible.value = true })
            bgColor = MaterialTheme.colorScheme.tertiaryContainer
            textColor = MaterialTheme.colorScheme.onTertiaryContainer
            iconComposable = { Icon(Icons.Default.DoubleArrow, "", tint = textColor) }
        }

        DayState.SkipDisabled -> {
            buttonAction = listOf(makeToast, makeToast)
            borderColor = MaterialTheme.colorScheme.tertiary
            bgColor = MaterialTheme.colorScheme.tertiaryContainer.copy(0.4f)
            textColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(0.5f)
            iconComposable = { Icon(Icons.Default.DoubleArrow, "", tint = textColor) }
        }

        DayState.Failed -> {
            borderColor = Color(0xFFF44336)
            buttonAction = listOf(incrementHabit, { isDialogVisible.value = true })
            bgColor = Color(0xFFF44336).copy(alpha = 0.2f)
            textColor = Color(0xFFF44336)
            iconComposable = {
                Text(
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
            }
        }

        DayState.Error -> {
            // Data integrity issue (multiple entries for one day); render with defaults.
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