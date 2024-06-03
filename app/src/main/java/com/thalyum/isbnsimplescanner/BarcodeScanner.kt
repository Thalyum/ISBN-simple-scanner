package com.thalyum.isbnsimplescanner

import android.content.Context
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

class BarcodeScanner {
    fun setup(context: Context): GmsBarcodeScanner {
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
        return GmsBarcodeScanning.getClient(context, options)
    }
}