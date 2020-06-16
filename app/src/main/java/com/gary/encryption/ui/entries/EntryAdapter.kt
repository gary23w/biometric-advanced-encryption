
package com.gary.encryption.ui.entries


import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gary.encryption.R
import com.gary.encryption.model.LogEntry
import com.gary.encryption.util.inflate
import kotlinx.android.synthetic.main.log_entry_layout.view.*


class EntryAdapter(
  private val listener: EntryAdapterListener
) : ListAdapter<LogEntry, EntryAdapter.ViewHolder>(
  EntryDiff
) {

  interface EntryAdapterListener {
    fun onEntryClicked(entry: LogEntry)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(parent.inflate(
      R.layout.log_entry_layout))
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position), listener)
  }

  inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private lateinit var entry: LogEntry

    fun bind(entry: LogEntry, listener: EntryAdapterListener?) {
      this.entry = entry
      itemView.stardate.text = entry.stardate
      itemView.hasImage.setImageResource(R.drawable.ic_check)
      itemView.setOnClickListener {
        listener?.onEntryClicked(entry)
      }
    }
  }
}

object EntryDiff : DiffUtil.ItemCallback<LogEntry>() {
  override fun areItemsTheSame(oldItem: LogEntry, newItem: LogEntry) = oldItem == newItem
  override fun areContentsTheSame(oldItem: LogEntry, newItem: LogEntry) = oldItem == newItem
}
