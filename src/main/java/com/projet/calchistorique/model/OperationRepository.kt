package com.projet.calchistorique.model

import androidx.annotation.WorkerThread
import com.projet.calchistorique.dao.OperationDao
import kotlinx.coroutines.flow.Flow

class OperationRepository(private val operationDao: OperationDao) {
    val allOperations: Flow<List<Operation>> = operationDao.getAllOperations()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertOperation(operation: Operation) {
        operationDao.insertOperation(operation)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteOperation(operation: Operation) {
        operationDao.deleteOperation(operation)
    }

    fun deleteAllOperations() {
        operationDao.deleteAllOperations()
    }
}