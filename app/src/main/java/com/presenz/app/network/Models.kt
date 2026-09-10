package com.presenz.app.network

import com.google.gson.annotations.SerializedName

data class RegisterRequest(val name: String, val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)

data class TeacherDto(val id: String, val name: String, val email: String)
data class LoginResponse(val token: String, val teacher: TeacherDto)

//data class StartSessionRequest(val subject: String)
data class StartSessionRequest(
    val subject: String,
    val groupId: String
)
//data class StartSessionResponse(
//    val sessionId: String,
//    val subject: String,
//    val token: String,
//    val startedAt: String
//)
data class StartSessionResponse(
    val sessionId: String,
    val subject: String,
    val token: String,
    val startedAt: String,
    val groupId: String,
    val groupName: String,
    val groupCode : String? = null   // nullable
)

data class EndSessionResponse(val sessionId: String, val active: Boolean, val presentCount: Int)

//data class SessionByTokenResponse(val sessionId: String, val subject: String, val active: Boolean)
data class SessionByTokenResponse(
    val sessionId: String,
    val subject: String,
    val active: Boolean,
    val groupId: String,
    val groupCode: String? = null,  // nullable
    val groupName: String
)
data class MarkAttendanceRequest(
    val sessionId: String,
    val token: String,
    val rollNo: String,
    val name: String?
)
data class MarkAttendanceResponse(
    val status: String,
    val sessionId: String,
    val subject: String,
    val rollNo: String,
    val markedAt: String
)

data class AttendanceStatusResponse(
    val present: Boolean,
    val subject: String? = null,
    val markedAt: String? = null
)

data class StudentAttendanceRow(val rollNo: String, val name: String?, val markedAt: String)
data class SessionAttendanceResponse(
    val sessionId: String,
    val subject: String,
    val active: Boolean,
    val students: List<StudentAttendanceRow>
)

data class SessionHistoryRow(
    val sessionId: String,
    val subject: String,
    val active: Boolean,
    val startedAt: String,
    val endedAt: String?,
    val presentCount: Int,
    val group: String,
)


data class StudentCheckRequest(
    val rollNo: String
)

data class StudentCheckResponse(
    val exists: Boolean,
    val student: StudentDto? = null
)

data class StudentDto(
    val id: String,
    val rollNo: String,
    val name: String,
    val email: String?,
    val groupId: String?,
    val groupName: String?
)

data class StudentLoginRequest(
    val rollNo: String,
    val androidId: String
)

data class StudentLoginResponse(
    val token: String,
    val student: StudentDto
)

data class StudentRegisterRequest(
    val rollNo: String,
    val name: String,
    val email: String,
    val groupId: String,
    val androidId: String
)

//data class GroupDto(
//    val id: String,
//    val name: String
//)
data class GroupDto(
    @SerializedName("id")
    val id: String,

    val name: String
)

data class AttendanceStudentRow(
    val rollNo: String,
    val name: String,
    val markedAt: String? = null
)

data class FullAttendanceResponse(
    val sessionId: String,
    val subject: String,
    val groupName: String,
    val active: Boolean,
    val present: List<AttendanceStudentRow>,
    val absent: List<AttendanceStudentRow>
)