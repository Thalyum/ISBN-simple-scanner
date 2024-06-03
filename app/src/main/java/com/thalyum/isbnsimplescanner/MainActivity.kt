package com.thalyum.isbnsimplescanner

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.common.Barcode
import com.thalyum.isbnsimplescanner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var scanadapter = ScanResultAdapter()
    private var scans = ScanDataSource()
    private var scanner = BarcodeScanner().setup(this)

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
                request()
            }
        }
    }

    fun request() {
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
        val url = "https://www.google.com"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                // Display the first 500 characters of the response string.
                Log.v("PER", "Response is: ${response.substring(0, 500)}")
            },
            { Log.v("PER", "That didn't work!") })

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    private fun requestScan() {
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
                                requestScan()
                            }
                        })
                        .show()
                }
            }
    }
}