package com.example.finditcampus

import kotlinx.serialization.Serializable

@Serializable data object Splash
@Serializable data object Onboarding
@Serializable data object SignUp
@Serializable data object Main
@Serializable data object PostItem
@Serializable data class Chat(val chatId: String, val title: String)
@Serializable data object ItemDetail
@Serializable data object Settings
@Serializable data object CampusMap
