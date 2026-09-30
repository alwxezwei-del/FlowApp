package ru.alexey.flowapp.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import ru.alexey.flowapp.core.database.FlowDatabase
import ru.alexey.flowapp.core.database.dao.CategoryDao
import ru.alexey.flowapp.core.database.dao.FocusSessionDao
import ru.alexey.flowapp.core.database.dao.HabitDao
import ru.alexey.flowapp.core.database.dao.TaskDao

@Module
class DatabaseModule {
    @Single
    fun database(context: Context): FlowDatabase =
        Room
            .databaseBuilder(context, FlowDatabase::class.java, FlowDatabase.NAME)
            // Foreign keys are off by default in SQLite, and the schema relies on them
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .build()

    @Single
    fun categoryDao(database: FlowDatabase): CategoryDao = database.categoryDao()

    @Single
    fun taskDao(database: FlowDatabase): TaskDao = database.taskDao()

    @Single
    fun habitDao(database: FlowDatabase): HabitDao = database.habitDao()

    @Single
    fun focusSessionDao(database: FlowDatabase): FocusSessionDao = database.focusSessionDao()
}