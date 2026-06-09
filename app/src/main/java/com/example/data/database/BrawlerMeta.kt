package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "brawler_metas")
data class BrawlerMeta(
    @PrimaryKey val brawlerId: String,
    val brawlerName: String,
    val brawlerRole: String, // e.g., "Suikastçı", "Keskin Nişancı", "Tank"
    val tier: String, // e.g., "S", "A", "B", "C"
    val bestModes: String, // Comma-separated game modes
    val counterTips: String = "", // Hard counters or tactical tips
    val userNotes: String = "" // Custom notes of the user
)
