package com.thalyum.isbnsimplescanner

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.common.Barcode
import com.thalyum.isbnsimplescanner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var scanadapter = ScanResultAdapter()
    private var scans = ScanDataSource()
    private var scanner = BarcodeScanner().setup(this)
    private var http = Http(this, "http://192.168.1.127:8080")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Init buttons callback
        // Scan only once
        binding.ScanOnceBtn.setOnClickListener {
            scanOnce()
        }
        // Scan until user stops
        binding.ScanCollectionBtn.setOnClickListener {
            // request collection id
            // the response listener will start the scanning loop
            val id = http.requestCollectionByName("test")
            if (id >= 0) {
                scanCollection(id)
            } else {
                Log.v(
                    "Scan Collection",
                    "Error while getting the requested collection id. Aborting."
                )
                Snackbar.make(binding.root, R.string.error_collection_id, Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

        // Setup adapter
        binding.ScanList.adapter = scanadapter

        // Setup observer on live data
        scans.scanResultLiveData.observe(this) {
            it?.let {
                scanadapter.submitList(it)
                http.requestRegisterNewISBN(scans)
            }
        }
    }

    private fun scanOnce() {
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
                        .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                            // once the notification is dismissed, scan again
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                super.onDismissed(transientBottomBar, event)
                                scanOnce()
                            }
                        })
                        .show()
                }
            }
    }

    private fun scanCollection(collectionId: Int) {
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
                    // rescan
                    scanCollection(collectionId)
                } else {
                    // otherwise, notice the user that it is not a valid ISBN
                    Snackbar.make(binding.root, R.string.not_isbn, Snackbar.LENGTH_SHORT)
                        .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                            // once the notification is dismissed, scan again
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                super.onDismissed(transientBottomBar, event)
                                scanCollection(collectionId)
                            }
                        })
                        .show()
                }
            }
    }
}