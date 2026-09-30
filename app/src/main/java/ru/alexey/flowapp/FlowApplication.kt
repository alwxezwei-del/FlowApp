package ru.alexey.flowapp

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.annotation.KoinApplication
import org.koin.plugin.module.dsl.startKoin
import ru.alexey.flowapp.core.data.repository.DefaultCategoriesInitializer
import ru.alexey.flowapp.core.domain.repository.FocusTimerController
import ru.alexey.flowapp.di.AppModule

/** App entry point */
@KoinApplication(modules = [AppModule::class])
class FlowApplication : Application() {
    private val categoriesInitializer: DefaultCategoriesInitializer by inject()
    private val timerController: FocusTimerController by inject()

    private val applicationScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

    override fun onCreate() {
        super.onCreate()
        initKoin()
        initData()
    }

    private fun initKoin() {
        startKoin<FlowApplication> {
            androidContext(this@FlowApplication)
            if (BuildConfig.DEBUG) androidLogger()
            allowOverride(false)
        }
    }

    private fun initData() {
        applicationScope.launch {
            categoriesInitializer.seedIfEmpty()
            timerController.restore()
            timerController.finishIfElapsed()
        }
    }
}