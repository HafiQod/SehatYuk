package com.example.mediplus.uii

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mediplus.R
import com.example.mediplus.uii.auth.LoginActivity
import com.example.mediplus.uii.database.AppointmentModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var rvHomeAppointments: RecyclerView
    private lateinit var tvHomeEmpty: TextView
    private lateinit var appointmentList: ArrayList<AppointmentModel>
    private lateinit var adapter: AppointmentAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()

        // Cek login
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        initViews()
        setupRecyclerView()
        setupBottomNav()
        loadUpcomingAppointments()
    }

    private fun initViews() {
        rvHomeAppointments = findViewById(R.id.rvHomeAppointments)
        tvHomeEmpty = findViewById(R.id.tvHomeEmpty)
        val ivProfile = findViewById<ImageView>(R.id.ivProfile)
        val btnStartMission = findViewById<Button>(R.id.btnStartMission)

        ivProfile.setOnClickListener { showProfileMenu(it) }

        // --- PERBAIKAN DI SINI ---
        btnStartMission.setOnClickListener {
            // Membuka GamifikasiActivity
            val intent = Intent(this, GamifikasiActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        rvHomeAppointments.layoutManager = LinearLayoutManager(this)
        rvHomeAppointments.setHasFixedSize(true)
        appointmentList = arrayListOf()

        adapter = AppointmentAdapter(appointmentList) {
            Toast.makeText(this, "Membuka detail: ${it.purpose}", Toast.LENGTH_SHORT).show()
        }
        rvHomeAppointments.adapter = adapter
    }

    private fun loadUpcomingAppointments() {
        val userId = auth.currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance("https://mediplusapp-e6128-default-rtdb.firebaseio.com")
            .getReference("appointments")

        ref.orderByChild("userId").equalTo(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                appointmentList.clear()
                val tempList = arrayListOf<AppointmentModel>()

                for (ds in snapshot.children) {
                    val appt = ds.getValue(AppointmentModel::class.java)
                    if (appt != null) {
                        tempList.add(appt)
                    }
                }

                // Logic Sorting & Filtering
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val now = Date()

                val futureList = tempList.filter {
                    try {
                        val dateObj = sdf.parse("${it.date} ${it.time}")
                        dateObj != null && dateObj.after(now)
                    } catch (e: Exception) {
                        false
                    }
                }

                val sortedList = futureList.sortedBy {
                    sdf.parse("${it.date} ${it.time}")
                }

                val topTwo = sortedList.take(2)

                appointmentList.addAll(topTwo)
                adapter.notifyDataSetChanged()

                if (appointmentList.isEmpty()) {
                    tvHomeEmpty.visibility = View.VISIBLE
                    rvHomeAppointments.visibility = View.GONE
                } else {
                    tvHomeEmpty.visibility = View.GONE
                    rvHomeAppointments.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeActivity", "Error: ${error.message}")
            }
        })
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_appointment -> {
                    startActivity(Intent(this, AppointmentActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_chatbot -> {
                    startActivity(Intent(this, ChatbotActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    private fun showProfileMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_profile, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    auth.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}