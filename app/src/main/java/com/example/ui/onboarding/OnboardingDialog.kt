package com.example.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.NovaBluePrimary
import com.example.ui.theme.NovaCyanAccent
import com.example.ui.theme.NovaNaraPurple
import com.example.ui.theme.NovaNavyDark
import com.example.ui.theme.NovaSheetsGreen
import com.example.ui.theme.NovaWriterBlue

@Composable
fun OnboardingDialog(
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (step == 0) {
                    // Welcome & Office Suite
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(
                                Brush.linearGradient(listOf(NovaNavyDark, NovaBluePrimary)),
                                RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("N", fontSize = 36.sp, fontWeight = FontWeight.Black, color = NovaCyanAccent)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Selamat Datang di\nNOVA OFFICE",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your Complete Office & Creative Workspace",
                        fontSize = 12.sp,
                        color = NovaBluePrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Aplikasi perkantoran lengkap dengan modul Nova Writer (Word/DOCX), Sheets (Excel/Formula), Slides (Presentasi), dan PDF Viewer profesional.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                } else {
                    // NARA AI Assistant highlight
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(
                                Brush.linearGradient(listOf(NovaNaraPurple, Color(0xFF4834DF))),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(36.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Temui NARA AI\nEditor Novel Profesional",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Eksklusif di NOVA OFFICE",
                        fontSize = 12.sp,
                        color = NovaNaraPurple,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Bukan chatbot biasa. NARA memahami ritme prosa, dialog fiksi, konsistensi tokoh, dan struktur novel berstandar penerbitan profesional.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Lewati")
                    }

                    Button(
                        onClick = {
                            if (step == 0) {
                                step = 1
                            } else {
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (step == 0) NovaBluePrimary else NovaNaraPurple
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (step == 0) "Lanjut" else "Mulai Menulis", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
