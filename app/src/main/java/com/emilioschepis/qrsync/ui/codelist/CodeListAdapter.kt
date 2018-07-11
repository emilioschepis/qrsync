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
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.text.DateFormat

class CodeListAdapter(private val listener: (String) -> Unit) : ListAdapter<QSCode, CodeListAdapter.ViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_code, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position), listener)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(code: QSCode, listener: (String) -> Unit) {
            val titleTev by lazy { itemView.findViewById<TextView>(R.id.item_code_title) }
            val timeTev by lazy { itemView.findViewById<TextView>(R.id.item_code_time) }
            val iconImv by lazy { itemView.findViewById<ImageView>(R.id.item_code_type) }

            doAsync {
                val content = if (code.title.isBlank()) code.content else code.title
                val formattedDate = code.formattedDate
                val typeIcon = code.typeIcon

                uiThread {
                    titleTev.text = content
                    timeTev.text = formattedDate
                    Glide.with(itemView)
                            .load(typeIcon)
                            .into(iconImv)
                    itemView.setOnClickListener { listener.invoke(code.id) }
                }
            }
        }

        private val QSCode.typeIcon: Int
            get() {
                return when (type) {
                    QSCode.CodeType.UNKNOWN -> R.drawable.ic_help_outline_black_24dp
                    QSCode.CodeType.TEXT -> R.drawable.ic_short_text_black_24dp
                    QSCode.CodeType.URL -> R.drawable.ic_link_black_24dp
                    QSCode.CodeType.CONTACT -> R.drawable.ic_person_black_24dp
                    QSCode.CodeType.EMAIL -> R.drawable.ic_email_black_24dp
                    QSCode.CodeType.SMS -> R.drawable.ic_sms_black_24dp
                    QSCode.CodeType.PHONE -> R.drawable.ic_phone_black_24dp
                    QSCode.CodeType.CALENDAR -> R.drawable.ic_event_black_24dp
                    QSCode.CodeType.PRODUCT -> R.drawable.ic_shopping_cart_black_24dp
                    QSCode.CodeType.ISBN -> R.drawable.ic_book_black_24dp
                }
            }

        private val QSCode.formattedDate: String
            get() = DateFormat
                    .getDateTimeInstance()
                    .format(timestamp.toDate()) ?: ""

    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<QSCode>() {
            override fun areItemsTheSame(oldItem: QSCode?, newItem: QSCode?): Boolean = oldItem?.id == newItem?.id
            override fun areContentsTheSame(oldItem: QSCode?, newItem: QSCode?): Boolean = oldItem == newItem
        }
    }
}