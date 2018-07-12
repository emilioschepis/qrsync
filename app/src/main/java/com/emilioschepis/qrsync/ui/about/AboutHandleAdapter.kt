package com.emilioschepis.qrsync.ui.about

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
import com.emilioschepis.qrsync.model.QSHandle

class AboutHandleAdapter(private val listener: (QSHandle) -> Unit) : ListAdapter<QSHandle, AboutHandleAdapter.ViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AboutHandleAdapter.ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.item_handle, parent, false)
        return AboutHandleAdapter.ViewHolder(layout).apply {
            itemView.setOnClickListener { listener.invoke(getItem(this.adapterPosition)) }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(handle: QSHandle) {
            val nameTev = itemView.findViewById<TextView>(R.id.item_handle_name)
            val iconImv = itemView.findViewById<ImageView>(R.id.item_handle_icon)

            nameTev.setText(handle.name)
            Glide.with(itemView).load(handle.icon).into(iconImv)
        }
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_action

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<QSHandle>() {
            override fun areItemsTheSame(oldItem: QSHandle?, newItem: QSHandle?): Boolean = oldItem == newItem
            override fun areContentsTheSame(oldItem: QSHandle?, newItem: QSHandle?): Boolean = oldItem == newItem
        }
    }
}