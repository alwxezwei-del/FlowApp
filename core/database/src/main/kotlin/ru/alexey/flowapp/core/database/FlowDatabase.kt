package ru.alexey.flowapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.alexey.flowapp.core.database.dao.CategoryDao
import ru.alexey.flowapp.core.database.dao.FocusSessionDao
import ru.alexey.flowapp.core.database.dao.HabitDao
import ru.alexey.flowapp.core.database.dao.TaskDao
import ru.alexey.flowapp.core.database.entity.CategoryEntity
import ru.alexey.flowapp.core.database.entity.FocusSessionEntity
import ru.alexey.flowapp.core.database.entity.HabitCompletionEntity
import ru.alexey.flowapp.core.database.entity.HabitEntity
import ru.alexey.flowapp.core.database.entity.TaskEntity

/** App database */
@Database(
    entities = [
        CategoryEntity::class,
        TaskEntity::class,
        HabitEntity::class,
        HabitCompletionEntity::class,
        FocusSessionEntity::class,
    ],
    version = FlowDatabase.VERSION,
    exportSchema = true,
)
abstract class FlowDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao

    abstract fun taskDao(): TaskDao

    abstract fun habitDao(): HabitDao

    abstract fun focusSessionDao(): FocusSessionDao

    companion object {
        const val VERSION = 1
        const val NAME = "flowapp.db"
    }
}