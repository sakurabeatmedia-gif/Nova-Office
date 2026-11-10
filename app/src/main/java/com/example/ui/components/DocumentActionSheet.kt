package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OfficeDocument

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentActionSheet(
    document: OfficeDocument,
    onDismiss: () -> Unit,
    onOpen: (OfficeDocument) -> Unit,
    onRename: (OfficeDocument) -> Unit,
    onDuplicate: (OfficeDocument) -> Unit,
    onToggleFavorite: (OfficeDocument) -> Unit,
    onDelete: (OfficeDocument) -> Unit
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            // Document summary header
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "${document.type.displayName} • ${document.formattedSize} • Diubah ${document.formattedDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Action Items
            ActionItem(
                icon = Icons.Default.OpenInNew,
                title = "Buka Dokumen",
                onClick = {
                    onDismiss()
                    onOpen(document)
                },
                testTag = "action_open"
            )

            ActionItem(
                icon = Icons.Default.Edit,
                title = "Ganti Nama",
                onClick = {
                    onDismiss()
                    onRename(document)
                },
                testTag = "action_rename"
            )

            ActionItem(
                icon = Icons.Default.ContentCopy,
                title = "Duplikasi / Buat Salinan",
                onClick = {
                    onDismiss()
                    onDuplicate(document)
                },
                testTag = "action_duplicate"
            )

            ActionItem(
                icon = if (document.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                title = if (document.isFavorite) "Hapus dari Favorit" else "Tambahkan ke Favorit",
                tint = if (document.isFavorite) Color(0xFFF5A623) else null,
                onClick = {
                    onDismiss()
                    onToggleFavorite(document)
                },
                testTag = "action_favorite"
            )

            ActionItem(
                icon = Icons.Default.Share,
                title = "Bagikan Dokumen",
                onClick = {
                    onDismiss()
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TITLE, document.title)
                        putExtra(Intent.EXTRA_TEXT, "${document.title}\n\n${document.content}")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Bagikan dokumen via"))
                },
                testTag = "action_share"
            )

            ActionItem(
                icon = Icons.Default.Delete,
                title = "Pindahkan ke Sampah",
                tint = MaterialTheme.colorScheme.error,
                onClick = {
                    onDismiss()
                    onDelete(document)
                },
                testTag = "action_delete"
            )
        }
    }
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    tint: Color? = null,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint ?: MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = tint ?: MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
