package com.presenz.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.presenz.app.databinding.ActivityMainBinding
import com.presenz.app.student.StudentHomeActivity
import com.presenz.app.student.StudentLoginActivity
import com.presenz.app.teacher.TeacherHomeActivity
import com.presenz.app.teacher.TeacherLoginActivity
import com.presenz.app.util.BlePermissions
import com.presenz.app.util.Prefs

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* results handled lazily - each screen re-checks before using BLE */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Prefs.init(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!BlePermissions.allGranted(this)) {
            permissionLauncher.launch(BlePermissions.required())
        }

        binding.btnTeacher.setOnClickListener {
            val dest = if (Prefs.teacherToken.isNullOrBlank()) {
                Intent(this, TeacherLoginActivity::class.java)
            } else {
                Intent(this, TeacherHomeActivity::class.java)
            }
            startActivity(dest)
        }

        binding.btnStudent.setOnClickListener {
//            val dest = if (Prefs.teacherToken.isNullOrBlank()) {
//                Intent(this, TeacherLoginActivity::class.java)
//            } else {
//                Intent(this, StudentHomeActivity::class.java)
//            }
//            startActivity(dest)
            val destination =
                if (Prefs.isStudentLoggedIn()) {
                    StudentHomeActivity::class.java
                } else {
                    StudentLoginActivity::class.java
                }

            startActivity(
                Intent(this, destination)
            )
        }
    }
}
