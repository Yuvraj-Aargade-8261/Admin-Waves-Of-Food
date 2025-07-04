package com.example.adminwavesoffood

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminwavesoffood.adapter.CompletedOrderAdapter
import com.example.adminwavesoffood.databinding.ActivityCompletedOrderBinding
import com.example.adminwavesoffood.model.OrderDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CompletedOrderActivity : AppCompatActivity(), CompletedOrderAdapter.OnItemClicked {

    private lateinit var binding: ActivityCompletedOrderBinding
    private val completedOrderList = arrayListOf<OrderDetails>()

    private lateinit var auth: FirebaseAuth
    private lateinit var completedRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompletedOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val hotelUserId = auth.currentUser?.uid ?: return

        completedRef = FirebaseDatabase.getInstance().reference
            .child("Hotel Users")
            .child(hotelUserId)
            .child("CompletedOrder")

        fetchCompletedOrders()
    }

    private fun fetchCompletedOrders() {
        completedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                completedOrderList.clear()
                for (orderSnap in snapshot.children) {
                    val order = orderSnap.getValue(OrderDetails::class.java)
                    if (order != null) {
                        completedOrderList.add(order)
                    }
                }
                setupAdapter()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CompletedOrderActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("CompletedOrders", error.message)
            }
        })
    }

    private fun setupAdapter() {
        binding.completedorderrecycler.layoutManager = LinearLayoutManager(this)
        binding.completedorderrecycler.adapter = CompletedOrderAdapter(
            context = this,
            list = completedOrderList,
            listener = this
        )
    }

    override fun onItemClick(position: Int) {
        val intent = Intent(this, OrderDetailesAActivity::class.java)
        intent.putExtra("userOrderDetailes", completedOrderList[position])
        startActivity(intent)
    }
}
