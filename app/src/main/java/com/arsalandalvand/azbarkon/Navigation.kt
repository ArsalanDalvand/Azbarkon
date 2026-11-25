package com.arsalandalvand.azbarkon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Folder // ایمپورت آیکون توپر
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Folder // ایمپورت آیکون توخالی
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arsalandalvand.azbarkon.ui.theme.VazirmatnFontFamily

enum class AppDestinations(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", "خانه", Icons.Filled.Home, Icons.Outlined.Home),
    ALL_POEMS("all_poems", "گنجینه", Icons.Filled.LibraryBooks, Icons.Outlined.LibraryBooks),
    // تب جدید مدیریت تگ‌ها
    TAGS("tags", "دسته‌ها", Icons.Filled.Folder, Icons.Outlined.Folder),

    MY_POEMS("notes_list", "نوشتار", Icons.Filled.EditNote, Icons.Outlined.EditNote),
    SUGGESTIONS("suggestions", "پیشنهاد", Icons.Filled.TipsAndUpdates, Icons.Outlined.TipsAndUpdates)
}

@Composable
fun AppBottomNavigation(
    currentScreen: String,
    onScreenSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        AppDestinations.values().forEach { screen ->
            val isSelected = currentScreen == screen.route

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.title
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = VazirmatnFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    )
                },
                selected = isSelected,
                onClick = { onScreenSelected(screen.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}