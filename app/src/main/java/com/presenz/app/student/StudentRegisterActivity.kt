package com.presenz.app.student

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.presenz.app.databinding.ActivityStudentRegisterBinding
import com.presenz.app.network.ApiClient
import com.presenz.app.network.StudentRegisterRequest
import com.presenz.app.network.GroupDto
import com.presenz.app.util.DeviceId
import com.presenz.app.util.Prefs
import kotlinx.coroutines.launch

class StudentRegisterActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ROLL_NO = "extra_roll_no"
    }

    private lateinit var binding: ActivityStudentRegisterBinding

    private var groups: List<GroupDto> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rollNo = intent.getStringExtra(EXTRA_ROLL_NO)

        if (rollNo.isNullOrBlank()) {
            finish()
            return
        }

        binding.etRollNo.setText(rollNo)
        binding.etRollNo.isEnabled = false

        loadGroups()

        binding.btnRegister.setOnClickListener {
            registerStudent()
        }
    }

    private fun setLoading(loading: Boolean) {

        binding.progress.visibility =
            if (loading) android.view.View.VISIBLE
            else android.view.View.GONE

        binding.btnRegister.isEnabled = !loading
    }

    private fun loadGroups() {

        binding.tvStatus.text =
            "Loading groups..."

        lifecycleScope.launch {

            try {

                /*
                 * IMPORTANT:
                 *
                 * Student registration is currently designed to allow
                 * selecting a group.
                 *
                 * Therefore this endpoint should eventually be public or
                 * replaced with an admin-controlled registration mechanism.
                 *
                 * For the current MVP, use the group-list endpoint.
                 */

                val response = ApiClient.service()
                    .getGroups()

                if (response.isSuccessful) {

                    groups = response.body().orEmpty()

                    if (groups.isEmpty()) {

                        binding.tvStatus.text =
                            "No groups available"

                        return@launch
                    }

                    setupGroupSpinner()

                    binding.tvStatus.text = ""

                } else {

                    binding.tvStatus.text =
                        "Unable to load groups"

                }

            } catch (e: Exception) {

                binding.tvStatus.text =
                    "Network error: ${e.message}"
            }
        }
    }

    private fun setupGroupSpinner() {

        val names = groups.map {
            it.name
        }

        val adapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            names
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.spinnerGroup.adapter = adapter
    }

    private fun registerStudent() {

        val rollNo = binding.etRollNo.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val name = binding.etName.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val email = binding.etEmail.text
            ?.toString()
            ?.trim()
            .orEmpty()

        if (name.isEmpty()) {
            binding.etName.error = "Enter your name"
            return
        }

        if (email.isEmpty()) {
            binding.etEmail.error = "Enter your email"
            return
        }

        if (groups.isEmpty()) {
            binding.tvStatus.text =
                "No group selected"

            return
        }

        val selectedIndex =
            binding.spinnerGroup.selectedItemPosition

        if (selectedIndex < 0 || selectedIndex >= groups.size) {
            binding.tvStatus.text =
                "Select your group"

            return
        }

        val selectedGroup = groups[selectedIndex]

        val androidId = DeviceId.get(this)

        if (androidId.isBlank()) {
            binding.tvStatus.text =
                "Unable to identify this device"

            return
        }

        setLoading(true)

        lifecycleScope.launch {

            try {

                val response = ApiClient.service()
                    .studentRegister(
                        StudentRegisterRequest(
                            rollNo = rollNo,
                            name = name,
                            email = email,
                            groupId = selectedGroup.id,
                            androidId = androidId
                        )
                    )

                if (response.isSuccessful) {

                    val body = response.body()

                    if (body == null) {
                        binding.tvStatus.text =
                            "Invalid server response"
                        return@launch
                    }

                    val student = body.student

                    Prefs.saveStudent(
                        token = body.token,
                        id = student.id,
                        rollNo = student.rollNo,
                        name = student.name,
                        email = student.email.orEmpty(),
                        groupId = student.groupId,
                        groupName = student.groupName
                    )

                    openStudentHome()

                } else {

                    binding.tvStatus.text =
                        when (response.code()) {

                            409 ->
                                "Student/device/email already registered"

                            else ->
                                response.errorBody()?.string()
                                    ?: "Registration failed"
                        }
                }

            } catch (e: Exception) {

                binding.tvStatus.text =
                    "Network error: ${e.message}"

            } finally {

                setLoading(false)
            }
        }
    }

    private fun openStudentHome() {

        val intent = Intent(
            this,
            StudentHomeActivity::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
    }
}