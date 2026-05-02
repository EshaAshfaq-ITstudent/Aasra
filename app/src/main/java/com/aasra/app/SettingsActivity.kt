package com.aasra.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class SettingsActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    
    private var tts: TextToSpeech? = null
    private var userCnic: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        tts = TextToSpeech(this, this)
        
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userCnic = sharedPref.getString("USER_CNIC", "")

        val mainView = findViewById<View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
                insets
            }
        }

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<View>(R.id.btnEditProfile).setOnClickListener {
            Toast.makeText(this, "Profile editing available soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.btnChangePhone).setOnClickListener {
            showChangePhoneDialog()
        }

        findViewById<View>(R.id.btnLogout).setOnClickListener {
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            sharedPref.edit().clear().apply()
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        findViewById<View>(R.id.btnHelpCentre).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        findViewById<View>(R.id.btnContactUs).setOnClickListener {
            showContactDialog()
        }

        findViewById<ImageView>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("USER_CNIC", userCnic)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.navHelp).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }
    }

    private fun showChangePhoneDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Change Phone Number")
        val input = android.widget.EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_PHONE
        builder.setView(input)
        builder.setPositiveButton("Update") { _, _ ->
            Toast.makeText(this, "Phone number updated", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showContactDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Contact Us")
        builder.setMessage("Email: support@aasra.com\nHelpline: 0800-12345")
        builder.setPositiveButton("OK", null)
        builder.show()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val isUrdu = sharedPref.getBoolean("USE_URDU", false)
            
            if (isUrdu) {
                tts?.setLanguage(Locale("ur", "PK"))
                tts?.speak("Settings mein khush aamdeed. Yahan aap apna phone number badal saktay hain.", TextToSpeech.QUEUE_FLUSH, null, "SettingsVoiceID")
            } else {
                tts?.setLanguage(Locale.US)
                "Welcome to Settings. Here you can change your phone number or contact support."
                tts?.speak("Welcome to Settings. Here you can change your phone number or contact support.", TextToSpeech.QUEUE_FLUSH, null, "SettingsVoiceID")
            }
        }
    }

    override fun onPause() {
        tts?.stop() // Stop speaking when leaving page
        super.onPause()
    }

    override fun onDestroy() {
        tts?.shutdown()
        super.onDestroy()
    }
}
