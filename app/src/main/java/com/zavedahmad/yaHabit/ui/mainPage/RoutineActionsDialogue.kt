package com.zavedahmad.yaHabit.ui.mainPage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import com.zavedahmad.yaHabit.R
import com.zavedahmad.yaHabit.Screen
import com.zavedahmad.yaHabit.database.entities.RoutineEntity
import com.zavedahmad.yaHabit.ui.mainPage.habitItemReorderable.HabitActionItem

/**
 * Mirror of [com.zavedahmad.yaHabit.ui.mainPage.habitItemReorderable.HabitActionsDialogue]
 * for routines: a "⋮" that opens a bottom sheet with archive/unarchive, edit and delete.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineActionsDialogue(
    routine: RoutineEntity,
    viewModel: MainPageViewModel,
    backStack: NavBackStack,
    showDeleteDialog: MutableState<Boolean>
) {
    val isDialogueVisible = rememberSaveable { mutableStateOf(false) }
    IconButton(onClick = { isDialogueVisible.value = !isDialogueVisible.value }) {
        Icon(
            imageVector = Icons.Outlined.MoreVert,
            contentDescription = "Routine actions"
        )
    }
    if (isDialogueVisible.value) {
        ModalBottomSheet(onDismissRequest = { isDialogueVisible.value = false }, dragHandle = { }) {
            Box(
                Modifier
                    .padding(20.dp)
                    .padding(bottom = 10.dp)
            ) {
                Row {
                    if (routine.isArchived) {
                        HabitActionItem(icon = {
                            Icon(
                                painterResource(R.drawable.archive_off_outline),
                                contentDescription = ""
                            )
                        }, label = "Unarchive", clickAction = {
                            isDialogueVisible.value = false
                            viewModel.unArchiveRoutineById(routine.id)
                        })
                    } else {
                        HabitActionItem(icon = {
                            Icon(
                                painterResource(R.drawable.archive_outline),
                                contentDescription = ""
                            )
                        }, label = "Archive", clickAction = {
                            isDialogueVisible.value = false
                            viewModel.archiveRoutineById(routine.id)
                        })
                    }

                    HabitActionItem(icon = {
                        Icon(Icons.Default.Edit, contentDescription = "")
                    }, label = "Edit", clickAction = {
                        isDialogueVisible.value = false
                        backStack.add(Screen.AddRoutinePageRoute(routineId = routine.id))
                    })

                    HabitActionItem(icon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }, label = "Delete", clickAction = {
                        showDeleteDialog.value = true
                    })
                }
            }
        }
    }
}
