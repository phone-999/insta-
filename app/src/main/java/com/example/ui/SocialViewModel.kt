package com.example.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.api.GeminiApiClient
import com.example.database.PostEntity
import com.example.database.SocialRepository
import com.example.database.StoryEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SocialViewModel(
    application: Application,
    private val repository: SocialRepository
) : AndroidViewModel(application) {

    // Retrieve live updates from local offline database
    val posts: StateFlow<List<PostEntity>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stories: StateFlow<List<StoryEntity>> = repository.allStories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state states
    private val _activeStory = MutableStateFlow<StoryEntity?>(null)
    val activeStory: StateFlow<StoryEntity?> = _activeStory.asStateFlow()

    private val _currentTab = MutableStateFlow("home") // "home", "add_post", "profile"
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // Caption generation state
    private val _generatedCaption = MutableStateFlow("")
    val generatedCaption: StateFlow<String> = _generatedCaption.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    init {
        viewModelScope.launch {
            // Seed defaults if fresh installation
            repository.seedIfNeeded()
        }
    }

    fun selectTab(tab: String) {
        _currentTab.value = tab
    }

    // Like a post and update local Room DB
    fun toggleLikePost(post: PostEntity) {
        viewModelScope.launch {
            val updated = post.copy(
                isLiked = !post.isLiked,
                likesCount = if (post.isLiked) post.likesCount - 1 else post.likesCount + 1
            )
            repository.updatePost(updated)
        }
    }

    // View story and mark as read
    fun openStory(story: StoryEntity) {
        _activeStory.value = story
        if (story.isUnread) {
            viewModelScope.launch {
                val updated = story.copy(isUnread = false)
                repository.updateStory(updated)
            }
        }
    }

    fun closeStory() {
        _activeStory.value = null
    }

    // Call Gemini to write caption
    fun generateAIVibeCaption(topic: String, styleVibe: String) {
        viewModelScope.launch {
            if (topic.isBlank()) {
                _generatedCaption.value = "Please type a topic first (e.g. delicious espresso) so Gemini can draft it!"
                return@launch
            }
            _isGenerating.value = true
            val draft = GeminiApiClient.generateCaption(topic, styleVibe)
            _generatedCaption.value = draft
            _isGenerating.value = false
        }
    }

    fun clearGeneratedCaption() {
        _generatedCaption.value = ""
    }

    // Submit new post to Room DB and navigate home
    fun createPost(username: String, imageUrl: String, caption: String, location: String?) {
        viewModelScope.launch {
            val newPost = PostEntity(
                username = username.ifBlank { "anonymous_creator" },
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100",
                imageUrl = imageUrl,
                caption = caption,
                likesCount = 0,
                isLiked = false,
                timestamp = System.currentTimeMillis(),
                location = location?.ifBlank { null }
            )
            repository.insertPost(newPost)
            _currentTab.value = "home"
        }
    }

    // Create a new story
    fun addStory(username: String, imageUrl: String) {
        viewModelScope.launch {
            val newStory = StoryEntity(
                username = username.ifBlank { "My Story" },
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100",
                imageUrl = imageUrl,
                isUnread = true,
                timestamp = System.currentTimeMillis()
            )
            repository.insertStory(newStory)
        }
    }

    // Delete post helper
    fun deletePost(post: PostEntity) {
        viewModelScope.launch {
            repository.deletePost(post.id)
        }
    }

    // Clean database to seed again
    fun resetDatabase() {
        viewModelScope.launch {
            repository.clearAll()
            repository.seedIfNeeded()
        }
    }
}

class SocialViewModelFactory(
    private val application: Application,
    private val repository: SocialRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SocialViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SocialViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
