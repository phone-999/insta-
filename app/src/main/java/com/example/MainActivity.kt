package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.database.AppDatabase
import com.example.database.SocialRepository
import com.example.ui.SocialMainScreen
import com.example.ui.SocialViewModel
import com.example.ui.SocialViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private lateinit var database: AppDatabase
  private lateinit var repository: SocialRepository
  private lateinit var viewModel: SocialViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize full SQLite Room DB
    database = Room.databaseBuilder(
      applicationContext,
      AppDatabase::class.java,
      "insta_posts.db"
    ).fallbackToDestructiveMigration().build()

    repository = SocialRepository(database)

    val factory = SocialViewModelFactory(application, repository)
    viewModel = ViewModelProvider(this, factory)[SocialViewModel::class.java]

    setContent {
      MyApplicationTheme(darkTheme = true) { // Force sleek Dark colors for post images contrast
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          SocialMainScreen(viewModel = viewModel)
        }
      }
    }
  }
}

