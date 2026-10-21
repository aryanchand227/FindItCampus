package com.example.finditcampus.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class ChatThread(
    val id: String = "",
    val itemId: String = "",
    val itemTitle: String = "",
    val participantIds: List<String> = emptyList(),
    val lastMessage: String = "",
    val timestamp: Long = 0L
)

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)

class ChatRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val chatsCollection = db.collection("chats")

    fun getUserChats(): Flow<List<ChatThread>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            return@callbackFlow
        }

        val listener = chatsCollection
            .whereArrayContains("participantIds", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val chats = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatThread::class.java)?.copy(id = doc.id)
                }?.sortedByDescending { it.timestamp } ?: emptyList()
                trySend(chats)
            }
        
        awaitClose { listener.remove() }
    }

    fun getMessages(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = chatsCollection.document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(messages)
            }
        
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(chatId: String, text: String) {
        val uid = auth.currentUser?.uid ?: return
        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderId = uid,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        
        // Add message
        chatsCollection.document(chatId).collection("messages").document(message.id).set(message).await()
        
        // Update last message in thread
        chatsCollection.document(chatId).update(
            "lastMessage", text,
            "timestamp", System.currentTimeMillis()
        ).await()
    }

    suspend fun getOrCreateChat(itemId: String, itemTitle: String, ownerId: String): String {
        val uid = auth.currentUser?.uid ?: throw Exception("Not logged in")
        if (uid == ownerId) throw Exception("Cannot chat with yourself")

        // Try to find existing chat
        val existing = chatsCollection
            .whereEqualTo("itemId", itemId)
            .whereArrayContains("participantIds", uid)
            .get().await()

        val chat = existing.documents.firstOrNull { 
            val participants = it.get("participantIds") as? List<*>
            participants?.contains(ownerId) == true
        }

        if (chat != null) {
            return chat.id
        }

        // Create new chat
        val newChatId = UUID.randomUUID().toString()
        val newChat = ChatThread(
            id = newChatId,
            itemId = itemId,
            itemTitle = itemTitle,
            participantIds = listOf(uid, ownerId),
            lastMessage = "Chat started",
            timestamp = System.currentTimeMillis()
        )
        chatsCollection.document(newChatId).set(newChat).await()
        return newChatId
    }
}
