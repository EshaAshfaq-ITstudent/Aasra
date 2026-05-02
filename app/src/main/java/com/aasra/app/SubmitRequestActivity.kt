package com.aasra.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import java.util.Locale
import java.util.Random

class SubmitRequestActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var dbHelper: DatabaseHelper
    private var selectedIssue: String = ""
    private var attachmentUri: Uri? = null
    private var tts: TextToSpeech? = null
    private var userCnic: String? = null
    private var isVoiceEnabled = true
    private lateinit var ivVoiceAssistant: ImageView
    
    private lateinit var options: List<LinearLayout>
    private lateinit var checks: List<ImageView>
    private val issueTypes = listOf("Application Stuck", "Document Rejected", "Incorrect Information", "Other Issue")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_submit_request)
        
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        isVoiceEnabled = sharedPref.getBoolean("VOICE_ENABLED", true)
        userCnic = sharedPref.getString("USER_CNIC", "")

        dbHelper = DatabaseHelper(this)
        tts = TextToSpeech(this, this)
        
        ivVoiceAssistant = findViewById(R.id.ivVoiceAssistant)
        updateVoiceIcon()

        ivVoiceAssistant.setOnClickListener {
            isVoiceEnabled = !isVoiceEnabled
            sharedPref.edit().putBoolean("VOICE_ENABLED", isVoiceEnabled).apply()
            updateVoiceIcon()
            if (isVoiceEnabled) {
                speakPrompt()
            } else {
                tts?.stop()
            }
        }

        val mainView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        setupIssueSelection()

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        val etDescription = findViewById<EditText>(R.id.etDescription)
        val tvAttachmentName = findViewById<TextView>(R.id.tvAttachmentName)
        
        val pickFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                attachmentUri = result.data?.data
                tvAttachmentName.text = attachmentUri?.lastPathSegment ?: "File attached"
            }
        }

        findViewById<View>(R.id.btnAttach).setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            val mimeTypes = arrayOf("image/*", "application/pdf")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            pickFileLauncher.launch(intent)
        }

        findViewById<MaterialButton>(R.id.btnSubmit).setOnClickListener {
            val description = etDescription.text.toString().trim()
            
            if (selectedIssue.isEmpty()) {
                Toast.makeText(this, "Please select an issue type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (description.isEmpty()) {
                Toast.makeText(this, "Please describe your issue", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userCnic.isNullOrEmpty()) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ticketId = "ASR-" + (100000 + Random().nextInt(900000))
            val success = dbHelper.addTicket(
                ticketId, userCnic!!, selectedIssue, description, attachmentUri?.toString() ?: ""
            )

            if (success) {
                if (isVoiceEnabled) {
                    val isUrdu = sharedPref.getBoolean("USE_URDU", false)
                    if (isUrdu) {
                        tts?.setLanguage(Locale("ur", "PK"))
                        tts?.speak("Aap ki shikayat jama ho gayi hai. Ticket number hai $ticketId", TextToSpeech.QUEUE_FLUSH, null, "SubmitID")
                    } else {
                        tts?.setLanguage(Locale.US)
                        tts?.speak("Your support request has been submitted. Ticket I D is $ticketId", TextToSpeech.QUEUE_FLUSH, null, "SubmitID")
                    }
                }
                val intent = Intent(this, ComplaintSuccessActivity::class.java)
                intent.putExtra("TICKET_ID", ticketId)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Failed to submit request", Toast.LENGTH_SHORT).show()
            }
        }

        // Bottom Navigation
        findViewById<ImageView>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("USER_CNIC", userCnic)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        findViewById<ImageView>(R.id.navHelp).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }
        findViewById<ImageView>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun updateVoiceIcon() {
        if (isVoiceEnabled) {
            ivVoiceAssistant.setImageResource(R.drawable.ic_volume_up)
        } else {
            ivVoiceAssistant.setImageResource(R.drawable.ic_volume_off)
        }
    }

    private fun speakPrompt() {
        if (!isVoiceEnabled) return
        val isUrdu = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getBoolean("USE_URDU", false)
        if (isUrdu) {
            tts?.setLanguage(Locale("ur", "PK"))
            tts?.speak("Yahan aap shikayat jama kar saktay hain. Maslay ki qism muntakhib karein aur tafseel darj karein.", TextToSpeech.QUEUE_FLUSH, null, "RequestID")
        } else {
            tts?.setLanguage(Locale.US)
            tts?.speak("You can submit a support request here. Select the type of issue you are facing and provide a description.", TextToSpeech.QUEUE_FLUSH, null, "RequestID")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            if (isVoiceEnabled) {
                speakPrompt()
            }
        }
    }

    override fun onPause() {
        tts?.stop()
        super.onPause()
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    private fun setupIssueSelection() {
        options = listOf(
            findViewById(R.id.optionAppStuck),
            findViewById(R.id.optionDocRejected),
            findViewById(R.id.optionIncorrectInfo),
            findViewById(R.id.optionOtherIssue)
        )
        checks = listOf(
            findViewById(R.id.checkAppStuck),
            findViewById(R.id.checkDocRejected),
            findViewById(R.id.checkIncorrectInfo),
            findViewById(R.id.checkOtherIssue)
        )
        options.forEachIndexed { index, layout ->
            layout.setOnClickListener { updateSelection(index) }
        }
    }

    private fun updateSelection(selectedIndex: Int) {
        selectedIssue = issueTypes[selectedIndex]
        options.forEachIndexed { index, layout ->
            if (index == selectedIndex) {
                layout.setBackgroundResource(R.drawable.bg_issue_selected)
                checks[index].visibility = View.VISIBLE
            } else {
                layout.setBackgroundResource(R.drawable.bg_input_field)
                checks[index].visibility = View.GONE
            }
        }
    }
}