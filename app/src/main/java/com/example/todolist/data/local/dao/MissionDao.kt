package com.example.todolist.data.local.dao

import androidx.room.*
import com.example.todolist.data.local.entity.MissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionDao {
    @Query("SELECT * FROM missions ORDER BY deadlineEpoch ASC")
    fun getAll(): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): MissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mission: MissionEntity): Long

    @Delete
    suspend fun delete(mission: MissionEntity)

    @Query("DELETE FROM missions WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE missions SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)
}
