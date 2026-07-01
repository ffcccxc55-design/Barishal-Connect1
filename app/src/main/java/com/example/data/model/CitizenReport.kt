package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "citizen_reports")
data class CitizenReport(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val location: String,
    val reporterName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val category: String = "General", // Entertainment, Meme, Travel, Sports, Photography, Education, Health, Business, etc.
    val imageUrl: String = "",
    val videoUrl: String = "",
    val votesCount: Int = 0,
    val commentsJson: String = "", // formatted: authorName::text::avatarUrl::timestamp|||...
    val reactionsJson: String = "", // formatted: username::emoji|||...
    
    // Community Hub social parameters with defaults for full backward compatibility
    val postType: String = "Text", // Text, Photo, Video, Voice, Poll, Question
    val hashtags: String = "",
    val voiceUrl: String = "",
    val pollOptions: String = "", // format Option1|||Option2|||Option3
    val pollVotes: String = "", // format username::optionIndex|||...
    val district: String = "",
    val upazila: String = "",
    val unionName: String = "",
    val privacy: String = "Public", // Public, Followers
    val mentionedUsers: String = "",
    
    // Social Analytics
    val viewsCount: Int = 0,
    val reachCount: Int = 0,
    val sharesCount: Int = 0,
    val savesCount: Int = 0,
    val watchTimeMs: Long = 0,
    
    // Admin Controls
    val isBoosted: Boolean = false,
    val isFeatured: Boolean = false,
    val isPinned: Boolean = false,
    val isTrending: Boolean = false,
    val isDiscussionLocked: Boolean = false,
    val isCommentsDisabled: Boolean = false,
    val isSharingDisabled: Boolean = false,
    val reachMultiplier: Float = 1.0f
) : Serializable

data class ReportComment(
    val authorName: String,
    val text: String,
    val avatarUrl: String,
    val timestamp: Long
) : Serializable

data class UserReaction(
    val username: String,
    val emoji: String
) : Serializable

fun CitizenReport.getComments(): List<ReportComment> {
    if (commentsJson.trim().isEmpty()) return emptyList()
    return commentsJson.split("|||").mapNotNull { item ->
        val parts = item.split("::")
        if (parts.size >= 2) {
            ReportComment(
                authorName = parts[0],
                text = parts[1],
                avatarUrl = parts.getOrNull(2) ?: "",
                timestamp = parts.getOrNull(3)?.toLongOrNull() ?: System.currentTimeMillis()
            )
        } else null
    }
}

fun CitizenReport.getReactions(): List<UserReaction> {
    if (reactionsJson.trim().isEmpty()) return emptyList()
    return reactionsJson.split("|||").mapNotNull { item ->
        val parts = item.split("::")
        if (parts.size >= 2) {
            UserReaction(username = parts[0], emoji = parts[1])
        } else null
    }
}

fun CitizenReport.withAddedComment(comment: ReportComment): CitizenReport {
    val sanitizedAuthor = comment.authorName.replace("::", " ").replace("|||", " ")
    val sanitizedText = comment.text.replace("::", " ").replace("|||", " ")
    val sanitizedAvatar = comment.avatarUrl.replace("::", " ").replace("|||", " ")
    val commentStr = "$sanitizedAuthor::$sanitizedText::$sanitizedAvatar::${comment.timestamp}"
    val newJson = if (commentsJson.trim().isEmpty()) commentStr else "$commentsJson|||$commentStr"
    return this.copy(commentsJson = newJson)
}

fun CitizenReport.withToggledReaction(username: String, emoji: String): CitizenReport {
    val reactions = getReactions().toMutableList()
    val existingIndex = reactions.indexOfFirst { it.username == username }
    if (existingIndex >= 0) {
        val existing = reactions[existingIndex]
        if (existing.emoji == emoji) {
            // Remove if clicked again
            reactions.removeAt(existingIndex)
        } else {
            // Update to new emoji
            reactions[existingIndex] = UserReaction(username, emoji)
        }
    } else {
        // Add new reaction
        reactions.add(UserReaction(username, emoji))
    }
    
    val newJson = reactions.joinToString("|||") { "${it.username}::${it.emoji}" }
    return this.copy(reactionsJson = newJson)
}


