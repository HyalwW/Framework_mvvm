package com.example.kleaner.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Bean(@PrimaryKey(autoGenerate = true) var id: Long, var time: String)