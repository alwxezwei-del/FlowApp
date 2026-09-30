package ru.alexey.flowapp.feature.focus.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import ru.alexey.flowapp.core.model.FocusKind
import ru.alexey.flowapp.core.model.RunningSession
import ru.alexey.flowapp.core.model.TimerState
import ru.alexey.flowapp.core.model.remainingAt
import ru.alexey.flowapp.core.model.runningSession
import ru.alexey.flowapp.feature.focus.R
import kotlin.time.Instant

/**
 * Active focus session notification with Pause / Resume / Stop actions sent to the service via
 * broadcasts
 */
internal object FocusNotifications {
    const val CHANNEL_ID = "focus_timer"
    const val NOTIFICATION_ID = 1001
    private const val FINISHED_CHANNEL_ID = "focus_finished"
    private const val FINISHED_NOTIFICATION_ID = 1002

    /** Creates the silent timer channel and the alerting "finished" channel; no-op below API 26. */
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService<NotificationManager>() ?: return

        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.focus_notification_channel),
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = context.getString(R.string.focus_notification_channel_description)
                    setShowBadge(false)
                },
            )
        }

        if (manager.getNotificationChannel(FINISHED_CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    FINISHED_CHANNEL_ID,
                    context.getString(R.string.focus_finished_channel),
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = context.getString(R.string.focus_finished_channel_description)
                },
            )
        }
    }

    /** Alerting notification with sound when the interval reaches zero. */
    @SuppressLint("MissingPermission")
    fun showFinished(
        context: Context,
        session: RunningSession,
        contentIntent: PendingIntent?,
    ) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val title = if (session.kind == FocusKind.FOCUS) {
            context.getString(R.string.focus_finished_focus)
        } else {
            context.getString(R.string.focus_finished_break)
        }
        val notification = NotificationCompat
            .Builder(context, FINISHED_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(
                session.taskTitle
                    ?: context.getString(R.string.focus_finished_text, session.plannedDuration.inWholeMinutes.toInt()),
            ).setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()
        manager.notify(FINISHED_NOTIFICATION_ID, notification)
    }

    /**
     * @param now time used to compute the remaining time
     * @param contentIntent opens the app on tap
     */
    fun build(
        context: Context,
        state: TimerState,
        now: Instant,
        contentIntent: PendingIntent?,
    ): Notification {
        val session = state.runningSession
        val remaining = state.remainingAt(now)
        val minutes = remaining.inWholeMinutes + if (remaining.inWholeSeconds % SECONDS_IN_MINUTE > 0) 1 else 0

        val title = when (session?.kind) {
            FocusKind.SHORT_BREAK -> context.getString(R.string.focus_notification_short_break)
            FocusKind.LONG_BREAK -> context.getString(R.string.focus_notification_long_break)
            else -> context.getString(R.string.focus_notification_focus)
        }

        val builder = NotificationCompat
            .Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(context.getString(R.string.focus_notification_remaining, minutes))
            .setSubText(session?.taskTitle)
            .setOngoing(true)
            .setSilent(true)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(contentIntent)

        when (state) {
            is TimerState.Running -> builder.addAction(
                0,
                context.getString(R.string.focus_action_pause),
                context.servicePendingIntent(FocusTimerService.ACTION_PAUSE),
            )

            is TimerState.Paused -> builder.addAction(
                0,
                context.getString(R.string.focus_action_resume),
                context.servicePendingIntent(FocusTimerService.ACTION_RESUME),
            )

            TimerState.Idle -> Unit
        }

        builder.addAction(
            0,
            context.getString(R.string.focus_action_stop),
            context.servicePendingIntent(FocusTimerService.ACTION_STOP),
        )

        return builder.build()
    }

    private fun Context.servicePendingIntent(action: String): PendingIntent {
        val intent = Intent(this, FocusTimerService::class.java).setAction(action)
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private const val SECONDS_IN_MINUTE = 60L
}