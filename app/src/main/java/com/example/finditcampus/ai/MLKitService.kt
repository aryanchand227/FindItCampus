package com.example.finditcampus.ai

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.tasks.await

class MLKitService {
    
    // Create the image labeler with default options (runs locally, completely free)
    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    /**
     * Scans an image locally using Google ML Kit and returns a list of tags.
     * No API key required. No network calls.
     */
    suspend fun extractTagsFromImage(bitmap: Bitmap?): List<String> {
        if (bitmap == null) return emptyList()
        
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val labels = labeler.process(image).await()
            
            // Return top tags with high confidence
            labels.filter { it.confidence > 0.6f }
                  .map { it.text }
                  .take(5)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
