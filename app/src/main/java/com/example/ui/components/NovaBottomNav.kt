package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import com.example.ui.theme.NovaBluePrimary
import com.example.ui.theme.NovaNaraPurple

data class NavItem(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun NovaBottomNav(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem("Home", Screen.Dashboard.route, Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
        NavItem("Files", Screen.Files.route, Icons.Filled.Folder, Icons.Outlined.Folder, "nav_files"),
        NavItem("Tools", Screen.Tools.route, Icons.Filled.Build, Icons.Outlined.Build, "nav_tools"),
        NavItem("NARA AI", Screen.NaraHub.route, Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_nara"),
        NavItem("Settings", Screen.Settings.route, Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
    )

    NavigationBar(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("nova_bottom_navigation"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        onNavigate(item.route)
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title,
                        tint = if (isSelected) {
                            if (item.route == Screen.NaraHub.route) NovaNaraPurple else NovaBluePrimary
                        } else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 11.sp,
                        color = if (isSelected) {
                            if (item.route == Screen.NaraHub.route) NovaNaraPurple else NovaBluePrimary
                        } else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = if (item.route == Screen.NaraHub.route) {
                        NovaNaraPurple.copy(alpha = 0.15f)
                    } else {
                        NovaBluePrimary.copy(alpha = 0.12f)
                    }
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}
