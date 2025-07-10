package com.example.adminwavesoffood.model
data class UserModel(
    val name: String? = null,
    val nameOfResturant: String? = null,
    val email: String? = null,
    val password: String? = null,
    var address: String? = null,
    var phone: String? = null,
    var latitude: Double? = null,   // New: hotel latitude
    var longitude: Double? = null   // New: hotel longitude
)
