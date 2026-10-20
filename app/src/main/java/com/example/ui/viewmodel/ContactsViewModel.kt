package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ContactEntity
import com.example.data.preferences.ContactSortOrder
import com.example.data.preferences.PreferenceManager
import com.example.data.repository.ContactRepository
import com.example.util.ContactUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavTab {
    HOME,
    FAVORITES,
    CATEGORIES,
    SETTINGS
}

data class ContactCategoryCount(
    val category: String,
    val count: Int
)

class ContactsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ContactRepository
    private val preferenceManager = PreferenceManager(application)

    // Current Navigation Tab
    private val _currentTab = MutableStateFlow(AppNavTab.HOME)
    val currentTab: StateFlow<AppNavTab> = _currentTab

    // Active detail screen contact ID (null if not in detail view)
    private val _detailContactId = MutableStateFlow<Long?>(null)
    val detailContactId: StateFlow<Long?> = _detailContactId

    // Contact being added or edited (null if modal/screen closed)
    private val _editingContact = MutableStateFlow<ContactEntity?>(null)
    val editingContact: StateFlow<ContactEntity?> = _editingContact
    private val _isCreatingNew = MutableStateFlow(false)
    val isCreatingNew: StateFlow<Boolean> = _isCreatingNew

    // Search query in Home
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Selected category filter (null = all)
    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter

    // Sort order
    private val _sortOrder = MutableStateFlow(preferenceManager.sortOrder)
    val sortOrder: StateFlow<ContactSortOrder> = _sortOrder

    // Status snackbar/toast message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = ContactRepository(database.contactDao())
    }

    // Raw contacts flow from DB
    val allContacts: StateFlow<List<ContactEntity>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered & sorted contacts for Home
    val displayContacts: StateFlow<List<ContactEntity>> = combine(
        allContacts,
        _searchQuery,
        _sortOrder
    ) { contacts, query, order ->
        val sorted = when (order) {
            ContactSortOrder.FIRST_NAME -> contacts.sortedWith(
                compareBy(String.CASE_INSENSITIVE_ORDER) { it.firstName.ifBlank { it.lastName } }
            )
            ContactSortOrder.LAST_NAME -> contacts.sortedWith(
                compareBy(String.CASE_INSENSITIVE_ORDER) { it.lastName.ifBlank { it.firstName } }
            )
        }
        if (query.isBlank()) {
            sorted
        } else {
            val q = query.trim().lowercase()
            sorted.filter { contact ->
                contact.firstName.lowercase().contains(q) ||
                contact.lastName.lowercase().contains(q) ||
                contact.phone.contains(q) ||
                contact.email.lowercase().contains(q) ||
                contact.notes.lowercase().contains(q) ||
                contact.category.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Favorite contacts list
    val favoriteContacts: StateFlow<List<ContactEntity>> = repository.favoriteContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Category breakdown counts
    val categoryCounts: StateFlow<List<ContactCategoryCount>> = allContacts.combine(_sortOrder) { contacts, _ ->
        val predefined = listOf("Family", "Work", "Friends", "Other")
        val groups = contacts.groupBy { it.category.ifBlank { "Other" } }
        val allKeys = (predefined + groups.keys).distinct()
        allKeys.map { cat ->
            ContactCategoryCount(
                category = cat,
                count = groups[cat]?.size ?: 0
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: AppNavTab) {
        _currentTab.value = tab
        _detailContactId.value = null
        _editingContact.value = null
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun openContactDetail(id: Long) {
        _detailContactId.value = id
    }

    fun closeContactDetail() {
        _detailContactId.value = null
    }

    fun startCreateContact() {
        _editingContact.value = ContactEntity(
            firstName = "",
            lastName = "",
            phone = "",
            email = "",
            category = "Friends",
            notes = "",
            isFavorite = false
        )
        _isCreatingNew.value = true
    }

    fun startEditContact(contact: ContactEntity) {
        _editingContact.value = contact
        _isCreatingNew.value = false
    }

    fun cancelEditContact() {
        _editingContact.value = null
        _isCreatingNew.value = false
    }

    fun saveContact(contact: ContactEntity) {
        viewModelScope.launch {
            if (contact.id == 0L) {
                val newId = repository.insertContact(contact)
                _userMessage.value = "Contact added"
                _detailContactId.value = newId
            } else {
                repository.updateContact(contact)
                _userMessage.value = "Contact updated"
            }
            _editingContact.value = null
            _isCreatingNew.value = false
        }
    }

    fun toggleFavorite(id: Long, current: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(id, !current)
            _userMessage.value = if (!current) "Added to favorites" else "Removed from favorites"
        }
    }

    fun deleteContact(id: Long) {
        viewModelScope.launch {
            repository.deleteContactById(id)
            if (_detailContactId.value == id) {
                _detailContactId.value = null
            }
            _userMessage.value = "Contact deleted"
        }
    }

    fun setSortOrder(order: ContactSortOrder) {
        _sortOrder.value = order
        preferenceManager.sortOrder = order
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun importDeviceContacts() {
        viewModelScope.launch {
            val contacts = ContactUtils.queryDeviceContacts(getApplication())
            if (contacts.isNotEmpty()) {
                repository.insertAll(contacts)
                _userMessage.value = "Imported ${contacts.size} contacts from device"
            } else {
                _userMessage.value = "No device contacts found or permission not granted"
            }
        }
    }

    fun loadSampleContacts() {
        viewModelScope.launch {
            repository.resetSampleData()
            _userMessage.value = "Loaded ${AppDatabase.SAMPLE_CONTACTS.size} sample contacts"
        }
    }

    fun clearAllContacts() {
        viewModelScope.launch {
            repository.deleteAll()
            _detailContactId.value = null
            _editingContact.value = null
            _userMessage.value = "All contacts cleared"
        }
    }

    fun exportContactsAsCsv() {
        val list = allContacts.value
        if (list.isEmpty()) {
            _userMessage.value = "No contacts to export"
            return
        }
        val csv = ContactUtils.generateCsv(list)
        ContactUtils.shareCsv(getApplication(), csv)
    }
}
