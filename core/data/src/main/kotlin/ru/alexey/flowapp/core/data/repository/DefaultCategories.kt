package ru.alexey.flowapp.core.data.repository

import org.koin.core.annotation.Single
import ru.alexey.flowapp.core.database.dao.CategoryDao
import ru.alexey.flowapp.core.database.entity.toEntity
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.model.Category
import ru.alexey.flowapp.core.model.FlowIconKey

@Single
class DefaultCategoriesInitializer(
    private val categoryDao: CategoryDao,
) {
    suspend fun seedIfEmpty() {
        if (categoryDao.count() > 0) return
        categoryDao.upsertAll(DefaultCategories.map { it.toEntity() })
    }

    private companion object {
        val DefaultCategories = listOf(
            Category(name = "Work", color = AccentColor.PURPLE, icon = FlowIconKey.WORK, sortOrder = 0),
            Category(name = "Learning", color = AccentColor.BLUE, icon = FlowIconKey.LEARNING, sortOrder = 1),
            Category(name = "Personal", color = AccentColor.ORANGE, icon = FlowIconKey.PERSONAL, sortOrder = 2),
            Category(name = "Health", color = AccentColor.GREEN, icon = FlowIconKey.HEALTH, sortOrder = 3),
        )
    }
}