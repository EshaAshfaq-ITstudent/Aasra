package com.aasra.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class TrackTicketActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var userCnic: String? = null
    
    private val handler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            loadTicketData()
            handler.postDelayed(this, 5000) // Refresh every 5 seconds
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_ticket)

        dbHelper = DatabaseHelper(this)
        
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        userCnic = sharedPref.getString("USER_CNIC", "")

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

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

        loadTicketData()
    }

    override fun onResume() {
        super.onResume()
        handler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshRunnable)
    }

    private fun loadTicketData() {
        val ticketData = userCnic?.let { dbHelper.getLatestTicket(it) }

        if (ticketData != null) {
            val ticketId = ticketData["ticketId"] ?: "---"
            val submissionTime = ticketData["timestamp"]?.toLong() ?: 0L
            val currentTime = System.currentTimeMillis()
            val elapsedSeconds = (currentTime - submissionTime) / 1000

            findViewById<TextView>(R.id.tvTicketIdValue).text = ticketId
            findViewById<TextView>(R.id.tvIssueTypeValue).text = ticketData["issueType"]
            
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            findViewById<TextView>(R.id.tvSubmittedOnValue).text = sdf.format(Date(submissionTime))

            var currentStatus = "Received"

            // Initial UI state
            setStepState(R.id.stepReceived, true, true)
            setStepState(R.id.stepUnderReview, false, false)
            setStepState(R.id.stepResolved, false, false)
            
            findViewById<View>(R.id.line1).alpha = 0.3f
            findViewById<View>(R.id.line2).alpha = 0.3f

            // 1. Under Review (after 20s)
            if (elapsedSeconds >= 20) {
                currentStatus = "Under Review"
                setStepState(R.id.stepUnderReview, true, true)
                findViewById<View>(R.id.line1).alpha = 1.0f
                findViewById<View>(R.id.tvUnderReviewDesc).visibility = View.VISIBLE
            }

            // 2. Resolved (after 45s)
            if (elapsedSeconds >= 45) {
                currentStatus = "Resolved"
                setStepState(R.id.stepResolved, true, true)
                findViewById<View>(R.id.line2).alpha = 1.0f
            }
            
            // Update status in DB if it changed
            if (currentStatus != ticketData["status"]) {
                dbHelper.updateTicketStatus(ticketId, currentStatus)
            }

        } else {
            findViewById<TextView>(R.id.tvTicketIdValue).text = "No Ticket Found"
        }
    }

    private fun setStepState(stepLayoutId: Int, isActive: Boolean, isCompleted: Boolean) {
        val layout = findViewById<View>(stepLayoutId)
        val icon = layout.findViewWithTag<ImageView>("statusIcon") ?: layout.findViewById<ImageView>(
            when(stepLayoutId) {
                R.id.stepReceived -> R.id.icReceived
                R.id.stepUnderReview -> R.id.icUnderReview
                else -> R.id.icResolved
            }
        )
        val title = layout.findViewWithTag<TextView>("statusTitle") ?: layout.findViewById<TextView>(
            when(stepLayoutId) {
                R.id.stepReceived -> R.id.tvReceivedTitle
                R.id.stepUnderReview -> R.id.tvUnderReviewTitle
                else -> R.id.tvResolvedTitle
            }
        )

        if (isActive) {
            layout.alpha = 1.0f
            icon.setImageResource(R.drawable.ic_radio_button_checked)
            icon.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#00A651"))
            title.setTypeface(null, android.graphics.Typeface.BOLD)
        } else {
            layout.alpha = 0.5f
            icon.setImageResource(R.drawable.ic_radio_button_checked)
            icon.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#BDBDBD"))
            title.setTypeface(null, android.graphics.Typeface.NORMAL)
        }
    }
}
