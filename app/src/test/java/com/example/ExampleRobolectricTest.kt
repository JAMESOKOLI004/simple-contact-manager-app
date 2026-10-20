package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ContactEntity
import com.example.util.ContactUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MyContacts", appName)
    }

    @Test
    fun `test contact entity initials and display name`() {
        val contact = ContactEntity(
            firstName = "Alice",
            lastName = "Walker",
            phone = "1234567890",
            category = "Work"
        )
        assertEquals("AW", contact.getInitials())
        assertEquals("Alice Walker", contact.getDisplayName(sortByLastName = false))
        assertEquals("Walker, Alice", contact.getDisplayName(sortByLastName = true))
        assertEquals('A', contact.getIndexLetter(sortByLastName = false))
        assertEquals('W', contact.getIndexLetter(sortByLastName = true))
    }

    @Test
    fun `test csv generation`() {
        val contacts = listOf(
            ContactEntity(
                firstName = "John",
                lastName = "Doe",
                phone = "+1 555 123",
                email = "john@example.com",
                category = "Friends",
                isFavorite = true
            )
        )
        val csv = ContactUtils.generateCsv(contacts)
        assertTrue(csv.contains("First Name,Last Name,Phone,Email,Category,Notes,Favorite"))
        assertTrue(csv.contains("John,Doe,+1 555 123,john@example.com,Friends,,Yes"))
    }
}
