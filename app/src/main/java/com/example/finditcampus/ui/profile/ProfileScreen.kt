package com.example.finditcampus.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
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

import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Delete
import com.example.finditcampus.data.GlobalState
import kotlinx.coroutines.launch

val MintBackground = Color(0xFFF2F8F8)

@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val myItems = GlobalState.items.filter { it.isMine }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MintBackground)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { Spacer(modifier = Modifier.height(32.dp)) }
        item { ProfileHeader(onSettingsClick) }
        item { StatsCard(reportCount = myItems.size, returnedCount = myItems.count { it.status == "resolved" }) }
        item { ReportsHeader() }
        
        items(myItems) { item ->
            ReportCard(
                title = item.title,
                subtitle = "${item.type.replaceFirstChar { it.uppercase() }} @ ${item.location}",
                status = "Active",
                statusColor = PrimaryOrange,
                imageUrl = item.imageUrl,
                actionIcon = {
                    val scope = androidx.compose.runtime.rememberCoroutineScope()
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(StatusRedLight)
                            .clickable {
                                scope.launch {
                                    com.example.finditcampus.data.ReportRepository().deleteReport(item.id)
                                }
                            }, 
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusRed)
                    }
                }
            )
        }
        
        if (myItems.isEmpty()) {
            item {
                Text(
                    "You haven't posted any reports yet.",
                    color = TextGray,
                    modifier = Modifier.padding(top = 16.dp, bottom = 48.dp)
                )
            }
        }
        
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun ProfileHeader(onSettingsClick: () -> Unit = {}) {
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val email = auth.currentUser?.email ?: "Guest"
    val regNo = if (email.contains("@")) email.substringBefore("@") else "N/A"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Profile", fontWeight = FontWeight.Black, fontSize = 36.sp, color = TextDark)
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.clip(CircleShape).background(SurfaceBeige)
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextDark)
        }
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        com.example.finditcampus.ui.components.ProfilePicture(modifier = Modifier.size(72.dp), iconSize = 40.dp)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("LPU student", fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextDark)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Reg No: $regNo  \nLovely Professional University", color = TextGray, fontSize = 13.sp, lineHeight = 18.sp)
        }
        Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = Color(0xFF4A8972), modifier = Modifier.size(28.dp))
    }
}

@Composable
fun StatsCard(reportCount: Int, returnedCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceBeige)
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(value = reportCount.toString(), label = "Reports")
        HorizontalDivider(modifier = Modifier.width(1.dp).height(32.dp), color = PrimaryOrangeDark.copy(alpha = 0.3f))
        StatItem(value = returnedCount.toString(), label = "Returned")
        HorizontalDivider(modifier = Modifier.width(1.dp).height(32.dp), color = PrimaryOrangeDark.copy(alpha = 0.3f))
        StatItem(value = "LPU", label = "Campus")
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Black, fontSize = 22.sp, color = PrimaryOrangeDark)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = TextDark, fontSize = 13.sp)
    }
}

@Composable
fun ReportsHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("My reports", fontWeight = FontWeight.Black, fontSize = 24.sp, color = TextDark)
            Text("Keep your posts up to date", color = TextGray, fontSize = 14.sp)
        }
        Icon(Icons.Outlined.FilterList, contentDescription = "Filter", tint = PrimaryOrangeDark)
    }
}

@Composable
fun ReportCard(title: String, subtitle: String, status: String, statusColor: Color, imageUrl: String? = null, actionIcon: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, SurfaceBeige, RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(SurfaceBeige),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl != null) {
                coil.compose.AsyncImage(
                    model = imageUrl,
                    contentDescription = "Report Image",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = PrimaryOrangeDark)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Black, fontSize = 16.sp, color = TextDark, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, color = TextGray, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(statusColor))
                Spacer(modifier = Modifier.width(6.dp))
                Text(status, color = TextDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        actionIcon()
    }
}



