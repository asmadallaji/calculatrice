package com.projet.calchistorique.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "operations")
data class Operation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val expression: String,
    val result: String,
    val timestamp: Long
)
