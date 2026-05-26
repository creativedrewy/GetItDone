package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.data.db.AppDatabase
import com.example.data.repository.ActivityRepository
import com.example.reminder.ReminderScheduler
import com.example.ui.screens.ActivityTrackerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ActivityViewModel
import com.example.ui.viewmodel.ActivityViewModelFactory

class MainActivity : ComponentActivity() {

    // Request Notification permission (Android 13+ / API 33+)
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Reminders notification channel active!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                this, 
                "Reminders permission was declined. Please enable in Settings to receive activity alerts.", 
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Initialize notification channels
        ReminderScheduler.createNotificationChannel(applicationContext)

        // 2. Request permission if needed
        checkAndRequestNotificationPermission()

        // 3. Initialize Database and Repositories
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ActivityRepository(database.categoryDao(), database.logDao())

        // 4. Instantiate ViewModel with factory
        val viewModel: ActivityViewModel by viewModels {
            ActivityViewModelFactory(repository)
        }

        enableEdgeToEdge()
        setContent {
            val context = androidx.compose.ui.platform.LocalContext.current
            viewModel.initPrefs(context)
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            MyApplicationTheme(
                darkTheme = isDarkTheme,
                dynamicColor = false // Keep consistent beautiful custom color design
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    ActivityTrackerScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val status = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (status != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
