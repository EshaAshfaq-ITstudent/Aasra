package com.aasra.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class SettingsActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    
    private lateinit var dbHelper: DatabaseHelper
    private var tts: TextToSpeech? = null
    private var userCnic: String? = null
    private var isVoiceEnabled = true
    private lateinit var ivVoiceAssistant: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        dbHelper = DatabaseHelper(this)
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        isVoiceEnabled = sharedPref.getBoolean("VOICE_ENABLED", true)
        userCnic = sharedPref.getString("USER_CNIC", "")

        tts = TextToSpeech(this, this)
        
        ivVoiceAssistant = findViewById(R.id.ivVoiceAssistant)
        updateVoiceIcon()

        ivVoiceAssistant.setOnClickListener {
            isVoiceEnabled = !isVoiceEnabled
            sharedPref.edit().putBoolean("VOICE_ENABLED", isVoiceEnabled).apply()
            updateVoiceIcon()
            if (isVoiceEnabled) speakGuidance() else tts?.stop()
        }

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<View>(R.id.btnEditProfile).setOnClickListener {
            showEditProfileDialog()
        }

        findViewById<View>(R.id.btnChangePhone).setOnClickListener {
            showEditProfileDialog() // Reuse same logic for simplicity
        }

        findViewById<View>(R.id.btnPrivacy).setOnClickListener {
            showPrivacyDialog()
        }

        findViewById<View>(R.id.btnLegal).setOnClickListener {
            showLegalDialog()
        }

        findViewById<View>(R.id.btnLogout).setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Logout")
            builder.setMessage("Are you sure you want to log out?")
            builder.setPositiveButton("Logout") { _, _ ->
                sharedPref.edit().clear().apply()
                val intent = Intent(this, WelcomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            builder.setNegativeButton("Cancel", null)
            builder.show()
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

    private fun showEditProfileDialog() {
        if (userCnic.isNullOrEmpty()) return
        
        val user = dbHelper.getUserDetails(userCnic!!) ?: return
        
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null)
        val etName = dialogView.findViewById<EditText>(R.id.etEditName)
        val etPhone = dialogView.findViewById<EditText>(R.id.etEditPhone)
        
        etName.setText(user["name"])
        etPhone.setText(user["phone"])

        AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = etName.text.toString().trim()
                val newPhone = etPhone.text.toString().trim()
                
                if (newName.isNotEmpty() && newPhone.isNotEmpty()) {
                    if (dbHelper.updateUser(userCnic!!, newName, newPhone)) {
                        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                        sharedPref.edit().putString("USER_NAME", newName).apply()
                        Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPrivacyDialog() {
        AlertDialog.Builder(this)
            .setTitle("Privacy & Security")
            .setMessage("1. Data Encryption: All your documents are encrypted.\n\n" +
                        "2. Biometric Lock: You can enable fingerprint lock from your phone settings for this app.\n\n" +
                        "3. Privacy Policy: We do not share your CNIC or personal details with third-party services.")
            .setPositiveButton("I Understand", null)
            .show()
    }

    private fun showLegalDialog() {
        AlertDialog.Builder(this)
            .setTitle("Legal & Policies")
            .setMessage("Terms of Service:\nBy using Aasra, you agree to provide authentic information for pension processing.\n\n" +
                        "Government Policy:\nThis app complies with the latest pension regulations of Pakistan.\n\n" +
                        "Version: 1.0.5 (Stable)")
            .setPositiveButton("Accept", null)
            .show()
    }

    private fun showContactDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Contact Us")
        builder.setMessage("Email: support@aasra.gov.pk\nHelpline: 0800-AASRA (22772)\nWhatsApp: +92 300 1234567")
        builder.setPositiveButton("OK", null)
        builder.show()
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
            tts?.speak("Settings mein khush aamdeed. Aap yahan apni maloomat badal saktay hain.", TextToSpeech.QUEUE_FLUSH, null, "SettingsVoiceID")
        } else {
            tts?.setLanguage(Locale.US)
            tts?.speak("Welcome to Settings. You can update your profile or contact support here.", TextToSpeech.QUEUE_FLUSH, null, "SettingsVoiceID")
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