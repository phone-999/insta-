package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_posts")
data class UserPost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val userAvatarUrl: String,
    val location: String,
    val imageUrl: String,
    val caption: String,
    val timestamp: Long = System.currentTimeMillis(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val isBookmarked: Boolean = false
)

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val postId: Int,
    val username: String,
    val userAvatarUrl: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "stories")
data class Story(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val userAvatarUrl: String,
    val storyImageUrl: String,
    val isUnread: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
