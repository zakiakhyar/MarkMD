package com.markmd.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Query("SELECT * FROM documents ORDER BY isPinned DESC, lastOpened DESC")
    fun getRecentDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE uri = :uri LIMIT 1")
    suspend fun getDocument(uri: String): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity)

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Query("UPDATE documents SET scrollPosition = :position WHERE uri = :uri")
    suspend fun updateScrollPosition(uri: String, position: Int)

    @Query("DELETE FROM documents WHERE uri = :uri")
    suspend fun deleteDocument(uri: String)

    @Query("DELETE FROM documents")
    suspend fun clearAll()

    @Query("UPDATE documents SET isPinned = CASE WHEN isPinned = 1 THEN 0 ELSE 1 END WHERE uri = :uri")
    suspend fun togglePin(uri: String)
}
