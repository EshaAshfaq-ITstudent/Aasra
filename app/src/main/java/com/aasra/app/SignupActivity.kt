package com.aasra.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Sign Up button par click karne se wapas Login page khulega
        findViewById<Button>(R.id.btnSignUp).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            // Clear current activity stack so it looks like a fresh start
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }
}