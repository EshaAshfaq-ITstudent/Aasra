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
    private var isVoiceEnabled = true
    private var passedTicketId: String? = null
    private lateinit var ivVoiceAssistant: ImageView

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

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        isVoiceEnabled = sharedPref.getBoolean("VOICE_ENABLED", true)

        dbHelper = DatabaseHelper(this)
        tts = TextToSpeech(this, this)

        userCnic = sharedPref.getString("USER_CNIC", "")
        passedTicketId = intent.getStringExtra("TICKET_ID")

        ivVoiceAssistant = findViewById(R.id.ivVoiceAssistant)
        updateVoiceIcon()

        ivVoiceAssistant.setOnClickListener {
            isVoiceEnabled = !isVoiceEnabled
            sharedPref.edit().putBoolean("VOICE_ENABLED", isVoiceEnabled).apply()
            updateVoiceIcon()
            if (isVoiceEnabled) speakGuidance() else tts?.stop()
        }

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<TextView>(R.id.tvVisitHelpCenter)?.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

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

    private fun updateVoiceIcon() {
        ivVoiceAssistant.setImageResource(if (isVoiceEnabled) R.drawable.ic_volume_up else R.drawable.ic_volume_off)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS && isVoiceEnabled) {
            speakGuidance()
        }
    }

    private fun speakGuidance() {
        if (!isVoiceEnabled) return
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isUrdu = sharedPref.getBoolean("USE_URDU", false)
        if (isUrdu) {
            tts?.setLanguage(Locale("ur", "PK"))
            tts?.speak("Aap yahan apni shikayat ki tafseelaat dekh saktay hain.", TextToSpeech.QUEUE_FLUSH, null, "TrackTicketID")
        } else {
            tts?.setLanguage(Locale.US)
            tts?.speak("You can see the progress of your support request here.", TextToSpeech.QUEUE_FLUSH, null, "TrackTicketID")
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        isVoiceEnabled = sharedPref.getBoolean("VOICE_ENABLED", true)
        updateVoiceIcon()
        handler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        tts?.stop()
        handler.removeCallbacks(refreshRunnable)
    }

    override fun onDestroy() {
        tts?.shutdown()
        super.onDestroy()
    }

    private fun loadTicketData() {
        val ticketData = if (!passedTicketId.isNullOrEmpty()) {
            dbHelper.getTicketDetails(passedTicketId!!)
        } else {
            userCnic?.let { dbHelper.getLatestTicket(it) }
        }

        if (ticketData != null) {
            findViewById<TextView>(R.id.tvTicketIdValue).text = "#${ticketData["ticketId"]}"
            findViewById<TextView>(R.id.tvIssueTypeValue).text = ticketData["issueType"]
            
            val timestamp = ticketData["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            findViewById<TextView>(R.id.tvSubmittedOnValue).text = sdf.format(Date(timestamp))

            updateStatusUI(ticketData["status"] ?: "Received")
        }
    }

    private fun updateStatusUI(status: String) {
        val icReceived = findViewById<ImageView>(R.id.icReceived)
        val icUnderReview = findViewById<ImageView>(R.id.icUnderReview)
        val icResolved = findViewById<ImageView>(R.id.icResolved)
        val line1 = findViewById<View>(R.id.line1)
        val line2 = findViewById<View>(R.id.line2)
        val tvUnderReviewDesc = findViewById<TextView>(R.id.tvUnderReviewDesc)

        // Reset
        val gray = android.graphics.Color.parseColor("#BDBDBD")
        val green = android.graphics.Color.parseColor("#00A651")
        
        icReceived.setColorFilter(green)
        line1.setBackgroundColor(gray)
        icUnderReview.setColorFilter(gray)
        line2.setBackgroundColor(gray)
        icResolved.setColorFilter(gray)
        tvUnderReviewDesc.visibility = View.GONE

        when (status) {
            "Received" -> {
                // Already set by default reset logic mostly
            }
            "Under Review" -> {
                line1.setBackgroundColor(green)
                icUnderReview.setColorFilter(green)
                tvUnderReviewDesc.visibility = View.VISIBLE
            }
            "Resolved" -> {
                line1.setBackgroundColor(green)
                icUnderReview.setColorFilter(green)
                line2.setBackgroundColor(green)
                icResolved.setColorFilter(green)
                tvUnderReviewDesc.visibility = View.VISIBLE
            }
        }
    }
}
