package com.example.adminwavesoffood.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adminwavesoffood.databinding.OrderdetailesitemsBinding

class OrderDetailesAdapter(
    private val context: Context,
    private val foodNames: List<String>,
    private val foodImages: List<String>,
    private val foodQuantities: List<Int>,
    private val foodPrices: List<String>
) : RecyclerView.Adapter<OrderDetailesAdapter.OrderDetailesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailesViewHolder {
        val binding = OrderdetailesitemsBinding.inflate(LayoutInflater.from(context), parent, false)
        return OrderDetailesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderDetailesViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = foodNames.size

    inner class OrderDetailesViewHolder(private val binding: OrderdetailesitemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val name = foodNames.getOrNull(position) ?: "Item"
            val quantity = foodQuantities.getOrNull(position)?.toString() ?: "0"
            val imageUrl = foodImages.getOrNull(position)
            val priceStr = foodPrices.getOrNull(position) ?: "0"

            val formattedPrice = priceStr.toIntOrNull()?.let {
                "₹ ${String.format("%,d", it)}"
            } ?: "₹ $priceStr"

            binding.apply {
                customernameorderdetailes.text = name
                orderdetailesquantitiyy.text = quantity
                orderdetailesprices.text = formattedPrice

                if (!imageUrl.isNullOrBlank()) {
                    Glide.with(context)
                        .load(Uri.parse(imageUrl))
                        .into(orderdetailesfoodimage)
                } else {
                    orderdetailesfoodimage.setImageResource(android.R.color.darker_gray)
                }
            }
        }
    }
}
