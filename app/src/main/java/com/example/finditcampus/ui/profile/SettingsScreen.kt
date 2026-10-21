package com.example.finditcampus.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
        var showProfileDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val photoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            com.example.finditcampus.data.ProfileManager.saveCustomImage(context, uri)
            showProfileDialog = false
        }
    }

    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("Profile Picture", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select an avatar:")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                                                listOf(com.example.finditcampus.R.drawable.avatar_robot, com.example.finditcampus.R.drawable.avatar_fox, com.example.finditcampus.R.drawable.avatar_voxel, com.example.finditcampus.R.drawable.avatar_astronaut).forEachIndexed { index, drawableId ->
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .border(
                                        width = if (com.example.finditcampus.data.ProfileManager.selectedAvatarIndex == index && com.example.finditcampus.data.ProfileManager.customImageUri == null) 3.dp else 0.dp,
                                        color = if (com.example.finditcampus.data.ProfileManager.selectedAvatarIndex == index && com.example.finditcampus.data.ProfileManager.customImageUri == null) PrimaryOrange else Color.Transparent,
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                                    .clickable {
                                        com.example.finditcampus.data.ProfileManager.saveAvatar(context, index)
                                        showProfileDialog = false
                                    }
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = drawableId),
                                    contentDescription = null,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceBeige)
                    ) {
                        Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = PrimaryOrangeDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Choose from Gallery", color = PrimaryOrangeDark, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Close", color = TextGray)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MintBackground
                )
            )
        },
        containerColor = MintBackground
    ) { paddingValues ->
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val user = auth.currentUser
        val email = user?.email ?: "Guest"
        val regNo = if (email.contains("@")) email.substringBefore("@") else "N/A"

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Centered Profile Picture
            Box(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(110.dp)
                ) {
                    com.example.finditcampus.ui.components.ProfilePicture(
                        modifier = Modifier.fillMaxSize(),
                        iconSize = 56.dp
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryOrange)
                            .border(3.dp, Color.White, CircleShape)
                            .clickable { showProfileDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit Profile Picture", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
            
            Text("Personal Information", color = TextGray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            
            // Info Display Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, SurfaceBeige, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoRow("Full Name", "LPU Student")
                InfoRow("Registration No.", regNo)
                InfoRow("Program", "B.Tech CSE")
                InfoRow("Email", email)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text("Preferences", color = TextGray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            
            var notificationsEnabled by remember { mutableStateOf(true) }
            
            // Toggle Item for Notifications
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Notifications, contentDescription = null, tint = TextDark, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Push Notifications", color = TextDark, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryOrange)
                )
            }
            
                        
            SettingsItem(icon = Icons.Outlined.Security, title = "Privacy & Security")
            SettingsItem(icon = Icons.Outlined.HelpOutline, title = "Help & Support")
            SettingsItem(icon = Icons.Outlined.Info, title = "About FindIt Campus")
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusRedLight),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Outlined.ExitToApp, contentDescription = null, tint = StatusRed, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", color = StatusRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column {
        Text(label, color = TextGray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, color = TextDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    title: String, 
    textColor: Color = TextDark,
    iconTint: Color = TextDark,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}





