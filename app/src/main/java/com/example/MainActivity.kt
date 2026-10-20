package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.preferences.ContactSortOrder
import com.example.ui.screens.AddEditContactScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.ContactDetailScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BackgroundSlate
import com.example.ui.theme.CardBorder
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.ContactsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(
    viewModel: ContactsViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val contacts by viewModel.displayContacts.collectAsStateWithLifecycle()
    val allContacts by viewModel.allContacts.collectAsStateWithLifecycle()
    val favorites by viewModel.favoriteContacts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val categoryCounts by viewModel.categoryCounts.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()

    val detailContactId by viewModel.detailContactId.collectAsStateWithLifecycle()
    val editingContact by viewModel.editingContact.collectAsStateWithLifecycle()
    val isCreatingNew by viewModel.isCreatingNew.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    // Handle back button on secondary tabs
    if (detailContactId == null && editingContact == null && currentTab != AppNavTab.HOME) {
        BackHandler {
            viewModel.selectTab(AppNavTab.HOME)
        }
    }

    val isTopLevelScreen = detailContactId == null && editingContact == null

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (isTopLevelScreen) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 4.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == AppNavTab.HOME,
                        onClick = { viewModel.selectTab(AppNavTab.HOME) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppNavTab.HOME) Icons.Filled.Contacts else Icons.Outlined.Contacts,
                                contentDescription = "Contacts"
                            )
                        },
                        label = { Text("Contacts") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            selectedTextColor = PrimaryBlue,
                            indicatorColor = PrimaryBlue.copy(alpha = 0.12f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_contacts")
                    )

                    NavigationBarItem(
                        selected = currentTab == AppNavTab.FAVORITES,
                        onClick = { viewModel.selectTab(AppNavTab.FAVORITES) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppNavTab.FAVORITES) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Favorites"
                            )
                        },
                        label = { Text("Favorites") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            selectedTextColor = PrimaryBlue,
                            indicatorColor = PrimaryBlue.copy(alpha = 0.12f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_favorites")
                    )

                    NavigationBarItem(
                        selected = currentTab == AppNavTab.CATEGORIES,
                        onClick = { viewModel.selectTab(AppNavTab.CATEGORIES) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppNavTab.CATEGORIES) Icons.Filled.Label else Icons.Outlined.Label,
                                contentDescription = "Categories"
                            )
                        },
                        label = { Text("Categories") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            selectedTextColor = PrimaryBlue,
                            indicatorColor = PrimaryBlue.copy(alpha = 0.12f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_categories")
                    )

                    NavigationBarItem(
                        selected = currentTab == AppNavTab.SETTINGS,
                        onClick = { viewModel.selectTab(AppNavTab.SETTINGS) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppNavTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text("Settings") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            selectedTextColor = PrimaryBlue,
                            indicatorColor = PrimaryBlue.copy(alpha = 0.12f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                editingContact != null -> {
                    AddEditContactScreen(
                        contact = editingContact!!,
                        isCreatingNew = isCreatingNew,
                        onSave = { updatedContact ->
                            viewModel.saveContact(updatedContact)
                        },
                        onCancel = {
                            viewModel.cancelEditContact()
                        }
                    )
                }

                detailContactId != null -> {
                    val contact = allContacts.find { it.id == detailContactId }
                    if (contact != null) {
                        ContactDetailScreen(
                            contact = contact,
                            sortByLastName = sortOrder == ContactSortOrder.LAST_NAME,
                            onBack = { viewModel.closeContactDetail() },
                            onEdit = { viewModel.startEditContact(contact) },
                            onDelete = { viewModel.deleteContact(contact.id) },
                            onToggleFavorite = {
                                viewModel.toggleFavorite(contact.id, contact.isFavorite)
                            }
                        )
                    } else {
                        viewModel.closeContactDetail()
                    }
                }

                else -> {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tab_transition"
                    ) { tab ->
                        when (tab) {
                            AppNavTab.HOME -> {
                                HomeScreen(
                                    contacts = contacts,
                                    favorites = favorites,
                                    searchQuery = searchQuery,
                                    sortOrder = sortOrder,
                                    onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                                    onContactClick = { id -> viewModel.openContactDetail(id) },
                                    onToggleFavorite = { id, current ->
                                        viewModel.toggleFavorite(id, current)
                                    },
                                    onAddContactClick = { viewModel.startCreateContact() }
                                )
                            }

                            AppNavTab.FAVORITES -> {
                                FavoritesScreen(
                                    favorites = favorites,
                                    sortOrder = sortOrder,
                                    onContactClick = { id -> viewModel.openContactDetail(id) },
                                    onToggleFavorite = { id, current ->
                                        viewModel.toggleFavorite(id, current)
                                    }
                                )
                            }

                            AppNavTab.CATEGORIES -> {
                                CategoriesScreen(
                                    categoryCounts = categoryCounts,
                                    allContacts = allContacts,
                                    selectedCategory = selectedCategory,
                                    sortOrder = sortOrder,
                                    onSelectCategory = { cat -> viewModel.setCategoryFilter(cat) },
                                    onContactClick = { id -> viewModel.openContactDetail(id) },
                                    onToggleFavorite = { id, current ->
                                        viewModel.toggleFavorite(id, current)
                                    }
                                )
                            }

                            AppNavTab.SETTINGS -> {
                                SettingsScreen(
                                    contactCount = allContacts.size,
                                    sortOrder = sortOrder,
                                    onSortOrderChange = { order -> viewModel.setSortOrder(order) },
                                    onImportDeviceContacts = { viewModel.importDeviceContacts() },
                                    onLoadSampleContacts = { viewModel.loadSampleContacts() },
                                    onExportCsv = { viewModel.exportContactsAsCsv() },
                                    onClearAllContacts = { viewModel.clearAllContacts() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
