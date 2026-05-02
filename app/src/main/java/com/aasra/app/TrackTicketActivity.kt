package com.aasra.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class TrackTicketActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var dbHelper: DatabaseHelper
    private var userCnic: String? = null
    private var tts: TextToSpeech? = null

    private val handler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            loadTicketData()
            handler.postDelayed(this, 5000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_ticket)

        dbHelper = DatabaseHelper(this)
        tts = TextToSpeech(this, this)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userCnic = sharedPref.getString("USER_CNIC", "")

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<TextView>(R.id.tvVisitHelpCenter)?.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        // Home Navigation Fix: Points to Dashboard
        findViewById<ImageView>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("USER_CNIC", userCnic)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.navHelp).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        findViewById<ImageView>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        loadTicketData()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val isUrdu = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getBoolean("USE_URDU", false)
            if (isUrdu) {
                tts?.setLanguage(Locale("ur", "PK"))
                tts?.speak("Aap yahan apni shikayat ki tafseelaat dekh saktay hain.", TextToSpeech.QUEUE_FLUSH, null, "TrackTicketID")
            } else {
                tts?.setLanguage(Locale.US)
                tts?.speak("You can see the progress of your support request here.", TextToSpeech.QUEUE_FLUSH, null, "TrackTicketID")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshRunnable)
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    private fun loadTicketData() {
        val ticketData = userCnic?.let { dbHelper.getLatestTicket(it) }
        if (ticketData != null) {
            findViewById<TextView>(R.id.tvTicketIdValue).text = ticketData["ticketId"]
            findViewById<TextView>(R.id.tvIssueTypeValue).text = ticketData["issueType"]
        }
    }

    private fun setStepState(stepLayoutId: Int, isActive: Boolean, isCompleted: Boolean) {
        val layout = findViewById<View>(stepLayoutId)
        if (isActive) layout.alpha = 1.0f else layout.alpha = 0.5f
    }
}
