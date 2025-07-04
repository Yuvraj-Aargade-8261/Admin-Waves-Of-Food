package com.example.adminwavesoffood.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adminwavesoffood.databinding.CompleteditemsBinding
import com.example.adminwavesoffood.model.OrderDetails

class CompletedOrderAdapter(
    private val context: Context,
    private val list: List<OrderDetails>,
    private val listener: OnItemClicked
) : RecyclerView.Adapter<CompletedOrderAdapter.CompletedViewHolder>() {

    interface OnItemClicked {
        fun onItemClick(position: Int)
    }

    inner class CompletedViewHolder(val binding: CompleteditemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onItemClick(pos)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompletedViewHolder {
        val binding = CompleteditemsBinding.inflate(LayoutInflater.from(context), parent, false)
        return CompletedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CompletedViewHolder, position: Int) {
        val item = list[position]

        holder.binding.apply {
            completedUserName.text = item.userNames ?: "Customer"
            completedQuantity.text = item.foodQuantities?.sum()?.toString() ?: "0"
            completedTotal.text = item.totalPrices ?: "₹0"

            if (!item.foodImages.isNullOrEmpty()) {
                Glide.with(context)
                    .load(item.foodImages[0])
                    .into(completedImage)
            } else {
                completedImage.setImageResource(android.R.color.darker_gray)
            }
        }
    }

    override fun getItemCount(): Int = list.size
}
