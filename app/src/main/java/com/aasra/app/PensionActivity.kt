package com.aasra.app

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.random.Random

class PensionActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var userCnic: String? = null
    
    private var cnicFrontUri: Uri? = null
    private var cnicBackUri: Uri? = null

    // Naya tareeka Gallery kholne ka
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
        userCnic = intent.getStringExtra("USER_CNIC")

        val etName = findViewById<EditText>(R.id.etPensionName)
        val etPhone = findViewById<EditText>(R.id.etPensionPhone)
        val tvDob = findViewById<TextView>(R.id.tvDob)
        val rgGender = findViewById<RadioGroup>(R.id.rgGender)
        val spinnerType = findViewById<Spinner>(R.id.spinnerPensionType)
        val etDept = findViewById<EditText>(R.id.etDepartment)
        val tvRetireDate = findViewById<TextView>(R.id.tvRetirementDate)
        val cbConfirm = findViewById<CheckBox>(R.id.cbConfirm)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitPension)

        // Date Pickers
        findViewById<LinearLayout>(R.id.btnSelectDob).setOnClickListener {
            showDatePicker { date -> tvDob.text = date }
        }

        findViewById<LinearLayout>(R.id.btnSelectRetirementDate).setOnClickListener {
            showDatePicker { date -> tvRetireDate.text = date }
        }
        
        // Gallery kholne ke liye click listeners
        findViewById<View>(R.id.btnUploadCnicFront).setOnClickListener {
            pickCnicFront.launch("image/*")
        }

        findViewById<View>(R.id.btnUploadCnicBack).setOnClickListener {
            pickCnicBack.launch("image/*")
        }

        // Back Button
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        btnSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val dob = tvDob.text.toString()
            val gender = if (rgGender.checkedRadioButtonId == R.id.rbMale) "Male" else "Female"
            val type = spinnerType.selectedItem.toString()
            val dept = etDept.text.toString().trim()
            val retireDate = tvRetireDate.text.toString()

            // Validations
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

            // ID Generate karein
            val appId = "ASR-" + Random.nextInt(100000, 999999).toString()
            
            val success = dbHelper.addPensionApplication(
                appId, userCnic ?: "", name, phone, dob, gender, type, dept, retireDate
            )

            if (success) {
                Toast.makeText(this, "Application Submitted! ID: $appId", Toast.LENGTH_LONG).show()
                finish() // Dashboard par wapas jayein
            } else {
                Toast.makeText(this, "Failed to submit application", Toast.LENGTH_SHORT).show()
            }
        }
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
