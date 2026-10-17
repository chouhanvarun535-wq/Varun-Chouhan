package com.example.model

data class Subject(
    val id: String,
    val name: String,
    val chaptersTotal: Int,
    val chaptersCompleted: Int
)

data class SyllabusChapter(
    val id: String,
    val subjectName: String,
    val chapterTitle: String,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val completedAt: Long = 0L
)

data class TimetableSlot(
    val id: String,
    val dayOfWeek: String, // e.g. "Monday"
    val timeRange: String, // e.g. "09:00 - 10:30 AM"
    val subject: String,
    val roomOrTopic: String = ""
)

data class Flashcard(
    val id: String,
    val question: String,
    val answer: String,
    val subject: String,
    val reviewsCount: Int = 0,
    val masteryLevel: Int = 0 // 0: Learning, 1: Review, 2: Mastered
)
