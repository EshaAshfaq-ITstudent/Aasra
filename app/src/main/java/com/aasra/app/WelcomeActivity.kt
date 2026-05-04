package com.aasra.app

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class WelcomeActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    
    private var tts: TextToSpeech? = null
    private lateinit var tvLangEng: TextView
    private lateinit var tvLangUrdu: TextView
    private lateinit var ivVoiceAssistant: ImageView
    private var isVoiceEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        isVoiceEnabled = sharedPref.getBoolean("VOICE_ENABLED", true)
        val isUrdu = sharedPref.getBoolean("USE_URDU", false)

        tts = TextToSpeech(this, this)
        
        tvLangEng = findViewById(R.id.tvLangEng)
        tvLangUrdu = findViewById(R.id.tvLangUrdu)
        ivVoiceAssistant = findViewById(R.id.ivVoiceAssistant)

        updateLanguageUI(isUrdu)
        updateVoiceIcon()

        tvLangEng.setOnClickListener {
            if (sharedPref.getBoolean("USE_URDU", false)) {
                setLanguage(false)
            }
        }

        tvLangUrdu.setOnClickListener {
            if (!sharedPref.getBoolean("USE_URDU", false)) {
                setLanguage(true)
            }
        }

        ivVoiceAssistant.setOnClickListener {
            isVoiceEnabled = !isVoiceEnabled
            sharedPref.edit().putBoolean("VOICE_ENABLED", isVoiceEnabled).apply()
            updateVoiceIcon()
            if (isVoiceEnabled) {
                speakWelcome(sharedPref.getBoolean("USE_URDU", false))
            } else {
                tts?.stop()
            }
        }

        val rootLayout = findViewById<android.view.View>(R.id.welcome_main)
        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        val btnGetStarted = findViewById<Button>(R.id.btnGetStarted)
        btnGetStarted?.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateVoiceIcon() {
        if (isVoiceEnabled) {
            ivVoiceAssistant.setImageResource(R.drawable.ic_volume_up)
        } else {
            ivVoiceAssistant.setImageResource(R.drawable.ic_volume_off)
        }
    }

    private fun setLanguage(isUrdu: Boolean) {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean("USE_URDU", isUrdu).apply()
        
        updateLanguageUI(isUrdu)
        if (isVoiceEnabled) {
            speakWelcome(isUrdu)
        }
    }

    private fun updateLanguageUI(isUrdu: Boolean) {
        if (isUrdu) {
            tvLangUrdu.setBackgroundResource(R.drawable.bg_lang_active)
            tvLangUrdu.setTextColor(Color.WHITE)
            
            tvLangEng.setBackgroundResource(0)
            tvLangEng.setTextColor(Color.BLACK)
        } else {
            tvLangEng.setBackgroundResource(R.drawable.bg_lang_active)
            tvLangEng.setTextColor(Color.WHITE)
            
            tvLangUrdu.setBackgroundResource(0)
            tvLangUrdu.setTextColor(Color.BLACK)
        }
    }

    private fun speakWelcome(isUrdu: Boolean) {
        if (!isVoiceEnabled) return
        
        if (isUrdu) {
            val result = tts?.setLanguage(Locale("ur", "PK"))
            if (result == TextToSpeech.LANG_AVAILABLE || result == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                tts?.speak("Aasra mein khush aamdeed. Shuru karne ke liye button par click karein.", TextToSpeech.QUEUE_FLUSH, null, "WelcomeID")
            } else {
                tts?.setLanguage(Locale.US)
                tts?.speak("Welcome to Aasra. Click on Get Started to begin your journey.", TextToSpeech.QUEUE_FLUSH, null, "WelcomeID")
            }
        } else {
            tts?.setLanguage(Locale.US)
            tts?.speak("Welcome to Aasra. Click on Get Started to begin your journey.", TextToSpeech.QUEUE_FLUSH, null, "WelcomeID")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            isVoiceEnabled = sharedPref.getBoolean("VOICE_ENABLED", true)
            val isUrdu = sharedPref.getBoolean("USE_URDU", false)
            if (isVoiceEnabled) {
                speakWelcome(isUrdu)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        isVoiceEnabled = sharedPref.getBoolean("VOICE_ENABLED", true)
        updateVoiceIcon()
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}