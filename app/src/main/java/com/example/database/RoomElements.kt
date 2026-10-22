package com.example.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val avatarUrl: String,
    val imageUrl: String,
    val caption: String,
    val likesCount: Int,
    val isLiked: Boolean,
    val timestamp: Long,
    val location: String? = null
)

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val avatarUrl: String,
    val imageUrl: String,
    val isUnread: Boolean,
    val timestamp: Long
)

@Dao
interface SocialDao {
    // Posts
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Update
    suspend fun updatePost(post: PostEntity)

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun deletePost(id: Int)

    @Query("DELETE FROM posts")
    suspend fun clearAllPosts()

    // Stories
    @Query("SELECT * FROM stories ORDER BY timestamp DESC")
    fun getAllStories(): Flow<List<StoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)

    @Update
    suspend fun updateStory(story: StoryEntity)

    @Query("DELETE FROM stories WHERE id = :id")
    suspend fun deleteStory(id: Int)

    @Query("DELETE FROM stories")
    suspend fun clearAllStories()
}

@Database(entities = [PostEntity::class, StoryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun socialDao(): SocialDao
}
