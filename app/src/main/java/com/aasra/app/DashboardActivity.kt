package com.aasra.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Navigation for Action Cards
        findViewById<MaterialCardView>(R.id.cardApplyPension).setOnClickListener {
            startActivity(Intent(this, PensionActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardTrackApp).setOnClickListener {
            startActivity(Intent(this, TrackApplicationActivity::class.java))
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