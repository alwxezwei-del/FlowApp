package ru.alexey.flowapp.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.alexey.flowapp.core.database.dao.CategoryDao
import ru.alexey.flowapp.core.database.dao.FocusSessionDao
import ru.alexey.flowapp.core.database.dao.HabitDao
import ru.alexey.flowapp.core.database.dao.TaskDao
import ru.alexey.flowapp.core.database.entity.CategoryEntity
import ru.alexey.flowapp.core.database.entity.FocusSessionEntity
import ru.alexey.flowapp.core.database.entity.HabitCompletionEntity
import ru.alexey.flowapp.core.database.entity.HabitEntity
import ru.alexey.flowapp.core.database.entity.TaskEntity

/** Schema and DAO tests on in-memory SQLite: foreign keys, date range and bitmask queries. */
@RunWith(AndroidJUnit4::class)
class FlowDatabaseTest {
    private lateinit var database: FlowDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var taskDao: TaskDao
    private lateinit var habitDao: HabitDao
    private lateinit var focusSessionDao: FocusSessionDao

    @Before
    fun setUp() {
        database = Room
            .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), FlowDatabase::class.java)
            .build()
        categoryDao = database.categoryDao()
        taskDao = database.taskDao()
        habitDao = database.habitDao()
        focusSessionDao = database.focusSessionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun taskIsStoredAndReadBack() =
        runTest {
            categoryDao.insert(category())
            taskDao.insert(task(id = "task", categoryId = CATEGORY_ID))

            val stored = taskDao.getById("task")

            assertEquals("Finish Compose navigation", stored?.title)
            assertEquals(CATEGORY_ID, stored?.categoryId)
        }

    /** Deleting a category keeps its tasks (`ON DELETE SET NULL`). */
    @Test
    fun deletingCategoryKeepsTasksAndClearsTheirCategory() =
        runTest {
            categoryDao.insert(category())
            taskDao.insert(task(id = "task", categoryId = CATEGORY_ID))

            categoryDao.deleteById(CATEGORY_ID)

            val stored = taskDao.getById("task")
            assertEquals("Finish Compose navigation", stored?.title)
            assertNull(stored?.categoryId)
        }

    /** Deleting a habit deletes its history (`ON DELETE CASCADE`). */
    @Test
    fun deletingHabitRemovesItsCompletions() =
        runTest {
            habitDao.insert(habit())
            habitDao.insertCompletion(completion(id = "c1", date = DAY))

            habitDao.deleteById(HABIT_ID)

            assertTrue(habitDao.getAllCompletions().isEmpty())
        }

    @Test
    fun tasksAreFilteredByDueDateRange() =
        runTest {
            taskDao.insert(task(id = "inside", dueDate = DAY))
            taskDao.insert(task(id = "before", dueDate = DAY - 5))
            taskDao.insert(task(id = "after", dueDate = DAY + 5))

            val inRange = taskDao.observeInRange(DAY - 1, DAY + 1).first()

            assertEquals(listOf("inside"), inRange.map { it.id })
        }

    @Test
    fun archivedTasksAreHiddenFromTheDayList() =
        runTest {
            taskDao.insert(task(id = "active", dueDate = DAY))
            taskDao.insert(task(id = "archived", dueDate = DAY, status = "archived"))

            val forDate = taskDao.observeForDate(DAY).first()

            assertEquals(listOf("active"), forDate.map { it.id })
        }

    @Test
    fun completingTaskStoresTheCompletionMoment() =
        runTest {
            taskDao.insert(task(id = "task"))

            taskDao.updateStatus(id = "task", status = "completed", completedAt = 1_700_000_000_000)

            val stored = taskDao.getById("task")
            assertEquals("completed", stored?.status)
            assertEquals(1_700_000_000_000, stored?.completedAt)
        }

    /** Increment happens in SQL so consecutive updates don't overwrite each other. */
    @Test
    fun focusedSecondsAccumulateAcrossSessions() =
        runTest {
            taskDao.insert(task(id = "task"))

            taskDao.addFocusedSeconds("task", 600)
            taskDao.addFocusedSeconds("task", 900)

            assertEquals(1500L, taskDao.getById("task")?.focusedSeconds)
        }

    @Test
    fun dailyHabitIsScheduledOnAnyWeekday() =
        runTest {
            habitDao.insert(habit(scheduleType = HabitEntity.SCHEDULE_DAILY, mask = HabitEntity.ALL_DAYS_MASK))

            val wednesday = habitDao.observeScheduledOn(dayBit = 1 shl 2).first()

            assertEquals(1, wednesday.size)
        }

    @Test
    fun habitWithSelectedDaysIsScheduledOnlyOnThoseDays() =
        runTest {
            // Monday and Wednesday
            val mask = (1 shl 0) or (1 shl 2)
            habitDao.insert(habit(scheduleType = HabitEntity.SCHEDULE_SELECTED_DAYS, mask = mask))

            assertEquals(1, habitDao.observeScheduledOn(dayBit = 1 shl 2).first().size)
            assertEquals(0, habitDao.observeScheduledOn(dayBit = 1 shl 3).first().size)
        }

    @Test
    fun archivedHabitDisappearsFromTheSchedule() =
        runTest {
            habitDao.insert(habit())

            habitDao.updateArchived(HABIT_ID, archived = true)

            assertTrue(habitDao.observeScheduledOn(dayBit = 1 shl 2).first().isEmpty())
            assertEquals(1, habitDao.observeAll().first().size)
        }

    @Test
    fun undoRemovesOnlyTheLastCompletionOfTheDay() =
        runTest {
            habitDao.insert(habit())
            habitDao.insertCompletion(completion(id = "c1", date = DAY, completedAt = 100))
            habitDao.insertCompletion(completion(id = "c2", date = DAY, completedAt = 200))

            habitDao.deleteLastCompletion(HABIT_ID, DAY)

            assertEquals(listOf("c1"), habitDao.getAllCompletions().map { it.id })
        }

    /** Breaks are excluded from focused time. */
    @Test
    fun totalFocusSumsOnlyFocusIntervals() =
        runTest {
            focusSessionDao.insert(focusSession(id = "focus", kind = "focus", seconds = 1500))
            focusSessionDao.insert(focusSession(id = "break", kind = "short_break", seconds = 300))

            val total = focusSessionDao.observeTotalSeconds(0, Long.MAX_VALUE).first()

            assertEquals(1500, total)
        }

    @Test
    fun totalFocusOfAnEmptyPeriodIsZeroRatherThanNull() =
        runTest {
            val total = focusSessionDao.observeTotalSeconds(0, Long.MAX_VALUE).first()

            assertEquals(0, total)
        }

    @Test
    fun deletingTaskKeepsItsSessionsInStatistics() =
        runTest {
            taskDao.insert(task(id = "task"))
            focusSessionDao.insert(focusSession(id = "session", taskId = "task"))

            taskDao.deleteById("task")

            val stored = focusSessionDao.getAll().single()
            assertEquals("session", stored.id)
            assertNull(stored.taskId)
        }

    private fun category() =
        CategoryEntity(
            id = CATEGORY_ID,
            name = "Work",
            color = "purple",
            icon = "work",
            sortOrder = 0,
        )

    private fun task(
        id: String,
        dueDate: Long? = DAY,
        status: String = "active",
        categoryId: String? = null,
    ) = TaskEntity(
        id = id,
        title = "Finish Compose navigation",
        description = null,
        categoryId = categoryId,
        estimateSeconds = 2700,
        focusedSeconds = 0,
        dueDate = dueDate,
        priority = "medium",
        status = status,
        createdAt = 1_700_000_000_000,
        completedAt = null,
    )

    private fun habit(
        scheduleType: String = HabitEntity.SCHEDULE_DAILY,
        mask: Int = HabitEntity.ALL_DAYS_MASK,
    ) = HabitEntity(
        id = HABIT_ID,
        name = "Read",
        icon = "read",
        color = "blue",
        scheduleType = scheduleType,
        scheduleDaysMask = mask,
        targetPerDay = 1,
        reminderMinuteOfDay = null,
        note = null,
        createdAt = 1_700_000_000_000,
        archived = false,
        sortOrder = 0,
    )

    private fun completion(
        id: String,
        date: Long,
        completedAt: Long = 1_700_000_000_000,
    ) = HabitCompletionEntity(id = id, habitId = HABIT_ID, date = date, completedAt = completedAt)

    private fun focusSession(
        id: String,
        kind: String = "focus",
        seconds: Long = 1500,
        taskId: String? = null,
    ) = FocusSessionEntity(
        id = id,
        taskId = taskId,
        categoryId = null,
        kind = kind,
        plannedSeconds = 1500,
        actualSeconds = seconds,
        startedAt = 1_700_000_000_000,
        endedAt = 1_700_000_100_000,
        completed = true,
    )

    private companion object {
        const val CATEGORY_ID = "category"
        const val HABIT_ID = "habit"

        /** Arbitrary epoch day. */
        const val DAY = 20_000L
    }
}