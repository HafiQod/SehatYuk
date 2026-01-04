package com.example.mediplus.uii

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mediplus.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var edtName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnBack: ImageView
    private lateinit var btnUpdate: MaterialButton
    private lateinit var loadingDialog: ProgressDialog
    private val database = FirebaseDatabase.getInstance("https://mediplusapp-e6128-default-rtdb.firebaseio.com")
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        loadUserData()

        btnBack.setOnClickListener { finish() }

        btnUpdate.setOnClickListener {
            updateUserProfile()
        }
    }

    private fun initViews() {
        edtName = findViewById(R.id.edtProfileName)
        edtEmail = findViewById(R.id.edtProfileEmail)
        edtPassword = findViewById(R.id.edtProfilePassword)
        btnBack = findViewById(R.id.btnBack)
        btnUpdate = findViewById(R.id.btnUpdateProfile)

        loadingDialog = ProgressDialog(this)
        loadingDialog.setMessage("Updating Profile...")
        loadingDialog.setCancelable(false)
    }

    private fun loadUserData() {
        val user = auth.currentUser
        if (user != null) {
            edtEmail.setText(user.email)

            val userRef = database.getReference("users").child(user.uid)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("fullName").getValue(String::class.java)
                        edtName.setText(name)
                    } else {
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProfileActivity, "Gagal ambil data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun updateUserProfile() {
        val user = auth.currentUser ?: return
        val newName = edtName.text.toString().trim()
        val newEmail = edtEmail.text.toString().trim()
        val newPassword = edtPassword.text.toString().trim()

        if (newName.isEmpty() || newEmail.isEmpty()) {
            Toast.makeText(this, "Nama dan Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        loadingDialog.show()

        val userRef = database.getReference("users").child(user.uid)
        val userMap = mapOf(
            "fullName" to newName,
            "email" to newEmail
        )

        userRef.updateChildren(userMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                updateAuthData(user, newEmail, newPassword)
            } else {
                loadingDialog.dismiss()
                Toast.makeText(this, "Gagal update database: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateAuthData(user: FirebaseUser, newEmail: String, newPassword: String) {
        if (newEmail != user.email) {
            user.updateEmail(newEmail).addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(this, "Gagal ganti email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        if (newPassword.isNotEmpty()) {
            if (newPassword.length < 6) {
                loadingDialog.dismiss()
                Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                return
            }
            user.updatePassword(newPassword).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password berhasil diubah", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Gagal ganti password: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        loadingDialog.dismiss()
        Toast.makeText(this, "Profil Berhasil Disimpan!", Toast.LENGTH_LONG).show()
    }
}