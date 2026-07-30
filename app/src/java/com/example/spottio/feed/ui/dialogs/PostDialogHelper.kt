package com.example.spottio.feed.ui.dialogs

import android.R
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
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
import com.example.spottio.feed.data.Comment
import com.google.android.material.bottomsheet.BottomSheetDialog

// L'import fondamentale per collegare i due file in cartelle diverse!
import com.example.spottio.feed.ui.components.CommentItem

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
        // Creazione programmatica del layout senza usare XML
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(64, 32, 64, 16)
        }

        val spinnerReason = Spinner(context).apply {
            val reasons = listOf("Seleziona un motivo", "Spam", "Contenuto inappropriato", "Violenza o incitamento all'odio", "Informazioni false", "Altro")
            adapter = ArrayAdapter(context, R.layout.simple_spinner_dropdown_item, reasons)
        }

        val etDescription = EditText(context).apply {
            hint = "Aggiungi dettagli (opzionale)"
            minLines = 3
            gravity = Gravity.TOP or Gravity.START
        }

        layout.addView(TextView(context).apply {
            text = "Motivo della segnalazione:"
            setPadding(0, 0, 0, 16)
            textSize = 16f
            setTextColor(Color.BLACK)
            setTypeface(null, Typeface.BOLD)
        })
        layout.addView(spinnerReason)

        layout.addView(TextView(context).apply {
            text = "Descrizione (opzionale):"
            setPadding(0, 32, 0, 16)
            textSize = 16f
            setTextColor(Color.BLACK)
            setTypeface(null, Typeface.BOLD)
        })
        layout.addView(etDescription)

        AlertDialog.Builder(context)
            .setTitle("Segnala Post")
            .setView(layout)
            .setNegativeButton("Annulla", null)
            .setPositiveButton("Invia") { _, _ ->
                if (spinnerReason.selectedItemPosition == 0) {
                    Toast.makeText(context, "Seleziona un motivo per continuare", Toast.LENGTH_SHORT).show()
                } else {
                    val reason = spinnerReason.selectedItem.toString()
                    val description = etDescription.text.toString().trim()
                    onSubmit(reason, description)
                }
            }
            .show()
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