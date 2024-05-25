package com.projet.calchistorique.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.projet.calchistorique.model.Operation
import kotlinx.coroutines.flow.Flow

@Dao
interface OperationDao {
    @Insert
    fun insertOperation(operation: Operation)

    @Delete
    fun deleteOperation(operation: Operation)

    @Query("SELECT * FROM operations ORDER BY timestamp DESC")
    fun getAllOperations(): Flow<List<Operation>>

    @Query("DELETE FROM operations")
    fun deleteAllOperations()
}