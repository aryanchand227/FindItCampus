package com.example.finditcampus.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finditcampus.theme.*

import com.example.finditcampus.data.ChatMessage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf("") }
    val repo = remember { com.example.finditcampus.data.ChatRepository() }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
    val currentUserId = auth.currentUser?.uid
    
    LaunchedEffect(chatId) {
        repo.getMessages(chatId).collect { remoteMessages ->
            messages = remoteMessages
        }
    }
    
    Scaffold(
        containerColor = MintBackground,
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDark, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(SurfaceBeige),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Shield, contentDescription = null, tint = PrimaryOrangeDark)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(title, fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextDark)
                        Text("Private campus conversation", color = TextGray, fontSize = 13.sp)
                    }
                }
                HorizontalDivider(color = SurfaceBeige, thickness = 1.dp)
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = SurfaceBeige, thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Write a private message", color = TextGray) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceBeige,
                            unfocusedContainerColor = SurfaceBeige,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(PrimaryOrange)
                            .clickable {
                                if (messageText.isNotBlank()) {
                                    val textToSend = messageText
                                    messageText = ""
                                    scope.launch {
                                        repo.sendMessage(chatId, textToSend)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(messages.size) { index ->
                val msg = messages[index]
                val isMe = msg.senderId == currentUserId
                MessageBubble(
                    text = msg.text,
                    time = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(msg.timestamp)),
                    isMe = isMe
                )
            }
        }
    }
}

@Composable
fun MessageBubble(text: String, time: String, isMe: Boolean = true) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(
                    topStart = 24.dp, 
                    topEnd = 24.dp, 
                    bottomStart = if (isMe) 24.dp else 8.dp, 
                    bottomEnd = if (isMe) 8.dp else 24.dp
                ))
                .background(if (isMe) PrimaryOrange else Color.White)
                .border(if (isMe) 0.dp else 1.dp, if (isMe) Color.Transparent else SurfaceBeige, RoundedCornerShape(
                    topStart = 24.dp, topEnd = 24.dp, bottomStart = if (isMe) 24.dp else 8.dp, bottomEnd = if (isMe) 8.dp else 24.dp
                ))
                .padding(16.dp)
        ) {
            Text(text, color = if (isMe) Color.White else TextDark, fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(time, color = if (isMe) Color.White.copy(alpha = 0.7f) else TextGray, fontSize = 12.sp)
        }
    }
}

