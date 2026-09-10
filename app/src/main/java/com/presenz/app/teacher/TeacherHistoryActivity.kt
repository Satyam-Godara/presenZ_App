package com.presenz.app.teacher

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.presenz.app.databinding.ActivityTeacherHistoryBinding
import com.presenz.app.network.ApiClient
import kotlinx.coroutines.launch

class TeacherHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.rvHistory.layoutManager = LinearLayoutManager(this)

        loadHistory()
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            try {
                binding.progress.visibility = View.VISIBLE
                val resp = ApiClient.service().getSessionHistory(ApiClient.teacherBearer())
                if (resp.isSuccessful && resp.body() != null) {
                    val rows = resp.body()!!
                    if (rows.isEmpty()) {
                        binding.tvEmpty.visibility = android.view.View.VISIBLE
                    } else {
                        binding.rvHistory.adapter = HistoryAdapter(rows) { session ->
                            val intent = Intent(this@TeacherHistoryActivity, SessionDetailActivity::class.java)
                            intent.putExtra(SessionDetailActivity.EXTRA_SESSION_ID, session.sessionId)
                            intent.putExtra(SessionDetailActivity.EXTRA_SUBJECT, session.subject)
                            startActivity(intent)
                        }
                    }
                } else {
                    binding.tvEmpty.visibility = android.view.View.VISIBLE
                    binding.tvEmpty.text = "Could not load history (${resp.code()})"
                }
            } catch (e: Exception) {
                binding.tvEmpty.visibility = android.view.View.VISIBLE
                binding.tvEmpty.text = "Network error: ${e.message}"
            }
            finally {
                binding.progress.visibility = View.GONE

            }
        }
    }
}