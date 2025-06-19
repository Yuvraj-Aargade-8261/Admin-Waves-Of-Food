package com.example.adminwavesoffood

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.adminwavesoffood.databinding.ActivityAdminProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AdminProfile : AppCompatActivity() {

    private val binding: ActivityAdminProfileBinding by lazy {
        ActivityAdminProfileBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var adminReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        adminReference = database.reference.child("user")

        disableEditing()

        binding.editbutton.setOnClickListener {
            toggleEditing()
        }

        binding.saveinfobutton.setOnClickListener {
            updateUserData()
        }

        retrieveUserData()
    }

    private fun disableEditing() {
        binding.name.isEnabled = false
        binding.address.isEnabled = false
        binding.email.isEnabled = false
        binding.phonenumber.isEnabled = false
        binding.password.isEnabled = false
        binding.saveinfobutton.isEnabled = false
    }

    private fun toggleEditing() {
        val isEditable = !binding.name.isEnabled
        binding.name.isEnabled = isEditable
        binding.address.isEnabled = isEditable
        binding.email.isEnabled = isEditable
        binding.phonenumber.isEnabled = isEditable
        binding.password.isEnabled = isEditable
        binding.saveinfobutton.isEnabled = isEditable

        if (isEditable) binding.name.requestFocus()
    }

    private fun retrieveUserData() {
        val currentUserUid = auth.currentUser?.uid ?: return
        val userRef = adminReference.child(currentUserUid)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.name.setText(snapshot.child("name").value.toString())
                    binding.email.setText(snapshot.child("email").value.toString())
                    binding.password.setText(snapshot.child("password").value.toString())
                    binding.phonenumber.setText(snapshot.child("phone").value.toString())
                    binding.address.setText(snapshot.child("address").value.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateUserData() {
        val uid = auth.currentUser?.uid ?: return

        val updatedData = mapOf(
            "name" to binding.name.text.toString(),
            "email" to binding.email.text.toString(),
            "password" to binding.password.text.toString(),
            "phone" to binding.phonenumber.text.toString(),
            "address" to binding.address.text.toString()
        )

        adminReference.child(uid).updateChildren(updatedData).addOnSuccessListener {
            Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
            auth.currentUser?.updateEmail(updatedData["email"]!!)
            auth.currentUser?.updatePassword(updatedData["password"]!!)
        }.addOnFailureListener {
            Toast.makeText(this, "Updatation Failed", Toast.LENGTH_SHORT).show()
        }
    }
}
