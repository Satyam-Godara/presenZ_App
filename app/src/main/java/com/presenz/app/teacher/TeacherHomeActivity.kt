//package com.presenz.app.teacher
//
//import android.content.Intent
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import com.presenz.app.MainActivity
//import com.presenz.app.databinding.ActivityTeacherHomeBinding
//import com.presenz.app.util.Prefs
//
//class TeacherHomeActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityTeacherHomeBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityTeacherHomeBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        binding.tvWelcome.text = "Hi, ${Prefs.teacherName ?: "Teacher"}"
//
//        binding.btnStartSession.setOnClickListener {
//            val subject = binding.etSubject.text?.toString()?.trim().orEmpty()
//            if (subject.isEmpty()) {
//                binding.etSubject.error = "Enter a subject name"
//                return@setOnClickListener
//            }
//            val intent = Intent(this, SessionActivity::class.java)
//            intent.putExtra(SessionActivity.EXTRA_SUBJECT, subject)
//            startActivity(intent)
//        }
//
//        binding.btnHistory.setOnClickListener {
//            startActivity(Intent(this, TeacherHistoryActivity::class.java))
//        }
//
//        binding.btnLogout.setOnClickListener {
//            Prefs.clearTeacher()
//            val intent = Intent(this, MainActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
//            startActivity(intent)
//            finish()
//        }
//    }
//}

package com.presenz.app.teacher

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.presenz.app.MainActivity
import com.presenz.app.databinding.ActivityTeacherHomeBinding
import com.presenz.app.network.ApiClient
import com.presenz.app.network.GroupDto
import com.presenz.app.util.Prefs
import kotlinx.coroutines.launch
import com.presenz.app.util.BluetoothEnabler

class TeacherHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherHomeBinding

    private var groups: List<GroupDto> = emptyList()
    private val bluetoothEnabler = BluetoothEnabler(this) { enabled ->
        if (!enabled) {
//            binding.tvStatus.text = "Bluetooth is required to start a session"
            Toast.makeText(this, "Bluetooth is required to start a session", Toast.LENGTH_SHORT).show()
        }
    }
    companion object {
        // TODO: replace with a backend call (getTeacherSubjects) once
        // subjects are stored per-teacher in the DB. Keep the same
        // pattern as `groups` below when you do.
        private val SUBJECTS = listOf(
            "System Design",
            "AiML",
            "PAuJ",
            "ADI",
            "NALR",
            "BPC",
            "AOC"
        )
    }
    private fun setupSubjectSpinner() {
        val subjectAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            SUBJECTS
        )
        subjectAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )
        binding.spinnerSubject.adapter = subjectAdapter
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Prefs.teacherToken.isNullOrBlank()) {
            openLogin()
            return
        }

        binding = ActivityTeacherHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvWelcome.text =
            "Hi, ${Prefs.teacherName ?: "Teacher"}"

        loadTeacherGroups()
        //loads hardcoded Subjects
        setupSubjectSpinner()
        binding.btnStartSession.setOnClickListener {
            startSession()
        }

        binding.btnHistory.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    TeacherHistoryActivity::class.java
                )
            )
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun loadTeacherGroups() {

        binding.tvStatus.text = "Loading groups..."
        binding.btnStartSession.isEnabled = false

        lifecycleScope.launch {

            try {

                val response = ApiClient.service()
                    .getTeacherGroups(
                        ApiClient.teacherBearer()
                    )

                if (!response.isSuccessful) {

                    binding.tvStatus.text =
                        when (response.code()) {
                            401 -> "Teacher session expired"
                            else -> "Unable to load assigned groups"
                        }

                    if (response.code() == 401) {
                        logout()
                    }

                    return@launch
                }

                groups = response.body().orEmpty()

                if (groups.isEmpty()) {

                    binding.tvStatus.text =
                        "No groups assigned to you"

                    return@launch
                }

                val groupNames = groups.map {
                    it.name
                }

                val spinnerAdapter = ArrayAdapter(
                    this@TeacherHomeActivity,
                    android.R.layout.simple_spinner_item,
                    groupNames
                )

                spinnerAdapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
                )

                binding.spinnerGroup.adapter = spinnerAdapter

                binding.tvStatus.text = ""
                binding.btnStartSession.isEnabled = true

            } catch (e: Exception) {

                binding.tvStatus.text =
                    "Network error: ${e.message}"

            } finally {

                if (groups.isNotEmpty()) {
                    binding.btnStartSession.isEnabled = true
                }
            }
        }
    }

    private fun startSession() {

//        val subject = binding.etSubject.text
//            ?.toString()
//            ?.trim()
//            .orEmpty()
//
//        if (subject.isEmpty()) {
//            binding.etSubject.error =
//                "Enter a subject name"
//            return
//        }
        // relpace for demo hardcoded dropdown subjects
        val selectedSubjectPosition = binding.spinnerSubject.selectedItemPosition

        if (selectedSubjectPosition < 0 || selectedSubjectPosition >= SUBJECTS.size) {
            binding.tvStatus.text = "Select a subject"
            return
        }

        val subject = SUBJECTS[selectedSubjectPosition]

        if (groups.isEmpty()) {
            binding.tvStatus.text =
                "No assigned group available"
            return
        }

        val selectedPosition =
            binding.spinnerGroup.selectedItemPosition

        if (
            selectedPosition < 0 ||
            selectedPosition >= groups.size
        ) {
            binding.tvStatus.text =
                "Select a group"
            return
        }

        val selectedGroup =
            groups[selectedPosition]

        val intent = Intent(
            this,
            SessionActivity::class.java
        )

        intent.putExtra(
            SessionActivity.EXTRA_SUBJECT,
            subject
        )

        intent.putExtra(
            SessionActivity.EXTRA_GROUP_ID,
            selectedGroup.id
        )

        intent.putExtra(
            SessionActivity.EXTRA_GROUP_NAME,
            selectedGroup.name
        )
//        Log.e(
//            "TEACHER_HOME_DEBUG",
//            "subject=$subject, groupId=$groupId, groupName=$groupName"
//        )
//        startActivity(intent)
        bluetoothEnabler.ensureEnabledThen {
            startActivity(intent)
        }
    }

    private fun logout() {

        Prefs.clearTeacher()

        val intent = Intent(
            this,
            MainActivity::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)

        finish()
    }

    private fun openLogin() {

        startActivity(
            Intent(
                this,
                TeacherLoginActivity::class.java
            )
        )

        finish()
    }
}