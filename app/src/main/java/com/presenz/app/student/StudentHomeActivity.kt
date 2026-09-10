//package com.presenz.app.student
//
//import android.content.Intent
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import com.presenz.app.databinding.ActivityStudentHomeBinding
//import com.presenz.app.util.Prefs
//
//class StudentHomeActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityStudentHomeBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityStudentHomeBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        binding.etName.setText(Prefs.studentName ?: "")
//        binding.etRollNo.setText(Prefs.studentRollNo ?: "")
//
//        binding.btnJoin.setOnClickListener {
//            val name = binding.etName.text?.toString()?.trim().orEmpty()
//            val rollNo = binding.etRollNo.text?.toString()?.trim().orEmpty()
//            if (name.isEmpty() || rollNo.isEmpty()) {
//                binding.etRollNo.error = "Name and roll number are required"
//                return@setOnClickListener
//            }
//            Prefs.studentName = name
//            Prefs.studentRollNo = rollNo
//            startActivity(Intent(this, StudentScanActivity::class.java))
//        }
//
//        binding.btnHistory.setOnClickListener {
//            startActivity(Intent(this, StudentHistoryActivity::class.java))
//        }
//    }
//}


package com.presenz.app.student

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.presenz.app.MainActivity
import com.presenz.app.databinding.ActivityStudentHomeBinding
import com.presenz.app.util.Prefs
import com.presenz.app.util.BluetoothEnabler

class StudentHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentHomeBinding
    private val bluetoothEnabler = BluetoothEnabler(this) { enabled ->
        if (!enabled) {
            Toast.makeText(this, "Bluetooth is required to mark attendance", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
         * StudentHome should never be opened without authentication.
         */
        if (!Prefs.isStudentLoggedIn()) {

            startActivity(
                Intent(this, StudentLoginActivity::class.java)
            )

            finish()
            return
        }

        binding = ActivityStudentHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        displayStudent();


        binding.btnJoin.setOnClickListener {
            bluetoothEnabler.ensureEnabledThen {
                startActivity(
                    Intent(
                        this,
                        StudentScanActivity::class.java
                    )
                )
            }
        }


        binding.btnHistory.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    StudentHistoryActivity::class.java
                )
            )
        }


        /*
         * Add a logout button to your XML if you want this.
         *
         * binding.btnLogout.setOnClickListener {
         *     Prefs.clearStudent()
         *
         *     val intent = Intent(
         *         this,
         *         MainActivity::class.java
         *     )
         *
         *     intent.flags =
         *         Intent.FLAG_ACTIVITY_NEW_TASK or
         *         Intent.FLAG_ACTIVITY_CLEAR_TASK
         *
         *     startActivity(intent)
         * }
         */
    }

    private fun displayStudent() {

        binding.etName.setText(
            Prefs.studentName ?: ""
        )

        binding.etRollNo.setText(
            Prefs.studentRollNo ?: ""
        )

        /*
         * If these fields are still present in your existing XML,
         * make them read-only.
         */

        binding.etName.isEnabled = false
        binding.etRollNo.isEnabled = false

        /*
         * If your layout contains a group TextView,
         * use:
         *
         * binding.tvGroup.text =
         *     Prefs.studentGroupName ?: "Unknown group"
         */
    }
}