package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.ContactDao
import com.example.data.model.ContactEntity
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val contactDao: ContactDao) {
    val allContacts: Flow<List<ContactEntity>> = contactDao.getAllContacts()
    val favoriteContacts: Flow<List<ContactEntity>> = contactDao.getFavoriteContacts()

    fun getContactById(id: Long): Flow<ContactEntity?> = contactDao.getContactById(id)

    fun getContactsByCategory(category: String): Flow<List<ContactEntity>> =
        contactDao.getContactsByCategory(category)

    suspend fun insertContact(contact: ContactEntity): Long = contactDao.insert(contact)

    suspend fun insertAll(contacts: List<ContactEntity>) = contactDao.insertAll(contacts)

    suspend fun updateContact(contact: ContactEntity) = contactDao.update(contact)

    suspend fun deleteContact(contact: ContactEntity) = contactDao.delete(contact)

    suspend fun deleteContactById(id: Long) = contactDao.deleteById(id)

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) =
        contactDao.updateFavoriteStatus(id, isFavorite)

    suspend fun deleteAll() = contactDao.deleteAll()

    suspend fun getCount(): Int = contactDao.getCount()

    suspend fun resetSampleData() {
        contactDao.deleteAll()
        contactDao.insertAll(AppDatabase.SAMPLE_CONTACTS)
    }
}
