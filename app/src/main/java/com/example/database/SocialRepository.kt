package com.example.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SocialRepository(private val database: AppDatabase) {
    private val dao = database.socialDao()

    val allPosts: Flow<List<PostEntity>> = dao.getAllPosts()
    val allStories: Flow<List<StoryEntity>> = dao.getAllStories()

    suspend fun insertPost(post: PostEntity) {
        dao.insertPost(post)
    }

    suspend fun updatePost(post: PostEntity) {
        dao.updatePost(post)
    }

    suspend fun deletePost(id: Int) {
        dao.deletePost(id)
    }

    suspend fun insertStory(story: StoryEntity) {
        dao.insertStory(story)
    }

    suspend fun updateStory(story: StoryEntity) {
        dao.updateStory(story)
    }

    suspend fun deleteStory(id: Int) {
        dao.deleteStory(id)
    }

    suspend fun clearAll() {
        dao.clearAllPosts()
        dao.clearAllStories()
    }

    // Seeding dynamic starter data if the local Room database is clean
    suspend fun seedIfNeeded() {
        val currentPosts = allPosts.first()
        if (currentPosts.isEmpty()) {
            val defaultPosts = listOf(
                PostEntity(
                    username = "pixel_wanderer",
                    avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100",
                    imageUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=600",
                    caption = "Lost in the whispers of the alpine peaks. Early morning light hitting just right. 🏔️✨ #nature #adventure",
                    likesCount = 1420,
                    isLiked = false,
                    timestamp = System.currentTimeMillis() - 3600000, // 1 hr ago
                    location = "Chamonix, France"
                ),
                PostEntity(
                    username = "chef_lucas",
                    avatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=100",
                    imageUrl = "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=600",
                    caption = "Artisan sourdough pizza straight from the woodfire oven. 48-hour slow fermentation makes all the difference! 🍕🔥 #pizza #baking #foodie",
                    likesCount = 852,
                    isLiked = false,
                    timestamp = System.currentTimeMillis() - 7200000, // 2 hr ago
                    location = "Naples, Italy"
                ),
                PostEntity(
                    username = "design_minimal",
                    avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100",
                    imageUrl = "https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?w=600",
                    caption = "Desk setup check. Harmonious grid alignment, warm oak textures, and a custom mechanical keyboard. Warmly productive. ☕💻 #desksetup #productivity",
                    likesCount = 2054,
                    isLiked = false,
                    timestamp = System.currentTimeMillis() - 14400000, // 4 hr ago
                    location = null
                )
            )
            for (post in defaultPosts) {
                dao.insertPost(post)
            }
        }

        val currentStories = allStories.first()
        if (currentStories.isEmpty()) {
            val defaultStories = listOf(
                StoryEntity(
                    username = "My Story",
                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100",
                    imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600",
                    isUnread = false,
                    timestamp = System.currentTimeMillis() - 50000
                ),
                StoryEntity(
                    username = "pixel_wanderer",
                    avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100",
                    imageUrl = "https://images.unsplash.com/photo-1500627869374-13cd993b1115?w=600",
                    isUnread = true,
                    timestamp = System.currentTimeMillis() - 1200000
                ),
                StoryEntity(
                    username = "chef_lucas",
                    avatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=100",
                    imageUrl = "https://images.unsplash.com/photo-1544025162-d76694265947?w=600",
                    isUnread = true,
                    timestamp = System.currentTimeMillis() - 2400000
                ),
                StoryEntity(
                    username = "travel_girl",
                    avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=100",
                    imageUrl = "https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=600",
                    isUnread = true,
                    timestamp = System.currentTimeMillis() - 3600000
                )
            )
            for (story in defaultStories) {
                dao.insertStory(story)
            }
        }
    }
}
