package com.aasra.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class SignupActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var dbHelper: DatabaseHelper
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        dbHelper = DatabaseHelper(this)
        tts = TextToSpeech(this, this)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etCnic = findViewById<EditText>(R.id.etCnic)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)

        btnSignUp.setOnClickListener {
            val name = etFullName.text.toString().trim()
            val phoneInput = etPhone.text.toString().trim()
            val cnic = etCnic.text.toString().trim()

            if (name.isEmpty() || phoneInput.isEmpty() || cnic.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (phoneInput.length != 10) {
                Toast.makeText(this, "Phone number must be exactly 10 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cnic.length != 13) {
                Toast.makeText(this, "CNIC must be exactly 13 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fullPhone = "+92$phoneInput"

            val success = dbHelper.addUser(name, fullPhone, cnic)
            if (success) {
                Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Registration Failed or CNIC already exists", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val isVoiceEnabled = sharedPref.getBoolean("VOICE_ENABLED", true)
            if (!isVoiceEnabled) return

            val isUrdu = sharedPref.getBoolean("USE_URDU", false)
            if (isUrdu) {
                tts?.setLanguage(Locale("ur", "PK"))
                tts?.speak("Meherbaani karke apna naam, phone number aur tera hindson ka se en i si number darj karein.", TextToSpeech.QUEUE_FLUSH, null, "SignupID")
            } else {
                tts?.setLanguage(Locale.US)
                tts?.speak("Please enter your full name, phone number, and thirteen digit C N I C to create your account.", TextToSpeech.QUEUE_FLUSH, null, "SignupID")
            }
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
