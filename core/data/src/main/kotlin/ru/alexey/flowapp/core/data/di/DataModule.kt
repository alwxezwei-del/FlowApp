package ru.alexey.flowapp.core.data.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import ru.alexey.flowapp.core.common.SystemTimeProvider
import ru.alexey.flowapp.core.common.TimeProvider
import ru.alexey.flowapp.core.database.di.DatabaseModule
import ru.alexey.flowapp.core.domain.di.DomainModule

@Module(includes = [DatabaseModule::class, DomainModule::class])
@ComponentScan("ru.alexey.flowapp.core.data")
class DataModule {
    @Single
    fun timeProvider(): TimeProvider = SystemTimeProvider()
}