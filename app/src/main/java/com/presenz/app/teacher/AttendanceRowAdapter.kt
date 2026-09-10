package com.presenz.app.teacher

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.presenz.app.databinding.ItemAttendanceRowBinding
import com.presenz.app.network.AttendanceStudentRow
import java.text.SimpleDateFormat
import java.util.Locale

class AttendanceRowAdapter(
    private val items: List<AttendanceStudentRow>,
    private val present: Boolean
) : RecyclerView.Adapter<AttendanceRowAdapter.VH>() {

//    private val displayFmt = SimpleDateFormat("hh:mm a", Locale.getDefault())
//    private val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    private val displayFmt = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).apply {
        timeZone = java.util.TimeZone.getTimeZone("Asia/Kolkata")
    }

    private val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = java.util.TimeZone.getTimeZone("UTC")
    }

    inner class VH(val binding: ItemAttendanceRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAttendanceRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.binding.tvName.text = item.name
        holder.binding.tvRoll.text = item.rollNo

        holder.binding.tvStatus.text = if (present) {
            val timeStr = try {
                item.markedAt?.let { displayFmt.format(isoFmt.parse(it) ?: java.util.Date()) } ?: ""
            } catch (e: Exception) { "" }
            "Present · $timeStr"
        } else {
            "Absent"
        }
    }

    override fun getItemCount() = items.size
}