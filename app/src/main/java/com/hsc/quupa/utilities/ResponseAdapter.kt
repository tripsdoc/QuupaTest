package com.hsc.quupa.utilities

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hsc.quupa.R
import kotlinx.android.synthetic.main.text_response.view.*

class ResponseAdapter(): RecyclerView.Adapter<ResponseAdapter.ViewHolder>() {
    private var responseList: ArrayList<String> = ArrayList()

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun setView(position: Int) {
            itemView.textResponse.text = "${responseList[position]} ms"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val cell = inflater.inflate(R.layout.text_response, parent, false)
        return ViewHolder(cell)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setView(position)
    }

    override fun getItemCount(): Int {
        return responseList.size
    }

    fun addResponse(responses: List<String>) {
        responseList.addAll(responses)
        notifyDataSetChanged()
    }

    fun clear() {
        responseList.clear()
        notifyDataSetChanged()
    }
}