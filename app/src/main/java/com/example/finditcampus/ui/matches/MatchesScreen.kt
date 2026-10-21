package com.example.finditcampus.ui.matches

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finditcampus.theme.*
import com.example.finditcampus.data.MatchResult
import com.example.finditcampus.data.GlobalState
import com.example.finditcampus.data.findMatches

val MintBackground = Color(0xFFF2F8F8)

@Composable
fun MatchesScreen(modifier: Modifier = Modifier) {
    val matches = findMatches(GlobalState.items)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MintBackground)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { Spacer(modifier = Modifier.height(32.dp)) }
        item { MatchesHeader() }
        item { InfoCard(matches.size) }
        
        items(matches) { match ->
            MatchCard(match)
        }
        
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun MatchesHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "SMART VISION",
                color = PrimaryOrangeDark,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Your matches",
                color = TextDark,
                fontWeight = FontWeight.Black,
                fontSize = 36.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "A quiet watchlist powered by your item tags and campus reports.",
                color = TextGray,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(SurfaceBeige),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryOrangeDark, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun InfoCard(matchCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceBeige)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(PrimaryOrange),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Timeline, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text("$matchCount possible matches", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextDark)
            Spacer(modifier = Modifier.height(4.dp))
            Text("We compare colors, categories, and descriptions as reports arrive.", color = TextGray, fontSize = 14.sp)
        }
    }
}

@Composable
fun MatchCard(match: MatchResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, SurfaceBeige, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        // AI Confidence Header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(LightOrangeTint)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryOrangeDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("AI confidence", color = PrimaryOrangeDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Text("${match.score}%", color = PrimaryOrangeDark, fontWeight = FontWeight.Black, fontSize = 24.sp)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Lost vs Found comparison
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Lost Item
            Column(modifier = Modifier.weight(1f)) {
                Text("LOST REPORT", color = TextGray, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(16.dp)).background(SurfaceBeige),
                    contentAlignment = Alignment.Center
                ) {
                    if (match.lostItem.imageUrl != null) {
                        coil.compose.AsyncImage(
                            model = match.lostItem.imageUrl,
                            contentDescription = "Item Image",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Outlined.Inventory, contentDescription = null, tint = PrimaryOrangeDark, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(match.lostItem.title, fontWeight = FontWeight.Black, fontSize = 16.sp, color = TextDark, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(match.lostItem.location, color = TextGray, fontSize = 12.sp, maxLines = 1)
                }
            }
            
            // Link Icon
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, SurfaceBeige, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Link, contentDescription = null, tint = PrimaryOrangeDark)
            }
            
            // Found Item
            Column(modifier = Modifier.weight(1f)) {
                Text("FOUND REPORT", color = TextGray, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(16.dp)).background(SurfaceBeige),
                    contentAlignment = Alignment.Center
                ) {
                    if (match.foundItem.imageUrl != null) {
                        coil.compose.AsyncImage(
                            model = match.foundItem.imageUrl,
                            contentDescription = "Item Image",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Outlined.Inventory, contentDescription = null, tint = PrimaryOrangeDark, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(match.foundItem.title, fontWeight = FontWeight.Black, fontSize = 16.sp, color = TextDark, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(match.foundItem.location, color = TextGray, fontSize = 12.sp, maxLines = 1)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Match Reason Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MintBackground.copy(alpha = 0.5f))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = Color(0xFF4A8972), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(match.reason, color = TextDark, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

