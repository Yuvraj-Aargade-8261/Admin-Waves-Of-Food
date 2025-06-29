package com.example.adminwavesoffood

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.adminwavesoffood.databinding.ActivityAddItemBinding
import com.example.adminwavesoffood.model.AllMenu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddItemActivity : AppCompatActivity() {

    private lateinit var foodName: String
    private lateinit var foodPrice: String
    private lateinit var foodDescription: String
    private lateinit var foodIngredients: String
    private var foodImageUri: Uri? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private val binding: ActivityAddItemBinding by lazy {
        ActivityAddItemBinding.inflate(layoutInflater)
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            foodImageUri = it
            binding.selectedimageview.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        initCloudinary()

        binding.selectimage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.AddItemButton.setOnClickListener {
            foodName = binding.enterfoodname.text.toString().trim()
            foodPrice = binding.enterfoodprice.text.toString().trim()
            foodDescription = binding.descriptionadmin.text.toString().trim()
            foodIngredients = binding.ingridients.text.toString().trim()

            if (foodName.isNotEmpty() && foodPrice.isNotEmpty() &&
                foodDescription.isNotEmpty() && foodIngredients.isNotEmpty() &&
                foodImageUri != null
            ) {
                uploadImageToCloudinary()
            } else {
                Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initCloudinary() {
        try {
            MediaManager.get()
        } catch (e: Exception) {
            val config = hashMapOf(
                "cloud_name" to "dx2z14jhc",
                "api_key" to "514881693863474",
                "api_secret" to "w2GQaDpgcbWKzkilRN1S5JmH8zY", // ⚠ Don't expose in prod
                "upload_preset" to "wavesoffood",
                "secure" to "true"
            )
            MediaManager.init(applicationContext, config)
        }
    }

    private fun uploadImageToCloudinary() {
        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show()

        MediaManager.get().upload(foodImageUri)
            .option("resource_type", "image")
            .option("upload_preset", "wavesoffood")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                    val imageUrl = resultData["secure_url"] as? String
                    if (imageUrl != null) {
                        uploadDataToFirebase(imageUrl)
                    } else {
                        Toast.makeText(this@AddItemActivity, "Image URL is null", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Toast.makeText(this@AddItemActivity, "Upload failed: ${error?.description}", Toast.LENGTH_LONG).show()
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    private fun uploadDataToFirebase(imageUrl: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val menuRef = database.getReference("Hotel Users")
            .child(currentUserId)
            .child("menu")
        val newItemKey = menuRef.push().key

        val newItem = AllMenu(
            foodName = foodName,
            foodPrice = foodPrice,
            foodDescription = foodDescription,
            foodIngredients = foodIngredients,
            foodImage = imageUrl
        )

        newItemKey?.let {
            menuRef.child(it).setValue(newItem)
                .addOnSuccessListener {
                    Toast.makeText(this, "Item Added Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
