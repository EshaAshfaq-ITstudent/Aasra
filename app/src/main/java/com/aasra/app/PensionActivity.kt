package com.aasra.app

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.random.Random

class PensionActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var dbHelper: DatabaseHelper
    private var userCnic: String? = null
    private var tts: TextToSpeech? = null
    private lateinit var ivVoiceAssistant: ImageView
    private var isVoiceEnabled = true
    
    private var cnicFrontUri: Uri? = null
    private var cnicBackUri: Uri? = null

    private val pickCnicFront = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            cnicFrontUri = uri
            findViewById<TextView>(R.id.tvCnicFront).text = "CNIC Front Uploaded"
            findViewById<ImageView>(R.id.ivCnicFront).setImageResource(R.drawable.ic_check_circle)
            findViewById<ImageView>(R.id.ivCnicFront).setColorFilter(android.graphics.Color.parseColor("#00A651"))
        }
    }

    private val pickCnicBack = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            cnicBackUri = uri
            findViewById<TextView>(R.id.tvCnicBack).text = "CNIC Back Uploaded"
            findViewById<ImageView>(R.id.ivCnicBack).setImageResource(R.drawable.ic_check_circle)
            findViewById<ImageView>(R.id.ivCnicBack).setColorFilter(android.graphics.Color.parseColor("#00A651"))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pension)

        dbHelper = DatabaseHelper(this)
        tts = TextToSpeech(this, this)
        userCnic = intent.getStringExtra("USER_CNIC")

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        isVoiceEnabled = sharedPref.getBoolean("VOICE_ENABLED", true)
        
        ivVoiceAssistant = findViewById(R.id.ivVoiceAssistant)
        updateVoiceIcon()

        ivVoiceAssistant.setOnClickListener {
            isVoiceEnabled = !isVoiceEnabled
            sharedPref.edit().putBoolean("VOICE_ENABLED", isVoiceEnabled).apply()
            updateVoiceIcon()
            if (isVoiceEnabled) speakGuidance() else tts?.stop()
        }

        val etName = findViewById<EditText>(R.id.etPensionName)
        val etPhone = findViewById<EditText>(R.id.etPensionPhone)
        val tvDob = findViewById<TextView>(R.id.tvDob)
        val rgGender = findViewById<RadioGroup>(R.id.rgGender)
        val spinnerType = findViewById<Spinner>(R.id.spinnerPensionType)
        val etDept = findViewById<EditText>(R.id.etDepartment)
        val tvRetireDate = findViewById<TextView>(R.id.tvRetirementDate)
        val cbConfirm = findViewById<CheckBox>(R.id.cbConfirm)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitPension)

        findViewById<LinearLayout>(R.id.btnSelectDob).setOnClickListener {
            showDatePicker { date -> tvDob.text = date }
        }

        findViewById<LinearLayout>(R.id.btnSelectRetirementDate).setOnClickListener {
            showDatePicker { date -> tvRetireDate.text = date }
        }
        
        findViewById<View>(R.id.btnUploadCnicFront).setOnClickListener {
            pickCnicFront.launch("image/*")
        }

        findViewById<View>(R.id.btnUploadCnicBack).setOnClickListener {
            pickCnicBack.launch("image/*")
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        btnSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val dob = tvDob.text.toString()
            val gender = if (rgGender.checkedRadioButtonId == R.id.rbMale) "Male" else "Female"
            val type = spinnerType.selectedItem.toString()
            val dept = etDept.text.toString().trim()
            val retireDate = tvRetireDate.text.toString()

            if (name.isEmpty() || phone.isEmpty() || dept.isEmpty() || 
                dob == "Select Date of Birth" || retireDate == "Select Retirement Date" ||
                spinnerType.selectedItemPosition == 0) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (phone.length != 11) {
                Toast.makeText(this, "Phone number must be 11 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (cnicFrontUri == null || cnicBackUri == null) {
                Toast.makeText(this, "Please upload both sides of CNIC", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!cbConfirm.isChecked) {
                Toast.makeText(this, "Please confirm the information", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val appId = "ASR-" + Random.nextInt(100000, 999999).toString()
            
            val success = dbHelper.addPensionApplication(
                appId, userCnic ?: "", name, phone, dob, gender, type, dept, retireDate
            )

            if (success) {
                val isUrdu = sharedPref.getBoolean("USE_URDU", false)
                Toast.makeText(this, "Application Submitted! ID: $appId", Toast.LENGTH_LONG).show()

                if (isVoiceEnabled) {
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {}
                        override fun onDone(utteranceId: String?) {
                            runOnUiThread { finish() }
                        }
                        override fun onError(utteranceId: String?) {
                            runOnUiThread { finish() }
                        }
                    })

                    val params = Bundle()
                    params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "SuccessID")
                    
                    if (isUrdu) {
                        tts?.setLanguage(Locale("ur", "PK"))
                        tts?.speak("Aap ki application jama ho gayi hai. Application I D hai $appId", TextToSpeech.QUEUE_FLUSH, params, "SuccessID")
                    } else {
                        tts?.setLanguage(Locale.US)
                        tts?.speak("Your pension application has been submitted successfully. Your Application I D is $appId", TextToSpeech.QUEUE_FLUSH, params, "SuccessID")
                    }
                } else {
                    finish()
                }
            } else {
                Toast.makeText(this, "Failed to submit application", Toast.LENGTH_SHORT).show()
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
            tts?.speak("Meherbaani karke apni tafseelaat darj karein aur se en i si ki dono sides upload karein.", TextToSpeech.QUEUE_FLUSH, null, "PensionID")
        } else {
            tts?.setLanguage(Locale.US)
            tts?.speak("Please provide your personal details and upload both sides of your C N I C card.", TextToSpeech.QUEUE_FLUSH, null, "PensionID")
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

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            onDateSelected("$selectedDay/${selectedMonth + 1}/$selectedYear")
        }, year, month, day).show()
    }
}
