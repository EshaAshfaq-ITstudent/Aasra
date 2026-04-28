package com.aasra.app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PensionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pension)
        
        // Handling window insets for edge-to-edge
        val mainView = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Adjust padding to avoid system bars if necessary, 
            // but ScrollView usually handles content well.
            // For this layout, we might want to apply top padding to the root or top bar.
            insets
        }
        
        findViewById<android.view.View>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}