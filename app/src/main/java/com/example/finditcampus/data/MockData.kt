package com.example.finditcampus.data

import androidx.compose.runtime.mutableStateListOf

data class MockItem(
    val id: String,
    val title: String,
    val description: String,
    val location: String,
    val time: String,
    val category: String,
    val type: String,
    val latOffset: Float = 0f,
    val lngOffset: Float = 0f,
    val isMine: Boolean = false,
    val ownerId: String = "",
    val imageUrl: String? = null,
    val imageTags: List<String> = emptyList(),
    val status: String = "open" // "open", "on_hold", "resolved"
)

object GlobalState {
    var selectedItem: MockItem? = null
    var lastPostedType: String = "found"
    val items = mutableStateListOf(
        MockItem("iphone_test", "Found White iPhone", "White iPhone 16 Pro Max. The lock screen has a pink car (looks like a Supra) on it.", "Food Court", "10m ago", "Electronics", "found", 0.45f, 0.55f, false, ""),
        MockItem("1", "Blue Hydro Flask", "Blue steel bottle with a small LPU sticker", "Central Library", "33m ago", "Others", "found", 0.2f, 0.4f, false, ""),
        MockItem("2", "Apple AirPods Pro", "White case, has a small scratch on back", "Block 34", "1h ago", "Electronics", "lost", 0.6f, 0.2f, false, ""),
        MockItem("3", "LPU ID Card", "ID card belonging to Rahul Sharma", "Main Gate", "2h ago", "IDs & Cards", "found", 0.8f, 0.8f, false, ""),
        MockItem("4", "Hostel Keys", "Two silver keys with a red keychain", "Food Court", "4h ago", "Keys", "lost", 0.3f, 0.7f, false, ""),
        MockItem("5", "Dell Laptop Charger", "Black charger, standard pin", "Block 14", "1d ago", "Electronics", "found", 0.5f, 0.5f, false, "")
    )
}

data class MatchResult(
    val lostItem: MockItem,
    val foundItem: MockItem,
    val score: Int,
    val reason: String
)

fun findMatches(items: List<MockItem>): List<MatchResult> {
    val lostItems = items.filter { it.type == "lost" }
    val foundItems = items.filter { it.type == "found" }
    
    val matches = mutableListOf<MatchResult>()
    
    for (lost in lostItems) {
        for (found in foundItems) {
            var score = 0
            val reasons = mutableListOf<String>()
            
            // 1. Category Match (40 points)
            if (lost.category == found.category) {
                score += 40
                reasons.add("Shared ${lost.category.lowercase()} category")
            }
            
            // 2. Keyword Match (up to 40 points)
            val lostWords = (lost.title + " " + lost.description).lowercase().split(Regex("\\W+")).filter { it.length > 3 }
            val foundWords = (found.title + " " + found.description).lowercase().split(Regex("\\W+")).filter { it.length > 3 }
            
            val commonWords = lostWords.intersect(foundWords.toSet())
            if (commonWords.isNotEmpty()) {
                score += (commonWords.size * 15).coerceAtMost(40)
                reasons.add("Shared text: ")
            }

            // 3. Visual Match via AI Image Tags (up to 30 points)
            val lostTags = lost.imageTags.map { it.lowercase() }
            val foundTags = found.imageTags.map { it.lowercase() }
            if (lostTags.isNotEmpty() && foundTags.isNotEmpty()) {
                val commonTags = lostTags.intersect(foundTags.toSet())
                if (commonTags.isNotEmpty()) {
                    score += (commonTags.size * 15).coerceAtMost(30)
                    reasons.add("Visual AI match: ")
                }
            }
            
            // Generate Match if score is decent
            if (score > 30) {
                matches.add(MatchResult(lost, found, score.coerceAtMost(99), reasons.joinToString(" and ")))
            }
        }
    }
    
    return matches.sortedByDescending { it.score }
}




