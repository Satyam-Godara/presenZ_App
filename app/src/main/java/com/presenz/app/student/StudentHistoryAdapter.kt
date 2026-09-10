package com.presenz.app.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.presenz.app.data.AttendanceRecord
import com.presenz.app.databinding.ItemHistoryRowBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudentHistoryAdapter(private val items: List<AttendanceRecord>) :
    RecyclerView.Adapter<StudentHistoryAdapter.VH>() {

    private val fmt = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    inner class VH(val binding: ItemHistoryRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemHistoryRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.binding.tvTitle.text = item.subject
        holder.binding.tvSubtitle.text = "${fmt.format(Date(item.timestampMillis))} · Present"
    }

    override fun getItemCount() = items.size
}
