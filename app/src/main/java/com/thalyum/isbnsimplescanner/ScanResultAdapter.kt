package com.thalyum.isbnsimplescanner

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ScanResultAdapter() :
    ListAdapter<ScanResult, ScanResultAdapter.ScanViewHolder>(ScanResultDiffCallback) {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ScanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Define click listener for the ViewHolder's View
        val isbn: TextView = view.findViewById(R.id.ScanResultISBN)
        val collectionId: TextView = view.findViewById(R.id.ScanResultCollection)
        val sentStatus: TextView = view.findViewById(R.id.ScanResultSent)

        fun bind(scanResult: ScanResult) {
            // fill ISBN
            isbn.text = scanResult.isbn

            // fill collection ID column
            val collection = scanResult.collection_id
            if (collection != 0) {
                collectionId.text = collection.toString()
            } else {
                collectionId.text =
                    itemView.context.getString(R.string.no_collection)
            }

            // fill 'Sent' column
            if (scanResult.sent) {
                sentStatus.text = itemView.context.getString(R.string.result_sent)
                sentStatus.setTextColor(Color.GREEN)
            } else {
                sentStatus.text =
                    itemView.context.getString(R.string.result_not_sent)
                sentStatus.setTextColor(Color.RED)
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.scan_result_row, parent, false)

        return ScanViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
        val scanResult = getItem(position)
        holder.bind(scanResult)
    }

    object ScanResultDiffCallback : DiffUtil.ItemCallback<ScanResult>() {
        override fun areItemsTheSame(oldItem: ScanResult, newItem: ScanResult): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ScanResult, newItem: ScanResult): Boolean {
            return oldItem.isbn == newItem.isbn
        }
    }
}

