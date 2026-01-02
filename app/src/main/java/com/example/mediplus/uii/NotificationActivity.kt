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
import androidx.compose.ui.res.colorResource
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

    // Mengambil warna dari colors.xml
    val primaryColor = colorResource(id = R.color.medi_purple_primary)
    val textSecondary = colorResource(id = R.color.medi_text_secondary)

    var appointmentList by remember { mutableStateOf<List<AppointmentModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // State untuk Dialog
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var selectedAppointmentId by remember { mutableStateOf("") }
    var feedbackText by remember { mutableStateOf("") }

    // Mengambil data dari Firebase
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

    // --- DIALOG KONFIRMASI (Are you sure?) ---
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text(text = "Confirm Completion", color = primaryColor, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to mark this appointment as done?") },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmationDialog = false
                        showFeedbackDialog = true // Lanjut ke dialog feedback
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text("Cancel", color = textSecondary)
                }
            },
            containerColor = Color.White
        )
    }

    // --- DIALOG FEEDBACK (Input Feedback) ---
    if (showFeedbackDialog) {
        AlertDialog(
            onDismissRequest = {
                showFeedbackDialog = false
                feedbackText = ""
            },
            title = { Text(text = "Give Feedback", color = primaryColor, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("How was your experience?", modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        label = { Text("Enter feedback") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedAppointmentId.isNotEmpty()) {
                            submitFeedbackAndComplete(selectedAppointmentId, feedbackText, context)
                            showFeedbackDialog = false
                            feedbackText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Submit & Finish")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showFeedbackDialog = false
                    feedbackText = ""
                }) {
                    Text("Skip / Cancel", color = textSecondary)
                }
            },
            containerColor = Color.White
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold, color = primaryColor) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(painter = painterResource(id = R.drawable.ic_notifications), contentDescription = "Back", tint = primaryColor)
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
                .background(Color(0xFFF8F9FA)) // Background sedikit lebih terang
                .padding(16.dp)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryColor)
                }
            } else {
                val upcomingList = appointmentList.filter { it.status == "Pending" }
                val completedList = appointmentList.filter { it.status == "Done" }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // --- UPCOMING ---
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
                            NotificationItem(
                                appt = appt,
                                isCompleted = false,
                                primaryColor = primaryColor,
                                onActionClick = {
                                    selectedAppointmentId = appt.id
                                    showConfirmationDialog = true
                                }
                            )
                        }
                    }

                    // --- COMPLETED ---
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
                            NotificationItem(
                                appt = appt,
                                isCompleted = true,
                                primaryColor = primaryColor,
                                onActionClick = {}
                            )
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
    primaryColor: Color,
    onActionClick: () -> Unit
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
                        color = primaryColor
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

            // Jika completed dan ada feedback, tampilkan feedback
            if (isCompleted && appt.feedback.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Feedback: \"${appt.feedback}\"",
                    fontSize = 13.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = Color(0xFF555555)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!isCompleted) {
                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mark as Done")
                }
            } else {
                Text(
                    text = "✓ Completed",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50), // Hijau Sukses
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

// Fungsi Simpan ke Firebase (Status + Feedback)
fun submitFeedbackAndComplete(apptId: String, feedback: String, context: Context) {
    val database = FirebaseDatabase.getInstance("https://mediplusapp-e6128-default-rtdb.firebaseio.com")
    val ref = database.getReference("appointments").child(apptId)

    // Update multiple fields secara bersamaan
    val updates = mapOf(
        "status" to "Done",
        "feedback" to feedback
    )

    ref.updateChildren(updates)
        .addOnSuccessListener {
            Toast.makeText(context, "Appointment completed! Thank you for your feedback.", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to update status", Toast.LENGTH_SHORT).show()
        }
}