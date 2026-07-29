package com.example.spottio.posts.ui.dialogs

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button as ComposeButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spottio.R
import com.example.spottio.posts.data.Comment
import com.google.android.material.bottomsheet.BottomSheetDialog

// L'import fondamentale per collegare i due file in cartelle diverse!
import com.example.spottio.posts.ui.components.CommentItem

object PostDialogHelper {

    @JvmStatic
    fun showDeletePostDialog(context: Context, onConfirm: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("Elimina Post")
            .setMessage("Vuoi eliminare definitivamente questo post?")
            .setPositiveButton("Sì") { _, _ -> onConfirm() }
            .setNegativeButton("No", null)
            .show()
    }

    @JvmStatic
    fun showReportDialog(context: Context, onSubmit: (String, String) -> Unit) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_report, null)
        builder.setView(view)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

        val spinnerReason = view.findViewById<Spinner>(R.id.spinnerReason)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val btnCancel = view.findViewById<android.widget.Button>(R.id.btnCancel)
        val btnSubmit = view.findViewById<android.widget.Button>(R.id.btnSubmit)

        val reasons = arrayOf("Seleziona un motivo", "Spam", "Contenuto inappropriato", "Violenza o incitamento all'odio", "Informazioni false", "Altro")
        val spinnerAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, reasons)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerReason.adapter = spinnerAdapter

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSubmit.setOnClickListener {
            if (spinnerReason.selectedItemPosition == 0) {
                Toast.makeText(context, "Seleziona un motivo per continuare", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            onSubmit(spinnerReason.selectedItem.toString(), etDescription.text.toString().trim())
            dialog.dismiss()
        }

        dialog.show()
    }

    @JvmStatic
    fun showEditPostDialog(context: Context, currentText: String?, onSave: (String) -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Modifica Post")

        val input = EditText(context)
        input.setText(currentText)

        val layout = LinearLayout(context).apply {
            setPadding(50, 20, 50, 0)
            addView(input)
        }

        input.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        builder.setView(layout)

        builder.setPositiveButton("Salva") { _, _ ->
            val newText = input.text.toString().trim()
            if (newText.isNotEmpty() && newText != currentText) {
                onSave(newText)
            }
        }

        builder.setNegativeButton("Annulla") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    @JvmStatic
    fun showCommentsSheet(context: Context, comments: List<Comment>, onCommentAdded: (String) -> Unit) {
        val sheet = BottomSheetDialog(context)

        val composeView = ComposeView(context).apply {
            setContent {
                MaterialTheme {
                    var inputText by remember { mutableStateOf("") }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.8f)
                            .background(ComposeColor.White)
                    ) {
                        Text(
                            text = "Commenti",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(
                                start = 32.dp, top = 32.dp, end = 32.dp, bottom = 16.dp
                            )
                        )

                        // Lista scorrevole dei commenti
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            items(comments) { comment ->
                                CommentItem(comment = comment)
                            }
                        }

                        // Area di Input in basso
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { newValue -> inputText = newValue },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Aggiungi un commento...") },
                                shape = RoundedCornerShape(24.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            ComposeButton(
                                onClick = {
                                    val txt = inputText.trim()
                                    if (txt.isNotEmpty()) {
                                        onCommentAdded(txt)
                                        inputText = ""
                                    }
                                }
                            ) {
                                Text("Invia")
                            }
                        }
                    }
                }
            }
        }

        sheet.setContentView(composeView)
        sheet.show()
    }
}