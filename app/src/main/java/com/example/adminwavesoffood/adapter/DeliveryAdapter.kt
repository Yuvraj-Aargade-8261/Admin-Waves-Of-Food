package com.example.adminwavesoffood.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.adminwavesoffood.databinding.DeliveryitemoutBinding

class DeliveryAdapter(
    private val customerNames: MutableList<String>,
    private val moneyStatuses: MutableList<Boolean>
) : RecyclerView.Adapter<DeliveryAdapter.DeliveryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryViewHolder {
        val binding = DeliveryitemoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeliveryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeliveryViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = customerNames.size

    inner class DeliveryViewHolder(private val binding: DeliveryitemoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val status = moneyStatuses[position]
            val color = if (status) Color.GREEN else Color.RED

            binding.apply {
                customername.text = customerNames[position]
                moneystatus.text = if (status) "Received" else "Not Received"
                moneystatus.setTextColor(color)
                statusgreenred.backgroundTintList = ColorStateList.valueOf(color)
            }
        }
    }
}
