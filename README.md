# MediPlus (Smart-Puskesmas)

**MediPlus** adalah aplikasi manajemen reservasi layanan kesehatan dan asisten cerdas berbasis *mobile* ("Smart-Puskesmas"). [cite_start]Aplikasi ini dirancang untuk mengatasi masalah antrean manual dan memberikan triase awal kepada pasien melalui fitur Chatbot[cite: 1].

## 🚀 Fitur Utama

Aplikasi ini memiliki 3 fitur unggulan untuk efisiensi layanan puskesmas/klinik:

1.  **Reservasi Online (Appointment System)**
    * Pasien dapat memilih Poli (Umum, Gigi, KIA, MCU), dan jadwal kunjungan dari rumah.
    * Sistem mengecek kuota harian secara otomatis.
2.  **Smart Assistant (Chatbot Triage)**
    * Fitur tanya-jawab cerdas untuk rekomendasi poli berdasarkan keluhan/gejala pasien.
    * Informasi prosedur medis (misal: syarat puasa sebelum Medical Check-up).
3.  **Tiket Antrean Digital**
    * Pasien mendapatkan *E-Ticket* dengan QR Code setelah booking berhasil.

## 👥 Pengguna Aplikasi

* **Pasien:** Melakukan pendaftaran, konsultasi chatbot, dan memantau antrean.
* **Staf Admin:** Memvalidasi kedatangan pasien dan mengatur jadwal di sistem.
* **Dokter:** Melihat daftar pasien harian.

## 🛠️ Tech Stack

* **Platform:** Mobile Application (Android)
* **Database:** Firebase Realtime Database
* **Authentication:** Firebase Auth
* **Design Tools:** Figma (untuk UI/UX)

## 📂 Struktur Data (Konsep)

Aplikasi ini menggunakan pendekatan basis data yang mencakup entitas berikut:
* `Users`: Data login dan role.
* `Pasien`: Profil detail (NIK).
* `Dokter & Jadwal`: Data tenaga medis dan slot waktu praktik.
* `Reservasi`: Data transaksi booking (Status: Booking/Check-in/Selesai).
* `ChatLog`: Riwayat percakapan untuk evaluasi akurasi chatbot.
