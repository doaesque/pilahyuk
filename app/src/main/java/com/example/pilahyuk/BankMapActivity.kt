
package com.example.pilahyuk

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.Executors

data class BankSampah(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val distance: Float
)

class BankMapActivity : AppCompatActivity() {

    private lateinit var locationManager: LocationManager
    private lateinit var statusText: TextView
    private lateinit var listLayout: LinearLayout

    private val executor = Executors.newSingleThreadExecutor()

    private var currentLocation: Location? = null
    private var locationListener: LocationListener? = null

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (hasLocationPermission()) {
                getUserLocation()
            } else {
                statusText.text =
                    "Izin lokasi ditolak. Aktifkan izin lokasi untuk mencari tempat terdekat."
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager

        createLayout()

        if (hasLocationPermission()) {
            getUserLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun createLayout() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 16, 20, 12)
            setBackgroundColor(0xFFF5F8F5.toInt())
        }

        val title = TextView(this).apply {
            text = "♻️ Daftar Bank Sampah"
            textSize = 23f
            setTextColor(0xFF24643A.toInt())
            setPadding(0, 8, 0, 12)
        }

        statusText = TextView(this).apply {
            text = "Menyiapkan pencarian lokasi..."
            textSize = 14f
            setTextColor(0xFF555555.toInt())
            setPadding(0, 4, 0, 12)
        }

        val refreshButton = Button(this).apply {
            text = "Cari di Lokasi Saya"

            setOnClickListener {
                if (hasLocationPermission()) {
                    getUserLocation()
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
        }

        listLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            addView(listLayout)
        }

        root.addView(title)
        root.addView(statusText)
        root.addView(refreshButton)

        root.addView(
            scrollView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
    }

    private fun hasLocationPermission(): Boolean {
        val finePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarsePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return finePermission || coarsePermission
    }

    @Suppress("MissingPermission")
    private fun getUserLocation() {
        if (!hasLocationPermission()) return

        try {
            statusText.text = "Memeriksa lokasi perangkat..."

            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER
            )

            var bestLocation: Location? = null

            for (provider in providers) {
                try {
                    if (locationManager.isProviderEnabled(provider)) {
                        val lastLocation =
                            locationManager.getLastKnownLocation(provider)

                        if (
                            lastLocation != null &&
                            (
                                    bestLocation == null ||
                                            lastLocation.accuracy < bestLocation.accuracy
                                    )
                        ) {
                            bestLocation = lastLocation
                        }
                    }
                } catch (_: Exception) {
                    // Provider lokasi ini tidak tersedia.
                }
            }

            if (bestLocation != null) {
                currentLocation = bestLocation
                loadNearbyBanks(
                    bestLocation.latitude,
                    bestLocation.longitude
                )
                return
            }

            statusText.text =
                "Sedang mendapatkan lokasi. Pastikan layanan lokasi aktif..."

            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    try {
                        locationManager.removeUpdates(this)
                    } catch (_: SecurityException) {
                        // Izin lokasi tidak tersedia.
                    }

                    locationListener = null
                    currentLocation = location

                    loadNearbyBanks(
                        location.latitude,
                        location.longitude
                    )
                }
            }

            locationListener = listener
            var requested = false

            for (provider in providers) {
                try {
                    if (locationManager.isProviderEnabled(provider)) {
                        locationManager.requestLocationUpdates(
                            provider,
                            2000L,
                            0f,
                            listener,
                            android.os.Looper.getMainLooper()
                        )
                        requested = true
                    }
                } catch (_: Exception) {
                    // Coba provider berikutnya.
                }
            }

            if (!requested) {
                statusText.text =
                    "Aktifkan layanan lokasi pada perangkat."
            }
        } catch (_: SecurityException) {
            statusText.text = "Izin lokasi belum tersedia."
        } catch (_: Exception) {
            statusText.text = "Gagal mendapatkan lokasi."
        }
    }

    private fun loadNearbyBanks(lat: Double, lon: Double) {
        statusText.text =
            "Mencari tempat daur ulang dalam radius 5 km..."

        listLayout.removeAllViews()

        executor.execute {
            try {
                val query = """
                    [out:json][timeout:25];
                    nwr(around:5000,$lat,$lon)["amenity"="recycling"];
                    out center tags;
                """.trimIndent()

                val endpoints = listOf(
                    "https://overpass-api.de/api/interpreter",
                    "https://overpass.private.coffee/api/interpreter",
                    "https://maps.mail.ru/osm/tools/overpass/api/interpreter"
                )

                var response: String? = null
                val errors = mutableListOf<String>()


                for ((index, endpoint) in endpoints.withIndex()) {
                    var connection: HttpURLConnection? = null

                    try {
                        // Jeda bertahap sebelum mencoba server berikutnya.
                        if (index > 0) {
                            val delayMillis = 2000L * (1L shl (index - 1))
                            Thread.sleep(delayMillis)
                        }

                        connection = URL(endpoint)
                            .openConnection() as HttpURLConnection

                        connection.requestMethod = "POST"
                        connection.connectTimeout = 20000
                        connection.readTimeout = 45000
                        connection.doOutput = true

                        connection.setRequestProperty(
                            "Content-Type",
                            "application/x-www-form-urlencoded; charset=UTF-8"
                        )

                        connection.setRequestProperty(
                            "User-Agent",
                            "PilahYukAndroid/1.0"
                        )

                        val body = "data=" + URLEncoder.encode(query, "UTF-8")

                        connection.outputStream.use { output ->
                            output.write(body.toByteArray(Charsets.UTF_8))
                        }

                        val code = connection.responseCode

                        val stream = if (code in 200..299) {
                            connection.inputStream
                        } else {
                            connection.errorStream
                        }

                        val responseText = stream?.bufferedReader()?.use {
                            it.readText()
                        } ?: ""

                        if (code !in 200..299) {
                            throw Exception("HTTP $code")
                        }

                        JSONObject(responseText)
                        response = responseText
                        break

                    } catch (e: Exception) {
                        errors.add(
                            "${URL(endpoint).host}: ${e.message ?: "permintaan gagal"}"
                        )
                    } finally {
                        connection?.disconnect()
                    }
                }


                val responseText = response ?: throw Exception(
                    "Semua server Overpass gagal: " +
                            errors.joinToString(" | ")
                )

                val elements = JSONObject(responseText)
                    .getJSONArray("elements")

                val results = mutableListOf<BankSampah>()

                for (i in 0 until elements.length()) {
                    val item = elements.getJSONObject(i)
                    val tags = item.optJSONObject("tags")
                        ?: JSONObject()

                    val center = item.optJSONObject("center")

                    val itemLat = if (item.has("lat")) {
                        item.optDouble("lat", Double.NaN)
                    } else {
                        center?.optDouble("lat", Double.NaN)
                            ?: Double.NaN
                    }

                    val itemLon = if (item.has("lon")) {
                        item.optDouble("lon", Double.NaN)
                    } else {
                        center?.optDouble("lon", Double.NaN)
                            ?: Double.NaN
                    }

                    if (!itemLat.isFinite() || !itemLon.isFinite()) {
                        continue
                    }

                    val name = tags.optString("name")
                        .takeIf {
                            it.isNotBlank() && it != "null"
                        }
                        ?: "Tempat daur ulang tanpa nama"

                    val address = buildAddress(tags)

                    val distanceResult = FloatArray(1)

                    Location.distanceBetween(
                        lat,
                        lon,
                        itemLat,
                        itemLon,
                        distanceResult
                    )

                    results.add(
                        BankSampah(
                            name = name,
                            address = address,
                            latitude = itemLat,
                            longitude = itemLon,
                            distance = distanceResult[0]
                        )
                    )
                }

                val sortedResults = results
                    .distinctBy {
                        "${it.latitude},${it.longitude}"
                    }
                    .sortedBy { it.distance }

                runOnUiThread {
                    statusText.text = if (sortedResults.isEmpty()) {
                        "Belum ada tempat daur ulang yang tercatat dalam radius 5 km."
                    } else {
                        "Ditemukan ${sortedResults.size} tempat dalam radius 5 km."
                    }

                    listLayout.removeAllViews()

                    sortedResults.forEach { bank ->
                        addBankCard(bank)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    statusText.text =
                        "Gagal mengambil data. Periksa internet lalu coba lagi."

                    Toast.makeText(
                        this,
                        "Pencarian gagal: ${
                            e.message ?: "koneksi bermasalah"
                        }",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun buildAddress(tags: JSONObject): String {
        val fullAddress = tags.optString("addr:full", "")

        if (fullAddress.isNotBlank() && fullAddress != "null") {
            return fullAddress
        }

        val parts = mutableListOf<String>()

        val street = tags.optString("addr:street", "")
        val houseNumber = tags.optString("addr:housenumber", "")
        val village = tags.optString("addr:suburb", "")
        val city = tags.optString("addr:city", "")

        if (street.isNotBlank() && street != "null") {
            parts.add(street)
        }

        if (houseNumber.isNotBlank() && houseNumber != "null") {
            parts.add("No. $houseNumber")
        }

        if (village.isNotBlank() && village != "null") {
            parts.add(village)
        }

        if (city.isNotBlank() && city != "null") {
            parts.add(city)
        }

        return parts.joinToString(", ").ifBlank {
            "Alamat belum tersedia di OpenStreetMap"
        }
    }

    private fun addBankCard(bank: BankSampah) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 14, 16, 14)
            setBackgroundColor(android.graphics.Color.WHITE)
        }

        val name = TextView(this).apply {
            text = "♻️ ${bank.name}"
            textSize = 17f
            setTextColor(0xFF24643A.toInt())
        }

        val address = TextView(this).apply {
            text = bank.address
            textSize = 14f
            setTextColor(0xFF555555.toInt())
            setPadding(0, 8, 0, 6)
        }

        val distanceKm = bank.distance / 1000f

        val distance = TextView(this).apply {
            text = String.format(
                Locale.getDefault(),
                "Jarak garis lurus: %.2f km",
                distanceKm
            )
            textSize = 13f
            setTextColor(0xFF666666.toInt())
        }

        val routeButton = Button(this).apply {
            text = "Lihat Rute di Google Maps"
            setOnClickListener {
                openGoogleMapsRoute(bank)
            }
        }

        card.addView(name)
        card.addView(address)
        card.addView(distance)
        card.addView(routeButton)

        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = 10
        }

        listLayout.addView(card, cardParams)

        val divider = android.view.View(this).apply {
            setBackgroundColor(0xFFE1E8E1.toInt())
        }

        listLayout.addView(
            divider,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            )
        )
    }

    private fun openGoogleMapsRoute(bank: BankSampah) {
        val destination = "${bank.latitude},${bank.longitude}"

        val uri = Uri.parse(
            "https://www.google.com/maps/dir/?api=1" +
                    "&destination=${Uri.encode(destination)}" +
                    "&travelmode=driving"
        )

        val intent = Intent(Intent.ACTION_VIEW, uri)

        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(
                this,
                "Tidak dapat membuka petunjuk arah.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroy() {
        locationListener?.let { listener ->
            try {
                locationManager.removeUpdates(listener)
            } catch (_: Exception) {
                // Tidak ada pembaruan lokasi yang perlu dihentikan.
            }
        }

        executor.shutdownNow()
        super.onDestroy()
    }
}
