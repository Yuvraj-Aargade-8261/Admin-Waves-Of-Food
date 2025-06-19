package com.example.adminwavesoffood

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.adminwavesoffood.databinding.ActivityMainBinding
import com.example.adminwavesoffood.model.OrderDetailes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var completedOrderReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        completedOrderReference = database.reference.child("CompletedOrder")

        setupNavigation()
        fetchDashboardData()
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

        binding.logout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }

    private fun fetchDashboardData() {
        getPendingOrders()
        getCompletedOrders()
        getTotalEarnings()
    }

    private fun getTotalEarnings() {
        val listOfTotalPay = mutableListOf<Int>()

        completedOrderReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listOfTotalPay.clear()
                for (orderSnapshot in snapshot.children) {
                    val order = orderSnapshot.getValue(OrderDetailes::class.java)
                    order?.totalPrices?.replace(" ₹", "")?.toIntOrNull()?.let {
                        listOfTotalPay.add(it)
                    }
                }
                binding.earning.text = "${listOfTotalPay.sum()} ₹"
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getPendingOrders() {
        val pendingRef = database.reference.child("orderDetails")
        pendingRef.addValueEventListener(object : ValueEventListener {
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
}
