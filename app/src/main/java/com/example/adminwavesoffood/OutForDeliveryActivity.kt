package com.example.adminwavesoffood

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminwavesoffood.adapter.DeliveryAdapter
import com.example.adminwavesoffood.databinding.ActivityOutForDeliveryBinding
import com.example.adminwavesoffood.model.OrderDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class OutForDeliveryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOutForDeliveryBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private val completedOrdersList = arrayListOf<OrderDetails>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOutForDeliveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        retrieveCompletedOrders()
    }

    private fun retrieveCompletedOrders() {
        val hotelUserId = auth.currentUser?.uid ?: return
        val completedRef = database.reference
            .child("Hotel Users")
            .child(hotelUserId)
            .child("CompletedOrder")
            .orderByChild("currentTime")

        completedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                completedOrdersList.clear()
                for (orderSnapshot in snapshot.children) {
                    val order = orderSnapshot.getValue(OrderDetails::class.java)
                    order?.let { completedOrdersList.add(it) }
                }

                completedOrdersList.reverse()
                setDataIntoRecyclerView()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("OutForDeliveryActivity", "Firebase error: ${error.message}")
            }
        })
    }

    private fun setDataIntoRecyclerView() {
        val customerNames = mutableListOf<String>()
        val moneyStatuses = mutableListOf<Boolean>()

        for (order in completedOrdersList) {
            customerNames.add(order.userNames ?: "Unknown")
            moneyStatuses.add(order.paymentReceived)
        }

        binding.outforrecycler.layoutManager = LinearLayoutManager(this)
        binding.outforrecycler.adapter = DeliveryAdapter(customerNames, moneyStatuses)
    }
}
