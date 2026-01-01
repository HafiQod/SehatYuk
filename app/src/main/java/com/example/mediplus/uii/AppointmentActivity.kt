package com.example.mediplus.uii

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mediplus.R
import com.example.mediplus.uii.auth.LoginActivity
import com.example.mediplus.uii.database.AppointmentModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AppointmentActivity : AppCompatActivity() {

    private lateinit var rvAppointments: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var appointmentList: ArrayList<AppointmentModel>
    private lateinit var adapter: AppointmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment)

        // 1. Inisialisasi View
        rvAppointments = findViewById(R.id.rvAppointments)
        tvEmpty = findViewById(R.id.tvEmpty)
        val ivProfile = findViewById<ImageView>(R.id.ivProfile)
        val btnGoToNewAppointment = findViewById<Button>(R.id.btnGoToNewAppointment)

        // Asumsi ID ImageView notifikasi kamu adalah ivNotification atau urutan imageview pertama
        // Sebaiknya beri ID di XML: android:id="@+id/ivNotification"
        // Kode di bawah mencari ImageView lonceng. Sesuaikan ID jika perlu.
        val ivNotification = findViewById<ImageView>(R.id.ivNotification) // Pastikan ID ini ada di XML

        // 2. Setup RecyclerView
        rvAppointments.layoutManager = LinearLayoutManager(this)
        rvAppointments.setHasFixedSize(true)
        appointmentList = arrayListOf()

        adapter = AppointmentAdapter(appointmentList) { selectedAppointment ->
            showDetailDialog(selectedAppointment)
        }

        rvAppointments.adapter = adapter

        getAppointmentData()

        ivProfile.setOnClickListener { view ->
            showProfileMenu(view)
        }

        // Navigasi ke Halaman Notifikasi Baru
        ivNotification?.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        btnGoToNewAppointment.setOnClickListener {
            startActivity(Intent(this, NewAppointmentActivity::class.java))
        }

        setupBottomNav()
    }

    private fun showProfileMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_profile, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showDetailDialog(appt: AppointmentModel) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_appointment_detail)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )

        val tvName = dialog.findViewById<TextView>(R.id.tvDetailName)
        val tvDate = dialog.findViewById<TextView>(R.id.tvDetailDate)
        val tvPurpose = dialog.findViewById<TextView>(R.id.tvDetailPurpose)
        val tvPhone = dialog.findViewById<TextView>(R.id.tvDetailPhone)
        val tvAddress = dialog.findViewById<TextView>(R.id.tvDetailAddress)
        val btnDelete = dialog.findViewById<Button>(R.id.btnDeleteAppointment) // Tombol Hapus Baru
        val btnClose = dialog.findViewById<Button>(R.id.btnCloseDialog)

        tvName.text = appt.fullName
        tvDate.text = "${appt.date} at ${appt.time}"
        tvPurpose.text = appt.purpose
        tvPhone.text = appt.phoneNumber
        tvAddress.text = appt.address

        // --- LOGIKA HAPUS APPOINTMENT ---
        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cancel Appointment")
                .setMessage("Are you sure you want to cancel and delete this appointment?")
                .setPositiveButton("Yes") { _, _ ->
                    val database = FirebaseDatabase.getInstance("https://mediplusapp-e6128-default-rtdb.firebaseio.com")
                    val ref = database.getReference("appointments").child(appt.id)

                    ref.removeValue().addOnSuccessListener {
                        Toast.makeText(this, "Appointment cancelled", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Failed to cancel", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getAppointmentData() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) return

        val database = FirebaseDatabase.getInstance("https://mediplusapp-e6128-default-rtdb.firebaseio.com")
        val ref = database.getReference("appointments")
        val query = ref.orderByChild("userId").equalTo(user.uid)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                appointmentList.clear()
                if (snapshot.exists()) {
                    for (apptSnapshot in snapshot.children) {
                        val appt = apptSnapshot.getValue(AppointmentModel::class.java)
                        if (appt != null) {
                            appointmentList.add(appt)
                        }
                    }
                    tvEmpty.visibility = View.GONE
                    rvAppointments.visibility = View.VISIBLE
                } else {
                    tvEmpty.visibility = View.VISIBLE
                    rvAppointments.visibility = View.GONE
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AppointmentActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupBottomNav() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_appointment
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_appointment -> true
                R.id.nav_chatbot -> {
                    startActivity(Intent(this, ChatbotActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }
}