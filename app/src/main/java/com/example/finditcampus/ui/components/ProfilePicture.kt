package com.example.finditcampus.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.finditcampus.R
import com.example.finditcampus.data.ProfileManager

@Composable
fun ProfilePicture(modifier: Modifier = Modifier, iconSize: androidx.compose.ui.unit.Dp = 24.dp) {
    Box(
        modifier = modifier.clip(CircleShape).background(com.example.finditcampus.theme.SurfaceBeige),
        contentAlignment = Alignment.Center
    ) {
        val uri = ProfileManager.customImageUri
        if (uri != null) {
            AsyncImage(
                model = uri,
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val avatarList = listOf(
                R.drawable.avatar_robot,
                R.drawable.avatar_fox,
                R.drawable.avatar_voxel,
                R.drawable.avatar_astronaut
            )
            val index = ProfileManager.selectedAvatarIndex.coerceIn(0, avatarList.size - 1)
            Image(
                painter = painterResource(id = avatarList[index]),
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

