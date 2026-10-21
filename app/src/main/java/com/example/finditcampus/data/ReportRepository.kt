package com.example.finditcampus.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReportRepository {
    private val db = FirebaseFirestore.getInstance()
    private val reportsCollection = db.collection("reports")

    fun getReportsFlow(): Flow<List<MockItem>> = callbackFlow {
        val listener = reportsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val reports = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    MockItem(
                        id = doc.id,
                        title = data["title"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        location = data["location"] as? String ?: "",
                        time = data["time"] as? String ?: "",
                        category = data["category"] as? String ?: "",
                        type = data["type"] as? String ?: "",
                        latOffset = (data["latOffset"] as? Double)?.toFloat() ?: 0f,
                        lngOffset = (data["lngOffset"] as? Double)?.toFloat() ?: 0f,
                        isMine = (data["ownerId"] as? String) == com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid,
                        ownerId = data["ownerId"] as? String ?: "",
                        imageUrl = data["imageUrl"] as? String,
                        imageTags = (data["imageTags"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        status = data["status"] as? String ?: "open"
                    )
                }
                // Filter out resolved items so they disappear from feed
                trySend(reports.filter { it.status != "resolved" })
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun addReport(item: MockItem) {
        val ownerId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
        val reportMap = hashMapOf(
            "title" to item.title,
            "description" to item.description,
            "location" to item.location,
            "time" to item.time,
            "category" to item.category,
            "type" to item.type,
            "latOffset" to item.latOffset.toDouble(),
            "lngOffset" to item.lngOffset.toDouble(),
            "ownerId" to ownerId,
            "imageUrl" to item.imageUrl,
            "imageTags" to item.imageTags,
            "status" to "open"
        )
        reportsCollection.add(reportMap).await()
    }
    
    suspend fun updateReportStatus(id: String, newStatus: String) {
        reportsCollection.document(id).update("status", newStatus).await()
    }

    suspend fun deleteReport(id: String) {
        reportsCollection.document(id).delete().await()
    }
}


