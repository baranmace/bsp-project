package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "esport_matches")
data class EsportMatch(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val teamA: String,
    val teamB: String,
    val dateMillis: Long,
    val gameMode: String = "", // e.g., "Savaş Topu", "Elmas Kapmaca", "Soygun"
    val mapName: String, // e.g., "Liman Arkası", "Süper Plaj"
    val stage: String, // e.g., "Grup Aşaması", "Çeyrek Final", "Büyük Final"
    val generalNotes: String = "",
    val draftPicksText: String = "", // e.g., "Pick A: Shelly, Ban A: Mortis"
    val aiTacticPlan: String = "", // Generated plan from Gemini
    val isScrim: Boolean = false,
    val scrimDetails: String = "",
    val scoreA: Int? = null,
    val scoreB: Int? = null
)
