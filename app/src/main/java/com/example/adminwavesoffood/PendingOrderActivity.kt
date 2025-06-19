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
import com.example.adminwavesoffood.model.OrderDetailes
import com.google.firebase.database.*

class PendingOrderActivity : AppCompatActivity(), PendingOrderAdapter.OnItemClicked {

    private lateinit var binding: ActivityPendingOrderBinding
    private val orderList = arrayListOf<OrderDetailes>()
    private val names = mutableListOf<String>()
    private val prices = mutableListOf<String>()
    private val firstImages = mutableListOf<String>()

    private lateinit var database: FirebaseDatabase
    private lateinit var ordersRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPendingOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        ordersRef = database.reference.child("orderDetails")

        fetchLiveOrders()
    }

    private fun fetchLiveOrders() {
        ordersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                orderList.clear()
                names.clear()
                prices.clear()
                firstImages.clear()

                for (snap in snapshot.children) {
                    val order = snap.getValue(OrderDetailes::class.java)
                    order?.let {
                        orderList.add(it)
                        names.add(it.userNames ?: "Unnamed")
                        prices.add(it.totalPrices ?: "0")
                        firstImages.add(it.foodImages?.firstOrNull() ?: "")
                    }
                }

                Log.d("FirebaseDebug", "Orders: ${orderList.size}")
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
            customerNames = names,
            quantities = prices,
            foodImages = firstImages,
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
        val pushKey = order.itemPushkey ?: return
        val userId = order.userId ?: return

        val orderRef = database.reference.child("orderDetails").child(pushKey)
        val userHistoryRef = database.reference
            .child("user")
            .child(userId)
            .child("BuyHistory")
            .child(pushKey)

        orderRef.child("orderAccepted").setValue(true)
        userHistoryRef.child("orderAccepted").setValue(true)
    }

    override fun onItemDispatchClickListener(position: Int) {
        val order = orderList[position]
        val pushKey = order.itemPushkey ?: return

        val completedRef = database.reference.child("CompletedOrder").child(pushKey)
        completedRef.setValue(order)
            .addOnSuccessListener {
                removeOrderFromPending(pushKey)
            }
    }

    private fun removeOrderFromPending(pushKey: String) {
        database.reference.child("orderDetails").child(pushKey)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Order Dispatched", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Dispatch Failed", Toast.LENGTH_SHORT).show()
            }
    }
}
