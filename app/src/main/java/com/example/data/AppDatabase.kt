package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Dao
interface AppDao {
    @Query("SELECT * FROM farmer_profile WHERE id = 1 LIMIT 1")
    fun getFarmerProfile(): Flow<FarmerProfile?>

    @Query("SELECT * FROM farmer_profile WHERE id = 1 LIMIT 1")
    suspend fun getFarmerProfileSync(): FarmerProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFarmerProfile(profile: FarmerProfile)

    @Query("SELECT * FROM crop_records ORDER BY sowingDate DESC")
    fun getAllCrops(): Flow<List<CropRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrop(crop: CropRecord)

    @Delete
    suspend fun deleteCrop(crop: CropRecord)

    @Query("SELECT * FROM financial_logs ORDER BY date DESC")
    fun getAllFinancials(): Flow<List<FinancialLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinancial(log: FinancialLog)

    @Query("SELECT * FROM community_posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<CommunityPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPost)
}

@Database(
    entities = [FarmerProfile::class, CropRecord::class, FinancialLog::class, CommunityPost::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "krushimitra_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val dao = database.dao()
                    // Seed some initial community posts to make it interactive and inspiring!
                    dao.insertPost(
                        CommunityPost(
                            authorName = "Ramesh Patil",
                            authorVillage = "Sillod, Aurangabad",
                            questionText = "What is the best bio-fertilizer for Bt Cotton to improve leaf health in the first 45 days?",
                            timestamp = System.currentTimeMillis() - 3600000 * 5,
                            likesCount = 14,
                            commentsCount = 2,
                            commentsJson = """
                                [
                                  {"author": "Vijay Shinde", "text": "Apply Neem Cake mixed with Vermicompost. It gives amazing results!"},
                                  {"author": "Suresh Kale", "text": "Azotobacter liquid fertilizer also works very well around this time."}
                                ]
                            """.trimIndent()
                        )
                    )
                    dao.insertPost(
                        CommunityPost(
                            authorName = "Anil Deshmukh",
                            authorVillage = "Akola, Vidarbha",
                            questionText = "Soyabean yellow mosaic virus detected in my neighboring farm. What proactive spray can I use with Gemini recommendation?",
                            timestamp = System.currentTimeMillis() - 3600000 * 24,
                            likesCount = 28,
                            commentsCount = 3,
                            commentsJson = """
                                [
                                  {"author": "Expert Krishi Mitra", "text": "Spray Acetamiprid 20% SP at 80g per acre to control the whiteflies that spread this virus."},
                                  {"author": "Vikas Jadhav", "text": "Set up blue and yellow sticky traps everywhere in your borders immediately!"}
                                ]
                            """.trimIndent()
                        )
                    )
                }
            }
        }
    }
}
