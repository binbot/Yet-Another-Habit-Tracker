package com.zavedahmad.yaHabit.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun DialogItem(
    modifier: Modifier = Modifier,
    state: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {

    Box(
        modifier
            .clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(shape = MaterialTheme.shapes.medium,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            )

            .combinedClickable(onClick = { onClick() })

    ) {

        content()

    }
}