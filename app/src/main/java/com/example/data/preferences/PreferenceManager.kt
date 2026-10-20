package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences

enum class ContactSortOrder {
    FIRST_NAME,
    LAST_NAME
}

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("my_contacts_prefs", Context.MODE_PRIVATE)

    var sortOrder: ContactSortOrder
        get() {
            val name = prefs.getString(KEY_SORT_ORDER, ContactSortOrder.FIRST_NAME.name)
            return try {
                ContactSortOrder.valueOf(name ?: ContactSortOrder.FIRST_NAME.name)
            } catch (e: Exception) {
                ContactSortOrder.FIRST_NAME
            }
        }
        set(value) {
            prefs.edit().putString(KEY_SORT_ORDER, value.name).apply()
        }

    companion object {
        private const val KEY_SORT_ORDER = "contact_sort_order"
    }
}
