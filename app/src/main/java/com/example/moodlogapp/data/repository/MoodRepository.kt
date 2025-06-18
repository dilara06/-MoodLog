package com.example.moodlogapp.data.repository

import com.example.moodlogapp.data.model.MoodEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Simple in-memory repository for storing mood entries
 */
object MoodRepository {
    private val _entries = MutableStateFlow<List<MoodEntry>>(emptyList())
    val entries: StateFlow<List<MoodEntry>> = _entries.asStateFlow()

    fun addEntry(entry: MoodEntry) {
        _entries.value = _entries.value + entry
    }
}