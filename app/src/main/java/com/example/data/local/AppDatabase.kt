package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ContactEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ContactEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_contacts_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val SAMPLE_CONTACTS = listOf(
            ContactEntity(
                firstName = "Alice",
                lastName = "Walker",
                phone = "+1 (555) 234-5678",
                email = "alice.walker@techflow.io",
                category = "Work",
                notes = "Senior Product Designer at TechFlow. Collaborates on design sprint.",
                isFavorite = true
            ),
            ContactEntity(
                firstName = "Bob",
                lastName = "Martinez",
                phone = "+1 (555) 345-6789",
                email = "bob.martinez@gmail.com",
                category = "Friends",
                notes = "College friend. Weekly basketball games on Thursdays.",
                isFavorite = true
            ),
            ContactEntity(
                firstName = "Charlie",
                lastName = "Chen",
                phone = "+1 (555) 456-7890",
                email = "charlie.chen@family.net",
                category = "Family",
                notes = "Cousin in San Francisco. Birthday is Nov 14.",
                isFavorite = true
            ),
            ContactEntity(
                firstName = "David",
                lastName = "Kim",
                phone = "+1 (555) 567-8901",
                email = "david.kim@studio.design",
                category = "Work",
                notes = "Lead Architect. Weekly sync at 10 AM Mondays.",
                isFavorite = false
            ),
            ContactEntity(
                firstName = "Emma",
                lastName = "Watson",
                phone = "+1 (555) 678-9012",
                email = "emma.watson@creative.org",
                category = "Friends",
                notes = "Book club coordinator. Recommended 'Project Hail Mary'.",
                isFavorite = true
            ),
            ContactEntity(
                firstName = "Frank",
                lastName = "Miller",
                phone = "+1 (555) 789-0123",
                email = "frank.miller@mechanic.com",
                category = "Other",
                notes = "Trusted auto specialist for annual inspection.",
                isFavorite = false
            ),
            ContactEntity(
                firstName = "Grace",
                lastName = "Hopper",
                phone = "+1 (555) 890-1234",
                email = "grace.hopper@navy.gov",
                category = "Work",
                notes = "Compiler engineering consultant & mentor.",
                isFavorite = false
            ),
            ContactEntity(
                firstName = "Henry",
                lastName = "Cavill",
                phone = "+1 (555) 901-2345",
                email = "henry.cavill@gymclub.com",
                category = "Friends",
                notes = "Fitness enthusiast, gaming buddy.",
                isFavorite = false
            ),
            ContactEntity(
                firstName = "Isabella",
                lastName = "Garcia",
                phone = "+1 (555) 012-3456",
                email = "isabella.garcia@family.net",
                category = "Family",
                notes = "Aunt Isabella. Homemade paella recipe keeper.",
                isFavorite = true
            ),
            ContactEntity(
                firstName = "Jack",
                lastName = "Daniels",
                phone = "+1 (555) 123-4567",
                email = "jack.daniels@culinary.com",
                category = "Other",
                notes = "Catering coordinator for events.",
                isFavorite = false
            ),
            ContactEntity(
                firstName = "Katherine",
                lastName = "Johnson",
                phone = "+1 (555) 234-6789",
                email = "katherine.johnson@space.nasa.gov",
                category = "Work",
                notes = "Lead mathematician & orbital mechanics researcher.",
                isFavorite = false
            ),
            ContactEntity(
                firstName = "Liam",
                lastName = "Neeson",
                phone = "+1 (555) 345-7890",
                email = "liam.neeson@rescue.org",
                category = "Friends",
                notes = "Always has a very particular set of skills.",
                isFavorite = false
            ),
            ContactEntity(
                firstName = "Mia",
                lastName = "Thorne",
                phone = "+1 (555) 456-8901",
                email = "mia.thorne@vetcare.com",
                category = "Other",
                notes = "Veterinarian at Oak Park Animal Clinic.",
                isFavorite = false
            ),
            ContactEntity(
                firstName = "Noah",
                lastName = "Centineo",
                phone = "+1 (555) 567-9012",
                email = "noah.centineo@media.co",
                category = "Friends",
                notes = "Podcast host & creative director.",
                isFavorite = false
            ),
            ContactEntity(
                firstName = "Olivia",
                lastName = "Pope",
                phone = "+1 (555) 678-0123",
                email = "olivia.pope@crisis.law",
                category = "Work",
                notes = "Crisis communication & brand consultant.",
                isFavorite = true
            ),
            ContactEntity(
                firstName = "Peter",
                lastName = "Parker",
                phone = "+1 (555) 789-1234",
                email = "peter.parker@dailybugle.com",
                category = "Friends",
                notes = "Freelance photographer with great reflexes.",
                isFavorite = false
            ),
            ContactEntity(
                firstName = "Rachel",
                lastName = "Green",
                phone = "+1 (555) 890-2345",
                email = "rachel.green@ralphlauren.com",
                category = "Work",
                notes = "Fashion merchandising manager.",
                isFavorite = false
            ),
            ContactEntity(
                firstName = "Samuel",
                lastName = "Jackson",
                phone = "+1 (555) 901-3456",
                email = "samuel.jackson@shield.org",
                category = "Other",
                notes = "Director of high-priority security initiatives.",
                isFavorite = false
            ),
            ContactEntity(
                firstName = "Tony",
                lastName = "Stark",
                phone = "+1 (555) 012-4567",
                email = "tony.stark@starkindustries.com",
                category = "Work",
                notes = "CEO & head engineer. Clean energy projects.",
                isFavorite = true
            ),
            ContactEntity(
                firstName = "Zoe",
                lastName = "Kravitz",
                phone = "+1 (555) 123-5678",
                email = "zoe.kravitz@soundrecords.com",
                category = "Friends",
                notes = "Musician and vinyl record collector.",
                isFavorite = false
            )
        )
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.contactDao())
                }
            }
        }

        suspend fun populateInitialData(dao: ContactDao) {
            if (dao.getCount() == 0) {
                dao.insertAll(SAMPLE_CONTACTS)
            }
        }
    }
}
