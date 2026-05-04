package com.aasra.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ComplaintSuccessActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    
    private lateinit var dbHelper: DatabaseHelper
    private var tts: TextToSpeech? = null
    private var ticketId: String = ""
    private var isVoiceEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_complaint_success)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        isVoiceEnabled = sharedPref.getBoolean("VOICE_ENABLED", true)

        dbHelper = DatabaseHelper(this)
        tts = TextToSpeech(this, this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        displayTicketDetails()

        findViewById<Button>(R.id.btnTrackRequest).setOnClickListener {
            startActivity(Intent(this, TrackTicketActivity::class.java))
        }

        findViewById<ImageView>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
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

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            if (!isVoiceEnabled) return

            val isUrdu = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getBoolean("USE_URDU", false)
            if (isUrdu) {
                tts?.setLanguage(Locale("ur", "PK"))
                tts?.speak("Mubarak ho! Aap ki shikayat jama ho gayi hai. Ticket number hai $ticketId.", TextToSpeech.QUEUE_FLUSH, null, "SuccessTicketID")
            } else {
                tts?.setLanguage(Locale.US)
                tts?.speak("Success! Your complaint has been submitted. Your ticket I D is $ticketId. You can track its progress by clicking the track my request button.", TextToSpeech.QUEUE_FLUSH, null, "SuccessTicketID")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        isVoiceEnabled = sharedPref.getBoolean("VOICE_ENABLED", true)
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    private fun displayTicketDetails() {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userCnic = sharedPref.getString("USER_CNIC", "") ?: ""

        if (userCnic.isNotEmpty()) {
            val ticketData = dbHelper.getLatestTicket(userCnic)
            if (ticketData != null) {
                ticketId = ticketData["ticketId"] ?: ""
                findViewById<TextView>(R.id.tvTicketId).text = "#$ticketId"
                findViewById<TextView>(R.id.tvIssueType).text = ticketData["issueType"]
                
                val timestamp = ticketData["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                findViewById<TextView>(R.id.tvSubmittedDate).text = sdf.format(Date(timestamp))
            }
        }
    }
}