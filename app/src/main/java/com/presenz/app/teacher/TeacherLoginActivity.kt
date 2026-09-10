package com.presenz.app.teacher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.presenz.app.databinding.ActivityTeacherLoginBinding
import com.presenz.app.network.ApiClient
import com.presenz.app.network.LoginRequest
import com.presenz.app.network.RegisterRequest
import com.presenz.app.util.Prefs
import kotlinx.coroutines.launch
import android.util.Log
import android.util.StatsLog

class TeacherLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener { doLogin() }
//        binding.btnRegister.setOnClickListener { doRegister() }
    }

    private fun setLoading(loading: Boolean) {
        binding.progress.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnLogin.isEnabled = !loading
//        binding.btnRegister.isEnabled = !loading
    }

    private fun doLogin() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString()?.trim().orEmpty()
        if (email.isEmpty() || password.isEmpty()) {
            binding.tvStatus.text = "Enter email and password"
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val resp = ApiClient.service().login(LoginRequest(email, password))
                if (resp.isSuccessful && resp.body() != null) {
                    val body = resp.body()!!
                    Log.e("Teacher Token",body.token);
                    Prefs.teacherToken = body.token
                    Prefs.teacherName = body.teacher.name
                    Prefs.teacherEmail = body.teacher.email
                    startActivity(Intent(this@TeacherLoginActivity, TeacherHomeActivity::class.java))
                    finish()
                } else {
                    binding.tvStatus.text = "Login failed: ${resp.errorBody()?.string() ?: resp.code()}"
                }
            } catch (e: Exception) {
                binding.tvStatus.text = "Network error: ${e.message}. Check server URL in Prefs.baseUrl"
            } finally {
                setLoading(false)
            }
        }
    }

//    private fun doRegister() {
//        val name = binding.etName.text?.toString()?.trim().orEmpty()
//        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
//        val password = binding.etPassword.text?.toString()?.trim().orEmpty()
//        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
//            binding.tvStatus.text = "Fill name, email and password to register"
//            return
//        }
//
//        setLoading(true)
//        lifecycleScope.launch {
//            try {
//                val resp = ApiClient.service().register(RegisterRequest(name, email, password))
//                if (resp.isSuccessful) {
//                    binding.tvStatus.text = "Registered! Now tap Login."
//                } else {
//                    binding.tvStatus.text = "Register failed: ${resp.errorBody()?.string() ?: resp.code()}"
//                }
//            } catch (e: Exception) {
//                binding.tvStatus.text = "Network error: ${e.message}"
//            } finally {
//                setLoading(false)
//            }
//        }
//    }
}
