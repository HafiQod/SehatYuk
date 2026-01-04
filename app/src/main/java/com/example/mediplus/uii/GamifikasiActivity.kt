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

    private lateinit var tvUserName: TextView
    private lateinit var ivBadgeMain: ImageView
    private lateinit var ivBadgeBronze: ImageView
    private lateinit var ivBadgeSilver: ImageView
    private lateinit var ivBadgeGold: ImageView
    private lateinit var pbBadge: ProgressBar
    private lateinit var tvProgressBadge: TextView
    private lateinit var tvFinishTask: TextView
    private lateinit var pbQuestOnTime: ProgressBar
    private lateinit var tvQuestOnTime: TextView
    private lateinit var pbQuestRoutine: ProgressBar
    private lateinit var tvQuestRoutine: TextView
    private lateinit var pbQuestReading: ProgressBar
    private lateinit var tvQuestReading: TextView
    private lateinit var pbQuestFeedback: ProgressBar
    private lateinit var tvQuestFeedback: TextView
    private lateinit var btnHome: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gamifikasi)

        initViews()

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
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
        ivBadgeMain = findViewById(R.id.ivBadgeMain)
        ivBadgeBronze = findViewById(R.id.ivBadgeBronze)
        ivBadgeSilver = findViewById(R.id.ivBadgeSilver)
        ivBadgeGold = findViewById(R.id.ivBadgeGold)
        pbBadge = findViewById(R.id.pbBadge)
        tvProgressBadge = findViewById(R.id.tvProgressBadge)
        tvFinishTask = findViewById(R.id.tvFinishTask)
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
        database.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("fullName").getValue(String::class.java)
                        ?: snapshot.child("username").getValue(String::class.java)
                        ?: "User"

                    tvUserName.text = name

                    val progOnTime = snapshot.child("quests/on_time").getValue(Int::class.java) ?: 0
                    val progRoutine = snapshot.child("quests/routine").getValue(Int::class.java) ?: 0
                    val progReading = snapshot.child("quests/reading").getValue(Int::class.java) ?: 0
                    val progFeedback = snapshot.child("quests/feedback").getValue(Int::class.java) ?: 0

                    updateQuestUI(pbQuestOnTime, tvQuestOnTime, progOnTime, 5)
                    updateQuestUI(pbQuestRoutine, tvQuestRoutine, progRoutine, 5)
                    updateQuestUI(pbQuestReading, tvQuestReading, progReading, 5)
                    updateQuestUI(pbQuestFeedback, tvQuestFeedback, progFeedback, 5)

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

        ivBadgeBronze.alpha = 0.3f
        ivBadgeSilver.alpha = 0.3f
        ivBadgeGold.alpha = 0.3f

        ivBadgeMain.setImageResource(R.drawable.badge_abu)
        tvFinishTask.text = "Finish tasks to\nunlock Bronze!"

        if (totalPoints >= 5) {
            ivBadgeBronze.alpha = 1.0f
            ivBadgeMain.setImageResource(R.drawable.badge_bronze)
            tvFinishTask.text = "Good job!\nGo for Silver!"
        }

        if (totalPoints >= 10) {
            ivBadgeSilver.alpha = 1.0f
            ivBadgeMain.setImageResource(R.drawable.badge_silver)
            tvFinishTask.text = "Awesome!\nAlmost Gold!"
        }

        if (totalPoints >= 20) {
            ivBadgeGold.alpha = 1.0f
            ivBadgeMain.setImageResource(R.drawable.badge_gold)
            tvFinishTask.text = "Congratulations!\nYou are a Hero!"
        }
    }
}