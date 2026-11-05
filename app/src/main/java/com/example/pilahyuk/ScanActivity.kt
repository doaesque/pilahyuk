
package com.example.pilahyuk

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class ScanActivity : AppCompatActivity() {

    private lateinit var imagePreview: ImageView
    private lateinit var scanStatus: TextView
    private lateinit var categoryContainer: LinearLayout
    private lateinit var tvWasteResult: TextView

    private val cameraLauncher =
        registerForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { bitmap: Bitmap? ->

            if (bitmap != null) {
                imagePreview.setImageBitmap(bitmap)

                scanStatus.text =
                    "Foto berhasil diambil! Silakan pilih kategori sampah."

                categoryContainer.visibility = View.VISIBLE
                tvWasteResult.visibility = View.GONE

            } else {
                scanStatus.text = "Foto belum berhasil diambil."
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        imagePreview = findViewById(R.id.imagePreview)
        scanStatus = findViewById(R.id.scanStatus)
        categoryContainer = findViewById(R.id.categoryContainer)
        tvWasteResult = findViewById(R.id.tvWasteResult)

        findViewById<Button>(R.id.btnTakePhoto).setOnClickListener {
            cameraLauncher.launch(null)
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnOrganic).setOnClickListener {
            showWasteResult(
                "ORGANIK",
                "Contoh: sisa makanan, kulit buah, dan daun.\n\nCara pengolahan: Pisahkan dari sampah lain. Sampah organik dapat diolah menjadi kompos."
            )
        }

        findViewById<Button>(R.id.btnPlastic).setOnClickListener {
            showWasteResult(
                "PLASTIK",
                "Contoh: botol plastik, gelas plastik, dan kemasan.\n\nCara pengolahan: Kosongkan isinya, bersihkan, lalu kumpulkan untuk didaur ulang."
            )
        }

        findViewById<Button>(R.id.btnPaper).setOnClickListener {
            showWasteResult(
                "KERTAS",
                "Contoh: kardus, koran, dan kertas bekas.\n\nCara pengolahan: Pisahkan dari sampah basah dan kumpulkan untuk didaur ulang."
            )
        }

        findViewById<Button>(R.id.btnResidual).setOnClickListener {
            showWasteResult(
                "RESIDU",
                "Contoh: tisu bekas, popok sekali pakai, dan sampah yang sulit didaur ulang.\n\nCara pengolahan: Bungkus dengan baik dan buang ke tempat sampah residu."
            )
        }
    }

    private fun showWasteResult(category: String, description: String) {
        tvWasteResult.text = "Kategori: $category\n\n$description"
        tvWasteResult.visibility = View.VISIBLE
    }
}
