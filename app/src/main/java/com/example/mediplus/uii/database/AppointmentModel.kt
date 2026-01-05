package com.example.mediplus.uii.database

data class AppointmentModel(
    val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val gender: String = "",
    val phoneNumber: String = "",
    val time: String = "",
    val date: String = "",
    val dob: String = "",
    val address: String = "",
    val idNumber: String = "",
    val purpose: String = "",
    val status: String = "Pending",
    val feedback: String = ""
)