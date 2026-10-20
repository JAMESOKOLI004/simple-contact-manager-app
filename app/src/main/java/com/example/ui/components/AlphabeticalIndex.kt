package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryBlue

val ALPHABET_LETTERS = ('A'..'Z').toList() + listOf('#')

@Composable
fun AlphabeticalIndex(
    availableLetters: Set<Char>,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    var totalHeight by remember { mutableStateOf(0f) }
    var selectedLetter by remember { mutableStateOf<Char?>(null) }

    fun calculateLetter(y: Float): Char? {
        if (totalHeight <= 0f) return null
        val itemHeight = totalHeight / ALPHABET_LETTERS.size
        val index = (y / itemHeight).toInt().coerceIn(0, ALPHABET_LETTERS.size - 1)
        return ALPHABET_LETTERS[index]
    }

    Box(
        modifier = modifier
            .width(28.dp)
            .fillMaxHeight()
            .padding(vertical = 8.dp)
            .testTag("alphabetical_index_bar"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.85f))
                .padding(vertical = 4.dp, horizontal = 2.dp)
                .onGloballyPositioned { coordinates ->
                    totalHeight = coordinates.size.height.toFloat()
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            val letter = calculateLetter(offset.y)
                            if (letter != null) {
                                selectedLetter = letter
                                onLetterSelected(letter)
                            }
                            tryAwaitRelease()
                            selectedLetter = null
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            val letter = calculateLetter(offset.y)
                            if (letter != null) {
                                selectedLetter = letter
                                onLetterSelected(letter)
                            }
                        },
                        onDragEnd = {
                            selectedLetter = null
                        },
                        onDragCancel = {
                            selectedLetter = null
                        },
                        onVerticalDrag = { change, _ ->
                            val letter = calculateLetter(change.position.y)
                            if (letter != null) {
                                selectedLetter = letter
                                onLetterSelected(letter)
                            }
                        }
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            ALPHABET_LETTERS.forEach { letter ->
                val isAvailable = availableLetters.contains(letter)
                val isSelected = selectedLetter == letter

                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) PrimaryBlue else Color.Transparent
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter.toString(),
                        fontSize = 9.sp,
                        fontWeight = if (isSelected || isAvailable) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            isSelected -> Color.White
                            isAvailable -> PrimaryBlue
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                        }
                    )
                }
            }
        }
    }
}
