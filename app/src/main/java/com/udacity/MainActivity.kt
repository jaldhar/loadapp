package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.udacity.databinding.ActivityMainBinding
import com.udacity.databinding.ContentMainBinding

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var contentMainBinding: ContentMainBinding
    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action
    private var url = ""
    private var notificationDescription = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        contentMainBinding = ContentMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        setContentView(contentMainBinding.root)
        setSupportActionBar(activityMainBinding.toolbar)

        notificationManager =
            ContextCompat.getSystemService(applicationContext, NotificationManager::class.java)
                    as NotificationManager

        createChannel()

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        contentMainBinding.customButton.setOnClickListener { it as LoadingButton
            if (!it.isLoading()) {
                download()
            }
        }

        contentMainBinding.choices.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioGlide -> {
                    url = GLIDE_URL
                    notificationDescription =
                        getString(R.string.glide_notification_description)
                }
                R.id.radioLoadApp -> {
                    url = UDACITY_URL
                    notificationDescription =
                        getString(R.string.udacity_notification_description)
                }
                R.id.radioRetrofit -> {
                    url = RETROFIT_URL
                    notificationDescription =
                        getString(R.string.retrofit_notification_description)
                }
                else -> {
                    url = ""
                    notificationDescription = ""
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            contentMainBinding.customButton.setState(ButtonState.Completed)

            downloadID = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)!!
            if (downloadID == -1L) {
                return
            }

            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadID))

            if (cursor.moveToFirst()) {

                sendNotification(
                    getString(R.string.notification_description, notificationDescription),
                    applicationContext
                )

                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        Toast.makeText(
                            context,
                            resources.getString(R.string.notification_description, notificationDescription),
                            Toast.LENGTH_SHORT
                        ).show()
                        url = ""
                        contentMainBinding.choices.clearCheck()
                    }
                    DownloadManager.STATUS_FAILED -> {
                        // download has failed
                    }
                    else -> {
                    }
                }
            } else {
                // download is cancelled
            }

        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
            )
                .apply {
                    setShowBadge(false)
                }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.notification_description)

            notificationManager.createNotificationChannel(notificationChannel)

        }
    }

    private fun download() {
        val customButton = contentMainBinding.customButton
        customButton.setState(ButtonState.Loading)

        if (url == "") {
            Toast.makeText(
                customButton.context, "Please select the file to download",
                Toast.LENGTH_SHORT
            ).show()
            customButton.setState(ButtonState.Completed)
            return
        }

        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                    URLUtil.guessFileName(url, null, null))

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    private fun sendNotification(messageBody: String, applicationContext: Context) {
        val detailIntent = Intent(applicationContext, DetailActivity::class.java)

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        pendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            detailIntent,
            flags
        )

        action = NotificationCompat.Action(
            R.drawable.ic_assistant_black_24dp,
            getString(R.string.check_status),
            pendingIntent )

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(messageBody)
            .setAutoCancel(true)
            .addAction(action)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    companion object {
        private const val GLIDE_URL =
            "https://github.com/bumptech/glide/archive/master.zip"
        private const val UDACITY_URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val RETROFIT_URL =
            "https://github.com/square/retrofit/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
        private const val NOTIFICATION_ID = 0
    }

}
