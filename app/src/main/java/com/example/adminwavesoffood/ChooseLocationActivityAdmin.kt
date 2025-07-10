package com.example.adminwavesoffood

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import okhttp3.*
import org.json.JSONArray
import java.io.IOException
import java.net.URLEncoder
import java.util.*

class ChooseLocationActivityAdmin : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var settingsClient: SettingsClient
    private lateinit var locationSettingsRequest: LocationSettingsRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var listOfLocationView: AutoCompleteTextView
    private lateinit var getCurrentLocationBtn: Button
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private val client = OkHttpClient()
    private lateinit var progressDialog: ProgressDialog

    private var isLocationSaved = false
    private lateinit var sharedPreferences: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("isLocationSet", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_choose_location_admin)

        listOfLocationView = findViewById(R.id.listoflocation)
        getCurrentLocationBtn = findViewById(R.id.getcurrentlocation)

        progressDialog = ProgressDialog(this).apply {
            setMessage("Fetching your location...")
            setCancelable(false)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        settingsClient = LocationServices.getSettingsClient(this)

        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
            fastestInterval = 500
            numUpdates = 1
        }

        locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
            .build()

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) checkIfGPSEnabled()
            else Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }

        listOfLocationView.setOnItemClickListener { parent, _, position, _ ->
            val address = parent.getItemAtPosition(position).toString()
            geocodeAndSave(address)
        }

        listOfLocationView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().length > 2) fetchLocationSuggestions(s.toString())
            }
        })

        getCurrentLocationBtn.setOnClickListener { askLocationPermission() }
    }

    private fun geocodeAndSave(address: String) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val results = geocoder.getFromLocationName(address, 1)
            if (!results.isNullOrEmpty()) {
                val loc = results[0]
                saveLocationToFirebase(address, loc.latitude, loc.longitude)
            } else showToastSafe("Unable to geocode address")
        } catch (e: Exception) {
            showToastSafe("Error geocoding address")
        }
    }

    private fun askLocationPermission() {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun checkIfGPSEnabled() {
        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener { getCurrentLocation() }
            .addOnFailureListener { showGPSDialog() }
    }

    private fun showGPSDialog() {
        AlertDialog.Builder(this)
            .setTitle("Enable Location")
            .setMessage("Please enable GPS and High Accuracy Mode.")
            .setPositiveButton("Turn On") { _, _ -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
            .setNegativeButton("No Thanks", null)
            .setNeutralButton("Skip") { _, _ -> finish() }
            .show()
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        progressDialog.setMessage("Getting your current location...")
        progressDialog.show()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation
                loc?.let { handleLocationResult(it) }
                progressDialog.dismiss()
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    private fun handleLocationResult(location: Location) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                ?.firstOrNull()?.getAddressLine(0) ?: "Unknown"
            listOfLocationView.setText(address, false)
            saveLocationToFirebase(address, location.latitude, location.longitude)
        } catch (e: Exception) {
            showToastSafe("Error getting address")
        }
    }

    private fun fetchLocationSuggestions(query: String) {
        val url = "https://nominatim.openstreetmap.org/search?q=${URLEncoder.encode(query, "UTF-8")}&format=json"
        progressDialog.setMessage("Fetching location suggestions...")
        progressDialog.show()

        client.newCall(Request.Builder().url(url).header("User-Agent", "WavesOfFoodAdmin").build())
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) = runOnUiThread {
                    progressDialog.dismiss()
                    showToastSafe("Failed to fetch suggestions")
                }
                override fun onResponse(call: Call, response: Response) {
                    val suggestions = mutableListOf<String>()
                    JSONArray(response.body?.string() ?: "[]").let { arr ->
                        for (i in 0 until arr.length()) suggestions.add(arr.getJSONObject(i).getString("display_name"))
                    }
                    runOnUiThread {
                        progressDialog.dismiss()
                        val adapter = ArrayAdapter(this@ChooseLocationActivityAdmin, android.R.layout.simple_list_item_1, suggestions)
                        listOfLocationView.setAdapter(adapter)
                        listOfLocationView.showDropDown()
                    }
                }
            })
    }

    private fun saveLocationToFirebase(address: String, lat: Double, lng: Double) {
        if (isLocationSaved) return
        isLocationSaved = true

        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            val ref = FirebaseDatabase.getInstance().reference.child("Hotel Users").child(uid)
            val locMap = mapOf(
                "address" to address,
                "latitude" to lat,
                "longitude" to lng
            )
            ref.child("address").setValue(locMap)
                .addOnSuccessListener {
                    sharedPreferences.edit().putBoolean("isLocationSet", true).apply()
                    Toast.makeText(this, "Location saved!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    isLocationSaved = false
                    showToastSafe("Failed to save location")
                }
        }
    }

    private fun showToastSafe(message: String) {
        if (!isFinishing && !isDestroyed) Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
