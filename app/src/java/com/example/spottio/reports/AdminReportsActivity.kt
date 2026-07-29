package com.example.spottio.reports

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class AdminReportsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var reportsListener: ListenerRegistration? = null

    // Lista reattiva di Compose che gestisce l'UI
    private val reportList = mutableStateListOf<Report>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadReports()

        setContent {
            MaterialTheme {
                AdminReportsScreen(
                    reports = reportList,
                    onIgnoreReport = { report -> ignoreReport(report) },
                    onDeletePost = { report -> deletePostAndReport(report) }
                )
            }
        }
    }

    private fun loadReports() {
        reportsListener = db.collection("reports")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("AdminReports", "Errore nel caricamento: ", error)
                    return@addSnapshotListener
                }

                if (value != null) {
                    reportList.clear()
                    for (doc in value.documents) {
                        try {
                            val r = doc.toObject(Report::class.java)
                            if (r != null) {
                                r.reportId = doc.id
                                reportList.add(r)
                            }
                        } catch (e: Exception) {
                            Log.e("AdminReports", "Documento ignorato: ${doc.id}", e)
                        }
                    }
                }
            }
    }

    private fun ignoreReport(report: Report) {
        report.reportId?.let { id ->
            db.collection("reports").document(id).delete()
                .addOnSuccessListener {
                    reportList.remove(report)
                    Toast.makeText(this, "Segnalazione ignorata", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deletePostAndReport(report: Report) {
        val pId = report.postId
        if (pId.isNullOrEmpty()) return

        db.collection("posts").document(pId).delete()
            .addOnSuccessListener {
                report.reportId?.let { id -> db.collection("reports").document(id).delete() }
                reportList.remove(report)
                Toast.makeText(this, "Post e segnalazione eliminati!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Errore eliminazione post: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        reportsListener?.remove()
    }
}