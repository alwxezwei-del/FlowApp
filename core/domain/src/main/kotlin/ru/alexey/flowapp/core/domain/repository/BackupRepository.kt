package ru.alexey.flowapp.core.domain.repository

/** JSON export/import. The only way to move data between devices */
interface BackupRepository {
    /** Serializes the whole database and settings to JSON */
    suspend fun export(): String

    /** Replaces database contents with data from the file */
    suspend fun import(json: String): ImportResult

    /** Wipes the database and settings */
    suspend fun reset()
}

sealed interface ImportResult {
    /** Import succeeded, with imported record counts */
    data class Success(
        val tasks: Int,
        val habits: Int,
        val sessions: Int,
    ) : ImportResult

    /** Not a FlowApp export or corrupted */
    data object InvalidFile : ImportResult

    /** File was created by a newer app version */
    data class UnsupportedVersion(
        val fileVersion: Int,
        val supportedVersion: Int,
    ) : ImportResult
}