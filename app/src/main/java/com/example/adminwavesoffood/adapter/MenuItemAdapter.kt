package com.example.adminwavesoffood.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adminwavesoffood.databinding.AllitememnuBinding
import com.example.adminwavesoffood.model.AllMenu
import com.google.firebase.database.DatabaseReference

class MenuItemAdapter(
    private val context: Context,
    private val menuList: ArrayList<AllMenu>,
    databaseReference: DatabaseReference,
    private val onDeleteClickListener: (position: Int) -> Unit
) : RecyclerView.Adapter<MenuItemAdapter.AddItemViewHolder>() {

    private val itemQuantities = IntArray(menuList.size) { 1 }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddItemViewHolder {
        val binding = AllitememnuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddItemViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = menuList.size

    inner class AddItemViewHolder(private val binding: AllitememnuBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val menuItem = menuList[position]
            val quantity = itemQuantities[position]

            binding.apply {
                foodmenutextview.text = menuItem.foodName
                foodmenuprice.text = menuItem.foodPrice
                quantitytextview.text = quantity.toString()
                Glide.with(context).load(Uri.parse(menuItem.foodImage)).into(foodimageview)

                plusbutton.setOnClickListener {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION && itemQuantities[pos] < 10) {
                        itemQuantities[pos]++
                        quantitytextview.text = itemQuantities[pos].toString()
                    }
                }

                minusbutton.setOnClickListener {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION && itemQuantities[pos] > 1) {
                        itemQuantities[pos]--
                        quantitytextview.text = itemQuantities[pos].toString()
                    }
                }

                deletebutton.setOnClickListener {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onDeleteClickListener(pos)
                    }
                }
            }
        }
    }
}
