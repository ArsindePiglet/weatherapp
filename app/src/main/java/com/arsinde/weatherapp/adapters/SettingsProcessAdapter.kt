package com.arsinde.weatherapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arsinde.weatherapp.R
import com.arsinde.weatherapp.models.system.RunningProcessData
import kotlinx.android.synthetic.main.card_process.view.*

class SettingsProcessAdapter(private val itemList: List<RunningProcessData>) :
    RecyclerView.Adapter<SettingsProcessAdapter.ProcessesViewHolder>() {

    class ProcessesViewHolder(private val viewItem: View) : RecyclerView.ViewHolder(viewItem) {
        fun bind(processData: RunningProcessData) {
            viewItem.tvServiceTitle.text = processData.process
            viewItem.tvServiceSubTitle.text =
                String.format("PID: %d; UID: %d", processData.pid, processData.uid)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProcessesViewHolder {
        val card = LayoutInflater.from(parent.context).inflate(R.layout.card_process, parent, false)
        return ProcessesViewHolder(card)
    }

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: ProcessesViewHolder, position: Int) {
        holder.bind(itemList[position])
    }
}