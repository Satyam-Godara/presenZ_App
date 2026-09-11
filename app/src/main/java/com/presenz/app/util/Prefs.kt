package com.presenz.app.util

import android.content.Context
import android.content.SharedPreferences

/**
 * SharedPreferences used for locally persisted authentication/profile data.
 *
 * Teacher:
 *  - JWT token
 *  - name
 *  - email
 *
 * Student:
 *  - JWT token
 *  - student ID
 *  - roll number
 *  - name
 *  - email
 *  - group ID
 *  - group code
 *  - group name
 *  - Android device ID
 */
object Prefs {

    private const val FILE = "presenz_prefs"

    private lateinit var sp: SharedPreferences

    // ============================================================
    // INITIALIZATION
    // ============================================================

    fun init(context: Context) {

        if (!::sp.isInitialized) {

            sp = context.applicationContext
                .getSharedPreferences(
                    FILE,
                    Context.MODE_PRIVATE
                )
        }
    }


    // ============================================================
    // SERVER
    // ============================================================

    var baseUrl: String
        get() =
            sp.getString(
                "base_url",
//                "https://presenz.godara.work.gd/"
                "http://161.118.176.127:4000/"
            )

                    ?: "https://presenz-ckzr.onrender.com/"

        set(value) =
            sp.edit()
                .putString(
                    "base_url",
                    value
                )
                .apply()


    // ============================================================
    // TEACHER
    // ============================================================

    var teacherToken: String?
        get() =
            sp.getString(
                "teacher_token",
                null
            )

        set(value) =
            sp.edit()
                .putString(
                    "teacher_token",
                    value
                )
                .apply()


    var teacherName: String?
        get() =
            sp.getString(
                "teacher_name",
                null
            )

        set(value) =
            sp.edit()
                .putString(
                    "teacher_name",
                    value
                )
                .apply()


    var teacherEmail: String?
        get() =
            sp.getString(
                "teacher_email",
                null
            )

        set(value) =
            sp.edit()
                .putString(
                    "teacher_email",
                    value
                )
                .apply()


    fun isTeacherLoggedIn(): Boolean {
        return !teacherToken.isNullOrBlank()
    }


    fun clearTeacher() {

        sp.edit()
            .remove("teacher_token")
            .remove("teacher_name")
            .remove("teacher_email")
            .apply()
    }


    // ============================================================
    // STUDENT
    // ============================================================

    /**
     * JWT token returned by the backend after student login
     * or registration.
     */
    var studentToken: String?
        get() =
            sp.getString(
                "student_token",
                null
            )

        set(value) =
            sp.edit()
                .putString(
                    "student_token",
                    value
                )
                .apply()


    /**
     * MongoDB student _id.
     */
    var studentId: String?
        get() =
            sp.getString(
                "student_id",
                null
            )

        set(value) =
            sp.edit()
                .putString(
                    "student_id",
                    value
                )
                .apply()


    /**
     * Student roll number.
     */
    var studentRollNo: String?
        get() =
            sp.getString(
                "student_roll",
                null
            )

        set(value) =
            sp.edit()
                .putString(
                    "student_roll",
                    value
                )
                .apply()


    /**
     * Student full name.
     */
    var studentName: String?
        get() =
            sp.getString(
                "student_name",
                null
            )

        set(value) =
            sp.edit()
                .putString(
                    "student_name",
                    value
                )
                .apply()


    /**
     * Student email.
     */
    var studentEmail: String?
        get() =
            sp.getString(
                "student_email",
                null
            )

        set(value) =
            sp.edit()
                .putString(
                    "student_email",
                    value
                )
                .apply()


    /**
     * MongoDB group _id.
     *
     * Used for backend/database relationships.
     *
     * Do not use this directly as BLE data.
     */
    var studentGroupId: String?
        get() =
            sp.getString(
                "student_group_id",
                null
            )

        set(value) =
            sp.edit()
                .putString(
                    "student_group_id",
                    value
                )
                .apply()


    /**
     * Short group identifier used by BLE.
     *
     * Examples:
     * CSEA
     * CSEB
     * ITA
     */
    var studentGroupCode: String?
        get() =
            sp.getString(
                "student_group_code",
                null
            )

        set(value) =
            sp.edit()
                .putString(
                    "student_group_code",
                    value
                )
                .apply()


    /**
     * Human-readable group name.
     */
    var studentGroupName: String?
        get() =
            sp.getString(
                "student_group_name",
                null
            )

        set(value) =
            sp.edit()
                .putString(
                    "student_group_name",
                    value
                )
                .apply()


    /**
     * Android ID of the device bound to this student account.
     *
     * This is stored locally so the app can determine whether
     * the current local profile has a device identity.
     */
    var studentAndroidId: String?
        get() =
            sp.getString(
                "student_android_id",
                null
            )

        set(value) =
            sp.edit()
                .putString(
                    "student_android_id",
                    value
                )
                .apply()


    // ============================================================
    // STUDENT STATE
    // ============================================================

    /**
     * Returns true when the student has a backend JWT token.
     */
    fun isStudentLoggedIn(): Boolean {
        return !studentToken.isNullOrBlank()
    }


    /**
     * Returns true when a student profile has been stored locally.
     *
     * A profile is considered registered only when the important
     * identity fields are available.
     */
    fun isStudentRegistered(): Boolean {

        return !studentRollNo.isNullOrBlank() &&
                !studentName.isNullOrBlank() &&
                !studentAndroidId.isNullOrBlank()
    }


    // ============================================================
    // SAVE STUDENT
    // ============================================================

    /**
     * Saves the complete student profile after successful
     * registration or login.
     *
     * groupId and groupName can be null because the backend
     * response may not always contain them.
     */
    fun saveStudent(
        token: String,
        id: String,
        rollNo: String,
        name: String,
        email: String,
        groupId: String?,
        groupName: String?
    ) {

        studentToken = token

        studentId = id

        studentRollNo = rollNo

        studentName = name

        studentEmail = email

        studentGroupId = groupId

        studentGroupName = groupName
    }


    // ============================================================
    // CLEAR STUDENT
    // ============================================================

    /**
     * Removes all locally stored student information.
     *
     * Used when the student logs out or the local session
     * needs to be completely reset.
     */
    fun clearStudent() {

        sp.edit()
            .remove("student_token")
            .remove("student_id")
            .remove("student_roll")
            .remove("student_name")
            .remove("student_email")
            .remove("student_group_id")
            .remove("student_group_code")
            .remove("student_group_name")
            .remove("student_android_id")
            .apply()
    }
}
