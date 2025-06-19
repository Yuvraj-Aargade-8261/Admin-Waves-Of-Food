package com.example.adminwavesoffood

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.adminwavesoffood.databinding.ActivitySignUpBinding
import com.example.adminwavesoffood.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private val binding: ActivitySignUpBinding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = Firebase.auth
        database = Firebase.database.reference

        val locationList = arrayOf("Chhatrapati Sambhajinagar", "Jalna", "Paithan", "Beed")
        binding.listoflocation.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, locationList)
        )

        binding.createbuttonsignup.setOnClickListener {
            val userName = binding.usernamesignup.text.toString().trim()
            val restaurant = binding.restorantname.text.toString().trim()
            val email = binding.useremailsignup.text.toString().trim()
            val password = binding.userpasswordsignup.text.toString().trim()

            if (userName.isBlank() || restaurant.isBlank() || email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show()
            } else {
                createAccount(userName, restaurant, email, password)
            }
        }

        binding.alredybutton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    private fun createAccount(userName: String, restaurant: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Account Created", Toast.LENGTH_SHORT).show()
                saveUserData(userName, restaurant, email, password)
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Account Creation Failed", Toast.LENGTH_SHORT).show()
                Log.d("FirebaseAuth", "Signup Error", task.exception)
            }
        }
    }

    private fun saveUserData(userName: String, restaurant: String, email: String, password: String) {
        val userId = auth.currentUser?.uid ?: return
        val user = UserModel(userName, restaurant, email, password)
        database.child("user").child(userId).setValue(user)
    }
}
