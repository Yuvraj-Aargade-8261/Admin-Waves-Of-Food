package com.example.adminwavesoffood

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
                // ✅ Fix: Correct key name based on your database (userNames)
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
        // Re-fetch values for quick update
        getPendingOrders()
        getCompletedOrders()
        getTotalEarnings()
    }

    private fun triggerLocalNotification(userName: String) {
        val intent = Intent(this, OrderNotificationReceiver::class.java).apply {
            putExtra("userNames", userName) // ✅ Match key with BroadcastReceiver
        }
        sendBroadcast(intent)
    }
}
