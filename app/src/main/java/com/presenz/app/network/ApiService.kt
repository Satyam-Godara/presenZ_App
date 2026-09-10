package com.presenz.app.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<TeacherDto>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("api/session/start")
    suspend fun startSession(
        @Header("Authorization") bearer: String,
        @Body body: StartSessionRequest
    ): Response<StartSessionResponse>

    @POST("api/session/{id}/end")
    suspend fun endSession(
        @Header("Authorization") bearer: String,
        @Path("id") sessionId: String
    ): Response<EndSessionResponse>

    @GET("api/session/{id}/attendance")
    suspend fun getSessionAttendance(
        @Header("Authorization") bearer: String,
        @Path("id") sessionId: String
    ): Response<SessionAttendanceResponse>

    @GET("api/session/history")
    suspend fun getSessionHistory(
        @Header("Authorization") bearer: String
    ): Response<List<SessionHistoryRow>>

    @GET("api/session/by-token/{token}")
    suspend fun getSessionByToken(@Path("token") token: String): Response<SessionByTokenResponse>

    @POST("api/attendance/mark")
    suspend fun markAttendance(@Body body: MarkAttendanceRequest): Response<MarkAttendanceResponse>

    //    @GET("api/attendance/status")
//    suspend fun getAttendanceStatus(
//        @Query("sessionId") sessionId: String,
//        @Query("rollNo") rollNo: String
//    ): Response<AttendanceStatusResponse>
    @GET("api/attendance/status")
    suspend fun getAttendanceStatus(
        @Header("Authorization") bearer: String,
        @Query("sessionId") sessionId: String
    ): Response<AttendanceStatusResponse>


    @POST("api/auth/student/check")
    suspend fun checkStudent(
        @Body body: StudentCheckRequest
    ): Response<StudentCheckResponse>


    @POST("api/auth/student/login")
    suspend fun studentLogin(
        @Body body: StudentLoginRequest
    ): Response<StudentLoginResponse>


    @POST("api/auth/student/register")
    suspend fun studentRegister(
        @Body body: StudentRegisterRequest
    ): Response<StudentLoginResponse>


    @GET("api/teacher/groups")
    suspend fun getTeacherGroups(
        @Header("Authorization") bearer: String
    ): Response<List<GroupDto>>

    @GET("api/group")
    suspend fun getGroups(): Response<List<GroupDto>>


    @GET("api/session/{id}/full-attendance")
    suspend fun getFullAttendance(
        @Header("Authorization") bearer: String,
        @Path("id") sessionId: String
    ): Response<FullAttendanceResponse>

}