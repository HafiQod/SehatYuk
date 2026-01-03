package com.example.mediplus.uii

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mediplus.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Appointment Reminder"
        val message = intent.getStringExtra("message") ?: "You have an upcoming appointment!"

        // 1. Tampilkan Notifikasi
        showNotification(context, title, message)

        // 2. Update Gamifikasi: On Time Hero
        // Poin bertambah saat notifikasi muncul (sesuai request)
        updateOnTimeQuest()
    }

    private fun updateOnTimeQuest() {
        // Ambil User yang sedang login di HP ini
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val database = FirebaseDatabase.getInstance("https://mediplusapp-e6128-default-rtdb.firebaseio.com")
            val questRef = database.getReference("users").child(user.uid).child("quests").child("on_time")

            // Pakai Transaction biar aman (tidak balapan data)
            questRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    var currentVal = currentData.getValue(Int::class.java) ?: 0
                    if (currentVal < 5) { // Maksimal 5
                        currentVal++
                    }
                    currentData.value = currentVal
                    return Transaction.success(currentData)
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                    if (error != null) {
                        Log.e("NotificationReceiver", "Gagal update quest: ${error.message}")
                    } else {
                        Log.d("NotificationReceiver", "Quest On Time Updated!")
                    }
                }
            })
        }
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val channelId = "appointment_channel"
        val channelName = "Appointment Notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Channel for appointment reminders"
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Saat diklik, arahkan ke NotificationActivity agar user bisa "Mark as Done"
        val openAppIntent = Intent(context, NotificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notifications) // Pastikan icon ini ada
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}