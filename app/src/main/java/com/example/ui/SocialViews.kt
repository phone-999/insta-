package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.database.PostEntity
import com.example.database.StoryEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Instagram Style Gradient for unread stories
val InstaGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFF99D1C), // Yellow/Orange
        Color(0xFFE20338), // Red
        Color(0xFFC6007A)  // Purple/Pink
    )
)

val SlateGray = Color(0xFF1E2124)
val SurfaceBlack = Color(0xFF121212)
val BorderGray = Color(0xFF2E2E2E)

@Composable
fun SocialMainScreen(viewModel: SocialViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val activeStory by viewModel.activeStory.collectAsState()

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceBlack,
                tonalElevation = 8.dp,
                modifier = Modifier.height(72.dp)
            ) {
                NavigationBarItem(
                    selected = currentTab == "home",
                    onClick = { viewModel.selectTab("home") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(26.dp)) },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFC6007A),
                        unselectedIconColor = Color.LightGray,
                        selectedTextColor = Color(0xFFC6007A),
                        unselectedTextColor = Color.LightGray,
                        indicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.testTag("nav_home")
                )
                NavigationBarItem(
                    selected = currentTab == "add_post",
                    onClick = { viewModel.selectTab("add_post") },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Create", modifier = Modifier.size(26.dp)) },
                    label = { Text("Post", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFC6007A),
                        unselectedIconColor = Color.LightGray,
                        selectedTextColor = Color(0xFFC6007A),
                        unselectedTextColor = Color.LightGray,
                        indicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.testTag("nav_add_post")
                )
                NavigationBarItem(
                    selected = currentTab == "profile",
                    onClick = { viewModel.selectTab("profile") },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile", modifier = Modifier.size(26.dp)) },
                    label = { Text("Profile", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFC6007A),
                        unselectedIconColor = Color.LightGray,
                        selectedTextColor = Color(0xFFC6007A),
                        unselectedTextColor = Color.LightGray,
                        indicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.testTag("nav_profile")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "home" -> HomeFeedScreen(viewModel = viewModel)
                "add_post" -> AddPostScreen(viewModel = viewModel)
                "profile" -> UserProfileScreen(viewModel = viewModel)
            }

            // High Fidelity Overlay Story Viewer Modal
            activeStory?.let { story ->
                StoryViewerModal(
                    story = story,
                    onDismiss = { viewModel.closeStory() }
                )
            }
        }
    }
}

@Composable
fun HomeFeedScreen(viewModel: SocialViewModel) {
    val posts by viewModel.posts.collectAsState()
    val stories by viewModel.stories.collectAsState()
    var showAddStoryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceBlack)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Insta",
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color.White,
                modifier = Modifier.testTag("app_logo")
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(onClick = { viewModel.resetDatabase() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Feed",
                        tint = Color.White
                    )
                }
            }
        }

        Divider(color = BorderGray, thickness = 0.5.dp)

        // Stories Row (Horizontal Scroll)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceBlack)
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "Add Story" cell
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { showAddStoryDialog = true }
                        .testTag("add_story_cell")
                        .padding(start = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(BorderGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Story Button",
                            tint = Color.LightGray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Add Story",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // User stories list
            items(stories) { story ->
                StoryCircleItem(story = story, onClick = { viewModel.openStory(story) })
            }
        }

        Divider(color = BorderGray, thickness = 0.5.dp)

        // Posts Feed
        if (posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFFC6007A))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Loading feed database...", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(posts, key = { it.id }) { post ->
                    val coroutineScope = rememberCoroutineScope()
                    PostCard(
                        post = post,
                        onLikeClicked = { viewModel.toggleLikePost(post) },
                        onDeleteClicked = { viewModel.deletePost(post) }
                    )
                }
            }
        }
    }

    if (showAddStoryDialog) {
        AddStoryDialog(
            onDismiss = { showAddStoryDialog = false },
            onAddStory = { url ->
                viewModel.addStory("My Story", url)
                showAddStoryDialog = false
            }
        )
    }
}

@Composable
fun StoryCircleItem(story: StoryEntity, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("story_item_${story.username}")
    ) {
        val ringBrush = if (story.isUnread) InstaGradient else Brush.linearGradient(listOf(BorderGray, BorderGray))
        Box(
            modifier = Modifier
                .size(68.dp)
                .background(ringBrush, shape = CircleShape)
                .padding(2.5.dp) // Ring separation
                .background(Color.Black, shape = CircleShape)
                .padding(3.dp) // Space inside ring
                .clip(CircleShape)
        ) {
            AsyncImage(
                model = story.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100" },
                contentDescription = "${story.username} avatar",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = story.username,
            color = if (story.isUnread) Color.White else Color.Gray,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(68.dp),
            fontWeight = if (story.isUnread) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun PostCard(
    post: PostEntity,
    onLikeClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    var showBigHeart by remember { mutableStateOf(false) }
    val heartScale = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceBlack),
        shape = RoundedCornerShape(0.dp) // Frame corners like proper Insta
    ) {
        Column {
            // Card Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Profile & username info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.DarkGray, CircleShape)
                    ) {
                        AsyncImage(
                            model = post.avatarUrl,
                            contentDescription = "${post.username} thumbnail",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = post.username,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        post.location?.let { loc ->
                            Text(
                                text = loc,
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Delete option for post control
                IconButton(
                    onClick = onDeleteClicked,
                    modifier = Modifier.testTag("delete_post_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Post",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Card Image with Double-Tap Heart Support
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Square feed aspect
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (!post.isLiked) {
                                    onLikeClicked()
                                }
                                showBigHeart = true
                                coroutineScope.launch {
                                    heartScale.snapTo(0f)
                                    heartScale.animateTo(
                                        targetValue = 1.3f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                    delay(500)
                                    heartScale.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(durationMillis = 150)
                                    )
                                    showBigHeart = false
                                }
                            },
                            onTap = {}
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = "Post image content",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Render Double Tap Big Pink Heart Popup
                if (showBigHeart) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Double tapped heart animated effect",
                        tint = Color(0xFFC6007A).copy(alpha = 0.9f),
                        modifier = Modifier
                            .size(100.dp)
                            .scale(heartScale.value)
                    )
                }
            }

            // Feed Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onLikeClicked) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Heart Like Button",
                        tint = if (post.isLiked) Color(0xFFE20338) else Color.White,
                        modifier = Modifier
                            .size(26.dp)
                            .testTag("post_like_${post.id}")
                    )
                }
                IconButton(onClick = { /* Non-functional aesthetic icons for completeness */ }) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Comments",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = { /* Non-functional aesthetic share icon */ }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Likes count, caption text, and timestamp
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${post.likesCount} likes",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${post.username}  ",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = post.caption,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                // Time elapsed calculation (simplified)
                val diffHrs = (System.currentTimeMillis() - post.timestamp) / 3600000
                val timeLabel = when {
                    diffHrs < 1 -> "Just now"
                    diffHrs == 1L -> "1 hour ago"
                    diffHrs < 24 -> "$diffHrs hours ago"
                    else -> "${diffHrs / 24} days ago"
                }
                Text(
                    text = timeLabel,
                    color = Color.DarkGray,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun StoryViewerModal(
    story: StoryEntity,
    onDismiss: () -> Unit
) {
    var progress by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()

    // Slide progress bar and auto-close story after 5s
    LaunchedEffect(story.id) {
        progress = 0f
        val duration = 5000L // 5 seconds
        val steps = 100
        val stepDelay = duration / steps
        for (i in 1..steps) {
            delay(stepDelay)
            progress = i.toFloat() / steps
        }
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("story_modal_screen")
        ) {
            // Full screen story image content
            AsyncImage(
                model = story.imageUrl,
                contentDescription = "Story contents",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onDismiss() },
                contentScale = ContentScale.Crop
            )

            // Top Gradient Cover for visibility of progress/profile
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.8f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 12.dp, end = 12.dp)
            ) {
                // Interactive Progress bar lines like proper Instagram Stories
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Profile and details
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.White, CircleShape)
                    ) {
                        AsyncImage(
                            model = story.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100" },
                            contentDescription = "${story.username} avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = story.username,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close story screen",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddPostScreen(viewModel: SocialViewModel) {
    var topicInput by remember { mutableStateOf("") }
    var captionInput by remember { mutableStateOf("") }
    var locationInput by remember { mutableStateOf("") }
    var selectedVibe by remember { mutableStateOf("Chill ☕") }

    // List of dynamic photorealistic sample content to publish
    val presets = listOf(
        "Modern Office Work" to "https://images.unsplash.com/photo-1497366216548-37526070297c?w=600",
        "Cozy Espresso Cafe" to "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=600",
        "Spectacular Alps Peaks" to "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=600",
        "Woodfire Baking Sourdough" to "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600",
        "Glowing City Neon Lights" to "https://images.unsplash.com/photo-1519501025264-65ba15a82390?w=600",
        "Sunset Coast Ocean Wave" to "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600"
    )

    var selectedImageIndex by remember { mutableIntStateOf(0) }
    val generatedCaption by viewModel.generatedCaption.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("add_post_screen")
    ) {
        Text(
            text = "Create New Post",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Choose photorealistic post imagery:",
            color = Color.Gray,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Row of Unsplash Image Presets
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(presets.size) { index ->
                val preset = presets[index]
                val border = if (selectedImageIndex == index) {
                    BorderStroke(2.5.dp, Color(0xFFC6007A))
                } else null

                Card(
                    modifier = Modifier
                        .size(100.dp)
                        .clickable { selectedImageIndex = index }
                        .testTag("preset_img_$index"),
                    border = border,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = preset.second,
                            contentDescription = preset.first,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = preset.first,
                                color = Color.White,
                                fontSize = 8.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = presets[selectedImageIndex].second,
                contentDescription = "Selected Imagery preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Location Info
        OutlinedTextField(
            value = locationInput,
            onValueChange = { locationInput = it },
            label = { Text("Add Location (Optional)") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("location_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFC6007A),
                unfocusedBorderColor = BorderGray,
                focusedLabelColor = Color(0xFFC6007A),
                unfocusedLabelColor = Color.Gray
            ),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = Color.Gray) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // AI Caption Drafting Companion Block
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateGray),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Gemini",
                        tint = Color(0xFFC6007A),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Draft Captions with Gemini AI",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Topic Input box
                OutlinedTextField(
                    value = topicInput,
                    onValueChange = { topicInput = it },
                    placeholder = { Text("Topic (e.g., fresh artisan sourdough pizza out oven)", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_topic_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFC6007A),
                        unfocusedBorderColor = BorderGray
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Select caption vibes
                val styleOptions = listOf("Chill ☕", "Energetic ⚡", "Poetic 🌸", "Minimalist ☁️")
                Text("Select Vibe:", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    styleOptions.forEach { vibe ->
                        val isSel = selectedVibe == vibe
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Color(0xFFC6007A) else BorderGray)
                                .clickable { selectedVibe = vibe }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(vibe, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Call Button
                Button(
                    onClick = { viewModel.generateAIVibeCaption(topicInput, selectedVibe) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("ai_generate_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isGenerating
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Draft Caption", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // AI Response box
                if (generatedCaption.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(text = generatedCaption, color = Color.White, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { viewModel.clearGeneratedCaption() },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Dismiss", color = Color.Gray, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Button(
                                    onClick = {
                                        captionInput = generatedCaption
                                        viewModel.clearGeneratedCaption()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC6007A)),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Apply Vibe", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Final Caption TextField
        OutlinedTextField(
            value = captionInput,
            onValueChange = { captionInput = it },
            label = { Text("Write Caption...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .testTag("caption_input_content"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFC6007A),
                unfocusedBorderColor = BorderGray,
                focusedLabelColor = Color(0xFFC6007A),
                unfocusedLabelColor = Color.Gray
            ),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Create Button
        Button(
            onClick = {
                viewModel.createPost(
                    username = "insta_artist",
                    imageUrl = presets[selectedImageIndex].second,
                    caption = captionInput,
                    location = locationInput
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("publish_post_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC6007A)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Publish to Insta", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun UserProfileScreen(viewModel: SocialViewModel) {
    val posts by viewModel.posts.collectAsState()
    val stories by viewModel.stories.collectAsState()

    // Filter only current user posts (e.g. ones with username containing artist or anonymous)
    val userPosts = posts.filter { it.username == "insta_artist" || it.username == "insta_posts" || it.username == "My Story" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("profile_screen")
    ) {
        // User Profile Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFFC6007A), CircleShape)
            ) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100",
                    contentDescription = "Active user avatar photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${posts.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Posts", color = Color.Gray, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "142K", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Followers", color = Color.Gray, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "382", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Following", color = Color.Gray, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Username & bio
        Text(text = "insta_artist", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(text = "Digital Creator & Coding Artisan 🌸💻", color = Color.LightGray, fontSize = 13.sp)
        Text(text = "Capturing moments offline, syncing live with Gemini.", color = Color.Gray, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(20.dp))

        Divider(color = BorderGray, thickness = 0.5.dp)

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "My Gallery Grid",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Posts visual grid representation
        if (posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No posts made yet.", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            // Display all posts in a custom grid-wrap layout
            val chunks = posts.chunked(3)
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                chunks.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (i in 0..2) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(BorderGray)
                            ) {
                                if (i < rowItems.size) {
                                    AsyncImage(
                                        model = rowItems[i].imageUrl,
                                        contentDescription = "Profile grid post",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStoryDialog(
    onDismiss: () -> Unit,
    onAddStory: (String) -> Unit
) {
    val presets = listOf(
        "Cosmic Sunset" to "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600",
        "Sparkling Beach Wave" to "https://images.unsplash.com/photo-1473116763269-25597c90cf21?w=600",
        "Neon Arcades" to "https://images.unsplash.com/photo-1538481199705-c710c4e965fc?w=600",
        "Coffee Cup Art" to "https://images.unsplash.com/photo-1541167760496-1628856ab772?w=600"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish a Story", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select spectacular visual moments for story view:", color = Color.LightGray, fontSize = 12.sp)

                presets.forEach { preset ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAddStory(preset.second) }
                            .background(SlateGray, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(6.dp))
                        ) {
                            AsyncImage(
                                model = preset.second,
                                contentDescription = preset.first,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(preset.first, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFFC6007A))
            }
        },
        containerColor = SurfaceBlack,
        shape = RoundedCornerShape(12.dp)
    )
}
