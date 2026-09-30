package ru.alexey.flowapp.feature.focus.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import ru.alexey.flowapp.core.common.TimeProvider
import ru.alexey.flowapp.core.domain.repository.FocusTimerController
import ru.alexey.flowapp.core.model.TimerState
import ru.alexey.flowapp.core.model.runningSession
import kotlin.time.Duration.Companion.seconds

/**
 * Foreground service for an active focus session.
 *
 * Doesn't count time itself: mirrors [FocusTimerController] in the notification and checks every
 * second whether the interval has elapsed. Stops itself on [TimerState.Idle].
 */
class FocusTimerService : Service() {
    private val controller: FocusTimerController by inject()
    private val timeProvider: TimeProvider by inject()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var started = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        FocusNotifications.ensureChannel(this)
        observeState()
        observeElapsed()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        when (intent?.action) {
            ACTION_PAUSE -> scope.launch { controller.pause() }
            ACTION_RESUME -> scope.launch { controller.resume() }
            ACTION_STOP -> scope.launch { controller.stop(completed = false) }
            else -> Unit
        }
        // After process death the session is restored by the controller from its snapshot,
        // not by the system redelivering the last intent
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun observeState() {
        scope.launch {
            controller.state.collectLatest { state ->
                if (state is TimerState.Idle) {
                    stopForegroundAndSelf()
                } else {
                    updateNotification(state)
                }
            }
        }
    }

    /** Per-second loop: updates the notification and finishes the interval when time is up. */
    private fun observeElapsed() {
        scope.launch {
            while (isActive) {
                delay(TICK)
                val state = controller.state.value
                if (state is TimerState.Idle) continue
                val running = state.runningSession
                if (controller.finishIfElapsed() != null && running != null) {
                    FocusNotifications.showFinished(this@FocusTimerService, running, launchAppIntent())
                }
                if (controller.state.value !is TimerState.Idle) {
                    updateNotification(controller.state.value)
                }
            }
        }
    }

    private fun updateNotification(state: TimerState) {
        val notification = FocusNotifications.build(
            context = this,
            state = state,
            now = timeProvider.now(),
            contentIntent = launchAppIntent(),
        )

        if (started) {
            val manager = NotificationManagerCompat.from(this)
            if (manager.areNotificationsEnabled()) {
                @SuppressLint("MissingPermission")
                manager.notify(FocusNotifications.NOTIFICATION_ID, notification)
            }
            return
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(this, FocusNotifications.NOTIFICATION_ID, notification, type)
        started = true
    }

    private fun stopForegroundAndSelf() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        started = false
        stopSelf()
    }

    /** Opens the app on its last screen. */
    private fun launchAppIntent(): PendingIntent? {
        val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return null
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val ACTION_PAUSE = "ru.alexey.flowapp.focus.PAUSE"
        const val ACTION_RESUME = "ru.alexey.flowapp.focus.RESUME"
        const val ACTION_STOP = "ru.alexey.flowapp.focus.STOP"

        private val TICK = 1.seconds

        fun start(context: Context) {
            val intent = Intent(context, FocusTimerService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FocusTimerService::class.java))
        }
    }
}