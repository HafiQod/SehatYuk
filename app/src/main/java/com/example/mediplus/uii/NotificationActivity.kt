package com.example.mediplus.uii

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediplus.R
import com.example.mediplus.uii.database.AppointmentModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotificationScreen(onBackClick = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    var appointmentList by remember { mutableStateOf<List<AppointmentModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Mengambil data dari Firebase Realtime Database
    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val database = FirebaseDatabase.getInstance("https://mediplusapp-e6128-default-rtdb.firebaseio.com")
            val ref = database.getReference("appointments")

            ref.orderByChild("userId").equalTo(user.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val tempList = mutableListOf<AppointmentModel>()
                        for (child in snapshot.children) {
                            val appt = child.getValue(AppointmentModel::class.java)
                            if (appt != null) {
                                tempList.add(appt)
                            }
                        }
                        // Data terbaru di atas
                        appointmentList = tempList.reversed()
                        isLoading = false
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        isLoading = false
                    }
                })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold, color = Color(0xFF6750A4)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        // Pastikan drawable ic_notifications ada, atau ganti icon back
                        Icon(painter = painterResource(id = R.drawable.ic_notifications), contentDescription = "Back", tint = Color(0xFF6750A4))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)) // Background abu-abu muda
                .padding(16.dp)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF6750A4))
                }
            } else {
                // Memisahkan data Pending dan Done
                val upcomingList = appointmentList.filter { it.status == "Pending" }
                val completedList = appointmentList.filter { it.status == "Done" }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // --- BAGIAN UPCOMING ---
                    item {
                        Text(
                            text = "Upcoming Appointments",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (upcomingList.isEmpty()) {
                        item { EmptyStateMessage("No upcoming appointments") }
                    } else {
                        items(upcomingList) { appt ->
                            NotificationItem(appt = appt, isCompleted = false) {
                                markAsDone(appt.id, context)
                            }
                        }
                    }

                    // --- BAGIAN HISTORY ---
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "History / Completed",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (completedList.isEmpty()) {
                        item { EmptyStateMessage("No completed appointments yet") }
                    } else {
                        items(completedList) { appt ->
                            NotificationItem(appt = appt, isCompleted = true, onMarkDone = {})
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    appt: AppointmentModel,
    isCompleted: Boolean,
    onMarkDone: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appt.purpose,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6750A4)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${appt.date} at ${appt.time}",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Patient: ${appt.fullName}",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (!isCompleted) {
                Button(
                    onClick = onMarkDone,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mark as Done (Selesai)")
                }
            } else {
                Text(
                    text = "✓ Completed",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50), // Hijau
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun EmptyStateMessage(msg: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = msg,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

// Fungsi Helper untuk Update Firebase
fun markAsDone(apptId: String, context: Context) {
    val database = FirebaseDatabase.getInstance("https://mediplusapp-e6128-default-rtdb.firebaseio.com")
    val ref = database.getReference("appointments").child(apptId)

    ref.child("status").setValue("Done")
        .addOnSuccessListener {
            Toast.makeText(context, "Status updated!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to update status", Toast.LENGTH_SHORT).show()
        }
}