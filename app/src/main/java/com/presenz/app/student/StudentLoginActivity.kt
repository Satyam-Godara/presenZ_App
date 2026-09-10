package com.presenz.app.student

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.presenz.app.databinding.ActivityStudentLoginBinding
import com.presenz.app.network.ApiClient
import com.presenz.app.network.StudentCheckRequest
import com.presenz.app.network.StudentLoginRequest
import com.presenz.app.util.DeviceId
import com.presenz.app.util.Prefs
import kotlinx.coroutines.launch

class StudentLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnContinue.setOnClickListener {
            checkStudent()
        }
    }

    private fun setLoading(loading: Boolean) {

        binding.progress.visibility =
            if (loading) android.view.View.VISIBLE
            else android.view.View.GONE

        binding.btnContinue.isEnabled = !loading
    }

    private fun checkStudent() {

        val rollNo = binding.etRollNo.text
            ?.toString()
            ?.trim()
            .orEmpty()

        if (rollNo.isEmpty()) {
            binding.etRollNo.error = "Enter roll number"
            return
        }

        setLoading(true)

        lifecycleScope.launch {

            try {

                val response = ApiClient.service()
                    .checkStudent(
                        StudentCheckRequest(rollNo)
                    )

                if (!response.isSuccessful) {

                    binding.tvStatus.text =
                        response.errorBody()?.string()
                            ?: "Unable to check student"

                    return@launch
                }

                val body = response.body()

                if (body == null) {
                    binding.tvStatus.text =
                        "Invalid server response"

                    return@launch
                }

                if (!body.exists) {

                    // New student
                    val intent = Intent(
                        this@StudentLoginActivity,
                        StudentRegisterActivity::class.java
                    )

                    intent.putExtra(
                        StudentRegisterActivity.EXTRA_ROLL_NO,
                        rollNo
                    )

                    startActivity(intent)

                } else {

                    // Existing student
                    loginExistingStudent(rollNo)
                }

            } catch (e: Exception) {

                binding.tvStatus.text =
                    "Network error: ${e.message}"

            } finally {

                setLoading(false)
            }
        }
    }

    private fun loginExistingStudent(rollNo: String) {

        val androidId = DeviceId.get(this)

        if (androidId.isBlank()) {
            binding.tvStatus.text =
                "Unable to identify this device"

            return
        }

        lifecycleScope.launch {

            try {

                val response = ApiClient.service()
                    .studentLogin(
                        StudentLoginRequest(
                            rollNo = rollNo,
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

                            403 ->
                                "This device is not registered for this roll number"

                            404 ->
                                "Student account not found"

                            else ->
                                response.errorBody()?.string()
                                    ?: "Login failed"
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