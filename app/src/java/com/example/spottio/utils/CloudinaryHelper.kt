package com.example.spottio.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.InputStream
import java.util.concurrent.TimeUnit

object CloudinaryHelper {
    // Stessi dati che usi nella versione Web
    private const val CLOUD_NAME = "c32kn8tz"
    private const val UPLOAD_PRESET = "spottio_preset"

    // Aumentiamo i timeout nel caso di video pesanti
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun uploadMedia(context: Context, uri: Uri, isVideo: Boolean): String? = withContext(Dispatchers.IO) {
        try {
            val mediaTypeStr = if (isVideo) "video" else "image"
            val url = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/$mediaTypeStr/upload"

            // Estrae i byte dal file locale (Uri)
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes == null) return@withContext null

            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())

            // Crea la richiesta multipart form-data
            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .addFormDataPart("file", "upload_file", requestBody)
                .build()

            val request = Request.Builder()
                .url(url)
                .post(multipartBody)
                .build()

            // Esegue la richiesta verso Cloudinary
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val jsonObject = JSONObject(responseBody)
                    val secureUrl = jsonObject.getString("secure_url")
                    // Sostituisce '/upload/' con i parametri di ottimizzazione, come in JS
                    return@withContext secureUrl.replace("/upload/", "/upload/f_auto,q_auto/")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
}