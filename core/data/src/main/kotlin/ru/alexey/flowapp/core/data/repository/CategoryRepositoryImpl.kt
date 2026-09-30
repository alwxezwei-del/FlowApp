package ru.alexey.flowapp.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import ru.alexey.flowapp.core.database.dao.CategoryDao
import ru.alexey.flowapp.core.database.entity.toDomain
import ru.alexey.flowapp.core.database.entity.toEntity
import ru.alexey.flowapp.core.domain.repository.CategoryRepository
import ru.alexey.flowapp.core.model.Category

@Single
internal class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao,
) : CategoryRepository {
    override fun observeCategories(): Flow<List<Category>> = categoryDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getCategory(id: String): Category? = categoryDao.getById(id)?.toDomain()

    override suspend fun createCategory(category: Category) = categoryDao.insert(category.toEntity())

    override suspend fun updateCategory(category: Category) = categoryDao.update(category.toEntity())

    override suspend fun deleteCategory(id: String) = categoryDao.deleteById(id)
}