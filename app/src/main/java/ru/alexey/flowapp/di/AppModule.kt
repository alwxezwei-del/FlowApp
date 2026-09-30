package ru.alexey.flowapp.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import ru.alexey.flowapp.core.data.di.DataModule
import ru.alexey.flowapp.feature.focus.di.FocusModule
import ru.alexey.flowapp.feature.habits.di.HabitsModule
import ru.alexey.flowapp.feature.history.di.HistoryModule
import ru.alexey.flowapp.feature.home.di.HomeModule
import ru.alexey.flowapp.feature.settings.di.SettingsModule
import ru.alexey.flowapp.feature.statistics.di.StatisticsModule
import ru.alexey.flowapp.feature.tasks.di.TasksModule

@Module(
    includes = [
        DataModule::class,
        HomeModule::class,
        TasksModule::class,
        HabitsModule::class,
        FocusModule::class,
        StatisticsModule::class,
        HistoryModule::class,
        SettingsModule::class,
    ],
)
@ComponentScan("ru.alexey.flowapp")
class AppModule