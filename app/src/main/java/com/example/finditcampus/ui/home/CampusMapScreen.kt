package com.example.finditcampus.ui.home

import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finditcampus.theme.*
import com.example.finditcampus.data.GlobalState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusMapScreen(
    onBack: () -> Unit,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredItems = GlobalState.items.filter { 
        searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true) 
    }

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    val transformState = androidx.compose.foundation.gestures.rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        offset += offsetChange
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE8ECEF)) // Map background color
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = transformState)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        ) {
            val mapAspect = 946f / 1024f
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val screenWidth = maxWidth
                val screenHeight = maxHeight
                val screenAspect = screenWidth / screenHeight
                
                val actualMapWidth = if (screenAspect > mapAspect) screenWidth else screenHeight * mapAspect
                val actualMapHeight = if (screenAspect > mapAspect) screenWidth / mapAspect else screenHeight

                Box(modifier = Modifier.requiredSize(actualMapWidth, actualMapHeight)) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.finditcampus.R.drawable.campus_map_clean),
                        contentDescription = "Campus Map",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.FillBounds
                    )

                    filteredItems.forEach { item ->
                        val campusLocation = com.example.finditcampus.data.CampusData.locations.find { it.name.equals(item.location, ignoreCase = true) }
                        val targetX = campusLocation?.x ?: item.lngOffset
                        val targetY = campusLocation?.y ?: item.latOffset

                        Box(
                            modifier = Modifier
                                .offset(
                                    x = actualMapWidth * targetX,
                                    y = actualMapHeight * targetY
                                )
                                .layout { measurable, constraints ->
                                    val placeable = measurable.measure(constraints)
                                    layout(0, 0) {
                                        // Anchor to bottom center
                                        placeable.placeRelative(-placeable.width / 2, -placeable.height)
                                    }
                                }
                                .clickable { onItemClick() }
                        ) {
                            Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.size(32.dp, 40.dp)) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = item.title,
                                    tint = if (item.type == "lost") StatusRed else PrimaryOrange,
                                    modifier = Modifier.size(40.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                    }
                }
            }
        } // Closes transformable box

        // Top Search Bar and Back Button Overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 20.dp, end = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDark)
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .weight(1f)
                        .shadow(4.dp, RoundedCornerShape(24.dp)),
                    placeholder = { Text("Search map...", color = TextGray) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = PrimaryOrange
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
            }
        }
    }
}










