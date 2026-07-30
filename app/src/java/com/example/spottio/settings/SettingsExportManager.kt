package com.example.spottio.settings

import android.content.Context
import android.widget.Toast
import com.example.spottio.utils.FileExportHelper
import com.example.spottio.settings.export.HtmlExportGenerator
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.json.JSONObject

class SettingsExportManager(
    private val mDb: FirebaseFirestore,
    private val context: Context,
    private val currentUsername: String
) {
    fun eseguiEstrazioneDati(formatoJson: Boolean, onComplete: () -> Unit) {
        mDb.collection("posts").whereEqualTo("username", currentUsername).get()
            .addOnCompleteListener { task ->
                try {
                    val exportRoot = JSONObject()
                    val postArray = JSONArray()

                    if (task.isSuccessful && task.result != null) {
                        for (doc in task.result!!) {
                            val postObj = JSONObject(doc.data)
                            postArray.put(postObj)
                        }
                    }

                    exportRoot.put("post_pubblicati", postArray)

                    var nomeFile = "Spottio_Dati_$currentUsername"
                    val mimeType: String
                    val contenutoFile: String

                    if (formatoJson) {
                        nomeFile += ".json"
                        mimeType = "application/json"
                        contenutoFile = exportRoot.toString(2)
                    } else {
                        nomeFile += ".html"
                        mimeType = "text/html"
                        contenutoFile = HtmlExportGenerator.generaPaginaHtmlArchivio(exportRoot)
                    }

                    FileExportHelper.salvaInCartellaDownloadPubblica(context, nomeFile, mimeType, contenutoFile)
                    Toast.makeText(context, "Dati esportati con successo!", Toast.LENGTH_SHORT).show()

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Errore formattazione dati.", Toast.LENGTH_SHORT).show()
                } finally {
                    onComplete()
                }
            }
    }
}