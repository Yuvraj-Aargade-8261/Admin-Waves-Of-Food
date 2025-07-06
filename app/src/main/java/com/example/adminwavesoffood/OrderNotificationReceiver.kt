package com.example.adminwavesoffood

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

class OrderNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val userName = intent.getStringExtra("userNames") ?: "Customer"
        val orderId = intent.getStringExtra("orderId") ?: System.currentTimeMillis().toString()

        Log.d("OrderNotification", "Received broadcast for order by: $userName")

        val sharedPref = context.getSharedPreferences("OrderNotifications", Context.MODE_PRIVATE)
        val alreadyNotified = sharedPref.getBoolean(orderId, false)

        if (alreadyNotified) {
            Log.d("OrderNotification", "Notification already shown for orderId: $orderId")
            return
        }

        // Mark this order as notified
        sharedPref.edit().putBoolean(orderId, true).apply()

        val channelId = "order_channel"
        val channelName = "Order Notifications"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for new order notifications"
            }
            manager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val requestCode = orderId.hashCode() // unique per order
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.baseline_notifications_24) // ✅ Update this icon as needed
            .setContentTitle("New Order Received!")
            .setContentText("Order placed by $userName")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(requestCode, notification)
    }
}
