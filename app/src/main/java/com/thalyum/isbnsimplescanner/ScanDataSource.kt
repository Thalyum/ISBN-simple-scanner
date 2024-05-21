package com.thalyum.isbnsimplescanner

import androidx.lifecycle.MutableLiveData

/* Handles operations on scanLiveData and holds details about it. */
class ScanDataSource {
    // Use an immutable list
    // Adding new elements will create a new list, and make sure
    // the ListAdapter is properly updated
    private val initialScanResultList = listOf<ScanResult>()
    val scanResultLiveData = MutableLiveData(initialScanResultList)

    /* Adds scanResult to liveData and posts value. */
    fun addScanResult(scan: ScanResult) {
        val currentList = scanResultLiveData.value
        if (currentList == null) {
            scanResultLiveData.postValue(arrayListOf(scan))
        } else {
            val updatedList = currentList.toMutableList()
            updatedList.add(0, scan)
            scanResultLiveData.postValue(updatedList)
        }
    }
}