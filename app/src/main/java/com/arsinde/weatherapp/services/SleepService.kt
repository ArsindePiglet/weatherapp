package com.arsinde.weatherapp.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.arsinde.weatherapp.R


class SleepService : Service() {

    private val CHANNEL_ID = "SLEEP"
    private val NOTIFICATION_ID = 1001
    private val nm by lazy { NotificationManagerCompat.from(this) }
    private val channel by lazy {
        NotificationChannel(
            CHANNEL_ID,
            "Channel human readable title",
            NotificationManager.IMPORTANCE_HIGH
        )
    }
    private val notification by lazy {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_service)
            .setContentTitle("SERVICE")
            .setContentText("I'm sleeping")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        nm.createNotificationChannel(channel)
        notification.flags += Notification.FLAG_AUTO_CANCEL
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "Service is started", Toast.LENGTH_LONG).show()
        var iterator = 0


        val handler = Handler(Looper.getMainLooper())
        Thread(Runnable {
            while (iterator < 5) {
                Thread.sleep(1000)
                handler.post {
                    println("****************")
                }
                with(nm) {
                    notify(NOTIFICATION_ID, notification)
                }

                iterator++
            }
            println("-----------------------------------")
            this.stopSelf()
        }).start()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Toast.makeText(this, "Service is destroyed.", Toast.LENGTH_LONG).show()
    }
}
