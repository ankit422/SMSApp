package com.smsapp

import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.smsapp.databinding.HeaderViewBinding
import com.smsapp.databinding.ItemViewBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MessageAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1

    private var list: List<Messages> = ArrayList()

    fun setData(data: List<Messages>) {
        list = data
        notifyDataSetChanged()
    }

    fun addNewMessage(msg: Messages) {
        list = listOf(msg).plus(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType === TYPE_HEADER) {
            val v: HeaderViewBinding = HeaderViewBinding.inflate(inflater, parent, false)
            VHHeader(v)
        } else {
            val v: ItemViewBinding = ItemViewBinding.inflate(inflater, parent, false)
            DataViewHolder(v)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is VHHeader)
            holder.bind(list[position])
        else
            (holder as DataViewHolder).bind(list[position])
    }


    override fun getItemViewType(position: Int): Int {
        if (isPositionHeader(position))
            return TYPE_HEADER
        return TYPE_ITEM
    }

    private fun isPositionHeader(position: Int): Boolean {
        return list[position].type == "header"
    }

    override fun getItemCount(): Int = list.size
}

class VHHeader(private val itemBinding: HeaderViewBinding) :
    RecyclerView.ViewHolder(itemBinding.root) {
    @RequiresApi(Build.VERSION_CODES.N)
    fun bind(dataItem: Messages) {
        itemBinding.title.text =
            Html.fromHtml("<b>${dataItem.dayDiff}</b>", Html.FROM_HTML_MODE_LEGACY)
    }
}

class DataViewHolder(private val itemBinding: ItemViewBinding) :
    RecyclerView.ViewHolder(itemBinding.root) {
    private lateinit var message: Messages

    @RequiresApi(Build.VERSION_CODES.N)
    fun bind(dataItem: Messages) {
        try {
            message = dataItem
            itemBinding.sender.text =
                Html.fromHtml("<b>Sender:</b> ${message.sender}", Html.FROM_HTML_MODE_LEGACY)
            itemBinding.message.text =
                Html.fromHtml("<b>Message:</b> ${message.message}", Html.FROM_HTML_MODE_LEGACY)
            itemBinding.date.text =
                Html.fromHtml(
                    "<b>Date:</b> ${millisecondToDate(message.date)}", Html.FROM_HTML_MODE_LEGACY
                )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun millisecondToDate(date: String?): String? {
        var dateString: String? = null
        val dateFormat = SimpleDateFormat("dd MMM,yyyy hh:mm:ss a")
        try {
            if (date != null && date.isNotEmpty()) {
                val value = date.toDouble().toLong()
                dateString = dateFormat.format(Date(value))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return dateString
    }
}