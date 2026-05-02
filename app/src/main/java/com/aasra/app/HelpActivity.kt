package com.aasra.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HelpActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    
    private lateinit var dbHelper: DatabaseHelper
    private var userCnic: String? = null
    private var tts: TextToSpeech? = null
    private var userName: String = "User"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_help)

        dbHelper = DatabaseHelper(this)
        tts = TextToSpeech(this, this)
        
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userCnic = sharedPref.getString("USER_CNIC", "")
        userName = sharedPref.getString("USER_NAME", "User") ?: "User"

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        findViewById<TextView>(R.id.tvHelpGreeting).text = "How can we assist you, $userName?"

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<MaterialCardView>(R.id.cardChatbot).setOnClickListener {
            startActivity(Intent(this, ChatbotActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardSubmitRequest).setOnClickListener {
            startActivity(Intent(this, SubmitRequestActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardSupportTickets).setOnClickListener {
            startActivity(Intent(this, TrackTicketActivity::class.java))
        }

        loadSupportHistory()

        findViewById<ImageView>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("USER_CNIC", userCnic)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        
        findViewById<ImageView>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val isUrdu = sharedPref.getBoolean("USE_URDU", false)
            
            if (isUrdu) {
                tts?.setLanguage(Locale("ur", "PK"))
                val message = "Help center mein khush aamdeed. Yahan aap hamaray assistant se baat kar saktay hain, nayi shikayat jama kar saktay hain ya purani tickets dekh saktay hain."
                tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "HelpID")
            } else {
                tts?.setLanguage(Locale.US)
                val message = "Welcome to the help center. You can chat with our assistant, submit a new support request, or track your existing tickets below."
                tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "HelpID")
            }
        }
    }

    override fun onPause() {
        tts?.stop()
        super.onPause()
    }

    override fun onDestroy() {
        tts?.shutdown()
        super.onDestroy()
    }

    private fun loadSupportHistory() {
        val container = findViewById<LinearLayout>(R.id.historyContainer)
        val tvNoHistory = findViewById<TextView>(R.id.tvNoHistory)
        container.removeAllViews()

        if (userCnic.isNullOrEmpty()) return

        val tickets = dbHelper.getAllTickets(userCnic!!)

        if (tickets.isEmpty()) {
            tvNoHistory.visibility = View.VISIBLE
        } else {
            tvNoHistory.visibility = View.GONE
            
            tickets.take(3).forEach { ticket ->
                val itemView = LayoutInflater.from(this).inflate(R.layout.item_ticket_history, container, false)
                
                itemView.findViewById<TextView>(R.id.tvTicketTitle).text = ticket["issueType"]
                itemView.findViewById<TextView>(R.id.tvTicketId).text = "#${ticket["ticketId"]}"
                itemView.findViewById<TextView>(R.id.tvTicketStatus).text = ticket["status"]
                itemView.findViewById<TextView>(R.id.tvTicketDesc).text = ticket["description"]
                
                val timestamp = ticket["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()
                val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                itemView.findViewById<TextView>(R.id.tvTicketDate).text = "Submitted on ${sdf.format(Date(timestamp))}"
                
                val statusText = itemView.findViewById<TextView>(R.id.tvTicketStatus)
                when(ticket["status"]) {
                    "Resolved" -> statusText.setTextColor(android.graphics.Color.parseColor("#00A651"))
                    "Under Review" -> statusText.setTextColor(android.graphics.Color.parseColor("#FF9800"))
                    else -> statusText.setTextColor(android.graphics.Color.parseColor("#2196F3"))
                }

                container.addView(itemView)
            }
        }
    }
}
