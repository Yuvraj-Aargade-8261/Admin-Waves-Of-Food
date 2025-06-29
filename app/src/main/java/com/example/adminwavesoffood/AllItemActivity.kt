package com.example.adminwavesoffood

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminwavesoffood.adapter.MenuItemAdapter
import com.example.adminwavesoffood.databinding.ActivityAllItemBinding
import com.example.adminwavesoffood.model.AllMenu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AllItemActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private val menuItems = ArrayList<AllMenu>()

    private val binding: ActivityAllItemBinding by lazy {
        ActivityAllItemBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference

        retrieveMenuItems()
    }

    private fun retrieveMenuItems() {
        val currentUserId = auth.currentUser?.uid ?: return
        val foodRef = database.reference
            .child("Hotel Users")
            .child(currentUserId)
            .child("menu")

        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                menuItems.clear()
                for (foodSnapshot in snapshot.children) {
                    val menuItem = foodSnapshot.getValue(AllMenu::class.java)
                    menuItem?.itemKey = foodSnapshot.key
                    menuItem?.let { menuItems.add(it) }
                }
                setAdapter()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AllItemActivity", "Database error: ${error.message}")
            }
        })
    }

    private fun setAdapter() {
        val adapter = MenuItemAdapter(this, menuItems, databaseReference) { position ->
            deleteMenuItem(position)
        }
        binding.menurecyclerview.layoutManager = LinearLayoutManager(this)
        binding.menurecyclerview.adapter = adapter
    }

    private fun deleteMenuItem(position: Int) {
        val currentUserId = auth.currentUser?.uid ?: return
        val item = menuItems[position]
        val key = item.itemKey ?: return

        database.reference
            .child("Hotel Users")
            .child(currentUserId)
            .child("menu")
            .child(key)
            .removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    menuItems.removeAt(position)
                    binding.menurecyclerview.adapter?.notifyItemRemoved(position)
                    binding.menurecyclerview.adapter?.notifyItemRangeChanged(position, menuItems.size)
                } else {
                    Toast.makeText(this, "Item Not Deleted", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
