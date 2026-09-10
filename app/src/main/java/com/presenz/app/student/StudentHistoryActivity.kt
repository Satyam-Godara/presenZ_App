package com.presenz.app.student

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.presenz.app.data.AppDatabase
import com.presenz.app.databinding.ActivityStudentHistoryBinding
import kotlinx.coroutines.launch

class StudentHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.rvHistory.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            val records = AppDatabase.get(this@StudentHistoryActivity).attendanceDao().getHistoryForRole("STUDENT")
            if (records.isEmpty()) {
                binding.tvEmpty.visibility = android.view.View.VISIBLE
            } else {
                binding.rvHistory.adapter = StudentHistoryAdapter(records)
            }
        }
    }
}
