package com.example.adminwavesoffood.model

import java.io.Serializable

class OrderDetailes : Serializable {
    var userId: String? = null
    var userNames: String? = null
    var foodNames: MutableList<String> = mutableListOf()
    var foodImages: MutableList<String> = mutableListOf()
    var foodPrices: MutableList<String> = mutableListOf()
    var foodQuantities: MutableList<Int> = mutableListOf()
    var address: String? = null
    var totalPrices: String? = null
    var phoneNumber: String? = null
    var orderAccepted: Boolean = false
    var paymentReceived: Boolean = false
    var itemPushkey: String? = null
    var currentTime: Long = 0
}
