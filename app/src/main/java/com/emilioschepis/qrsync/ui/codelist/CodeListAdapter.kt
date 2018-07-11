package com.emilioschepis.qrsync.ui.codelist

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.emilioschepis.qrsync.R
import com.emilioschepis.qrsync.model.QSCode

class CodeListAdapter(private val listener: (String) -> Unit) : ListAdapter<QSCode, CodeListAdapter.ViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.item_code, parent, false)
        return ViewHolder(layout).apply {
            itemView.setOnClickListener { listener.invoke(getItem(this.adapterPosition).id) }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(code: QSCode) {
            val titleTev = itemView.findViewById<TextView>(R.id.item_code_title)
            val timeTev = itemView.findViewById<TextView>(R.id.item_code_time)
            val iconImv = itemView.findViewById<ImageView>(R.id.item_code_type)

            titleTev.text = if (code.title.isBlank()) code.content else code.title
            timeTev.text = code.formattedDate
            Glide.with(itemView).load(code.typeIcon).into(iconImv)
        }
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_code

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<QSCode>() {
            override fun areItemsTheSame(oldItem: QSCode?, newItem: QSCode?): Boolean = oldItem?.id == newItem?.id
            override fun areContentsTheSame(oldItem: QSCode?, newItem: QSCode?): Boolean = oldItem == newItem
        }
    }
}