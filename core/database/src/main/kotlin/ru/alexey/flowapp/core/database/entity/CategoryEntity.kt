package ru.alexey.flowapp.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.alexey.flowapp.core.model.AccentColor
import ru.alexey.flowapp.core.model.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "color")
    val color: String,
    @ColumnInfo(name = "icon")
    val icon: String,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,
)

fun CategoryEntity.toDomain(): Category =
    Category(
        id = id,
        name = name,
        color = AccentColor.parse(color),
        icon = icon,
        sortOrder = sortOrder,
    )

fun Category.toEntity(): CategoryEntity =
    CategoryEntity(
        id = id,
        name = name,
        color = color.key,
        icon = icon,
        sortOrder = sortOrder,
    )