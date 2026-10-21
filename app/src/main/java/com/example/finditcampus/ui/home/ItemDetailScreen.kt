package com.example.finditcampus.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
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
import com.example.finditcampus.data.GlobalState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    onBack: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialItem = GlobalState.selectedItem
    if (initialItem == null) {
        onBack()
        return
    }
    
    // Auto-update item if GlobalState.items changes (real-time updates)
    val item = GlobalState.items.find { it.id == initialItem.id } ?: initialItem

    val scope = rememberCoroutineScope()
    var isLoadingChat by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBackground
                )
            )
        },
        containerColor = AppBackground,
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (item.isMine) {
                    if (item.status == "open") {
                        Button(
                            onClick = {
                                scope.launch {
                                    com.example.finditcampus.data.ReportRepository().updateReportStatus(item.id, "on_hold")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                            shape = RoundedCornerShape(24.dp)
                        ) { Text("Put on Hold", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                    } else if (item.status == "on_hold") {
                        Button(
                            onClick = {
                                scope.launch {
                                    com.example.finditcampus.data.ReportRepository().updateReportStatus(item.id, "resolved")
                                    onBack()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(24.dp)
                        ) { 
                            val text = if (item.type == "lost") "Confirm Receipt & Resolve" else "Confirm Handover & Resolve"
                            Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold) 
                        }
                    }
                } else {
                    // Always show Contact Reporter option
                    Button(
                        onClick = {
                            if (item.ownerId.isNotEmpty()) {
                                isLoadingChat = true
                                scope.launch {
                                    try {
                                        val repo = com.example.finditcampus.data.ChatRepository()
                                        val chatId = repo.getOrCreateChat(item.id, item.title, item.ownerId)
                                        onNavigateToChat(chatId)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    } finally {
                                        isLoadingChat = false
                                    }
                                }
                            } else {
                                onNavigateToChat("dummy_chat_id") // Fallback
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        shape = RoundedCornerShape(24.dp),
                        enabled = !isLoadingChat
                    ) {
                        if (isLoadingChat) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Contact Reporter", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    // Show Confirm option if item is on hold
                    if (item.status == "on_hold") {
                        Button(
                            onClick = {
                                scope.launch {
                                    com.example.finditcampus.data.ReportRepository().updateReportStatus(item.id, "resolved")
                                    onBack()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(24.dp)
                        ) { 
                            val text = if (item.type == "lost") "Confirm Handover" else "Confirm Receipt"
                            Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold) 
                        }
                    }
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Box(
                  modifier = Modifier
                      .fillMaxWidth()
                      .height(200.dp)
                      .clip(RoundedCornerShape(24.dp))
                      .background(LightOrangeTint),
                  contentAlignment = Alignment.Center
              ) {
                  if (item.imageUrl != null) {
                      coil.compose.AsyncImage(
                          model = item.imageUrl,
                          contentDescription = "Item Image",
                          contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                          modifier = Modifier.fillMaxSize()
                      )
                  } else {
                      Text("No Image Available", color = PrimaryOrangeDark, fontWeight = FontWeight.Bold)
                  }
              }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (item.type == "lost") StatusRedLight else Color(0xFFE8F5E9))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    item.type.uppercase(), 
                    color = if (item.type == "lost") StatusRed else Color(0xFF2E7D32), 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(item.title, fontWeight = FontWeight.Black, fontSize = 28.sp, color = TextDark, lineHeight = 34.sp)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = PrimaryOrange)
                Spacer(modifier = Modifier.width(8.dp))
                Text(item.location, color = TextGray, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Text("â€¢", color = TextGray)
                Spacer(modifier = Modifier.width(16.dp))
                Text(item.time, color = TextGray, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Description", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                item.description,
                color = TextGray,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Reporter", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.LightGray)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(if (item.isMine) "You" else "LPU Student", fontWeight = FontWeight.Bold, color = TextDark)
                    Text("Verified Campus Member", color = TextGray, fontSize = 12.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

