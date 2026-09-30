package ru.alexey.flowapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ru.alexey.flowapp.core.database.entity.HabitCompletionEntity
import ru.alexey.flowapp.core.database.entity.HabitEntity

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE archived = 0 ORDER BY sort_order ASC, name ASC")
    fun observeActive(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY archived ASC, sort_order ASC, name ASC")
    fun observeAll(): Flow<List<HabitEntity>>

    /**
     * Habits scheduled for a day.
     *
     * @param dayBit `1 shl (ISO day - 1)`
     */
    @Query(
        """
        SELECT * FROM habits
        WHERE archived = 0
          AND (schedule_type = 'daily' OR (schedule_days_mask & :dayBit) != 0)
        ORDER BY sort_order ASC, name ASC
        """,
    )
    fun observeScheduledOn(dayBit: Int): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    fun observeById(id: String): Flow<HabitEntity?>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getById(id: String): HabitEntity?

    /** Full dump for JSON export. */
    @Query("SELECT * FROM habits")
    suspend fun getAll(): List<HabitEntity>

    /** Full dump for JSON export. */
    @Query("SELECT * FROM habit_completions")
    suspend fun getAllCompletions(): List<HabitCompletionEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(habit: HabitEntity)

    @Upsert
    suspend fun upsertAll(habits: List<HabitEntity>)

    @Update
    suspend fun update(habit: HabitEntity)

    @Query("UPDATE habits SET archived = :archived WHERE id = :id")
    suspend fun updateArchived(
        id: String,
        archived: Boolean,
    )

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM habits")
    suspend fun clear()

    // --- completions ---

    /** @param date epoch day */
    @Query("SELECT * FROM habit_completions WHERE date = :date")
    fun observeCompletionsForDate(date: Long): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE date BETWEEN :from AND :to ORDER BY completed_at DESC")
    fun observeCompletionsInRange(
        from: Long,
        to: Long,
    ): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE habit_id = :habitId ORDER BY date ASC")
    fun observeCompletionsForHabit(habitId: String): Flow<List<HabitCompletionEntity>>

    /**
     * All completions, used for streaks. The table is small, so one full query beats per-habit
     * flows.
     */
    @Query("SELECT * FROM habit_completions ORDER BY date ASC")
    fun observeAllCompletions(): Flow<List<HabitCompletionEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCompletion(completion: HabitCompletionEntity)

    @Upsert
    suspend fun upsertCompletions(completions: List<HabitCompletionEntity>)

    /** Removes the latest completion of the day (undo). */
    @Query(
        """
        DELETE FROM habit_completions
        WHERE id = (
            SELECT id FROM habit_completions
            WHERE habit_id = :habitId AND date = :date
            ORDER BY completed_at DESC
            LIMIT 1
        )
        """,
    )
    suspend fun deleteLastCompletion(
        habitId: String,
        date: Long,
    )

    @Query("DELETE FROM habit_completions")
    suspend fun clearCompletions()
}