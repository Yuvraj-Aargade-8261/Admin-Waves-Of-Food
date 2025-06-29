package com.example.adminwavesoffood

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminwavesoffood.adapter.PendingOrderAdapter
import com.example.adminwavesoffood.databinding.ActivityPendingOrderBinding
import com.example.adminwavesoffood.model.OrderDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PendingOrderActivity : AppCompatActivity(), PendingOrderAdapter.OnItemClicked {

    private lateinit var binding: ActivityPendingOrderBinding
    private val orderList = arrayListOf<OrderDetails>()

    private lateinit var database: FirebaseDatabase
    private lateinit var ordersRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPendingOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val hotelUserId = auth.currentUser?.uid ?: return
        ordersRef = database.reference.child("Hotel Users").child(hotelUserId).child("OrderDetails")

        fetchLiveOrders()
    }

    private fun fetchLiveOrders() {
        ordersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                orderList.clear()

                for (snap in snapshot.children) {
                    val order = snap.getValue(OrderDetails::class.java)
                    if (order != null) {
                        orderList.add(order)
                    }
                }

                Log.d("FirebaseDebug", "Hotel Orders: ${orderList.size}")
                setupAdapter()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDebug", "Error: ${error.message}")
            }
        })
    }

    private fun setupAdapter() {
        binding.pendinforderrecycler.layoutManager = LinearLayoutManager(this)
        binding.pendinforderrecycler.adapter = PendingOrderAdapter(
            context = this,
            orderList = orderList,
            itemClicked = this
        )
    }

    override fun onItemClickListener(position: Int) {
        val intent = Intent(this, OrderDetailesAActivity::class.java)
        intent.putExtra("userOrderDetailes", orderList[position])
        startActivity(intent)
    }

    override fun onItemAcceptClickListener(position: Int) {
        val order = orderList[position]
        val hotelUserId = auth.currentUser?.uid ?: return
        val pushKey = order.itemPushkey ?: return

        val hotelRef = database.reference
            .child("Hotel Users").child(hotelUserId)
            .child("OrderDetails").child(pushKey)

        hotelRef.child("orderAccepted").setValue(true)
    }

    override fun onItemDispatchClickListener(position: Int) {
        val order = orderList[position]
        val hotelUserId = auth.currentUser?.uid ?: return
        val pushKey = order.itemPushkey ?: return

        // ✅ Save to Hotel Users/{hotelUserId}/CompletedOrder
        val completedRef = database.reference
            .child("Hotel Users")
            .child(hotelUserId)
            .child("CompletedOrder")
            .child(pushKey)

        completedRef.setValue(order)
            .addOnSuccessListener {
                // ✅ Remove from Hotel Users/{hotelUserId}/OrderDetails
                database.reference
                    .child("Hotel Users")
                    .child(hotelUserId)
                    .child("OrderDetails")
                    .child(pushKey)
                    .removeValue()

                Toast.makeText(this, "Order Dispatched", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Dispatch Failed", Toast.LENGTH_SHORT).show()
            }
    }
}
