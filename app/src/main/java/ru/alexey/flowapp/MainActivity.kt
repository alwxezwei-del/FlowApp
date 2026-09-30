package ru.alexey.flowapp

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.koin.android.ext.android.inject
import ru.alexey.flowapp.core.designsystem.theme.FlowTheme
import ru.alexey.flowapp.core.domain.repository.SettingsRepository
import ru.alexey.flowapp.core.model.AppSettings
import ru.alexey.flowapp.core.ui.navigation.TopLevelDestination
import ru.alexey.flowapp.navigation.FlowBottomBar
import ru.alexey.flowapp.navigation.FlowNavHost
import ru.alexey.flowapp.navigation.isTopLevel
import ru.alexey.flowapp.navigation.navigateToTopLevel

/**
 * Single activity
 */
class MainActivity : ComponentActivity() {
    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        super.onCreate(savedInstanceState)

        setContent {
            val settings by settingsRepository
                .observeSettings()
                .collectAsStateWithLifecycle(initialValue = AppSettings())

            FlowTheme(themeMode = settings.themeMode, accentColor = settings.accentColor) {
                FlowApp(appVersion = BuildConfig.VERSION_NAME)
            }
        }
    }
}

@Composable
private fun FlowApp(appVersion: String) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination

    Scaffold(
        containerColor = FlowTheme.colors.layer0,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            AnimatedVisibility(
                visible = isTopLevel(destination),
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                FlowBottomBar(
                    currentDestination = destination,
                    onSelect = { target: TopLevelDestination ->
                        navController.navigateToTopLevel(target.route)
                    },
                )
            }
        },
    ) { padding ->
        FlowNavHost(
            navController = navController,
            appVersion = appVersion,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .consumeWindowInsets(padding),
        )
    }
}