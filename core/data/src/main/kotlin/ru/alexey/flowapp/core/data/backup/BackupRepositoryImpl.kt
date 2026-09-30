package ru.alexey.flowapp.core.data.backup

import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import ru.alexey.flowapp.core.common.TimeProvider
import ru.alexey.flowapp.core.data.datastore.SettingsStore
import ru.alexey.flowapp.core.database.FlowDatabase
import ru.alexey.flowapp.core.domain.repository.BackupRepository
import ru.alexey.flowapp.core.domain.repository.ImportResult

@Single
internal class BackupRepositoryImpl(
    private val database: FlowDatabase,
    private val settingsStore: SettingsStore,
    private val timeProvider: TimeProvider,
) : BackupRepository {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun export(): String =
        withContext(Dispatchers.IO) {
            val dto = BackupDto(
                version = BackupDto.CURRENT_VERSION,
                exportedAt = timeProvider.now().toEpochMilliseconds(),
                settings = settingsStore.settings.first().toDto(),
                categories = database.categoryDao().getAll().map { it.toDto() },
                tasks = database.taskDao().getAll().map { it.toDto() },
                habits = database.habitDao().getAll().map { it.toDto() },
                habitCompletions = database.habitDao().getAllCompletions().map { it.toDto() },
                focusSessions = database.focusSessionDao().getAll().map { it.toDto() },
            )
            json.encodeToString(dto)
        }

    override suspend fun import(rawJson: String): ImportResult =
        withContext(Dispatchers.IO) {
            val dto = runCatching { json.decodeFromString<BackupDto>(rawJson) }.getOrNull()
                ?: return@withContext ImportResult.InvalidFile

            if (dto.version > BackupDto.CURRENT_VERSION) {
                return@withContext ImportResult.UnsupportedVersion(
                    fileVersion = dto.version,
                    supportedVersion = BackupDto.CURRENT_VERSION,
                )
            }

            database.withTransaction {
                clearDatabase()
                // Order matters: foreign keys require parents before children
                database.categoryDao().upsertAll(dto.categories.map { it.toEntity() })
                database.taskDao().upsertAll(dto.tasks.map { it.toEntity() })
                database.habitDao().upsertAll(dto.habits.map { it.toEntity() })
                database.habitDao().upsertCompletions(dto.habitCompletions.map { it.toEntity() })
                database.focusSessionDao().upsertAll(dto.focusSessions.map { it.toEntity() })
            }
            settingsStore.update(dto.settings.toDomain())

            ImportResult.Success(
                tasks = dto.tasks.size,
                habits = dto.habits.size,
                sessions = dto.focusSessions.size,
            )
        }

    override suspend fun reset() =
        withContext(Dispatchers.IO) {
            database.withTransaction { clearDatabase() }
            settingsStore.clear()
        }

    private suspend fun clearDatabase() {
        database.focusSessionDao().clear()
        database.habitDao().clearCompletions()
        database.habitDao().clear()
        database.taskDao().clear()
        database.categoryDao().clear()
    }
}