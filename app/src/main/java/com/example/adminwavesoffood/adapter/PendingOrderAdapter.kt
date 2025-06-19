package com.example.adminwavesoffood.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adminwavesoffood.databinding.PendingitemBinding

class PendingOrderAdapter(
    private val context: Context,
    private val customerNames: MutableList<String>,
    private val quantities: MutableList<String>,
    private val foodImages: MutableList<String>,
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

    override fun getItemCount(): Int = customerNames.size

    inner class PendingViewHolder(private val binding: PendingitemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var isAccepted = false

        fun bind(position: Int) {
            if (position >= customerNames.size) return

            binding.apply {
                customernamepending2.text = customerNames[position]
                pendingorderquantity.text = quantities[position]
                Glide.with(context).load(Uri.parse(foodImages[position])).into(orderfooditemimage)

                acceptbutton.text = if (isAccepted) "Dispatch" else "Accept"

                acceptbutton.setOnClickListener {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        if (!isAccepted) {
                            isAccepted = true
                            acceptbutton.text = "Dispatch"
                            showToast("Order is Accepted")
                            itemClicked.onItemAcceptClickListener(pos)
                        } else {
                            customerNames.removeAt(pos)
                            quantities.removeAt(pos)
                            foodImages.removeAt(pos)
                            notifyItemRemoved(pos)
                            showToast("Order is Dispatched")
                            itemClicked.onItemDispatchClickListener(pos)
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
        }

        private fun showToast(message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
