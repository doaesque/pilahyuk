
package com.example.pilahyuk

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var isIndonesian = true

    private lateinit var searchInput: EditText
    private lateinit var searchResults: LinearLayout

    private data class WasteItem(
        val nameEn: String,
        val nameId: String,
        val categoryEn: String,
        val categoryId: String,
        val keywords: List<String>,
        val detailsEn: String,
        val detailsId: String
    )

    private val wasteItems = listOf(
        WasteItem(
            "Banana Peel", "Kulit Pisang",
            "Organic Waste", "Sampah Organik",
            listOf("banana", "banana peel", "kulit pisang", "pisang"),
            "Can be composted. Cut into small pieces and mix with organic materials.",
            "Dapat dijadikan kompos. Potong kecil dan campurkan dengan bahan organik lainnya."
        ),
        WasteItem(
            "Plastic Bottle", "Botol Plastik",
            "Plastic Waste", "Sampah Plastik",
            listOf("plastic", "bottle", "botol", "plastik"),
            "Empty and clean the bottle before taking it to a recycling center.",
            "Kosongkan dan bersihkan botol sebelum disetorkan ke tempat daur ulang."
        ),
        WasteItem(
            "Cardboard", "Kardus",
            "Paper Waste", "Sampah Kertas",
            listOf("cardboard", "paper", "kardus", "kertas", "koran"),
            "Keep paper dry and remove food residue before recycling.",
            "Jaga kertas tetap kering dan bersihkan sisa makanan sebelum didaur ulang."
        ),
        WasteItem(
            "Used Tissue", "Tisu Bekas",
            "Residual Waste", "Sampah Residu",
            listOf("tissue", "tisu", "diaper", "popok"),
            "Generally not accepted in paper recycling. Dispose according to local rules.",
            "Umumnya tidak diterima dalam daur ulang kertas. Buang sesuai aturan setempat."
        ),
        WasteItem(
            "Aluminum Can", "Kaleng Aluminium",
            "Metal Recyclables", "Sampah Logam",
            listOf("aluminum", "can", "kaleng", "logam"),
            "Empty and rinse the can before recycling.",
            "Kosongkan dan bilas kaleng sebelum didaur ulang."
        ),
        WasteItem(
            "Glass Bottle", "Botol Kaca",
            "Glass Recyclables", "Sampah Kaca",
            listOf("glass", "bottle", "kaca", "botol kaca"),
            "Separate glass from other waste and handle broken glass carefully.",
            "Pisahkan kaca dari sampah lainnya dan tangani pecahan kaca dengan hati-hati."
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchInput = findViewById(R.id.searchInput)
        searchResults = findViewById(R.id.searchResults)

        // Pencarian sampah
        searchInput.addTextChangedListener(
            object : android.text.TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?, start: Int, before: Int, count: Int
                ) {
                    searchWaste(s?.toString().orEmpty())
                }

                override fun afterTextChanged(s: android.text.Editable?) {}
            }
        )


        // Kategori sampah
        findViewById<Button>(R.id.btnOrganic).setOnClickListener {
            showCategory(
                if (isIndonesian) "Sampah Organik" else "Organic Waste",
                if (isIndonesian)
                    "Contoh: kulit buah, sisa makanan, sayuran dan daun.\n\nCara mengolah: jadikan kompos."
                else
                    "Examples: fruit peels, food scraps, vegetables and leaves.\n\nHow to manage: turn them into compost."
            )
        }

        findViewById<Button>(R.id.btnPlastic).setOnClickListener {
            showCategory(
                if (isIndonesian) "Sampah Plastik" else "Plastic Waste",
                if (isIndonesian)
                    "Contoh: botol plastik dan kemasan.\n\nCara mengolah: bersihkan dan setorkan ke tempat daur ulang."
                else
                    "Examples: plastic bottles and packaging.\n\nHow to manage: clean and take them to a recycling center."
            )
        }

        findViewById<Button>(R.id.btnPaper).setOnClickListener {
            showCategory(
                if (isIndonesian) "Sampah Kertas" else "Paper Waste",
                if (isIndonesian)
                    "Contoh: koran, kardus dan kertas bekas.\n\nCara mengolah: jaga tetap kering dan daur ulang."
                else
                    "Examples: newspapers, cardboard and used paper.\n\nHow to manage: keep dry and recycle."
            )
        }

        findViewById<Button>(R.id.btnResidual).setOnClickListener {
            showCategory(
                if (isIndonesian) "Sampah Residu" else "Residual Waste",
                if (isIndonesian)
                    "Contoh: tisu bekas dan popok sekali pakai.\n\nCara mengolah: buang sesuai aturan setempat."
                else
                    "Examples: used tissues and disposable diapers.\n\nHow to manage: dispose according to local rules."
            )
        }

        // Bank sampah
        findViewById<Button>(R.id.btnFindBank).setOnClickListener {
            startActivity(
                Intent(this, BankMapActivity::class.java)
            )
        }

        // Navigasi bawah
        setupNavigation()

        // Membuka halaman Scan
        findViewById<Button>(R.id.btnScan).setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }

    }

    override fun onResume() {
        super.onResume()

        // Ambil bahasa yang dipilih dari halaman Profil
        val preferences = getSharedPreferences(
            "PilahYukSettings",
            MODE_PRIVATE
        )

        isIndonesian = preferences.getString("language", "id") == "id"

        // Perbarui seluruh teks Beranda
        updateLanguage()
    }
    private fun setupNavigation() {

        findViewById<TextView>(R.id.navHome).setOnClickListener {
            updateNavigation(0)
        }

        findViewById<TextView>(R.id.navScan).setOnClickListener {
            updateNavigation(1)
            showComingSoon("Scan")
        }

        findViewById<TextView>(R.id.navLocation).setOnClickListener {
            updateNavigation(2)
            startActivity(
                Intent(this, BankMapActivity::class.java)
            )
        }

        findViewById<TextView>(R.id.navProfile).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.profileIcon).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateNavigation(selected: Int) {

        val navIds = listOf(
            R.id.navHome,
            R.id.navScan,
            R.id.navLocation,
            R.id.navProfile
        )

        navIds.forEachIndexed { index, id ->
            val item = findViewById<TextView>(id)

            item.setTextColor(
                if (index == selected)
                    android.graphics.Color.rgb(33, 131, 75)
                else
                    android.graphics.Color.rgb(130, 144, 132)
            )

            item.textSize = if (index == selected) 13f else 12f
        }
    }

    private fun showComingSoon(feature: String) {
        Toast.makeText(
            this,
            if (isIndonesian)
                "Fitur $feature sedang dalam pengembangan."
            else
                "$feature feature is under development.",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun searchWaste(input: String) {

        searchResults.removeAllViews()

        val query = input.trim().lowercase(Locale.ROOT)

        if (query.isEmpty()) {
            searchResults.visibility = View.GONE
            return
        }

        val results = wasteItems.filter { item ->
            val terms = item.keywords + listOf(
                item.nameEn,
                item.nameId,
                item.categoryEn,
                item.categoryId
            )

            terms.any {
                it.lowercase(Locale.ROOT).contains(query)
            }
        }

        searchResults.visibility = View.VISIBLE

        if (results.isEmpty()) {
            val message = TextView(this).apply {
                text = if (isIndonesian)
                    "Sampah tidak ditemukan."
                else
                    "No waste items found."

                textSize = 14f
                setTextColor(android.graphics.Color.DKGRAY)
                setPadding(12, 16, 12, 16)
            }

            searchResults.addView(message)
            return
        }

        results.forEach { item ->

            val resultButton = Button(this).apply {
                text = if (isIndonesian)
                    "${item.nameId} — ${item.categoryId}"
                else
                    "${item.nameEn} — ${item.categoryEn}"

                isAllCaps = false

                setOnClickListener {
                    showCategory(
                        if (isIndonesian) item.nameId else item.nameEn,
                        if (isIndonesian) item.detailsId else item.detailsEn
                    )
                }
            }

            searchResults.addView(resultButton)
        }
    }

    private fun showCategory(title: String, description: String) {

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(description)
            .setPositiveButton(
                if (isIndonesian) "Tutup" else "Close",
                null
            )
            .show()
    }

    private fun updateLanguage() {

        findViewById<TextView>(R.id.welcomeText).text =
            if (isIndonesian) "SELAMAT DATANG KEMBALI!"
            else "WELCOME BACK!"

        findViewById<TextView>(R.id.titleText).text = "PilahYuk 🌱"

        findViewById<TextView>(R.id.subtitleText).text =
            if (isIndonesian)
                "Mari ciptakan lingkungan yang lebih bersih!"
            else
                "Let's make our environment cleaner!"

        findViewById<TextView>(R.id.scanTitle).text =
            if (isIndonesian) "Kenali Jenis Sampahmu"
            else "Identify Your Waste"

        findViewById<TextView>(R.id.scanDescription).text =
            if (isIndonesian)
                "Pindai dan temukan kategori yang tepat."
            else
                "Scan and discover the right category."

        findViewById<Button>(R.id.btnScan).text =
            if (isIndonesian) "Mulai Scan"
            else "Start Scan"

        findViewById<TextView>(R.id.searchTitle).text =
            if (isIndonesian) "Cari Kategori Sampah"
            else "Find Your Waste Category"

        searchInput.hint =
            if (isIndonesian) "🔍  Cari jenis sampah..."
            else "🔍  Search waste items..."

        findViewById<TextView>(R.id.categoriesTitle).text =
            if (isIndonesian) "Kategori Sampah"
            else "Waste Categories"

        findViewById<TextView>(R.id.categoryDescription).text =
            if (isIndonesian)
                "Pilih kategori untuk mengetahui lebih lanjut."
            else
                "Choose a category to learn more."

        findViewById<Button>(R.id.btnOrganic).text =
            if (isIndonesian) "🍃\nOrganik" else "🍃\nOrganic\nWaste"

        findViewById<Button>(R.id.btnPlastic).text =
            if (isIndonesian) "♻️\nPlastik" else "♻️\nPlastic\nWaste"

        findViewById<Button>(R.id.btnPaper).text =
            if (isIndonesian) "📄\nKertas" else "📄\nPaper\nWaste"

        findViewById<Button>(R.id.btnResidual).text =
            if (isIndonesian) "🗑️\nResidu" else "🗑️\nResidual\nWaste"

        findViewById<TextView>(R.id.bankTitle).text =
            if (isIndonesian)
                "Cari Bank Sampah Terdekat"
            else
                "Find a Waste Bank Near You"

        findViewById<TextView>(R.id.bankDescription).text =
            if (isIndonesian)
                "Temukan tempat daur ulang dan pengumpulan sampah di sekitarmu."
            else
                "Find nearby recycling and waste collection points."

        findViewById<Button>(R.id.btnFindBank).text =
            if (isIndonesian)
                "📍 Cari Bank Sampah Terdekat"
            else
                "📍 Find Nearby Waste Banks"

        findViewById<TextView>(R.id.tipTitle).text =
            if (isIndonesian)
                "🌱 Tips Ramah Lingkungan"
            else
                "🌱 Today's Eco Tip"

        findViewById<TextView>(R.id.tipText).text =
            if (isIndonesian)
                "Bersihkan dan keringkan barang yang dapat didaur ulang sebelum memasukkannya ke tempat daur ulang."
            else
                "Clean and dry recyclable items before placing them in the recycling bin."

        findViewById<TextView>(R.id.navHome).text =
            if (isIndonesian) "⌂\nBeranda" else "⌂\nHome"

        findViewById<TextView>(R.id.navScan).text =
            if (isIndonesian) "▣\nPindai" else "▣\nScan"

        findViewById<TextView>(R.id.navLocation).text =
            if (isIndonesian) "⌖\nLokasi" else "⌖\nLocation"

        findViewById<TextView>(R.id.navProfile).text =
            if (isIndonesian) "♙\nProfil" else "♙\nProfile"

        updateNavigation(0)

        searchWaste(searchInput.text.toString())
    }
}
