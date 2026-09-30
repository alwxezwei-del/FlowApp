package ru.alexey.flowapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ru.alexey.flowapp.core.database.entity.TaskEntity

@Dao
interface TaskDao {
    @Query(
        """
        SELECT * FROM tasks
        WHERE status != 'archived'
        ORDER BY due_date IS NULL, due_date ASC, created_at DESC
        """,
    )
    fun observeAll(): Flow<List<TaskEntity>>

    /** @param date epoch day */
    @Query(
        """
        SELECT * FROM tasks
        WHERE due_date = :date AND status != 'archived'
        ORDER BY status ASC, created_at ASC
        """,
    )
    fun observeForDate(date: Long): Flow<List<TaskEntity>>

    /**
     * @param from first epoch day
     * @param to last epoch day, inclusive
     */
    @Query(
        """
        SELECT * FROM tasks
        WHERE due_date BETWEEN :from AND :to
        ORDER BY due_date ASC, created_at ASC
        """,
    )
    fun observeInRange(
        from: Long,
        to: Long,
    ): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun observeById(id: String): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: String): TaskEntity?

    /** Full dump for JSON export. */
    @Query("SELECT * FROM tasks")
    suspend fun getAll(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(task: TaskEntity)

    @Upsert
    suspend fun upsertAll(tasks: List<TaskEntity>)

    @Update
    suspend fun update(task: TaskEntity)

    @Query("UPDATE tasks SET status = :status, completed_at = :completedAt WHERE id = :id")
    suspend fun updateStatus(
        id: String,
        status: String,
        completedAt: Long?,
    )

    /** @param dueDate epoch day or `null` */
    @Query("UPDATE tasks SET due_date = :dueDate WHERE id = :id")
    suspend fun updateDueDate(
        id: String,
        dueDate: Long?,
    )

    /** Increment in SQL to avoid losing concurrent updates from the timer service. */
    @Query("UPDATE tasks SET focused_seconds = focused_seconds + :seconds WHERE id = :id")
    suspend fun addFocusedSeconds(
        id: String,
        seconds: Long,
    )

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM tasks")
    suspend fun clear()
}