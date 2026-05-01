package com.aasra.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class DashboardActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var userCnic: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        dbHelper = DatabaseHelper(this)
        
        // Receive User Info from Intent
        val userName = intent.getStringExtra("USER_NAME") ?: "User"
        userCnic = intent.getStringExtra("USER_CNIC")
        
        findViewById<TextView>(R.id.tvHelloName).text = "Hello, $userName"

        // Load Application Status
        updateApplicationStatus()

        // Navigation for Action Cards
        findViewById<MaterialCardView>(R.id.cardApplyPension).setOnClickListener {
            val intent = Intent(this, PensionActivity::class.java)
            intent.putExtra("USER_CNIC", userCnic)
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.cardTrackApp).setOnClickListener {
            val intent = Intent(this, TrackApplicationActivity::class.java)
            intent.putExtra("USER_CNIC", userCnic)
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.cardComplaint).setOnClickListener {
            startActivity(Intent(this, SubmitRequestActivity::class.java))
        }

        // Setup Expandable FAQs
        setupFaq(R.id.faqItem1, R.id.tvAnswer1, R.id.ivExpand1)
        setupFaq(R.id.faqItem2, R.id.tvAnswer2, R.id.ivExpand2)

        // Bottom Navigation
        findViewById<ImageView>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<ImageView>(R.id.navHelp).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh status when returning to dashboard
        updateApplicationStatus()
    }

    private fun updateApplicationStatus() {
        val tvUpdateTitle = findViewById<TextView>(R.id.tvUpdateTitle)
        val tvUpdateId = findViewById<TextView>(R.id.tvUpdateId)
        
        val appData = userCnic?.let { dbHelper.getPensionApplication(it) }
        
        if (appData != null) {
            tvUpdateTitle.text = "Pension Application"
            tvUpdateId.text = "#${appData["appId"]} - ${appData["status"]}"
            
            // Allow clicking the status card to track
            findViewById<View>(R.id.cardRecentUpdates).setOnClickListener {
                val intent = Intent(this, TrackApplicationActivity::class.java)
                intent.putExtra("USER_CNIC", userCnic)
                startActivity(intent)
            }
        } else {
            tvUpdateTitle.text = "No current application"
            tvUpdateId.text = "Apply for pension to see status here."
        }
    }

    private fun setupFaq(layoutId: Int, answerId: Int, iconId: Int) {
        val layout = findViewById<MaterialCardView>(layoutId)
        val answer = findViewById<TextView>(answerId)
        val icon = findViewById<ImageView>(iconId)

        layout.setOnClickListener {
            if (answer.visibility == View.GONE) {
                answer.visibility = View.VISIBLE
                icon.setImageResource(R.drawable.ic_remove) // Minus icon
            } else {
                answer.visibility = View.GONE
                icon.setImageResource(R.drawable.ic_add) // Plus icon
            }
        }
    }
}
