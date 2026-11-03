package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity

object NotificationHelper {

    const val CHANNEL_ID = "sera_akisos_title_updates"
    private const val CHANNEL_NAME = "Atualizações de Títulos e Temporadas"
    private const val CHANNEL_DESC = "Notifica quando há novos episódios ou temporadas de títulos salvos"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableLights(true)
                lightColor = android.graphics.Color.MAGENTA
                enableVibration(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun sendTitleUpdateNotification(
        context: Context,
        titleName: String,
        message: String,
        notificationId: Int = (System.currentTimeMillis() % 100000).toInt()
    ) {
        createNotificationChannel(context)

        if (!hasNotificationPermission(context)) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentTitle("Sera Akisos: Atualização!")
            .setContentText("$titleName: $message")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Novidades sobre '$titleName':\n$message\nAbra o Sera Akisos para acompanhar o novo episódio.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(0xFFB026FF.toInt())
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Handled safely
        }
    }

    fun sendTestNotification(context: Context) {
        sendTitleUpdateNotification(
            context = context,
            titleName = "Sera Akisos",
            message = "Notificações ativas! Você será avisado assim que novos episódios ou temporadas forem encontrados.",
            notificationId = 1001
        )
    }
}
