package com.aasra.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import java.util.Locale

class DashboardActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var dbHelper: DatabaseHelper
    private var userCnic: String? = null
    private var tts: TextToSpeech? = null
    private var userName: String = "User"
    private lateinit var ivVoiceAssistant: ImageView
    private var isVoiceEnabled = true
    
    private lateinit var hsvActions: HorizontalScrollView
    private val scrollHandler = Handler(Looper.getMainLooper())
    private var scrollPosition = 0
    private var scrollDirection = 1 // 1 for right, -1 for left

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        isVoiceEnabled = sharedPref.getBoolean("VOICE_ENABLED", true)

        dbHelper = DatabaseHelper(this)
        tts = TextToSpeech(this, this)
        
        userName = intent.getStringExtra("USER_NAME") ?: "User"
        userCnic = intent.getStringExtra("USER_CNIC") ?: sharedPref.getString("USER_CNIC", "")
        
        findViewById<TextView>(R.id.tvHelloName).text = "Hello, $userName"
        ivVoiceAssistant = findViewById(R.id.ivVoiceAssistant)
        hsvActions = findViewById(R.id.hsvActions)
        
        updateApplicationStatus()
        updateVoiceIcon()
        startAutoScroll()

        ivVoiceAssistant.setOnClickListener {
            isVoiceEnabled = !isVoiceEnabled
            sharedPref.edit().putBoolean("VOICE_ENABLED", isVoiceEnabled).apply()
            updateVoiceIcon()
            if (isVoiceEnabled) {
                speakGuidance()
            } else {
                tts?.stop()
            }
        }

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

        findViewById<MaterialCardView>(R.id.cardVault).setOnClickListener {
            startActivity(Intent(this, DocumentVaultActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardComplaint).setOnClickListener {
            startActivity(Intent(this, SubmitRequestActivity::class.java))
        }

        setupFaq(R.id.faqItem1, R.id.tvAnswer1, R.id.ivExpand1)
        setupFaq(R.id.faqItem2, R.id.tvAnswer2, R.id.ivExpand2)

        findViewById<ImageView>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<ImageView>(R.id.navHelp).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        findViewById<ImageView>(R.id.navHome).setOnClickListener {
            updateApplicationStatus()
            if (isVoiceEnabled) speakGuidance()
        }
    }

    private fun startAutoScroll() {
        val scrollRunnable = object : Runnable {
            override fun run() {
                val maxScroll = hsvActions.getChildAt(0).width - hsvActions.width
                if (maxScroll > 0) {
                    scrollPosition += (5 * scrollDirection)
                    if (scrollPosition >= maxScroll) {
                        scrollDirection = -1
                    } else if (scrollPosition <= 0) {
                        scrollDirection = 1
                    }
                    hsvActions.scrollTo(scrollPosition, 0)
                }
                scrollHandler.postDelayed(this, 50)
            }
        }
        scrollHandler.postDelayed(scrollRunnable, 2000)
    }

    private fun updateVoiceIcon() {
        if (isVoiceEnabled) {
            ivVoiceAssistant.setImageResource(R.drawable.ic_volume_up)
        } else {
            ivVoiceAssistant.setImageResource(R.drawable.ic_volume_off)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            if (isVoiceEnabled) {
                speakGuidance()
            }
        }
    }

    private fun speakGuidance() {
        if (!isVoiceEnabled) return
        
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isUrdu = sharedPref.getBoolean("USE_URDU", false)

        if (isUrdu) {
            val result = tts?.setLanguage(Locale("ur", "PK"))
            if (result == TextToSpeech.LANG_AVAILABLE || result == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                val message = "Khush aamdeed $userName. Ye aap ka dashboard hai. Yahan se aap pension ke liye apply kar saktay hain, apni application track kar saktay hain, ya apne documents save kar saktay hain."
                tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "DashboardID")
            } else {
                speakEnglishWelcome()
            }
        } else {
            speakEnglishWelcome()
        }
    }

    private fun speakEnglishWelcome() {
        tts?.setLanguage(Locale.US)
        val message = "Welcome back $userName. This is your dashboard. You can apply for pension, track status, or access your document vault."
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "DashboardID")
    }

    override fun onResume() {
        super.onResume()
        updateApplicationStatus()
    }

    override fun onPause() {
        tts?.stop()
        super.onPause()
    }

    override fun onDestroy() {
        scrollHandler.removeCallbacksAndMessages(null)
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    private fun updateApplicationStatus() {
        val tvUpdateTitle = findViewById<TextView>(R.id.tvUpdateTitle)
        val tvUpdateId = findViewById<TextView>(R.id.tvUpdateId)
        val appData = userCnic?.let { dbHelper.getPensionApplication(it) }
        
        if (appData != null) {
            tvUpdateTitle.text = "Pension Application"
            tvUpdateId.text = "#${appData["appId"]} - ${appData["status"]}"
            findViewById<View>(R.id.cardRecentUpdates).setOnClickListener {
                val intent = Intent(this, TrackApplicationActivity::class.java)
                intent.putExtra("USER_CNIC", userCnic)
                startActivity(intent)
            }
        }
    }

    private fun setupFaq(layoutId: Int, answerId: Int, iconId: Int) {
        val layout = findViewById<MaterialCardView>(layoutId)
        val answer = findViewById<TextView>(answerId)
        val icon = findViewById<ImageView>(iconId)
        layout.setOnClickListener {
            if (answer.visibility == View.GONE) {
                answer.visibility = View.VISIBLE
                icon.setImageResource(R.drawable.ic_remove)
            } else {
                answer.visibility = View.GONE
                icon.setImageResource(R.drawable.ic_add)
            }
        }
    }
}
