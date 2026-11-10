package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DocumentType
import com.example.data.model.OfficeDocument

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val typeName: String,
    val content: String,
    val lastModified: Long,
    val sizeBytes: Long,
    val isFavorite: Boolean,
    val isTrash: Boolean,
    val folderName: String,
    val wordCount: Int,
    val characterCount: Int,
    val version: Int
) {
    fun toModel(): OfficeDocument {
        val docType = try {
            DocumentType.valueOf(typeName)
        } catch (e: Exception) {
            DocumentType.WRITER
        }
        return OfficeDocument(
            id = id,
            title = title,
            type = docType,
            content = content,
            lastModified = lastModified,
            sizeBytes = sizeBytes,
            isFavorite = isFavorite,
            isTrash = isTrash,
            folderName = folderName,
            wordCount = wordCount,
            characterCount = characterCount,
            version = version
        )
    }

    companion object {
        fun fromModel(model: OfficeDocument): DocumentEntity {
            return DocumentEntity(
                id = model.id,
                title = model.title,
                typeName = model.type.name,
                content = model.content,
                lastModified = model.lastModified,
                sizeBytes = model.sizeBytes,
                isFavorite = model.isFavorite,
                isTrash = model.isTrash,
                folderName = model.folderName,
                wordCount = model.wordCount,
                characterCount = model.characterCount,
                version = model.version
            )
        }
    }
}
