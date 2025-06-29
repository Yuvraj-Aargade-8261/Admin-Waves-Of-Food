package com.example.adminwavesoffood.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adminwavesoffood.databinding.PendingitemBinding
import com.example.adminwavesoffood.model.OrderDetails

class PendingOrderAdapter(
    private val context: Context,
    private val orderList: List<OrderDetails>,
    private val itemClicked: OnItemClicked
) : RecyclerView.Adapter<PendingOrderAdapter.PendingViewHolder>() {

    interface OnItemClicked {
        fun onItemClickListener(position: Int)
        fun onItemAcceptClickListener(position: Int)
        fun onItemDispatchClickListener(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingViewHolder {
        val binding = PendingitemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PendingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PendingViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = orderList.size

    inner class PendingViewHolder(private val binding: PendingitemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val order = orderList[position]

            binding.customernamepending2.text = order.userNames ?: "Unnamed"
            binding.pendingorderquantity.text = order.foodPrices?.firstOrNull() ?: "0"
            Glide.with(context)
                .load(Uri.parse(order.foodImages?.firstOrNull()))
                .into(binding.orderfooditemimage)

            if (order.orderAccepted == true) {
                binding.acceptbutton.text = "Dispatch"
            } else {
                binding.acceptbutton.text = "Accept"
            }

            binding.acceptbutton.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    if (order.orderAccepted == true) {
                        itemClicked.onItemDispatchClickListener(pos)
                        showToast("Order is Dispatched")
                    } else {
                        itemClicked.onItemAcceptClickListener(pos)
                        showToast("Order is Accepted")
                    }
                }
            }

            itemView.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    itemClicked.onItemClickListener(pos)
                }
            }
        }

        private fun showToast(message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
