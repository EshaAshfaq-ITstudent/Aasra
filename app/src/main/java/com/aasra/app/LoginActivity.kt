package com.aasra.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class LoginActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var dbHelper: DatabaseHelper
    private var tts: TextToSpeech? = null
    private lateinit var ivVoiceAssistant: ImageView
    private var isVoiceEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        isVoiceEnabled = sharedPref.getBoolean("VOICE_ENABLED", true)

        dbHelper = DatabaseHelper(this)
        tts = TextToSpeech(this, this)

        val etCnic = findViewById<EditText>(R.id.etCnic)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvClickHere = findViewById<TextView>(R.id.tvClickHere)
        ivVoiceAssistant = findViewById(R.id.ivVoiceAssistant)

        updateVoiceIcon()

        ivVoiceAssistant.setOnClickListener {
            isVoiceEnabled = !isVoiceEnabled
            sharedPref.edit().putBoolean("VOICE_ENABLED", isVoiceEnabled).apply()
            updateVoiceIcon()
            if (isVoiceEnabled) {
                speakLoginPrompt()
            } else {
                tts?.stop()
            }
        }

        btnLogin.setOnClickListener {
            val cnic = etCnic.text.toString().trim()
            if (cnic.isEmpty() || cnic.length != 13) {
                Toast.makeText(this, "Please enter valid 13-digit CNIC", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userName = dbHelper.checkUser(cnic)
            if (userName != null) {
                val editor = sharedPref.edit()
                editor.putString("USER_CNIC", cnic)
                editor.putString("USER_NAME", userName)
                editor.apply()
                
                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("USER_NAME", userName) 
                intent.putExtra("USER_CNIC", cnic) 
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                Toast.makeText(this, "Invalid CNIC. Please Sign Up first.", Toast.LENGTH_SHORT).show()
            }
        }

        tvClickHere.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun updateVoiceIcon() {
        if (isVoiceEnabled) {
            ivVoiceAssistant.setImageResource(R.drawable.ic_volume_up)
        } else {
            ivVoiceAssistant.setImageResource(R.drawable.ic_volume_off)
        }
    }

    private fun speakLoginPrompt() {
        if (!isVoiceEnabled) return
        
        val isUrdu = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getBoolean("USE_URDU", false)
        if (isUrdu) {
            tts?.setLanguage(Locale("ur", "PK"))
            tts?.speak("Apna tera hindson ka se en i si number darj karein.", TextToSpeech.QUEUE_FLUSH, null, "LoginID")
        } else {
            tts?.setLanguage(Locale.US)
            tts?.speak("Please enter your thirteen digit C N I C number to login.", TextToSpeech.QUEUE_FLUSH, null, "LoginID")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Re-read preference to be safe
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            isVoiceEnabled = sharedPref.getBoolean("VOICE_ENABLED", true)
            if (isVoiceEnabled) {
                speakLoginPrompt()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Sync voice setting when returning to activity
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