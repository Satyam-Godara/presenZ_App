package com.presenz.app.teacher

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.presenz.app.databinding.ItemHistoryRowBinding
import com.presenz.app.network.SessionHistoryRow
import java.text.SimpleDateFormat
import java.util.Locale
import android.util.Log

class HistoryAdapter(
    private val items: List<SessionHistoryRow>,
    private val onClick: (SessionHistoryRow) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.VH>() {

//    private val displayFmt = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
//    private val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    private val displayFmt = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).apply {
        timeZone = java.util.TimeZone.getTimeZone("Asia/Kolkata")
    }

    private val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = java.util.TimeZone.getTimeZone("UTC")
    }


    inner class VH(val binding: ItemHistoryRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemHistoryRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
//        Log.e("None",items.toString());
        holder.binding.tvTitle.text = "${item.subject} · ${item.group}"
        val dateStr = try {
            displayFmt.format(isoFmt.parse(item.startedAt) ?: java.util.Date())
        } catch (e: Exception) {
            item.startedAt
        }
        holder.binding.tvSubtitle.text = "$dateStr · ${item.presentCount} present"

        holder.binding.root.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount() = items.size
}