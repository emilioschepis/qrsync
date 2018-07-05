package com.emilioschepis.qrsync.ui.detail

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
import com.emilioschepis.qrsync.model.QSCodeAction

class DetailActionAdapter(private val listener: (QSCodeAction) -> Unit) : ListAdapter<QSCodeAction, DetailActionAdapter.ViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_action, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position), listener)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(action: QSCodeAction, listener: (QSCodeAction) -> Unit) {
            val nameTev by lazy { itemView.findViewById<TextView>(R.id.item_action_name) }
            val iconImv by lazy { itemView.findViewById<ImageView>(R.id.item_action_icon) }

            nameTev.setText(action.name)

            Glide.with(itemView)
                    .load(action.icon)
                    .into(iconImv)

            itemView.setOnClickListener { listener.invoke(action) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<QSCodeAction>() {
            override fun areItemsTheSame(oldItem: QSCodeAction?, newItem: QSCodeAction?): Boolean = oldItem == newItem
            override fun areContentsTheSame(oldItem: QSCodeAction?, newItem: QSCodeAction?): Boolean = oldItem == newItem
        }
    }
}