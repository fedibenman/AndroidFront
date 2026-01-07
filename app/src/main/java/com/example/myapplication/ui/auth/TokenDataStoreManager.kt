package com.example.myapplication.ui.auth

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Single-row entity holding tokens and userId
@Entity(tableName = "auth_tokens")
data class AuthTokenEntity(
    @PrimaryKey val id: Int = 0,
    val accessToken: String?,
    val refreshToken: String?,
    val userId: String? = null
)

// DAO for reading/writing the single token row
@Dao
interface AuthTokenDao {

    @Query("SELECT * FROM auth_tokens WHERE id = 0 LIMIT 1")
    fun observeTokens(): Flow<AuthTokenEntity?>

    @Query("SELECT * FROM auth_tokens WHERE id = 0 LIMIT 1")
    suspend fun getTokens(): AuthTokenEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AuthTokenEntity)

    @Query("DELETE FROM auth_tokens")
    suspend fun clear()
}

// Room database definition
@Database(entities = [AuthTokenEntity::class], version = 2, exportSchema = false)
abstract class AuthTokenDatabase : RoomDatabase() {
    abstract fun authTokenDao(): AuthTokenDao

    companion object {
        @Volatile
        private var INSTANCE: AuthTokenDatabase? = null

        fun getInstance(context: Context): AuthTokenDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AuthTokenDatabase::class.java,
                    "auth_tokens_db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}

/**
 * Manager API compatible with previous usage, but backed by Room instead of DataStore.
 */
class TokenDataStoreManager(context: Context) {

    private val dao: AuthTokenDao = AuthTokenDatabase.getInstance(context).authTokenDao()

    val accessTokenFlow: Flow<String?> =
        dao.observeTokens().map { it?.accessToken }

    val refreshTokenFlow: Flow<String?> =
        dao.observeTokens().map { it?.refreshToken }
        
    val userIdFlow: Flow<String?> =
        dao.observeTokens().map { it?.userId }

    suspend fun saveTokens(accessToken: String, refreshToken: String, userId: String? = null) {
        // Preserve existing userId if not provided
        val existing = dao.getTokens()
        val finalUserId = userId ?: existing?.userId
        dao.upsert(AuthTokenEntity(accessToken = accessToken, refreshToken = refreshToken, userId = finalUserId))
    }

    suspend fun saveAccessTokenOnly(accessToken: String) {
        val existing = dao.getTokens()
        dao.upsert(AuthTokenEntity(accessToken = accessToken, refreshToken = existing?.refreshToken, userId = existing?.userId))
    }
    
    suspend fun saveUserId(userId: String) {
        val existing = dao.getTokens()
        dao.upsert(AuthTokenEntity(
            accessToken = existing?.accessToken,
            refreshToken = existing?.refreshToken,
            userId = userId
        ))
    }
    
    suspend fun getUserId(): String? {
        return dao.getTokens()?.userId
    }

    suspend fun clearTokens() {
        dao.clear()
    }
}
