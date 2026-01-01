package com.example.mediplus.uii

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediplus.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

// Warna sesuai tema
val MediPurplePrimary = Color(0xFF6200EE)
val MediTextPrimary = Color(0xFF000000)
val MediTextSecondary = Color(0xFF757575)

class GamifikasiComposeActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        // Pastikan URL database sesuai
        database = FirebaseDatabase.getInstance("https://mediplusapp-e6128-default-rtdb.firebaseio.com").getReference("users")

        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(this, "User tidak ditemukan, silakan login ulang", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            // State untuk menampung data dari Firebase
            var userName by remember { mutableStateOf("Loading...") }
            var quests by remember { mutableStateOf(mapOf(
                "on_time" to 0,
                "routine" to 0,
                "reading" to 0,
                "feedback" to 0
            )) }

            // Mengambil data dari Firebase saat halaman dibuka
            LaunchedEffect(currentUser.uid) {
                database.child(currentUser.uid).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // 1. Ambil Nama
                            val name = snapshot.child("fullName").getValue(String::class.java)
                                ?: snapshot.child("username").getValue(String::class.java)
                                ?: "User"
                            userName = name

                            // 2. Ambil Quest Progress
                            val qOnTime = snapshot.child("quests/on_time").getValue(Int::class.java) ?: 0
                            val qRoutine = snapshot.child("quests/routine").getValue(Int::class.java) ?: 0
                            val qReading = snapshot.child("quests/reading").getValue(Int::class.java) ?: 0
                            val qFeedback = snapshot.child("quests/feedback").getValue(Int::class.java) ?: 0

                            quests = mapOf(
                                "on_time" to qOnTime,
                                "routine" to qRoutine,
                                "reading" to qReading,
                                "feedback" to qFeedback
                            )
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@GamifikasiComposeActivity, "Gagal: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            // Hitung Total Point untuk Badge
            val totalPoints = quests.values.sum()

            GamifikasiScreen(
                userName = userName,
                totalPoints = totalPoints,
                quests = quests,
                onHomeClick = {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}

@Composable
fun GamifikasiScreen(
    userName: String,
    totalPoints: Int,
    quests: Map<String, Int>,
    onHomeClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val maxPoints = 20

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MediPurplePrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp)
                .verticalScroll(scrollState)
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logoputih),
                contentDescription = "Logo",
                modifier = Modifier
                    .width(120.dp)
                    .height(32.dp)
                    .padding(start = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.padding(16.dp)) {
                // Header (Profile & Badge)
                HeaderSection(userName, totalPoints, maxPoints)

                Spacer(modifier = Modifier.height(16.dp))

                // List Quest
                QuestItem("On Time Hero", quests["on_time"] ?: 0, 5)
                Spacer(modifier = Modifier.height(8.dp))
                QuestItem("Routine Check-up", quests["routine"] ?: 0, 5)
                Spacer(modifier = Modifier.height(8.dp))
                QuestItem("Healthy Reader", quests["reading"] ?: 0, 5)
                Spacer(modifier = Modifier.height(8.dp))
                QuestItem("Feedback Appointment", quests["feedback"] ?: 0, 5)

                Spacer(modifier = Modifier.height(24.dp))

                // Tombol Home
                OutlinedButton(
                    onClick = onHomeClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
                ) {
                    Text("Home")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun HeaderSection(userName: String, totalPoints: Int, maxPoints: Int) {
    // Logika penentuan Badge & Kata motivasi
    val mainBadgeRes = when {
        totalPoints >= 20 -> R.drawable.badge_gold
        totalPoints >= 10 -> R.drawable.badge_silver
        totalPoints >= 5 -> R.drawable.badge_bronze
        else -> R.drawable.badge_abu
    }

    val motivationalText = when {
        totalPoints >= 20 -> "Congratulations!\nYou are a Hero!"
        totalPoints >= 10 -> "Awesome!\nAlmost Gold!"
        totalPoints >= 5 -> "Good job!\nGo for Silver!"
        else -> "Finish the task\nTo upgrade your\nBadge"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column {
                // Baris Profile
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Gray) // Placeholder bg_profile_circle
                    )
                    Text(
                        text = userName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MediTextPrimary,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1f)
                    )
                    Image(
                        painter = painterResource(id = mainBadgeRes),
                        contentDescription = "Main Badge",
                        modifier = Modifier.size(width = 80.dp, height = 50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar Badge
                Box(modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        progress = { totalPoints / maxPoints.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .align(Alignment.Center),
                        color = MediPurplePrimary,
                        trackColor = Color.LightGray,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.badge_bronze),
                            contentDescription = "Bronze",
                            modifier = Modifier.size(24.dp).alpha(if (totalPoints >= 5) 1f else 0.3f)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.badge_silver),
                            contentDescription = "Silver",
                            modifier = Modifier.size(24.dp).alpha(if (totalPoints >= 10) 1f else 0.3f)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.badge_gold),
                            contentDescription = "Gold",
                            modifier = Modifier.size(24.dp).alpha(if (totalPoints >= 20) 1f else 0.3f)
                        )
                    }
                }

                Text(
                    text = "$totalPoints/$maxPoints",
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = motivationalText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MediPurplePrimary,
                    lineHeight = 28.sp
                )
            }

        }
    }
}

@Composable
fun QuestItem(title: String, current: Int, max: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.dokterkecil),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                color = MediTextPrimary,
                modifier = Modifier.weight(1f)
            )
            Column(horizontalAlignment = Alignment.End) {
                LinearProgressIndicator(
                    progress = { current / max.toFloat() },
                    modifier = Modifier
                        .width(100.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MediPurplePrimary,
                    trackColor = Color.LightGray
                )
                Text(
                    text = "$current/$max",
                    fontSize = 12.sp,
                    color = MediTextSecondary
                )
            }
        }
    }
}