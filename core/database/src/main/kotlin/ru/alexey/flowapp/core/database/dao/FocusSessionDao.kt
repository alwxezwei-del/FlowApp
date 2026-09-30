package ru.alexey.flowapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ru.alexey.flowapp.core.database.entity.FocusSessionEntity

@Dao
interface FocusSessionDao {
    /**
     * @param from period start, epoch millis
     * @param to period end, epoch millis, inclusive
     */
    @Query("SELECT * FROM focus_sessions WHERE ended_at BETWEEN :from AND :to ORDER BY ended_at DESC")
    fun observeInRange(
        from: Long,
        to: Long,
    ): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions ORDER BY ended_at DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<FocusSessionEntity>>

    /** Total actual work time for the period, breaks excluded. Returns 0 for an empty period. */
    @Query(
        """
        SELECT COALESCE(SUM(actual_seconds), 0) FROM focus_sessions
        WHERE kind = 'focus' AND ended_at BETWEEN :from AND :to
        """,
    )
    fun observeTotalSeconds(
        from: Long,
        to: Long,
    ): Flow<Long>

    /** Full dump for JSON export. */
    @Query("SELECT * FROM focus_sessions")
    suspend fun getAll(): List<FocusSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: FocusSessionEntity)

    @Upsert
    suspend fun upsertAll(sessions: List<FocusSessionEntity>)

    @Query("DELETE FROM focus_sessions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM focus_sessions")
    suspend fun clear()
}