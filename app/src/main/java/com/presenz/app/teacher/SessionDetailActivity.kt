package com.presenz.app.teacher

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.presenz.app.databinding.ActivitySessionDetailBinding
import com.presenz.app.network.ApiClient
import com.presenz.app.network.AttendanceStudentRow
import com.presenz.app.network.FullAttendanceResponse
import kotlinx.coroutines.launch

class SessionDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SESSION_ID = "extra_session_id"
        const val EXTRA_SUBJECT = "extra_subject"
    }

    private lateinit var binding: ActivitySessionDetailBinding
    private var sessionId: String? = null
    private var loadedData: FullAttendanceResponse? = null

    private val createCsvLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> writeCsvToUri(uri) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySessionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
        binding.tvSubject.text = intent.getStringExtra(EXTRA_SUBJECT) ?: "Session"

        binding.rvPresent.layoutManager = LinearLayoutManager(this)
        binding.rvAbsent.layoutManager = LinearLayoutManager(this)

        binding.btnExport.setOnClickListener { launchExportPicker() }

        loadDetail()
    }

    private fun loadDetail() {
        val sid = sessionId
        if (sid.isNullOrBlank()) {
            binding.tvStatus.text = "Missing session id"
            return
        }

        binding.tvStatus.text = "Loading attendance..."
        binding.tvStatus.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val resp = ApiClient.service().getFullAttendance(ApiClient.teacherBearer(), sid)
                if (resp.isSuccessful && resp.body() != null) {
                    val body = resp.body()!!
                    loadedData = body

                    binding.tvSubject.text = "${body.subject} · ${body.groupName}"
                    binding.tvPresentCount.text = "Present (${body.present.size})"
                    binding.tvAbsentCount.text = "Absent (${body.absent.size})"

                    binding.rvPresent.adapter = AttendanceRowAdapter(body.present, present = true)
                    binding.rvAbsent.adapter = AttendanceRowAdapter(body.absent, present = false)

                    binding.tvStatus.visibility = View.GONE
                    binding.btnExport.isEnabled = true
                } else {
                    binding.tvStatus.text = "Failed to load (${resp.code()})"
                }
            } catch (e: Exception) {
                binding.tvStatus.text = "Network error: ${e.message}"
            }
        }
    }

    private fun launchExportPicker() {
        val data = loadedData ?: return
        val fileName = "attendance_${data.subject}_${sessionId}.csv"
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        createCsvLauncher.launch(intent)
    }

    private fun writeCsvToUri(uri: Uri) {
        val data = loadedData ?: return
        try {
            contentResolver.openOutputStream(uri)?.use { out ->
                out.write(buildCsv(data).toByteArray())
            }
            Toast.makeText(this, "Exported successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildCsv(data: FullAttendanceResponse): String {
        val sb = StringBuilder()
        sb.append("Roll No,Name,Status,Marked At\n")

        fun appendRow(row: AttendanceStudentRow, status: String) {
            val roll = row.rollNo.replace(",", " ")
            val name = row.name.replace(",", " ")
            val markedAt = row.markedAt ?: ""
            sb.append("$roll,$name,$status,$markedAt\n")
        }

        data.present.forEach { appendRow(it, "Present") }
        data.absent.forEach { appendRow(it, "Absent") }

        return sb.toString()
    }
}