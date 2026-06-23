package com.gracelink.android.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fm_schedule")
data class FmScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val day: String,           // MON, TUE, WED, THU, FRI, SAT, SUN
    val timeSlot: String,      // "06:00-08:00"
    val startHour: Int,        // 6
    val preacher: String,      // "Pastor Anil Kumar"
    val description: String,
    val category: ContentCategory,
)
