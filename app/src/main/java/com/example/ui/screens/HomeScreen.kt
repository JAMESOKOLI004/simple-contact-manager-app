package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactEntity
import com.example.data.preferences.ContactSortOrder
import com.example.ui.components.AlphabeticalIndex
import com.example.ui.components.ContactAvatar
import com.example.ui.components.ContactRowItem
import com.example.ui.theme.BackgroundSlate
import com.example.ui.theme.CardBorder
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.StarGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    contacts: List<ContactEntity>,
    favorites: List<ContactEntity>,
    searchQuery: String,
    sortOrder: ContactSortOrder,
    onSearchQueryChange: (String) -> Unit,
    onContactClick: (Long) -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    onAddContactClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val sortByLastName = sortOrder == ContactSortOrder.LAST_NAME

    // Group contacts by index letter
    val groupedContacts by remember(contacts, sortByLastName) {
        derivedStateOf {
            contacts.groupBy { it.getIndexLetter(sortByLastName) }
                .toSortedMap { a, b ->
                    if (a == '#') 1 else if (b == '#') -1 else a.compareTo(b)
                }
        }
    }

    val availableLetters by remember(groupedContacts) {
        derivedStateOf { groupedContacts.keys.toSet() }
    }

    // Map each letter to its scroll position in LazyColumn
    val letterPositionMap by remember(groupedContacts, favorites, searchQuery) {
        derivedStateOf {
            val map = mutableMapOf<Char, Int>()
            var currentIndex = 0

            // If favorites banner is shown, account for it as an item
            if (searchQuery.isBlank() && favorites.isNotEmpty()) {
                currentIndex += 1
            }

            groupedContacts.forEach { (letter, contactList) ->
                map[letter] = currentIndex
                currentIndex += 1 // Header item
                currentIndex += contactList.size // Contacts in this group
            }
            map
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundSlate)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar / Title Header
            Surface(
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MyContacts",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "${contacts.size} ${if (contacts.size == 1) "contact" else "contacts"}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("search_bar_input"),
                        placeholder = {
                            Text(
                                "Search name, phone, email, notes...",
                                fontSize = 14.sp,
                                color = TextSecondary.copy(alpha = 0.8f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSearchQueryChange("") },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = CardBorder,
                            focusedContainerColor = BackgroundSlate,
                            unfocusedContainerColor = BackgroundSlate
                        )
                    )
                }
            }

            // Main Content Area with List and Alphabetical Index
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (contacts.isEmpty()) {
                    // Empty Search or Empty Contacts State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonSearch,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No contacts match \"$searchQuery\"" else "No contacts yet",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Try checking the spelling or search by category" else "Tap '+ Add Contact' to create your first contact",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary
                            )
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("contacts_list"),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        // 1. Favorites Horizontal Scroll at top
                        if (searchQuery.isBlank() && favorites.isNotEmpty()) {
                            item(key = "favorites_section") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = StarGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "FAVORITES",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextSecondary,
                                                letterSpacing = 1.sp
                                            )
                                        )
                                    }

                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                    ) {
                                        items(favorites, key = { "fav_${it.id}" }) { contact ->
                                            FavoriteContactItem(
                                                contact = contact,
                                                onClick = { onContactClick(contact.id) }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = CardBorder)
                                }
                            }
                        }

                        // 2. Alphabetical Contact Groups
                        groupedContacts.forEach { (letter, letterContacts) ->
                            item(key = "header_$letter") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(BackgroundSlate)
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = letter.toString(),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlue,
                                            fontSize = 15.sp
                                        )
                                    )
                                }
                            }

                            items(letterContacts, key = { it.id }) { contact ->
                                ContactRowItem(
                                    contact = contact,
                                    sortByLastName = sortByLastName,
                                    onClick = { onContactClick(contact.id) },
                                    onToggleFavorite = {
                                        onToggleFavorite(contact.id, contact.isFavorite)
                                    }
                                )
                                HorizontalDivider(
                                    color = CardBorder.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(start = 78.dp)
                                )
                            }
                        }
                    }

                    // Alphabetical Scroll Index on the right edge
                    if (searchQuery.isBlank() && availableLetters.isNotEmpty()) {
                        AlphabeticalIndex(
                            availableLetters = availableLetters,
                            onLetterSelected = { letter ->
                                val targetPosition = letterPositionMap[letter]
                                if (targetPosition != null) {
                                    coroutineScope.launch {
                                        listState.scrollToItem(targetPosition)
                                    }
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 2.dp)
                        )
                    }
                }
            }
        }

        // Floating Action Button (+ Add Contact)
        ExtendedFloatingActionButton(
            onClick = onAddContactClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
                .testTag("add_contact_fab"),
            containerColor = PrimaryBlue,
            contentColor = Color.White,
            icon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
            text = {
                Text(
                    text = "Add Contact",
                    fontWeight = FontWeight.SemiBold
                )
            }
        )
    }
}

@Composable
private fun FavoriteContactItem(
    contact: ContactEntity,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(68.dp)
            .clickable(onClick = onClick)
            .testTag("fav_avatar_${contact.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ContactAvatar(
            contact = contact,
            size = 56.dp,
            showStarBadge = true
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = contact.firstName.ifBlank { contact.getDisplayName() },
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                fontSize = 12.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
