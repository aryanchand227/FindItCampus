package com.example.finditcampus.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Shield
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

import androidx.compose.runtime.*
import com.example.finditcampus.data.ChatThread

val MintBackground = Color(0xFFF2F8F8)

@Composable
fun MessagesListScreen(
    onNavigateToChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val repo = remember { com.example.finditcampus.data.ChatRepository() }
    var chats by remember { mutableStateOf<List<ChatThread>>(emptyList()) }

    LaunchedEffect(Unit) {
        repo.getUserChats().collect { remoteChats ->
            chats = remoteChats
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MintBackground)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(32.dp)) }
        item { MessagesHeader() }
        
        items(chats.size) { index ->
            val chat = chats[index]
            MessageThreadCard(
                title = chat.itemTitle,
                subtitle = chat.lastMessage.ifEmpty { "New match! Say hi." },
                time = if (chat.timestamp > 0) java.text.SimpleDateFormat("MMM dd, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(chat.timestamp)) else "",
                onClick = { onNavigateToChat(chat.id) }
            )
        }
        
        if (chats.isEmpty()) {
            item {
                Text(
                    "No messages yet. When you contact a reporter, your chats will appear here.",
                    color = TextGray,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
        
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun MessagesHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "PRIVATE CONNECTIONS",
                color = PrimaryOrangeDark,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Messages",
                color = TextDark,
                fontWeight = FontWeight.Black,
                fontSize = 36.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Connect safely without sharing your number.",
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
            Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = PrimaryOrangeDark, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun MessageThreadCard(title: String, subtitle: String, time: String, onClick: () -> Unit) {
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
                .size(56.dp)
                .clip(CircleShape)
                .background(SurfaceBeige),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Shield, contentDescription = null, tint = PrimaryOrangeDark)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextDark)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, color = TextGray, fontSize = 14.sp, maxLines = 1)
            if (time.isNotEmpty()) {
                Text(time, color = PrimaryOrangeDark, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.Default.ChevronRight, contentDescription = "Open Chat", tint = TextGray)
    }
}

