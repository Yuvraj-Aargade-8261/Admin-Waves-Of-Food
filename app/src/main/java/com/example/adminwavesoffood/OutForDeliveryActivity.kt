package com.example.adminwavesoffood

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminwavesoffood.adapter.DeliveryAdapter
import com.example.adminwavesoffood.databinding.ActivityOutForDeliveryBinding
import com.example.adminwavesoffood.model.OrderDetailes
import com.google.firebase.database.*

class OutForDeliveryActivity : AppCompatActivity() {

    private val binding: ActivityOutForDeliveryBinding by lazy {
        ActivityOutForDeliveryBinding.inflate(layoutInflater)
    }

    private lateinit var database: FirebaseDatabase
    private val completedOrdersList: ArrayList<OrderDetailes> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        retrieveCompletedOrders()
    }

    private fun retrieveCompletedOrders() {
        database = FirebaseDatabase.getInstance()
        val completedRef = database.reference
            .child("CompletedOrder")
            .orderByChild("currentTime")

        completedRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                completedOrdersList.clear()
                for (orderSnapshot in snapshot.children) {
                    val order = orderSnapshot.getValue(OrderDetailes::class.java)
                    order?.let { completedOrdersList.add(it) }
                }

                completedOrdersList.reverse() // ✅ To show latest first
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
            order.userNames?.let { customerNames.add(it) }
            moneyStatuses.add(order.paymentReceived)
        }

        binding.outforrecycler.layoutManager = LinearLayoutManager(this)
        val adapter = DeliveryAdapter(customerNames, moneyStatuses)
        binding.outforrecycler.adapter = adapter
    }
}
