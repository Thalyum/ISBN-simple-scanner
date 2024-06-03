package com.thalyum.isbnsimplescanner

// Single element of the list
data class ScanResult(
    val isbn: String,
    val collection_id: Int,
    var sent: Boolean
)

