package com.example.adminwavesoffood

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminwavesoffood.adapter.OrderDetailesAdapter
import com.example.adminwavesoffood.databinding.ActivityOrderDetailesAactivityBinding
import com.example.adminwavesoffood.model.OrderDetails

class OrderDetailesAActivity : AppCompatActivity() {

    private val binding: ActivityOrderDetailesAactivityBinding by lazy {
        ActivityOrderDetailesAactivityBinding.inflate(layoutInflater)
    }

    private var userName: String? = null
    private var address: String? = null
    private var phoneNumber: String? = null
    private var totalPrice: String? = null

    private var foodName = arrayListOf<String>()
    private var foodImages = arrayListOf<String>()
    private var foodQuantity = arrayListOf<Int>()
    private var foodPrices = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        getOrderDataFromIntent()
    }

    private fun getOrderDataFromIntent() {
        val orderDetailes = intent.getSerializableExtra("userOrderDetailes") as? OrderDetails
        if (orderDetailes != null) {
            userName = orderDetailes.userNames
            address = orderDetailes.address
            phoneNumber = orderDetailes.phoneNumber
            totalPrice = orderDetailes.totalPrices

            foodName = ArrayList(orderDetailes.foodNames ?: emptyList())
            foodImages = ArrayList(orderDetailes.foodImages ?: emptyList())
            foodPrices = ArrayList(orderDetailes.foodPrices ?: emptyList())
            foodQuantity = ArrayList(orderDetailes.foodQuantities ?: emptyList())

            setUserDetails()
            setAdapter()
        } else {
            Log.e("OrderDetailesAActivity", "Order data is null or invalid.")
        }
    }

    private fun setAdapter() {
        binding.orderdetailesrecy.layoutManager = LinearLayoutManager(this)
        val adapter = OrderDetailesAdapter(this, foodName, foodImages, foodQuantity, foodPrices)
        binding.orderdetailesrecy.adapter = adapter
    }

    private fun setUserDetails() {
        binding.personName.text = userName ?: "No Name"
        binding.personAdress.text = address ?: "No Address"
        binding.personPhone.text = phoneNumber ?: "No Phone"
        binding.totalpay.text = totalPrice ?: "₹0"
    }
}
