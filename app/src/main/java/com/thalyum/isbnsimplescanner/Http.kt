package com.thalyum.isbnsimplescanner

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class Http(context: Context, private val baseUrl: String) {
    private val queue = Volley.newRequestQueue(context)
    private val isbnPostQueue = mutableListOf<String>()

    fun requestCollectionByName(name: String): Int {
        val url = "$baseUrl/collection?name=$name&create=true"

        // use blocking request
        val future = RequestFuture.newFuture<String>()

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(Request.Method.GET, url, future, future)

        // Add the request to the RequestQueue.
        queue.add(stringRequest)

        var collectionId = 0
        try {
            collectionId = future.get(5, TimeUnit.SECONDS).toInt()
        } catch (e: TimeoutException) {
            Log.v("Http/Collection (GET)", "Timeout error: $e")
            collectionId = -1
        } catch (e: InterruptedException) {
            Log.v("Http/Collection (GET)", "Interruption error: $e")
            collectionId = -1
        } catch (e: ExecutionException) {
            Log.v("Http/Collection (GET)", "Execution error: $e")
            collectionId = -1
        } catch (e: NumberFormatException) {
            Log.v("Http/Collection (GET)", "Return (collectionId) error: $e")
            collectionId = -1
        }

        return collectionId
    }

    fun requestRegisterNewISBN(data: ScanDataSource) {
        val scanList = data.scanResultLiveData.value ?: return
        for (i in scanList.indices) {
            val scan = scanList[i]
            if (!scan.sent) {
                val isbn = scan.isbn
                val collection = scan.collection_id

                // ISBN POST request is already listed in the queue
                if (isbnPostQueue.contains(isbn)) {
                    continue
                }

                // FIXME: pass data as Json in body, not in query
                var url = "$baseUrl/book?isbn=$isbn"
                if (collection >= 0) {
                    url += "&collection=$collection"
                }

                // Request a string response from the provided URL.
                val stringRequest = StringRequest(
                    Request.Method.POST, url,
                    { response ->
                        Log.v("Http/Book (POST)", "Response is: $response")
                        // update 'sent' status of the ScanResult that has been successfully sent
                        data.updateScanSentStatus(i)
                        // clean-up the queue
                        isbnPostQueue.remove(isbn)
                    },
                    { error ->
                        Log.v("Http/Book (POST)", "Error is: $error")
                        // remove from queue, to try to send it again later
                        isbnPostQueue.remove(isbn)
                    })

                // Add the request to the RequestQueue.
                queue.add(stringRequest)
                isbnPostQueue.add(isbn)
            }
        }
    }
}