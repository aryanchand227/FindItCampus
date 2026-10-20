package com.example.finditcampus.data

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.io.File
import java.io.FileOutputStream

object ProfileManager {
    var customImageUri by mutableStateOf<String?>(null)
    var selectedAvatarIndex by mutableStateOf(0)

    fun load(context: Context) {
        val prefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        customImageUri = prefs.getString("customImageUri", null)
        selectedAvatarIndex = prefs.getInt("selectedAvatarIndex", 0)
    }

    fun saveAvatar(context: Context, index: Int) {
        selectedAvatarIndex = index
        customImageUri = null
        context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
            .edit()
            .putInt("selectedAvatarIndex", index)
            .putString("customImageUri", null)
            .apply()
    }

    fun saveCustomImage(context: Context, uri: Uri) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, "profile_pic.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            
            val newUri = Uri.fromFile(file).toString()
            customImageUri = newUri
            selectedAvatarIndex = -1
            
            context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
                .edit()
                .putInt("selectedAvatarIndex", -1)
                .putString("customImageUri", newUri)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
