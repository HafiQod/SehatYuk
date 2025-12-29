package com.example.mediplus.uii.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mediplus.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var edtUsername: EditText
    private lateinit var edtPassword: EditText

    // Inisialisasi Database
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        edtUsername = findViewById(R.id.edtUsername)
        edtPassword = findViewById(R.id.edtPassword)

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        btnRegister.setOnClickListener {
            val email = edtUsername.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            // Buat username sederhana dari email (contoh: budi@gmail.com -> budi)
            val username = if (email.contains("@")) email.split("@")[0] else email

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan Password harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            // SIMPAN DATA QUEST AWAL (Default 0)
                            saveDefaultUserData(userId, username, email)
                        }
                    } else {
                        Toast.makeText(this, "Gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun saveDefaultUserData(userId: String, username: String, email: String) {
        val userMap = hashMapOf(
            "username" to username,
            "email" to email,
            "quests" to hashMapOf(
                "on_time" to 0,
                "routine" to 0,
                "reading" to 0,
                "feedback" to 0
            )
        )

        database.child("users").child(userId).setValue(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Registrasi Berhasil!", Toast.LENGTH_LONG).show()
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}