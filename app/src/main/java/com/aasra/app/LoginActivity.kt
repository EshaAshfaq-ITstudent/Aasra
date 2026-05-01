package com.aasra.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dbHelper = DatabaseHelper(this)

        val etCnic = findViewById<EditText>(R.id.etCnic)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvClickHere = findViewById<TextView>(R.id.tvClickHere)

        btnLogin.setOnClickListener {
            val cnic = etCnic.text.toString().trim()

            if (cnic.isEmpty()) {
                Toast.makeText(this, "Please enter your CNIC", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cnic.length != 13) {
                Toast.makeText(this, "CNIC must be exactly 13 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Database se user name check karein
            val userName = dbHelper.checkUser(cnic)

            if (userName != null) {
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("USER_NAME", userName) 
                intent.putExtra("USER_CNIC", cnic) // Pass CNIC to Dashboard
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
}
