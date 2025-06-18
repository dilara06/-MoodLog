package com.example.moodlogapp.data.model

import java.time.LocalDateTime

/**
 * Represents a single mood entry with timestamp
 */
data class MoodEntry(
    val moodEmoji: String,
    val moodLabel: String,
    val note: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)