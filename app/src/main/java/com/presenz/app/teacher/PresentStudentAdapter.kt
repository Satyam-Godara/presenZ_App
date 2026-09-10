package com.presenz.app.teacher

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.presenz.app.databinding.ItemStudentRowBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PresentStudent(val rollNo: String, val markedAtMillis: Long)

class PresentStudentAdapter : RecyclerView.Adapter<PresentStudentAdapter.VH>() {

    private val items = mutableListOf<PresentStudent>()
    private val timeFmt = SimpleDateFormat("hh:mm a", Locale.getDefault())

    fun addIfNew(rollNo: String): Boolean {
        if (items.any { it.rollNo.equals(rollNo, ignoreCase = true) }) return false
        items.add(0, PresentStudent(rollNo, System.currentTimeMillis()))
        notifyItemInserted(0)
        return true
    }

    fun count() = items.size

    inner class VH(val binding: ItemStudentRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemStudentRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.binding.tvRollNo.text = item.rollNo
        holder.binding.tvTime.text = timeFmt.format(Date(item.markedAtMillis))
    }

    override fun getItemCount() = items.size
}
