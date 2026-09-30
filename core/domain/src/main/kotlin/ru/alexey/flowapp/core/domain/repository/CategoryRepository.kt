package ru.alexey.flowapp.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alexey.flowapp.core.model.Category

/** User defined categories */
interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>

    suspend fun getCategory(id: String): Category?

    suspend fun createCategory(category: Category)

    suspend fun updateCategory(category: Category)

    suspend fun deleteCategory(id: String)
}