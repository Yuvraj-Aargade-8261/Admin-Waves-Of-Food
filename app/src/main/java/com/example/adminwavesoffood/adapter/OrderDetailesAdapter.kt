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
    private val foodNames: ArrayList<String>,
    private val foodImages: ArrayList<String>,
    private val foodQuantities: ArrayList<Int>,
    private val foodPrices: ArrayList<String>
) : RecyclerView.Adapter<OrderDetailesAdapter.OrderDetailesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailesViewHolder {
        val binding = OrderdetailesitemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderDetailesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderDetailesViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = foodNames.size

    inner class OrderDetailesViewHolder(private val binding: OrderdetailesitemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val priceValue = foodPrices[position].toIntOrNull()
            val formattedPrice = priceValue?.let { "₹ ${String.format("%,d", it)}" }
                ?: "₹ ${foodPrices[position]}"

            binding.apply {
                customernameorderdetailes.text = foodNames[position]
                orderdetailesquantitiyy.text = foodQuantities[position].toString()
                orderdetailesprices.text = formattedPrice
                Glide.with(context).load(Uri.parse(foodImages[position])).into(orderdetailesfoodimage)
            }
        }
    }
}
