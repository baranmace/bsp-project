package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matcherinos")
data class Matcherino(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val prizePool: String,
    val url: String,
    val status: String, // "Sürüyor", "Tamamlandı", "Yakında"
    val notes: String = "",
    val gameMode: String = "Savaş Topu"
)
