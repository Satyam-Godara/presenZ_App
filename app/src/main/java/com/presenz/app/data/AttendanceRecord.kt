package com.presenz.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local, on-device history table. Used by BOTH the teacher app and the
 * student app (each device only ever sees its own rows).
 *
 * role = "TEACHER" -> one row per session the teacher ran, presentCount = number marked present
 * role = "STUDENT" -> one row per session the student attended
 */
@Entity(tableName = "attendance_history")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String,
    val sessionId: String,
    val subject: String,
    val rollNo: String? = null,        // populated for STUDENT rows
    val studentName: String? = null,   // populated for STUDENT rows
    val presentCount: Int = 0,         // populated for TEACHER rows
    val timestampMillis: Long
)
