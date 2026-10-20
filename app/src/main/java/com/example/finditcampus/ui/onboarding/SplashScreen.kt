package com.example.finditcampus.ui.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finditcampus.R
import com.example.finditcampus.theme.PrimaryOrange
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateNext: (Boolean) -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.4f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 100f),
        label = "LogoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "LogoAlpha"
    )
    
    val textOffsetY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 60f,
        animationSpec = tween(durationMillis = 800, delayMillis = 400, easing = FastOutSlowInEasing),
        label = "TextOffset"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 400),
        label = "TextAlpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2200) // Beautiful short pause
        val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
        onNavigateNext(isLoggedIn)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_splash_logo),
                contentDescription = "FindIt Logo",
                modifier = Modifier
                    .size(160.dp)
                    .graphicsLayer(
                        scaleX = logoScale,
                        scaleY = logoScale,
                        alpha = logoAlpha
                    )
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "FindIt Campus",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryOrange,
                modifier = Modifier.graphicsLayer(
                    translationY = textOffsetY,
                    alpha = textAlpha
                )
            )
            Text(
                text = "Your University's Lost & Found",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .graphicsLayer(
                        translationY = textOffsetY,
                        alpha = textAlpha
                    )
            )
        }
    }
}


