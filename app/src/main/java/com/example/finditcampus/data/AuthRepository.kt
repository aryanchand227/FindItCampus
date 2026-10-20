package com.example.finditcampus.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    val currentUser get() = auth.currentUser

    // We map Registration Number to a pseudo-email for Firebase Auth
    private fun regNoToEmail(regNo: String) = "$regNo@lpu.edu"

    suspend fun login(regNo: String, pass: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(regNoToEmail(regNo), pass).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun signUp(fullName: String, regNo: String, pass: String): Boolean {
        return try {
            // Wait, we don't store fullName here yet, but we can update the profile later
            auth.createUserWithEmailAndPassword(regNoToEmail(regNo), pass).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun logout() {
        auth.signOut()
    }
}
