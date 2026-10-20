package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import com.example.data.model.ContactEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ContactUtils {

    fun queryDeviceContacts(context: Context): List<ContactEntity> {
        val result = mutableListOf<ContactEntity>()
        val contentResolver = context.contentResolver
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE
        )

        try {
            val cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )

            cursor?.use {
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                val seenNumbers = mutableSetOf<String>()

                while (it.moveToNext()) {
                    val rawName = if (nameIdx != -1) it.getString(nameIdx) ?: "" else ""
                    val rawNumber = if (numIdx != -1) it.getString(numIdx) ?: "" else ""

                    val cleanNumber = rawNumber.trim()
                    if (cleanNumber.isNotEmpty() && !seenNumbers.contains(cleanNumber)) {
                        seenNumbers.add(cleanNumber)

                        val parts = rawName.trim().split("\\s+".toRegex())
                        val firstName = parts.firstOrNull() ?: "Contact"
                        val lastName = if (parts.size > 1) parts.drop(1).joinToString(" ") else ""

                        result.add(
                            ContactEntity(
                                firstName = firstName,
                                lastName = lastName,
                                phone = cleanNumber,
                                email = "",
                                category = "Other",
                                notes = "Imported from device contacts",
                                isFavorite = false
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun generateCsv(contacts: List<ContactEntity>): String {
        val sb = StringBuilder()
        sb.append("First Name,Last Name,Phone,Email,Category,Notes,Favorite\n")
        for (c in contacts) {
            sb.append(escapeCsv(c.firstName)).append(",")
            sb.append(escapeCsv(c.lastName)).append(",")
            sb.append(escapeCsv(c.phone)).append(",")
            sb.append(escapeCsv(c.email)).append(",")
            sb.append(escapeCsv(c.category)).append(",")
            sb.append(escapeCsv(c.notes)).append(",")
            sb.append(if (c.isFavorite) "Yes" else "No").append("\n")
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        var clean = value.replace("\"", "\"\"")
        if (clean.contains(",") || clean.contains("\n") || clean.contains("\"")) {
            clean = "\"$clean\""
        }
        return clean
    }

    fun shareCsv(context: Context, csvContent: String) {
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "MyContacts_Export_$dateStr.csv")
            putExtra(Intent.EXTRA_TEXT, csvContent)
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Export Contacts via"))
        } catch (e: Exception) {
            Toast.makeText(context, "No app available to export CSV", Toast.LENGTH_SHORT).show()
        }
    }

    fun dialPhoneNumber(context: Context, phone: String) {
        val cleanNumber = phone.replace("[^0-9+]".toRegex(), "")
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$cleanNumber")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open dialer", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendSms(context: Context, phone: String) {
        val cleanNumber = phone.replace("[^0-9+]".toRegex(), "")
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$cleanNumber")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open messaging app", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendEmail(context: Context, email: String) {
        if (email.isBlank()) {
            Toast.makeText(context, "No email address provided", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open email app", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareContactInfo(context: Context, contact: ContactEntity) {
        val text = buildString {
            append("Contact: ${contact.getDisplayName()}\n")
            if (contact.phone.isNotBlank()) append("Phone: ${contact.phone}\n")
            if (contact.email.isNotBlank()) append("Email: ${contact.email}\n")
            if (contact.category.isNotBlank()) append("Category: ${contact.category}\n")
            if (contact.notes.isNotBlank()) append("Notes: ${contact.notes}\n")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, contact.getDisplayName())
            putExtra(Intent.EXTRA_TEXT, text)
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Share Contact"))
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to share contact", Toast.LENGTH_SHORT).show()
        }
    }
}
