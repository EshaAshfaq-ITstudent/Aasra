package com.aasra.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ComplaintSuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_complaint_success)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        findViewById<Button>(R.id.btnTrackRequest).setOnClickListener {
            startActivity(Intent(this, TrackTicketActivity::class.java))
        }

        // Bottom Navigation
        findViewById<ImageView>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.navHelp).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }
        
        findViewById<ImageView>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}