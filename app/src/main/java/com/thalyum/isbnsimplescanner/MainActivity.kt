package com.thalyum.isbnsimplescanner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.thalyum.isbnsimplescanner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var scanadapter = ScanResultAdapter()
    private var scans = ScanDataSource()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Init button callback
        binding.ScanOnceBtn.setOnClickListener {
            requestScan()
        }

        // Setup adapter
        binding.ScanList.adapter = scanadapter

        // Setup observer on data
        scans.scanResultLiveData.observe(this) {
        //scanListViewModel.scanResultsLiveData.observe(this) {
            it?.let {
                scanadapter.submitList(it)
            }
        }
    }

    private fun requestScan() {
        // Setup barcode scanner options
        val options = GmsBarcodeScannerOptions
            .Builder()
            // Only read EAN_13 format
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13
            )
            // Enable automatic zoom
            .enableAutoZoom()
            .build()

        // Get scanner client with custom options
        val scanner = GmsBarcodeScanning.getClient(this, options)

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                // If the result is a valid ISBN, display the value
                if (barcode.valueType == Barcode.TYPE_ISBN) {
                    // create new scanResult
                    val new = ScanResult(
                        isbn = barcode.rawValue.toString(),
                        collection_id = 0,
                        sent = false
                    )
                    // add to list
                    scans.addScanResult(new)
                } else {
                    // otherwise, notice the user that it is not a valid ISBN
                    Snackbar.make(binding.root, R.string.not_isbn, Snackbar.LENGTH_SHORT)
                        .addCallback(object: BaseTransientBottomBar.BaseCallback<Snackbar>() {
                            // once the notification is dismissed, scan again
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                super.onDismissed(transientBottomBar, event)
                                requestScan()
                            }
                        })
                        .show()
                }
            }
    }
}