package com.example.adminwavesoffood

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
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
        listenForPaymentNotifications() // ✅ NEW
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

    // ✅ NEW: Listen to "notifications" node under Hotel Users
    private fun listenForPaymentNotifications() {
        val hotelUserId = auth.currentUser?.uid ?: return

        val notificationsRef = database.reference
            .child("Hotel Users")
            .child(hotelUserId)
            .child("notifications")

        notificationsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val title = snapshot.child("title").getValue(String::class.java) ?: "Update"
                val message = snapshot.child("message").getValue(String::class.java) ?: return

                showLocalNotification(title, message)

                // Remove the notification after showing
                snapshot.ref.removeValue()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showLocalNotification(title: String, message: String) {
        val channelId = "admin_updates"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Admin Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Payment and order updates"
            }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
