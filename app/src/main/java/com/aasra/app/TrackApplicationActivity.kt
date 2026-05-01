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

class TrackApplicationActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var userCnic: String? = null

    private val handler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            loadApplicationData()
            handler.postDelayed(this, 5000) // Refresh every 5 seconds
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_application)

        dbHelper = DatabaseHelper(this)
        userCnic = intent.getStringExtra("USER_CNIC")

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        
        findViewById<View>(R.id.btnRaiseComplaint).setOnClickListener {
            startActivity(Intent(this, SubmitRequestActivity::class.java))
        }

        findViewById<ImageView>(R.id.navHome).setOnClickListener {
            finish()
        }
        
        findViewById<ImageView>(R.id.navHelp).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        findViewById<ImageView>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        loadApplicationData()
    }

    override fun onResume() {
        super.onResume()
        handler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshRunnable)
    }

    private fun loadApplicationData() {
        val appData = userCnic?.let { dbHelper.getPensionApplication(it) }

        if (appData != null) {
            val appId = appData["appId"] ?: "---"
            val submissionTime = appData["timestamp"]?.toLong() ?: 0L
            val currentTime = System.currentTimeMillis()
            val elapsedSeconds = (currentTime - submissionTime) / 1000

            findViewById<TextView>(R.id.tvTrackIdValue).text = appId

            // Logic for status progression based on appId
            // Last digit determines the fate
            val lastDigit = appId.lastOrNull()?.digitToInt() ?: 0
            
            var currentStatus = "Submitted"
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val subDate = sdf.format(Date(submissionTime))
            
            findViewById<TextView>(R.id.tvDateSubmitted).text = subDate

            // Initial UI state (All blurred except first)
            setStepState(R.id.stepSubmitted, true)
            setStepState(R.id.stepReview, false)
            setStepState(R.id.stepVerification, false)
            setStepState(R.id.stepFinal, false)
            findViewById<View>(R.id.line1).alpha = 0.3f
            findViewById<View>(R.id.line2).alpha = 0.3f
            findViewById<View>(R.id.line3).alpha = 0.3f

            // 1. Under Review (after 30s)
            if (elapsedSeconds >= 30 && lastDigit <= 8) {
                currentStatus = "Under Review"
                setStepState(R.id.stepReview, true)
                findViewById<View>(R.id.line1).alpha = 1.0f
                findViewById<TextView>(R.id.tvDateReview).text = sdf.format(Date(submissionTime + 30000))
            }

            // 2. Verification (after 60s)
            if (elapsedSeconds >= 60 && lastDigit <= 7) {
                currentStatus = "Verification"
                setStepState(R.id.stepVerification, true)
                findViewById<View>(R.id.line2).alpha = 1.0f
                findViewById<TextView>(R.id.tvDateVerification).text = sdf.format(Date(submissionTime + 60000))
            }

            // 3. Final Step (after 90s)
            if (elapsedSeconds >= 90) {
                if (lastDigit <= 5) {
                    currentStatus = "Approved"
                    setStepState(R.id.stepFinal, true)
                    findViewById<View>(R.id.line3).alpha = 1.0f
                    findViewById<TextView>(R.id.tvFinalTitle).text = "Approved"
                    findViewById<ImageView>(R.id.icFinal).setImageResource(R.drawable.ic_approved)
                    findViewById<TextView>(R.id.tvDateFinal).text = sdf.format(Date(submissionTime + 90000))
                } else if (lastDigit == 9) {
                    currentStatus = "Rejected"
                    setStepState(R.id.stepFinal, true)
                    findViewById<View>(R.id.line3).alpha = 1.0f
                    findViewById<TextView>(R.id.tvFinalTitle).text = "Rejected"
                    findViewById<TextView>(R.id.tvFinalTitle).setTextColor(android.graphics.Color.RED)
                    findViewById<ImageView>(R.id.icFinal).setImageResource(R.drawable.ic_doc_rejected)
                    findViewById<TextView>(R.id.tvDateFinal).text = sdf.format(Date(submissionTime + 90000))
                }
            }
            
            // Update status in DB if it changed
            if (currentStatus != appData["status"]) {
                dbHelper.updateApplicationStatus(appId, currentStatus)
            }

        } else {
            findViewById<TextView>(R.id.tvTrackIdValue).text = "No Application Found"
            findViewById<View>(R.id.statusContainer).visibility = View.GONE
        }
    }

    private fun setStepState(viewId: Int, active: Boolean) {
        val view = findViewById<View>(viewId)
        view.alpha = if (active) 1.0f else 0.3f
    }
}
