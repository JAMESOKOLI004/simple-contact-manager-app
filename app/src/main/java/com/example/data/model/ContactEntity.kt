package com.example.data.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ui.theme.AvatarColors
import com.example.ui.theme.CategoryFamily
import com.example.ui.theme.CategoryFamilyBg
import com.example.ui.theme.CategoryFriends
import com.example.ui.theme.CategoryFriendsBg
import com.example.ui.theme.CategoryOther
import com.example.ui.theme.CategoryOtherBg
import com.example.ui.theme.CategoryWork
import com.example.ui.theme.CategoryWorkBg
import kotlin.math.abs

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firstName: String,
    val lastName: String = "",
    val phone: String,
    val email: String = "",
    val category: String = "Other",
    val notes: String = "",
    val photoUri: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getDisplayName(sortByLastName: Boolean = false): String {
        val f = firstName.trim()
        val l = lastName.trim()
        return when {
            f.isNotEmpty() && l.isNotEmpty() -> {
                if (sortByLastName) "$l, $f" else "$f $l"
            }
            f.isNotEmpty() -> f
            l.isNotEmpty() -> l
            phone.isNotEmpty() -> phone
            else -> "Unnamed Contact"
        }
    }

    fun getInitials(): String {
        val f = firstName.trim()
        val l = lastName.trim()
        val firstChar = f.firstOrNull()?.uppercaseChar()
        val lastChar = l.firstOrNull()?.uppercaseChar()

        return when {
            firstChar != null && lastChar != null -> "$firstChar$lastChar"
            firstChar != null -> "$firstChar"
            lastChar != null -> "$lastChar"
            else -> "?"
        }
    }

    fun getIndexLetter(sortByLastName: Boolean = false): Char {
        val target = if (sortByLastName && lastName.isNotBlank()) {
            lastName.trim()
        } else if (firstName.isNotBlank()) {
            firstName.trim()
        } else if (lastName.isNotBlank()) {
            lastName.trim()
        } else {
            "#"
        }
        val first = target.firstOrNull()?.uppercaseChar() ?: '#'
        return if (first in 'A'..'Z') first else '#'
    }

    fun getAvatarColor(): Color {
        val seed = (firstName + lastName + id.toString()).hashCode()
        val index = abs(seed) % AvatarColors.size
        return AvatarColors[index]
    }

    fun getCategoryColors(): Pair<Color, Color> {
        return when (category.lowercase()) {
            "family" -> CategoryFamily to CategoryFamilyBg
            "work" -> CategoryWork to CategoryWorkBg
            "friends" -> CategoryFriends to CategoryFriendsBg
            else -> CategoryOther to CategoryOtherBg
        }
    }
}
