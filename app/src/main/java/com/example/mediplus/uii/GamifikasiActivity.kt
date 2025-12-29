package com.example.mediplus.uii

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mediplus.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class GamifikasiActivity : AppCompatActivity() {

    // UI Components
    private lateinit var tvUserName: TextView

    // Badge Components
    private lateinit var ivBadgeMain: ImageView
    private lateinit var ivBadgeBronze: ImageView
    private lateinit var ivBadgeSilver: ImageView
    private lateinit var ivBadgeGold: ImageView
    private lateinit var pbBadge: ProgressBar
    private lateinit var tvProgressBadge: TextView
    private lateinit var tvFinishTask: TextView

    // Quest Components
    private lateinit var pbQuestOnTime: ProgressBar
    private lateinit var tvQuestOnTime: TextView

    private lateinit var pbQuestRoutine: ProgressBar
    private lateinit var tvQuestRoutine: TextView

    private lateinit var pbQuestReading: ProgressBar
    private lateinit var tvQuestReading: TextView

    private lateinit var pbQuestFeedback: ProgressBar
    private lateinit var tvQuestFeedback: TextView

    private lateinit var btnHome: Button

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gamifikasi)

        initViews()

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        // PENTING: Pastikan URL Database sama dengan ProfileActivity
        // Jika di ProfileActivity pakai URL panjang, di sini juga sebaiknya disamakan atau cukup getInstance() jika google-services.json sudah benar.
        database = FirebaseDatabase.getInstance("https://mediplusapp-e6128-default-rtdb.firebaseio.com").getReference("users")

        if (user != null) {
            loadUserData(user.uid)
        } else {
            Toast.makeText(this, "User tidak ditemukan, silakan login ulang", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun initViews() {
        tvUserName = findViewById(R.id.tvUserName)

        // Inisialisasi Badge Area
        ivBadgeMain = findViewById(R.id.ivBadgeMain)
        ivBadgeBronze = findViewById(R.id.ivBadgeBronze)
        ivBadgeSilver = findViewById(R.id.ivBadgeSilver)
        ivBadgeGold = findViewById(R.id.ivBadgeGold)
        pbBadge = findViewById(R.id.pbBadge)
        tvProgressBadge = findViewById(R.id.tvProgressBadge)
        tvFinishTask = findViewById(R.id.tvFinishTask)

        // Inisialisasi Quest Area
        pbQuestOnTime = findViewById(R.id.pbQuestOnTime)
        tvQuestOnTime = findViewById(R.id.tvQuestOnTime)

        pbQuestRoutine = findViewById(R.id.pbQuestRoutine)
        tvQuestRoutine = findViewById(R.id.tvQuestRoutine)

        pbQuestReading = findViewById(R.id.pbQuestReading)
        tvQuestReading = findViewById(R.id.tvQuestReading)

        pbQuestFeedback = findViewById(R.id.pbQuestFeedback)
        tvQuestFeedback = findViewById(R.id.tvQuestFeedback)

        btnHome = findViewById(R.id.btnHome)
    }

    private fun loadUserData(userId: String) {
        // Mendengarkan perubahan data secara realtime
        database.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // 1. Ambil Username (Sesuai ProfileActivity: 'fullName')
                    // Sebelumnya 'username', sekarang diganti 'fullName' agar sinkron
                    val name = snapshot.child("fullName").getValue(String::class.java)
                        ?: snapshot.child("username").getValue(String::class.java) // Fallback ke username jika fullName kosong
                        ?: "User"

                    tvUserName.text = name

                    // 2. Ambil Progress Quest (Default 0 jika belum ada)
                    val progOnTime = snapshot.child("quests/on_time").getValue(Int::class.java) ?: 0
                    val progRoutine = snapshot.child("quests/routine").getValue(Int::class.java) ?: 0
                    val progReading = snapshot.child("quests/reading").getValue(Int::class.java) ?: 0
                    val progFeedback = snapshot.child("quests/feedback").getValue(Int::class.java) ?: 0

                    // 3. Update Tampilan Quest
                    updateQuestUI(pbQuestOnTime, tvQuestOnTime, progOnTime, 5)
                    updateQuestUI(pbQuestRoutine, tvQuestRoutine, progRoutine, 5)
                    updateQuestUI(pbQuestReading, tvQuestReading, progReading, 5)
                    updateQuestUI(pbQuestFeedback, tvQuestFeedback, progFeedback, 5)

                    // 4. Hitung Total Poin dan Update Badge
                    val totalPoints = progOnTime + progRoutine + progReading + progFeedback
                    updateBadgeSystem(totalPoints)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateQuestUI(progressBar: ProgressBar, textView: TextView, current: Int, target: Int) {
        progressBar.max = target
        progressBar.progress = current
        textView.text = "$current/$target"
    }

    private fun updateBadgeSystem(totalPoints: Int) {
        val maxPoints = 20

        pbBadge.max = maxPoints
        pbBadge.progress = totalPoints
        tvProgressBadge.text = "$totalPoints/$maxPoints"

        // Set Default: Badge Kecil Transparan (Redup)
        ivBadgeBronze.alpha = 0.3f
        ivBadgeSilver.alpha = 0.3f
        ivBadgeGold.alpha = 0.3f

        // Set Default: Badge Utama Abu-abu
        ivBadgeMain.setImageResource(R.drawable.badge_abu)
        tvFinishTask.text = "Finish tasks to\nunlock Bronze!"

        // LEVEL 1: BRONZE (>= 5 Quest Point)
        if (totalPoints >= 5) {
            ivBadgeBronze.alpha = 1.0f
            ivBadgeMain.setImageResource(R.drawable.badge_bronze)
            tvFinishTask.text = "Good job!\nGo for Silver!"
        }

        // LEVEL 2: SILVER (>= 10 Quest Point)
        if (totalPoints >= 10) {
            ivBadgeSilver.alpha = 1.0f
            ivBadgeMain.setImageResource(R.drawable.badge_silver)
            tvFinishTask.text = "Awesome!\nAlmost Gold!"
        }

        // LEVEL 3: GOLD (>= 20 Quest Point / Selesai Semua)
        if (totalPoints >= 20) {
            ivBadgeGold.alpha = 1.0f
            ivBadgeMain.setImageResource(R.drawable.badge_gold)
            tvFinishTask.text = "Congratulations!\nYou are a Hero!"
        }
    }
}