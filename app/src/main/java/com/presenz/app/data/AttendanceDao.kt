package com.presenz.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AttendanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AttendanceRecord): Long

    @Query("SELECT * FROM attendance_history WHERE role = :role ORDER BY timestampMillis DESC")
    suspend fun getHistoryForRole(role: String): List<AttendanceRecord>

    @Query("SELECT * FROM attendance_history WHERE role = 'STUDENT' AND sessionId = :sessionId AND rollNo = :rollNo LIMIT 1")
    suspend fun findStudentRecord(sessionId: String, rollNo: String): AttendanceRecord?
}
