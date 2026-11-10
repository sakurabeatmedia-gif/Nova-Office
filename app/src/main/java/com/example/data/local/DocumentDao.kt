package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents WHERE isTrash = 0 ORDER BY lastModified DESC")
    fun getAllActiveDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isTrash = 1 ORDER BY lastModified DESC")
    fun getTrashDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isFavorite = 1 AND isTrash = 0 ORDER BY lastModified DESC")
    fun getFavoriteDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: String): DocumentEntity?

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    fun getDocumentFlow(id: String): Flow<DocumentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(document: DocumentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(documents: List<DocumentEntity>)

    @Update
    suspend fun update(document: DocumentEntity)

    @Delete
    suspend fun delete(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE isTrash = 1")
    suspend fun emptyTrash()

    @Query("SELECT COUNT(*) FROM documents WHERE isTrash = 0")
    suspend fun getActiveCount(): Int
}
