package com.example.mediplus.uii

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
        // PENTING: Pastikan URL Database sesuai
        database = FirebaseDatabase.getInstance("https://mediplusapp-e6128-default-rtdb.firebaseio.com").getReference("users")

        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(this, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            // State untuk menampung data
            var userName by remember { mutableStateOf("Loading...") }
            // Map untuk menyimpan progress tiap misi (Key: Nama Misi, Value: Progress 0-5)
            var quests by remember { mutableStateOf(mapOf(
                "on_time" to 0,
                "routine" to 0,
                "reading" to 0,
                "feedback" to 0
            )) }

            // Mengambil data Realtime dari Firebase
            LaunchedEffect(currentUser.uid) {
                database.child(currentUser.uid).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // 1. Ambil Nama User
                            val name = snapshot.child("fullName").getValue(String::class.java)
                                ?: snapshot.child("username").getValue(String::class.java)
                                ?: "User"
                            userName = name

                            // 2. Ambil Progress Quest (Pastikan struktur di Firebase: users -> uid -> quests -> on_time)
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
                        Toast.makeText(this@GamifikasiComposeActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            // Hitung Total Point (Dijumlahkan dari semua misi)
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
                },
                onArticleClick = {
                    // TODO: Arahkan ke Halaman Artikel
                    Toast.makeText(this, "Membuka Healthy Reader...", Toast.LENGTH_SHORT).show()
                    // val intent = Intent(this, ArticleActivity::class.java)
                    // startActivity(intent)
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
    onHomeClick: () -> Unit,
    onArticleClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val maxPoints = 20 // 4 misi x 5 poin

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
                // Header (Profile, Total Poin & Badge Logic)
                HeaderSection(userName, totalPoints, maxPoints)

                Spacer(modifier = Modifier.height(16.dp))

                // --- DAFTAR MISI ---

                // 1. On Time Hero (Datang Tepat Waktu)
                QuestItem(
                    title = "On Time Hero",
                    current = quests["on_time"] ?: 0,
                    max = 5,
                    // Misi ini otomatis nambah by system, user tidak perlu klik
                    onClick = { /* Tidak ada aksi klik, hanya display */ }
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 2. Routine Check-up (1 bulan sekali)
                QuestItem(
                    title = "Routine Check-up",
                    current = quests["routine"] ?: 0,
                    max = 5,
                    onClick = { /* Tidak ada aksi klik */ }
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 3. Healthy Reader (Baca Artikel) - BISA DIKLIK
                QuestItem(
                    title = "Healthy Reader",
                    current = quests["reading"] ?: 0,
                    max = 5,
                    onClick = onArticleClick, // Aksi pindah halaman
                    isClickable = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 4. Feedback Appointment (Isi Feedback)
                QuestItem(
                    title = "Feedback Appointment",
                    current = quests["feedback"] ?: 0,
                    max = 5,
                    onClick = { /* Biasanya trigger dari halaman feedback selesai */ }
                )

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
    // Logika Level Badge berdasarkan Total Poin
    val mainBadgeRes = when {
        totalPoints >= 20 -> R.drawable.badge_gold // Hero
        totalPoints >= 10 -> R.drawable.badge_silver // Hampir Gold
        totalPoints >= 5 -> R.drawable.badge_bronze  // Pemula
        else -> R.drawable.badge_abu // Belum ada badge
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Profile Placeholder
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
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
                    // Badge Utama Besar
                    Image(
                        painter = painterResource(id = mainBadgeRes),
                        contentDescription = "Main Badge",
                        modifier = Modifier.size(width = 80.dp, height = 50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar Total Poin
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

                    // Indikator Badge Kecil
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
fun QuestItem(
    title: String,
    current: Int,
    max: Int,
    onClick: () -> Unit,
    isClickable: Boolean = false
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isClickable) Modifier.clickable { onClick() } else Modifier) // Hanya bisa diklik jika isClickable true
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

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = MediTextPrimary
                )
                // Jika Healthy Reader, tambahkan hint "Tap to read"
                if (isClickable) {
                    Text(
                        text = "Tap to read articles",
                        fontSize = 10.sp,
                        color = MediPurplePrimary
                    )
                }
            }

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