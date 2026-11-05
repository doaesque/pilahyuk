
package com.example.pilahyuk

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private var isIndonesian = true

    // Pengaturan bahasa bersama untuk seluruh aplikasi
    private val preferences by lazy {
        getSharedPreferences("PilahYukSettings", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Ambil bahasa yang terakhir dipilih
        isIndonesian = preferences.getString("language", "id") == "id"

        val menuLanguage = findViewById<TextView>(R.id.menuLanguage)
        val menuContact = findViewById<TextView>(R.id.menuContact)
        val menuAbout = findViewById<TextView>(R.id.menuAbout)
        val menuPrivacy = findViewById<TextView>(R.id.menuPrivacy)

        // Tampilkan bahasa sesuai pengaturan tersimpan
        updateLanguage()

        // LANGUAGE
        menuLanguage.setOnClickListener {
            isIndonesian = !isIndonesian

            // Simpan pilihan bahasa
            preferences.edit()
                .putString("language", if (isIndonesian) "id" else "en")
                .apply()

            updateLanguage()
        }

        // CONTACT DEVELOPER
        menuContact.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(
                    if (isIndonesian) "Hubungi Pengembang"
                    else "Contact Developer"
                )
                .setMessage(
                    if (isIndonesian) {
                        "Terima kasih telah menggunakan PilahYuk!"
                    } else {
                        "Thank you for using PilahYuk!"
                    }
                )
                .setPositiveButton("OK", null)
                .show()
        }

        // ABOUT
        menuAbout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(
                    if (isIndonesian) "Tentang PilahYuk"
                    else "About PilahYuk"
                )
                .setMessage(
                    if (isIndonesian) {
                        "PilahYuk adalah aplikasi edukasi pemilahan sampah dan pencarian bank sampah terdekat untuk membantu menjaga lingkungan."
                    } else {
                        "PilahYuk is a waste sorting education and nearby waste bank finder application."
                    }
                )
                .setPositiveButton("OK", null)
                .show()
        }

        // PRIVACY
        menuPrivacy.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(
                    if (isIndonesian) "Kebijakan Privasi"
                    else "Privacy Policy"
                )
                .setMessage(
                    if (isIndonesian) {
                        "PilahYuk menggunakan izin lokasi untuk membantu menemukan bank sampah terdekat. Data lokasi digunakan untuk kebutuhan fitur tersebut."
                    } else {
                        "PilahYuk uses location permission to help find nearby waste banks. Location data is used for this feature."
                    }
                )
                .setPositiveButton("OK", null)
                .show()
        }

        // LOGIN
        findViewById<TextView>(R.id.btnLogin).setOnClickListener {
            Toast.makeText(
                this,
                if (isIndonesian) "Fitur akun akan segera hadir."
                else "Account feature is coming soon.",
                Toast.LENGTH_SHORT
            ).show()
        }

        // BOTTOM NAVIGATION
        findViewById<TextView>(R.id.profileNavHome).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.profileNavScan).setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }

        findViewById<TextView>(R.id.profileNavLocation).setOnClickListener {
            startActivity(Intent(this, BankMapActivity::class.java))
        }

        findViewById<TextView>(R.id.profileNavProfile).setOnClickListener {
            // Sudah berada di halaman profil
        }
    }

    private fun updateLanguage() {
        findViewById<TextView>(R.id.menuLanguage).text =
            if (isIndonesian) "🌐  Bahasa Indonesia"
            else "🌐  English"

        findViewById<TextView>(R.id.profileTitle).text =
            if (isIndonesian) "Profil" else "Profile"

        findViewById<TextView>(R.id.guestTitle).text =
            if (isIndonesian) "Pengguna Tamu" else "Guest User"

        findViewById<TextView>(R.id.guestDescription).text =
            if (isIndonesian) {
                "Nikmati fitur PilahYuk tanpa perlu membuat akun."
            } else {
                "Enjoy PilahYuk features without creating an account."
            }

        findViewById<TextView>(R.id.settingsTitle).text =
            if (isIndonesian) "Pengaturan" else "Settings"

        findViewById<TextView>(R.id.menuContact).text =
            if (isIndonesian) "✉️  Hubungi Pengembang"
            else "✉️  Contact Developer"

        findViewById<TextView>(R.id.menuAbout).text =
            if (isIndonesian) "ℹ️  Tentang PilahYuk"
            else "ℹ️  About PilahYuk"

        findViewById<TextView>(R.id.menuPrivacy).text =
            if (isIndonesian) "🔒  Kebijakan Privasi"
            else "🔒  Privacy Policy"

        findViewById<TextView>(R.id.profileNavLocation).text =
            if (isIndonesian) "⌖\nLokasi" else "⌖\nLocation"

        findViewById<TextView>(R.id.profileNavProfile).text =
            if (isIndonesian) "●\nProfil" else "●\nProfile"
    }
}
