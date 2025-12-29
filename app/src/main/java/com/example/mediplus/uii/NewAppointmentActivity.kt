package com.example.mediplus.uii

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mediplus.R
import com.example.mediplus.uii.database.AppointmentModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NewAppointmentActivity : AppCompatActivity() {

    // Variabel UI
    private lateinit var edtFullName: EditText
    private lateinit var autoGender: AutoCompleteTextView
    private lateinit var edtPhone: EditText
    private lateinit var edtTime: EditText
    private lateinit var edtDate: EditText
    private lateinit var edtDob: EditText
    private lateinit var edtAddress: EditText
    private lateinit var edtIdNumber: EditText

    private lateinit var cbGeneral: CheckBox
    private lateinit var cbMaternal: CheckBox
    private lateinit var cbDental: CheckBox
    private lateinit var cbOthers: CheckBox
    private lateinit var edtOtherPurpose: EditText

    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_appointment)

        checkPermissions()
        initViews()
        setupDropdownGender()
        setupPickers()

        btnSave.setOnClickListener {
            saveAppointment()
        }
    }

    // Cek Permission Notifikasi & Alarm
    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
        // Cek Izin Exact Alarm (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }

    private fun initViews() {
        edtFullName = findViewById(R.id.edtFullName)
        autoGender = findViewById(R.id.autoGender)
        edtPhone = findViewById(R.id.edtPhone)
        edtTime = findViewById(R.id.edtTime)
        edtDate = findViewById(R.id.edtDate)
        edtDob = findViewById(R.id.edtDob)
        edtAddress = findViewById(R.id.edtAddress)
        edtIdNumber = findViewById(R.id.edtIdNumber)

        cbGeneral = findViewById(R.id.cbGeneral)
        cbMaternal = findViewById(R.id.cbMaternal)
        cbDental = findViewById(R.id.cbDental)
        cbOthers = findViewById(R.id.cbOthers)
        edtOtherPurpose = findViewById(R.id.edtOtherPurpose)

        btnSave = findViewById(R.id.btnMakeAppointment)
    }

    private fun setupDropdownGender() {
        val genders = arrayOf("Male", "Female")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genders)
        autoGender.setAdapter(adapter)
        autoGender.keyListener = null
        autoGender.setOnTouchListener { _, _ -> autoGender.showDropDown(); false }
    }

    private fun setupPickers() {
        edtDate.keyListener = null
        edtDate.setOnClickListener { showDatePicker(edtDate) }
        edtDob.keyListener = null
        edtDob.setOnClickListener { showDatePicker(edtDob) }
        edtTime.keyListener = null
        edtTime.setOnClickListener { showTimePicker() }
    }

    private fun showDatePicker(targetEditText: EditText) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                val formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)
                targetEditText.setText(formattedDate)
                targetEditText.error = null
            },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hour, minute ->
                var roundedMinute = (Math.round(minute.toFloat() / 10) * 10)
                var finalHour = hour
                if (roundedMinute == 60) {
                    roundedMinute = 0; finalHour += 1
                }
                if (finalHour == 24) finalHour = 0
                val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", finalHour, roundedMinute)
                edtTime.setText(formattedTime)
                edtTime.error = null
            },
            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true
        )
        timePickerDialog.show()
    }

    // --- FUNGSI JADWALKAN NOTIFIKASI REAL (PERBAIKAN) ---
    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleNotification(date: String, time: String) {
        try {
            // 1. Parse Waktu Appointment
            val dateTimeString = "$date $time" // Contoh: "24/12/2025 14:00"
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val dateObj = sdf.parse(dateTimeString)

            val calendar = Calendar.getInstance()
            if (dateObj != null) {
                calendar.time = dateObj

                // 2. Mundurkan 30 Menit untuk Reminder
                calendar.add(Calendar.MINUTE, -30)
                val triggerTime = calendar.timeInMillis

                // Debug: Cek selisih waktu
                val now = System.currentTimeMillis()
                val diffSeconds = (triggerTime - now) / 1000

                if (triggerTime > now) {
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val intent = Intent(this, NotificationReceiver::class.java).apply {
                        putExtra("title", "Appointment Reminder")
                        putExtra("message", "Halo! 30 menit lagi jadwal konsultasimu dimulai.")
                    }

                    val uniqueId = (System.currentTimeMillis() / 1000).toInt()
                    val pendingIntent = PendingIntent.getBroadcast(
                        this, uniqueId, intent, PendingIntent.FLAG_IMMUTABLE
                    )

                    // 3. Gunakan setExactAndAllowWhileIdle (LEBIH KUAT DARI DOZE MODE)
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        } else {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        }

                        // INFO PENTING BUAT KAMU:
                        Toast.makeText(this, "Pengingat aktif! Akan bunyi dalam ${diffSeconds / 60} menit", Toast.LENGTH_LONG).show()
                        Log.d("NotifDebug", "Alarm set for: ${sdf.format(calendar.time)}")

                    } catch (e: SecurityException) {
                        Toast.makeText(this, "Gagal: Izin Alarm belum diberikan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Beri tahu user kalau waktunya kependekan
                    Toast.makeText(this, "Waktu < 30 menit, pengingat tidak dipasang.", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error Waktu: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveAppointment() {
        val name = edtFullName.text.toString().trim()
        val gender = autoGender.text.toString().trim()
        val phone = edtPhone.text.toString().trim()
        val time = edtTime.text.toString().trim()
        val date = edtDate.text.toString().trim()
        val dob = edtDob.text.toString().trim()
        val address = edtAddress.text.toString().trim()
        val idNum = edtIdNumber.text.toString().trim()

        val purposes = StringBuilder()
        if (cbGeneral.isChecked) purposes.append("General Check-up, ")
        if (cbMaternal.isChecked) purposes.append("Maternal/Immunization, ")
        if (cbDental.isChecked) purposes.append("Dental Care, ")
        if (cbOthers.isChecked) {
            val otherText = edtOtherPurpose.text.toString().trim()
            if (otherText.isNotEmpty()) purposes.append("Others: $otherText")
        }

        var finalPurpose = purposes.toString()
        if (finalPurpose.endsWith(", ")) finalPurpose = finalPurpose.substring(0, finalPurpose.length - 2)
        if (finalPurpose.isEmpty()) finalPurpose = "General Check-up"

        if (name.isEmpty() || phone.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Mohon lengkapi data utama!", Toast.LENGTH_SHORT).show()
            return
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        btnSave.isEnabled = false
        btnSave.text = "Saving..."

        val database = FirebaseDatabase.getInstance("https://mediplusapp-e6128-default-rtdb.firebaseio.com")
        val ref = database.getReference("appointments")
        val appointmentId = ref.push().key

        if (appointmentId != null) {
            val newAppt = AppointmentModel(
                id = appointmentId,
                userId = user.uid,
                fullName = name,
                gender = gender,
                phoneNumber = phone,
                time = time,
                date = date,
                dob = dob,
                address = address,
                idNumber = idNum,
                purpose = finalPurpose,
                status = "Pending"
            )

            ref.child(appointmentId).setValue(newAppt)
                .addOnSuccessListener {
                    btnSave.isEnabled = true
                    btnSave.text = "Make Appointment"

                    // --- PASANG PENGINGAT ---
                    scheduleNotification(date, time)

                    Toast.makeText(this, "Janji Temu Berhasil!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    btnSave.isEnabled = true
                    btnSave.text = "Make Appointment"
                    Toast.makeText(this, "Gagal: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}