package com.example.adminwavesoffood

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
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
        ordersRef = database.reference
            .child("Hotel Users").child(hotelUserId).child("OrderDetails")

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
                        // 🔕 Do NOT trigger notifications here
                    }
                }
                setupAdapter()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupAdapter() {
        binding.pendinforderrecycler.layoutManager = LinearLayoutManager(this)
        binding.pendinforderrecycler.adapter = PendingOrderAdapter(this, orderList, this)
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

        sendNotificationWithHotelName(
            userId = order.userId ?: return,
            hotelUserId = hotelUserId,
            title = "Order Accepted",
            status = "accepted"
        )
    }

    override fun onItemDispatchClickListener(position: Int) {
        val order = orderList[position]
        val hotelUserId = auth.currentUser?.uid ?: return
        val pushKey = order.itemPushkey ?: return

        val completedRef = database.reference
            .child("Hotel Users").child(hotelUserId)
            .child("CompletedOrder").child(pushKey)

        completedRef.setValue(order).addOnSuccessListener {
            database.reference.child("Hotel Users")
                .child(hotelUserId).child("OrderDetails")
                .child(pushKey).removeValue()

            Toast.makeText(this, "Order Dispatched", Toast.LENGTH_SHORT).show()

            showLocalDispatchNotification(order)

            sendNotificationWithHotelName(
                userId = order.userId ?: return@addOnSuccessListener,
                hotelUserId = hotelUserId,
                title = "Order Dispatched",
                status = "dispatched"
            )
        }.addOnFailureListener {
            Toast.makeText(this, "Dispatch Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendNotificationWithHotelName(
        userId: String,
        hotelUserId: String,
        title: String,
        status: String
    ) {
        val hotelRef = database.reference
            .child("Hotel Users").child(hotelUserId).child("nameOfResturant")

        hotelRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(hotelSnapshot: DataSnapshot) {
                val hotelName = hotelSnapshot.getValue(String::class.java) ?: "your hotel"

                val customerRef = database.reference
                    .child("Users").child(userId).child("userName")

                customerRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(customerSnapshot: DataSnapshot) {
                        val customerName = customerSnapshot.getValue(String::class.java) ?: "a customer"
                        val message = when (status) {
                            "accepted" -> "Your order has been accepted by $hotelName"
                            "dispatched" -> "Your order has been dispatched by $hotelName"
                            else -> "Order update from $hotelName"
                        }

                        val timestamp = System.currentTimeMillis().toString()
                        val notifyMap = mapOf(
                            "title" to title,
                            "message" to message,
                            "status" to status,
                            "timestamp" to timestamp
                        )

                        database.reference
                            .child("Users").child(userId)
                            .child("notifications").child(timestamp)
                            .setValue(notifyMap)
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showLocalDispatchNotification(order: OrderDetails) {
        val channelId = "dispatch_channel"
        val channelName = "Dispatch Notifications"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Admin-side dispatch notifications"
            }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, CompletedOrderActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            order.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val userName = order.userNames ?: "Customer"
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle("Order Completed")
            .setContentText("Order from $userName has been completed.")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(order.hashCode(), notification)
    }
}
