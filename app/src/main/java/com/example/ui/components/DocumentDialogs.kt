package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DocumentType
import com.example.ui.theme.NovaBluePrimary

@Composable
fun CreateDocumentDialog(
    initialType: DocumentType = DocumentType.WRITER,
    onDismiss: () -> Unit,
    onConfirm: (title: String, type: DocumentType) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(initialType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat Dokumen Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Dokumen") },
                    placeholder = { Text("Contoh: Bab 2 - Rahasia Brankas") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Jenis Dokumen:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        DocumentType.WRITER to "Writer",
                        DocumentType.SHEETS to "Sheets",
                        DocumentType.SLIDES to "Slides",
                        DocumentType.PDF to "PDF"
                    ).forEach { (type, label) ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = title.ifBlank {
                        "Dokumen Baru ${System.currentTimeMillis() % 1000}"
                    }
                    onConfirm(finalTitle, selectedType)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NovaBluePrimary)
            ) {
                Text("Buat")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun RenameDocumentDialog(
    currentTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (newTitle: String) -> Unit
) {
    var newTitle by remember { mutableStateOf(currentTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ganti Nama Dokumen", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = newTitle,
                onValueChange = { newTitle = it },
                label = { Text("Nama Baru") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newTitle.isNotBlank()) {
                        onConfirm(newTitle)
                    }
                },
                enabled = newTitle.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NovaBluePrimary)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Hapus",
    isDestructive: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = if (isDestructive) {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                } else {
                    ButtonDefaults.buttonColors(containerColor = NovaBluePrimary)
                }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
