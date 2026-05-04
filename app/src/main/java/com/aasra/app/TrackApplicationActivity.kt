package com.aasra.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.aasra.app.databinding.ActivityTrackApplicationBinding
import java.text.SimpleDateFormat
import java.util.*

class TrackApplicationActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityTrackApplicationBinding
    private lateinit var dbHelper: DatabaseHelper
    private var userCnic: String? = null
    private var tts: TextToSpeech? = null
    private var isVoiceEnabled = true
    private lateinit var ivVoiceAssistant: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackApplicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        isVoiceEnabled = sharedPref.getBoolean("VOICE_ENABLED", true)

        dbHelper = DatabaseHelper(this)
        tts = TextToSpeech(this, this)
        
        ivVoiceAssistant = findViewById(R.id.ivVoiceAssistant)
        updateVoiceIcon()

        ivVoiceAssistant.setOnClickListener {
            isVoiceEnabled = !isVoiceEnabled
            sharedPref.edit().putBoolean("VOICE_ENABLED", isVoiceEnabled).apply()
            updateVoiceIcon()
            if (isVoiceEnabled) speakGuidance() else tts?.stop()
        }

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
            tts?.speak("Aap ki application ki maaloomaat yahan mojood hain.", TextToSpeech.QUEUE_FLUSH, null, "TrackID")
        } else {
            tts?.setLanguage(Locale.US)
            tts?.speak("Your application status details are displayed here.", TextToSpeech.QUEUE_FLUSH, null, "TrackID")
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        isVoiceEnabled = sharedPref.getBoolean("VOICE_ENABLED", true)
        updateVoiceIcon()
    }

    override fun onPause() {
        tts?.stop()
        super.onPause()
    }

    override fun onDestroy() {
        tts?.shutdown()
        super.onDestroy()
    }
}