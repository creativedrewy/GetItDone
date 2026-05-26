package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ActivityCategory
import com.example.data.model.ActivityLog
import com.example.ui.viewmodel.ActivityViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// UI Color Options
val ColorOptions = listOf(
    "#2196F3" to Color(0xFF2196F3), // Sky Blue
    "#4CAF50" to Color(0xFF4CAF50), // Emerald Green
    "#9C27B0" to Color(0xFF9C27B0), // Purple
    "#FF9800" to Color(0xFFFF9800), // Orange
    "#E91E63" to Color(0xFFE91E63), // Pink
    "#009688" to Color(0xFF009688), // Teal
    "#FFEB3B" to Color(0xFFFFEB3B)  // Yellow
)

// UI Icon Options
val IconOptions = listOf(
    "water_drop" to Icons.Default.LocalDrink,
    "fitness_center" to Icons.Default.FitnessCenter,
    "menu_book" to Icons.Default.MenuBook,
    "check_circle" to Icons.Default.CheckCircle,
    "laptop" to Icons.Default.Laptop,
    "star" to Icons.Default.Star
)

fun getIconByName(name: String): ImageVector {
    return IconOptions.find { it.first == name }?.second ?: Icons.Default.Star
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityTrackerScreen(
    viewModel: ActivityViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val logsToday by viewModel.logsToday.collectAsStateWithLifecycle()
    val allLogs by viewModel.allLogs.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<ActivityCategory?>(null) }
    var showManualLogDialogByCategoryId by remember { mutableStateOf<Int?>(null) }

    // Standard timer ticking to update values smoothly live of active timing categories
    var timeTicker by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(key1 = true) {
        while (true) {
            delay(1000L)
            timeTicker = System.currentTimeMillis()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCategoryDialog = true },
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White,
                modifier = Modifier.testTag("add_category_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Category")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "GetItDone",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Track Tasks & Get Things Done",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }

                // Header Right Quick Reminder info or small UTC clock
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val formattedTime = timeFormat.format(Date(timeTicker))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
                            .size(36.dp)
                            .testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Toggle Theme",
                            tint = if (isDarkTheme) Color(0xFFFFD54F) else Color(0xFF78909C),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Time",
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = formattedTime,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dashboard Summary Card
                item {
                    DashboardSummary(categories = categories, logsToday = logsToday)
                }

                // Section Title
                item {
                    Text(
                        text = "YOUR ACTIVE GOALS",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }

                if (categories.isEmpty()) {
                    item {
                        EmptyCategoriesState { showAddCategoryDialog = true }
                    }
                } else {
                    items(categories, key = { it.id }) { category ->
                        CategoryCard(
                            category = category,
                            logsToday = logsToday.filter { it.categoryId == category.id },
                            currentTime = timeTicker,
                            onPlayClick = { viewModel.startTimer(category) },
                            onStopClick = { viewModel.stopTimer(context, category) },
                            onLogInstantClick = { viewModel.logQuickInstant(category) },
                            onManualLogClick = { showManualLogDialogByCategoryId = category.id },
                            onEditClick = { categoryToEdit = category },
                            onDeleteClick = { viewModel.deleteCategory(context, category) }
                        )
                    }
                }

                // High balance bottom spacing so floating button doesn't cover elements
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
        AddEditCategoryDialog(
            category = null,
            onDismiss = { showAddCategoryDialog = false },
            onSave = { name, icon, color, isRemind, times, interval ->
                viewModel.addCategory(context, name, icon, color, isRemind, times, interval)
                showAddCategoryDialog = false
                Toast.makeText(context, "Category '$name' added successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Edit Category Dialog
    categoryToEdit?.let { category ->
        AddEditCategoryDialog(
            category = category,
            onDismiss = { categoryToEdit = null },
            onSave = { name, icon, color, isRemind, times, interval ->
                val updated = category.copy(
                    name = name,
                    iconName = icon,
                    colorHex = color,
                    isReminderEnabled = isRemind,
                    timesPerDay = times,
                    intervalHours = interval
                )
                viewModel.updateCategory(context, updated)
                categoryToEdit = null
                Toast.makeText(context, "Category revised successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Manual Log Duration Entry Dialog
    showManualLogDialogByCategoryId?.let { categoryId ->
        val selectedCategory = categories.find { it.id == categoryId }
        selectedCategory?.let { category ->
            ManualLogDialog(
                category = category,
                onDismiss = { showManualLogDialogByCategoryId = null },
                onSave = { durationMinutes ->
                    val endTime = System.currentTimeMillis()
                    val startTime = endTime - (durationMinutes * 60 * 1000)
                    viewModel.logCustomDuration(category.id, Date(endTime), startTime, endTime)
                    showManualLogDialogByCategoryId = null
                    Toast.makeText(context, "Log logged retroactively!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun DashboardSummary(
    categories: List<ActivityCategory>,
    logsToday: List<ActivityLog>
) {
    // Elegant radial Canvas indicator inside a nice glass-like ElevationCard
    val totalGoalTimes = categories.sumOf { it.timesPerDay }
    val totalLoggedTimes = logsToday.size
    
    // Calculate total minutes tracked today
    var totalMinutesTracked = 0L
    for (log in logsToday) {
        val delta = log.endTime - log.startTime
        if (delta > 0) {
            totalMinutesTracked += (delta / (1000 * 60))
        }
    }

    val completionRatio = if (totalGoalTimes > 0) totalLoggedTimes.toFloat() / totalGoalTimes.toFloat() else 0f
    val progressColor = Color(0xFF2196F3)
    val trackColor = if (MaterialTheme.colorScheme.background == Color(0xFF141416)) Color(0xFF2E2E36) else Color(0xFFDFE2E6)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.5f)) {
                Text(
                    text = "TODAY'S SUMMARY",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Daily Productivity",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Logged",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$totalLoggedTimes tracks log done",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Tracked Minutes",
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$totalMinutesTracked total mins tracked",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                }
            }

            // Radial canvas progress display
            Box(
                modifier = Modifier
                    .weight(1f)
                    .size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(84.dp)) {
                    // Gray background track
                    drawCircle(
                        color = trackColor,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Active progression
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Color(0xFF2196F3), Color(0xFF00BCD4), Color(0xFF2196F3))
                        ),
                        startAngle = -90f,
                        sweepAngle = (completionRatio * 360f).coerceIn(0f, 360f),
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val percent = (completionRatio * 100).toInt().coerceAtMost(100)
                    Text(
                        text = "$percent%",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Goal",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyCategoriesState(onCreateClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Empty",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Goals Active Yet",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add goals (e.g., Drink Water, Gym, Coding) to log your progress and set up recurring intervals.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onCreateClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("create_first_category_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Your First Category", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: ActivityCategory,
    logsToday: List<ActivityLog>,
    currentTime: Long,
    onPlayClick: () -> Unit,
    onStopClick: () -> Unit,
    onLogInstantClick: () -> Unit,
    onManualLogClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val isTimerActive = category.currentSessionStartTime > 0L
    val categoryColor = remember(category.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(category.colorHex))
        } catch (e: Exception) {
            Color(0xFF2196F3)
        }
    }

    // Dynamic timer text calculation (active hours, minutes, seconds)
    val timerString = if (isTimerActive) {
        val totalSec = ((currentTime - category.currentSessionStartTime) / 1000).coerceAtLeast(0)
        val hrs = totalSec / 3600
        val mins = (totalSec % 3600) / 60
        val secs = totalSec % 60
        if (hrs > 0) {
            String.format("%02d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format("%02d:%02d", mins, secs)
        }
    } else {
        ""
    }

    // Subtly animating light indicators if active
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("category_card_${category.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Main Card Summary Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1.2f)
                ) {
                    // Category Circle Icon with selected color
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(categoryColor.copy(alpha = 0.15f), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getIconByName(category.iconName),
                            contentDescription = category.name,
                            tint = categoryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = category.name,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Today: ${logsToday.size} times done",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                            if (category.timesPerDay > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "•  Goal ${category.timesPerDay}x",
                                    color = if (logsToday.size >= category.timesPerDay) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontSize = 12.sp,
                                    fontWeight = if (logsToday.size >= category.timesPerDay) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Active Time Offset or Toggles
                if (isTimerActive) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.weight(0.8f)
                    ) {
                        // Pulsing Recording Dot
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Red.copy(alpha = pulseAlpha), shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = timerString,
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Icon(
                        imageVector = if (expanded) Icons.Default.Close else Icons.Default.Info,
                        contentDescription = "Expand Status",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fast Logging control buttons (Inline actions)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isTimerActive) {
                    Button(
                        onClick = { onStopClick() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("stop_timer_button_${category.id}"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Stop track session", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = { onPlayClick() },
                        colors = ButtonDefaults.buttonColors(containerColor = categoryColor.copy(alpha = 0.2f), contentColor = categoryColor),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("start_timer_button_${category.id}"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start", tint = categoryColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start Timer", color = categoryColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = { onLogInstantClick() },
                        border = null,
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("instant_log_button_${category.id}"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Log", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Quick Log (10m)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Expanded detail items (Schedules, logs list, edit forms)
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp)
                ) {
                    Divider(color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Reminders scheduling report summary
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (category.isReminderEnabled) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = "Reminder Icon",
                                tint = if (category.isReminderEnabled) categoryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (category.isReminderEnabled) "Reminders active" else "Reminders currently off",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (category.isReminderEnabled) {
                                    Text(
                                        text = "Interval: every ${category.intervalHours} hours",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Edit Config shortcut
                        IconButton(onClick = onEditClick) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Adjust config", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Logs of completed tasks
                    Text(
                        text = "HISTORICAL TRACKS TODAY",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (logsToday.isEmpty()) {
                        Text(
                            text = "No instances tracked today yet.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 2.dp, top = 6.dp, bottom = 6.dp)
                        )
                    } else {
                        // Display list of logs completed today
                        logsToday.forEach { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Done",
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    val timeFmt = SimpleDateFormat("h:mm a", Locale.getDefault())
                                    val startStr = timeFmt.format(Date(log.startTime))
                                    val endStr = timeFmt.format(Date(log.endTime))
                                    val durationMin = (log.endTime - log.startTime) / (1000 * 60)
                                    
                                    Text(
                                        text = "$startStr - $endStr",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "($durationMin m)",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        fontSize = 11.sp
                                    )
                                }

                                // We can delete logs if need be
                                // Let's omit or just keep it simple
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Shortcut buttons list for other actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onManualLogClick,
                            colors = ButtonDefaults.textButtonColors(contentColor = categoryColor)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Custom Past Log", fontSize = 12.sp)
                            }
                        }

                        IconButton(onClick = onDeleteClick) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Category", tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditCategoryDialog(
    category: ActivityCategory?,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String, color: String, isRemind: Boolean, times: Int, interval: Float) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(category?.iconName ?: "water_drop") }
    var selectedColorHex by remember { mutableStateOf(category?.colorHex ?: "#2196F3") }
    var isReminderEnabled by remember { mutableStateOf(category?.isReminderEnabled ?: false) }
    var timesPerDay by remember { mutableStateOf(category?.timesPerDay ?: 5) }
    var intervalHours by remember { mutableStateOf(category?.intervalHours ?: 2.0f) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("add_edit_category_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = if (category == null) "CREATE GOAL" else "EDIT GOAL",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Name Input field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = Color(0xFF2196F3),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_name_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Select Icon Section
                Text(text = "Icon representation:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconOptions.forEach { option ->
                        val isSelected = selectedIcon == option.first
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedIcon = option.first },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = option.second,
                                contentDescription = option.first,
                                tint = if (isSelected) Color(0xFF2196F3) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Select Color Section
                Text(text = "Color designation:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ColorOptions.forEach { option ->
                        val isSelected = selectedColorHex == option.first
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(option.second, shape = CircleShape)
                                .clickable { selectedColorHex = option.first }
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White.copy(alpha = 0.4f), shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.Black,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(14.dp))

                // Reminder Configurations
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Enable Intervals Reminders", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                    }
                    Checkbox(
                        checked = isReminderEnabled,
                        onCheckedChange = { isReminderEnabled = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF2196F3),
                            uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("reminder_enabled_checkbox")
                    )
                }

                AnimatedVisibility(visible = isReminderEnabled) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Times per day slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Goal frequency:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
                            Text(text = "$timesPerDay times a day", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = timesPerDay.toFloat(),
                            onValueChange = { timesPerDay = it.toInt() },
                            valueRange = 1f..12f,
                            steps = 11,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF2196F3),
                                activeTrackColor = Color(0xFF2196F3)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Interval selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Interval spacing:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
                            Text(text = "Every $intervalHours hours", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = intervalHours,
                            onValueChange = { intervalHours = Math.round(it * 2f) / 2f }, // Snap to nearest 0.5 hours
                            valueRange = 0.5f..8.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF2196F3),
                                activeTrackColor = Color(0xFF2196F3)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (name.trim().isNotEmpty()) {
                                onSave(name, selectedIcon, selectedColorHex, isReminderEnabled, timesPerDay, intervalHours)
                            }
                        },
                        enabled = name.trim().isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("submit_category_button")
                    ) {
                        Text("Save Category", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ManualLogDialog(
    category: ActivityCategory,
    onDismiss: () -> Unit,
    onSave: (durationMinutes: Int) -> Unit
) {
    var durationText by remember { mutableStateOf("15") }
    val durationMin = durationText.toIntOrNull() ?: 15

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("manual_log_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "RECORD PAST DEED",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = category.name,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "How long did you do this activity?",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it.filter { char -> char.isDigit() } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = Color(0xFF2196F3),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .width(100.dp)
                            .testTag("manual_duration_input")
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Minutes logged done", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (durationMin > 0) {
                                onSave(durationMin)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("submit_manual_log_button")
                    ) {
                        Text("Log Completed Track", color = Color.White)
                    }
                }
            }
        }
    }
}
