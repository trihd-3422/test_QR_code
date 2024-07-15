package com.example.testqrcodecamera

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.testqrcodecamera.databinding.ActivityMainBinding
import com.google.mlkit.vision.barcode.common.Barcode

class MainActivity : AppCompatActivity() {

    private val cameraPermission = android.Manifest.permission.CAMERA
    private lateinit var binding: ActivityMainBinding

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            startScanner()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonOpenScanner.setOnClickListener {
            requestCameraAndStartScanner()
        }
    }

    private fun requestCameraAndStartScanner() {
        if (isPermissionGranted(cameraPermission)) {
            startScanner()
        } else {
            requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        when {
            shouldShowRequestPermissionRationale(cameraPermission) -> {
                cameraPermissionRequest(
                    positive = { openPermissionSetting() }
                )
            }
            else -> {
                requestPermissionLauncher.launch(cameraPermission)
            }
        }
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun cameraPermissionRequest(positive: () -> Unit) {
        AlertDialog.Builder(this).apply {
            setTitle("Camera Permission Needed")
            setMessage("This app needs the camera to scan QR codes. Please grant the permission.")
            setPositiveButton("Settings") { _, _ -> positive() }
            setNegativeButton("Cancel", null)
            create()
            show()
        }
    }

    private fun openPermissionSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun startScanner() {
        ScannerActivity.startScanner(this) { barcodes ->
            barcodes.forEach { barcode ->
                when (barcode.valueType) {
                    Barcode.TYPE_URL -> {
                        binding.textViewQrType.text = "URL"
                        binding.textViewQrContent.text = barcode.url.toString()
                    }
                    Barcode.TYPE_CONTACT_INFO -> {
                        binding.textViewQrType.text = "Contact"
                        binding.textViewQrContent.text = barcode.contactInfo.toString()
                    }
                    else -> {
                        binding.textViewQrType.text = "Other"
                        binding.textViewQrContent.text = barcode.rawValue.toString()
                    }
                }
            }
        }
    }
}