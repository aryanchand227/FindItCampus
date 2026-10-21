package com.example.finditcampus.ui.home
import androidx.compose.animation.animateContentSize

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.*
import com.example.finditcampus.data.MockItem
import com.example.finditcampus.data.GlobalState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finditcampus.theme.*

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.finditcampus.ui.matches.MatchesScreen
import com.example.finditcampus.ui.messages.MessagesListScreen
import com.example.finditcampus.ui.profile.ProfileScreen

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToPost: () -> Unit = {},
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToItemDetail: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToMap: () -> Unit = {}
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }
        androidx.compose.runtime.LaunchedEffect(Unit) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showNotifications by rememberSaveable { mutableStateOf(false) }

    if (showNotifications) {
        NotificationsScreen(onBackClick = { showNotifications = false })
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            bottomBar = { FindItBottomNav(selectedTab) { selectedTab = it } },
            floatingActionButton = {
                if (selectedTab == 0) { // Only show FAB on home feed
                    ExtendedFloatingActionButton(
                        onClick = onNavigateToPost,
                        containerColor = PrimaryOrange,
                        contentColor = Color.White,
                        icon = { Icon(Icons.Default.Add, contentDescription = "Post item") },
                        text = { Text("Post item", fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(24.dp)
                    )
                }
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (selectedTab) {
                    0 -> HomeFeedContent(
                        onProfileClick = { selectedTab = 3 },
                        onNotificationsClick = { showNotifications = true },
                        onItemClick = onNavigateToItemDetail,
                        onMapClick = onNavigateToMap,
                        modifier = modifier
                    )
                    1 -> MatchesScreen(modifier)
                    2 -> MessagesListScreen(onNavigateToChat = onNavigateToChat, modifier = modifier)
                    3 -> ProfileScreen(onSettingsClick = onNavigateToSettings, modifier = modifier)
                }
            }
        }
    }
} 

@Composable
fun HomeFeedContent(
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onItemClick: () -> Unit = {},
    onMapClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var reportType by remember { mutableStateOf(GlobalState.lastPostedType) }
    var selectedFilter by remember { mutableStateOf("All") }
    
    val filteredItems = GlobalState.items.filter { 
        it.type == reportType && 
        (selectedFilter == "All" || it.category == selectedFilter) &&
        (searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true))
    }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { HomeHeader(onProfileClick = onProfileClick, onNotificationsClick = onNotificationsClick) }
        item { CampusSignalBanner() }
        item { 
            SearchBar(
                query = searchQuery, 
                onQueryChange = { searchQuery = it },
                onMapClick = onMapClick
            ) 
        }
        item { 
            TypeToggle(
                selectedType = reportType, 
                onTypeChange = { reportType = it }
            ) 
        }
        item { 
            FilterChips(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            ) 
        }
        item { FeedHeader(title = if (reportType == "found") "Found reports" else "Lost reports", count = filteredItems.size) }
        
        items(filteredItems, key = { it.id }) { itemData ->
            FeedItemCard(itemData = itemData, onClick = {
                GlobalState.selectedItem = itemData
                onItemClick()
            })
        }
        
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun HomeHeader(onProfileClick: () -> Unit = {}, onNotificationsClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "LOVELY PROFESSIONAL UNIVERSITY",
                color = PrimaryOrangeDark,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = "FindIt Campus",
                color = TextDark,
                fontWeight = FontWeight.Black,
                fontSize = 32.sp
            )
            Text(
                text = "A little help goes a long way.",
                color = TextGray,
                fontSize = 14.sp
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SurfaceBeige)
                    .clickable { onNotificationsClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = PrimaryOrangeDark)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SurfaceBeige)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Person, contentDescription = "Profile", tint = PrimaryOrangeDark)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    FindItCampusTheme {
        HomeScreen()
    }
}


















@Composable
fun CampusSignalBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LightOrangeTint)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Inventory, contentDescription = null, tint = PrimaryOrange)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text("Campus Signal", fontWeight = FontWeight.Bold, color = PrimaryOrangeDark)
            Text("Help keep our campus connected.", color = PrimaryOrangeDark, fontSize = 12.sp)
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, onMapClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search items...", color = TextGray) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = TextGray) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceBeige,
                unfocusedContainerColor = SurfaceBeige,
                focusedBorderColor = PrimaryOrange,
                unfocusedBorderColor = Color.Transparent
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(PrimaryOrange)
                .clickable { onMapClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Map, contentDescription = "Map View", tint = Color.White)
        }
    }
}

@Composable
fun TypeToggle(selectedType: String, onTypeChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceBeige)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(if (selectedType == "found") PrimaryOrange else Color.Transparent)
                .clickable { onTypeChange("found") }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Found Items", fontWeight = FontWeight.Bold, color = if (selectedType == "found") Color.White else TextGray)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(if (selectedType == "lost") PrimaryOrange else Color.Transparent)
                .clickable { onTypeChange("lost") }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Lost Items", fontWeight = FontWeight.Bold, color = if (selectedType == "lost") Color.White else TextGray)
        }
    }
}

@Composable
fun FilterChips(selectedFilter: String, onFilterSelected: (String) -> Unit) {
    val filters = listOf("All", "Electronics", "IDs & Cards", "Keys", "Wallet/Purse", "Books/Notes", "Others")
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { filter ->
            val isSelected = selectedFilter == filter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) PrimaryOrangeDark else SurfaceBeige)
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = filter,
                    color = if (isSelected) Color.White else TextGray,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun FeedHeader(title: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextDark)
        Text("$count items", color = TextGray, fontSize = 14.sp)
    }
}

@Composable
fun FeedItemCard(itemData: MockItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, SurfaceBeige, RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceBeige),
            contentAlignment = Alignment.Center
        ) {
            if (itemData.imageUrl != null && itemData.imageUrl.isNotEmpty()) {
                coil.compose.AsyncImage(
                    model = itemData.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Icon(Icons.Outlined.Inventory, contentDescription = null, tint = PrimaryOrangeDark)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(itemData.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
            Text(itemData.location, color = TextGray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(itemData.time, color = PrimaryOrange, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun FindItBottomNav(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = PrimaryOrangeDark,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryOrange,
                selectedTextColor = PrimaryOrange,
                indicatorColor = AppBackground,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray
            )
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Matches") },
            label = { Text("Matches", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryOrange,
                selectedTextColor = PrimaryOrange,
                indicatorColor = AppBackground,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray
            )
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = { Icon(Icons.AutoMirrored.Filled.Message, contentDescription = "Messages") },
            label = { Text("Messages", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryOrange,
                selectedTextColor = PrimaryOrange,
                indicatorColor = AppBackground,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray
            )
        )
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            icon = { Icon(Icons.Outlined.Person, contentDescription = "Profile") },
            label = { Text("Profile", fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryOrange,
                selectedTextColor = PrimaryOrange,
                indicatorColor = AppBackground,
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray
            )
        )
    }
}
