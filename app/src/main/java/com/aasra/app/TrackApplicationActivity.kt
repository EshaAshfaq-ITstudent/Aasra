package com.aasra.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.aasra.app.databinding.ActivityTrackApplicationBinding
import java.text.SimpleDateFormat
import java.util.*

class TrackApplicationActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityTrackApplicationBinding
    private lateinit var dbHelper: DatabaseHelper
    private var userCnic: String? = null
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackApplicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        tts = TextToSpeech(this, this)
        
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userCnic = intent.getStringExtra("USER_CNIC") ?: sharedPref.getString("USER_CNIC", "")

        binding.btnBack.setOnClickListener { finish() }
        binding.btnRaiseComplaint.setOnClickListener {
            startActivity(Intent(this, SubmitRequestActivity::class.java))
        }

        binding.navHome.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        
        binding.navHelp.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        binding.navSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        loadApplicationData()
    }

    private fun loadApplicationData() {
        val appData = userCnic?.let { dbHelper.getPensionApplication(it) }
        if (appData != null) {
            val appId = appData["appId"] ?: "---"
            val status = appData["status"] ?: "Submitted"
            val timestamp = appData["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()
            
            binding.tvTrackIdValue.text = appId
            
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.tvDateSubmitted.text = "Submitted on ${sdf.format(Date(timestamp))}"
            
            updateStepper(status)
        }
    }

    private fun updateStepper(status: String) {
        // Reset colors to default gray
        val inactiveColor = 0xFFCCCCCC.toInt()
        val activeColor = 0xFFD99B82.toInt()
        val textColorInactive = 0xFF999999.toInt()
        val textColorActive = 0xFF1A1A1A.toInt()

        // Step 1 is always active if app exists
        binding.dot1.setBackgroundColor(activeColor)
        
        when (status) {
            "Submitted" -> {
                // Only step 1 active
            }
            "Under Review" -> {
                binding.dot2.setBackgroundColor(activeColor)
                binding.line1.setBackgroundColor(activeColor)
                binding.tvStep2Title.setTextColor(textColorActive)
                binding.tvStep2Status.text = "In Progress"
                binding.tvStep2Status.setTextColor(0xFFD99B82.toInt())
            }
            "Verified" -> {
                binding.dot2.setBackgroundColor(activeColor)
                binding.line1.setBackgroundColor(activeColor)
                binding.dot3.setBackgroundColor(activeColor)
                binding.line2.setBackgroundColor(activeColor)
                binding.tvStep2Title.setTextColor(textColorActive)
                binding.tvStep3Title.setTextColor(textColorActive)
                binding.tvStep2Status.text = "Completed"
                binding.tvStep3Status.text = "Verified"
            }
            "Approved", "Disbursed" -> {
                binding.dot2.setBackgroundColor(activeColor)
                binding.line1.setBackgroundColor(activeColor)
                binding.dot3.setBackgroundColor(activeColor)
                binding.line2.setBackgroundColor(activeColor)
                binding.dot4.setBackgroundColor(activeColor)
                binding.line3.setBackgroundColor(activeColor)
                binding.tvStep2Title.setTextColor(textColorActive)
                binding.tvStep3Title.setTextColor(textColorActive)
                binding.tvStep4Title.setTextColor(textColorActive)
                binding.tvStep4Status.text = "Payment Disbursed"
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val isUrdu = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getBoolean("USE_URDU", false)
            val voiceEnabled = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getBoolean("VOICE_ENABLED", true)
            
            if (voiceEnabled) {
                if (isUrdu) {
                    tts?.setLanguage(Locale("ur", "PK"))
                    tts?.speak("Aap ki application ki maaloomaat yahan mojood hain.", TextToSpeech.QUEUE_FLUSH, null, "TrackID")
                } else {
                    tts?.setLanguage(Locale.US)
                    tts?.speak("Your application status details are displayed here.", TextToSpeech.QUEUE_FLUSH, null, "TrackID")
                }
            }
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
