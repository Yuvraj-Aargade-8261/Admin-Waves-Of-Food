package com.example.adminwavesoffood

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminwavesoffood.adapter.OrderDetailesAdapter
import com.example.adminwavesoffood.databinding.ActivityOrderDetailesAactivityBinding
import com.example.adminwavesoffood.model.OrderDetails

class OrderDetailesAActivity : AppCompatActivity() {

    private val binding: ActivityOrderDetailesAactivityBinding by lazy {
        ActivityOrderDetailesAactivityBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        getOrderDataFromIntent()
    }

    private fun getOrderDataFromIntent() {
        val orderDetails = intent.getSerializableExtra("userOrderDetailes") as? OrderDetails

        if (orderDetails == null) {
            Log.e("OrderDetailesAActivity", "Order data is null or invalid.")
            return
        }

        // Set user info
        binding.personName.text = orderDetails.userNames ?: "No Name"
        binding.personAdress.text = orderDetails.address ?: "No Address"
        binding.personPhone.text = orderDetails.phoneNumber ?: "No Phone"
        binding.totalpay.text = orderDetails.totalPrices ?: "₹0"

        // Set food item list
        val foodNames = orderDetails.foodNames ?: emptyList()
        val foodImages = orderDetails.foodImages ?: emptyList()
        val foodQuantities = orderDetails.foodQuantities ?: emptyList()
        val foodPrices = orderDetails.foodPrices ?: emptyList()

        binding.orderdetailesrecy.apply {
            layoutManager = LinearLayoutManager(this@OrderDetailesAActivity)
            adapter = OrderDetailesAdapter(
                context = this@OrderDetailesAActivity,
                foodNames = foodNames,
                foodImages = foodImages,
                foodQuantities = foodQuantities,
                foodPrices = foodPrices
            )
        }
    }
}
