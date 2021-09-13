package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.udacity.databinding.ActivityDetailBinding
import com.udacity.databinding.ContentDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var detailActivityBinding: ActivityDetailBinding
    private lateinit var contentDetailBinding: ContentDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        detailActivityBinding = ActivityDetailBinding.inflate(layoutInflater)
        contentDetailBinding = ContentDetailBinding.inflate(layoutInflater)

        setContentView(detailActivityBinding.root)
        setContentView(contentDetailBinding.root)
        setSupportActionBar(detailActivityBinding.toolbar)

        val notificationManager =
            ContextCompat.getSystemService(applicationContext, NotificationManager::class.java)
                    as NotificationManager

        notificationManager.cancelAll()

        when(intent.getIntExtra("STATUS", DownloadManager.STATUS_FAILED)) {
            DownloadManager.STATUS_SUCCESSFUL -> {
                contentDetailBinding.status.apply {
                    text = "SUCCESS"
                    setTextColor(Color.GREEN)
                }
            }
            DownloadManager.STATUS_FAILED -> {
                contentDetailBinding.status.apply {
                    text = "FAILED"
                    setTextColor(Color.RED)
                }
            }
            else -> {
                contentDetailBinding.status.text = "UNKNOWN"
            }
        }

        val filename = intent.getStringExtra("FILENAME")
        contentDetailBinding.filename.text = filename

        contentDetailBinding.loadingButton.setState(ButtonState.Clicked)
        contentDetailBinding.loadingButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

}
