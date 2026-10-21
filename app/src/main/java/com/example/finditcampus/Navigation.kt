package com.example.finditcampus

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.finditcampus.ui.home.HomeScreen
import com.example.finditcampus.ui.onboarding.OnboardingScreen

import com.example.finditcampus.ui.onboarding.SignUpScreen
import com.example.finditcampus.ui.home.ItemDetailScreen
import com.example.finditcampus.ui.profile.SettingsScreen
import com.example.finditcampus.ui.home.CampusMapScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.example.finditcampus.data.GlobalState
import androidx.navigation.toRoute

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.compose.ui.platform.LocalContext

import com.example.finditcampus.ui.onboarding.SplashScreen

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val repo = remember { com.example.finditcampus.data.ReportRepository() }
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        repo.getReportsFlow().collect { reports ->
            GlobalState.items.clear()
            GlobalState.items.addAll(reports)
            
            // Generate match notifications
            val myMatches = com.example.finditcampus.data.findMatches(reports).filter { 
                it.score >= 50 && (it.lostItem.isMine || it.foundItem.isMine)
            }
            
            if (myMatches.isNotEmpty()) {
                val match = myMatches.first() // Notify about top match
                val channelId = "matches_channel"
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(channelId, "Matches", NotificationManager.IMPORTANCE_HIGH)
                    notificationManager.createNotificationChannel(channel)
                }
                
                val notification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("New Match Found! (${match.score}%)")
                    .setContentText("Potential match for: ${if (match.lostItem.isMine) match.lostItem.title else match.foundItem.title}")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()
                    
                notificationManager.notify(match.lostItem.id.hashCode(), notification)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Splash
    ) {
        composable<Splash> {
            SplashScreen(onNavigateNext = { isLoggedIn ->
                if (isLoggedIn) {
                    navController.navigate(Main) { popUpTo(Splash) { inclusive = true } }
                } else {
                    navController.navigate(Onboarding) { popUpTo(Splash) { inclusive = true } }
                }
            })
        }
        
        composable<Onboarding> {
            OnboardingScreen(
                onLoginSuccess = { 
                    navController.navigate(Main) {
                        popUpTo(Onboarding) { inclusive = true }
                    }
                },
                onNavigateToSignUp = { navController.navigate(SignUp) }
            )
        }
        composable<SignUp> {
            SignUpScreen(
                onSignUpSuccess = { 
                    navController.navigate(Main) {
                        popUpTo(Onboarding) { inclusive = true }
                    } 
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable<Main> {
            HomeScreen(
                onNavigateToPost = { navController.navigate(PostItem) },
                onNavigateToChat = { chatId -> navController.navigate(Chat(chatId, "Chat")) },
                onNavigateToItemDetail = { navController.navigate(ItemDetail) },
                onNavigateToSettings = { navController.navigate(Settings) },
                onNavigateToMap = { navController.navigate(CampusMap) }
            )
        }
        composable<PostItem> {
            com.example.finditcampus.ui.post.PostItemScreen(
                onClose = { navController.popBackStack() }
            )
        }
        composable<Chat> { backStackEntry ->
            val chat = backStackEntry.toRoute<Chat>()
            com.example.finditcampus.ui.messages.ChatScreen(
                chatId = chat.chatId,
                title = chat.title,
                onBack = { navController.popBackStack() }
            )
        }
        composable<ItemDetail> {
            ItemDetailScreen(
                onBack = { navController.popBackStack() },
                onNavigateToChat = { chatId -> navController.navigate(Chat(chatId, "Chat")) }
            )
        }
        composable<Settings> {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = { 
                    navController.navigate(Onboarding) {
                        popUpTo(0) // Clear entire backstack
                    }
                }
            )
        }
        composable<CampusMap> {
            CampusMapScreen(
                onBack = { navController.popBackStack() },
                onItemClick = { navController.navigate(ItemDetail) }
            )
        }
    }
}
