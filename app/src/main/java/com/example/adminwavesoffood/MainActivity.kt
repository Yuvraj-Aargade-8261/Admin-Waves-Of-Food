package com.example.adminwavesoffood

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.adminwavesoffood.databinding.ActivityMainBinding
import com.example.adminwavesoffood.model.OrderDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var completedOrderReference: DatabaseReference
    private lateinit var pendingOrderReference: DatabaseReference
    private lateinit var hotelUserId: String
    private var seenOrderIds = mutableSetOf<String>()
    private var seenNotificationIds = mutableSetOf<String>() // ✅ For payment notification tracking

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        hotelUserId = auth.currentUser?.uid ?: return

        completedOrderReference = database.reference
            .child("Hotel Users")
            .child(hotelUserId)
            .child("CompletedOrder")

        pendingOrderReference = database.reference
            .child("Hotel Users")
            .child(hotelUserId)
            .child("OrderDetails")

        setupNavigation()
        fetchDashboardData()
        listenForNewOrders()
        listenForPaymentNotifications() // ✅ Moved here
    }

    private fun setupNavigation() {
        binding.addmenu.setOnClickListener {
            startActivity(Intent(this, AddItemActivity::class.java))
        }
        binding.allitemmenu.setOnClickListener {
            startActivity(Intent(this, AllItemActivity::class.java))
        }
        binding.outfordeleiverycardbutton.setOnClickListener {
            startActivity(Intent(this, OutForDeliveryActivity::class.java))
        }
        binding.profilemaincard.setOnClickListener {
            startActivity(Intent(this, AdminProfile::class.java))
        }
        binding.createusermain.setOnClickListener {
            startActivity(Intent(this, CreateUSerActivity::class.java))
        }
        binding.pendingordermaintextview.setOnClickListener {
            startActivity(Intent(this, PendingOrderActivity::class.java))
        }
        binding.completedorderlist.setOnClickListener {
            startActivity(Intent(this, CompletedOrderActivity::class.java))
        }
        binding.logout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun fetchDashboardData() {
        getPendingOrders()
        getCompletedOrders()
        getTotalEarnings()
    }

    private fun getPendingOrders() {
        pendingOrderReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.number.text = snapshot.childrenCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getCompletedOrders() {
        completedOrderReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.completenumber.text = snapshot.childrenCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getTotalEarnings() {
        completedOrderReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var total = 0
                for (orderSnapshot in snapshot.children) {
                    val order = orderSnapshot.getValue(OrderDetails::class.java)
                    val price = order?.totalPrices?.replace("[^0-9]".toRegex(), "")?.toIntOrNull()
                    if (price != null) {
                        total += price
                    }
                }
                binding.earning.text = "$total ₹"
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun listenForNewOrders() {
        pendingOrderReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val orderId = snapshot.key ?: return
                if (seenOrderIds.contains(orderId)) return

                seenOrderIds.add(orderId)
                val userName = snapshot.child("userNames").getValue(String::class.java) ?: "Customer"

                triggerLocalNotification(userName)
                refreshDashboardImmediately()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun refreshDashboardImmediately() {
        getPendingOrders()
        getCompletedOrders()
        getTotalEarnings()
    }

    private fun triggerLocalNotification(userName: String) {
        val intent = Intent(this, OrderNotificationReceiver::class.java).apply {
            putExtra("userNames", userName)
        }
        sendBroadcast(intent)
    }

    // ✅ PAYMENT NOTIFICATION LISTENER
    private fun listenForPaymentNotifications() {
        val notificationsRef = database.reference
            .child("Hotel Users")
            .child(hotelUserId)
            .child("notifications")

        notificationsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val notificationId = snapshot.key ?: return
                if (seenNotificationIds.contains(notificationId)) return
                seenNotificationIds.add(notificationId)

                val title = snapshot.child("title").getValue(String::class.java) ?: "Update"
                val message = snapshot.child("message").getValue(String::class.java) ?: return

                showPaymentNotification(title, message)
                snapshot.ref.removeValue()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showPaymentNotification(title: String, message: String) {
        val channelId = "admin_payment_updates"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Admin Payment Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Payment confirmations"
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
