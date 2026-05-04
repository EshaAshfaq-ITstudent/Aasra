package com.aasra.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var dbHelper: DatabaseHelper
    private var userCnic: String? = null
    private var tts: TextToSpeech? = null
    private var userName: String = "User"
    private lateinit var ivVoiceAssistant: ImageView
    private var isVoiceEnabled = true
    
    private lateinit var hsvActions: HorizontalScrollView
    private val scrollHandler = Handler(Looper.getMainLooper())
    private var scrollDirection = 1 
    private var isUserInteracting = false
    private var lastInteractionTime: Long = 0

    private lateinit var rvHistory: RecyclerView
    private var historyList = mutableListOf<Map<String, String>>()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        isVoiceEnabled = sharedPref.getBoolean("VOICE_ENABLED", true)

        dbHelper = DatabaseHelper(this)
        tts = TextToSpeech(this, this)
        
        userName = intent.getStringExtra("USER_NAME") ?: sharedPref.getString("USER_NAME", "User") ?: "User"
        userCnic = intent.getStringExtra("USER_CNIC") ?: sharedPref.getString("USER_CNIC", "")
        
        findViewById<TextView>(R.id.tvHelloName).text = "Hello, $userName"
        ivVoiceAssistant = findViewById(R.id.ivVoiceAssistant)
        hsvActions = findViewById(R.id.hsvActions)
        
        // Setup History RecyclerView
        rvHistory = findViewById(R.id.rvApplicationHistory)
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.isNestedScrollingEnabled = false
        
        updateApplicationStatus()
        updateVoiceIcon()
        
        // Start smooth auto-scroll for all 4 cards
        startAutoScroll()

        // Touch listener for auto-scroll management
        val touchListener = View.OnTouchListener { v, event ->
            when (event.actionMasked) {

                MotionEvent.ACTION_DOWN -> {
                    isUserInteracting = true
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    isUserInteracting = false
                }
            }
            false // IMPORTANT: allow user scrolling
        }

        hsvActions.setOnTouchListener(touchListener)
        
        findViewById<MaterialCardView>(R.id.cardApplyPension).setOnTouchListener(touchListener)
        findViewById<MaterialCardView>(R.id.cardTrackApp).setOnTouchListener(touchListener)
        findViewById<MaterialCardView>(R.id.cardVault).setOnTouchListener(touchListener)
        findViewById<MaterialCardView>(R.id.cardComplaint).setOnTouchListener(touchListener)



        // Navigation Search Logic
        findViewById<ImageView>(R.id.navSearch).setOnClickListener {
            // Opening Chatbot as it provides assistant/search features
            Toast.makeText(this, "Opening Aasra Assistant...", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, ChatbotActivity::class.java))
        }

        ivVoiceAssistant.setOnClickListener {
            isVoiceEnabled = !isVoiceEnabled
            sharedPref.edit().putBoolean("VOICE_ENABLED", isVoiceEnabled).apply()
            updateVoiceIcon()
            if (isVoiceEnabled) speakGuidance() else tts?.stop()
        }

        findViewById<MaterialCardView>(R.id.cardApplyPension).setOnClickListener {
            val intent = Intent(this, PensionActivity::class.java)
            intent.putExtra("USER_CNIC", userCnic)
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.cardTrackApp).setOnClickListener {
            val intent = Intent(this, TrackApplicationActivity::class.java)
            intent.putExtra("USER_CNIC", userCnic)
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.cardVault).setOnClickListener {
            startActivity(Intent(this, DocumentVaultActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardComplaint).setOnClickListener {
            startActivity(Intent(this, SubmitRequestActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardRecentUpdates).setOnClickListener {
            val appData = userCnic?.let { dbHelper.getPensionApplication(it) }
            if (appData != null) {
                val intent = Intent(this, TrackApplicationActivity::class.java)
                intent.putExtra("USER_CNIC", userCnic)
                intent.putExtra("APP_ID", appData["appId"])
                startActivity(intent)
            } else {
                val intent = Intent(this, PensionActivity::class.java)
                intent.putExtra("USER_CNIC", userCnic)
                startActivity(intent)
            }
        }

        // FAQ Setup
        setupFaq(R.id.faqItem1, R.id.tvAnswer1, R.id.ivExpand1)
        setupFaq(R.id.faqItem2, R.id.tvAnswer2, R.id.ivExpand2)
        setupFaq(R.id.faqItem3, R.id.tvAnswer3, R.id.ivExpand3)
        setupFaq(R.id.faqItem4, R.id.tvAnswer4, R.id.ivExpand4)
        setupFaq(R.id.faqItem5, R.id.tvAnswer5, R.id.ivExpand5)

        findViewById<ImageView>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<ImageView>(R.id.navHelp).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }
        findViewById<ImageView>(R.id.navHome).setOnClickListener {
            if (isVoiceEnabled) speakGuidance()
        }
    }

    private fun setupFaq(layoutId: Int, answerId: Int, iconId: Int) {
        val layout = findViewById<View>(layoutId) ?: return
        val answer = findViewById<TextView>(answerId)
        val icon = findViewById<ImageView>(iconId)
        layout.setOnClickListener {
            if (answer.visibility == View.GONE) {
                answer.visibility = View.VISIBLE
                icon.setImageResource(R.drawable.ic_remove)
            } else {
                answer.visibility = View.GONE
                icon.setImageResource(R.drawable.ic_add)
            }
        }
    }

    private var autoScrollRunnable: Runnable? = null

    private fun startAutoScroll() {

        // stop previous runnable safely
        autoScrollRunnable?.let {
            scrollHandler.removeCallbacks(it)
        }

        autoScrollRunnable = object : Runnable {
            override fun run() {
                if (isFinishing) return

                val container = hsvActions.getChildAt(0) ?: return
                val maxScroll = container.width - hsvActions.width

                if (maxScroll > 0) {

                    val speed = if (isUserInteracting) 1 else 3

                    hsvActions.scrollBy(scrollDirection * speed, 0)

                    if (hsvActions.scrollX >= maxScroll) {
                        scrollDirection = -1
                    } else if (hsvActions.scrollX <= 0) {
                        scrollDirection = 1
                    }
                }

                scrollHandler.postDelayed(this, 16)
            }
        }

        scrollHandler.post(autoScrollRunnable!!)
    }

    private fun updateVoiceIcon() {
        ivVoiceAssistant.setImageResource(if (isVoiceEnabled) R.drawable.ic_volume_up else R.drawable.ic_volume_off)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS && isVoiceEnabled) speakGuidance()
    }

    private fun speakGuidance() {
        if (!isVoiceEnabled) return
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isUrdu = sharedPref.getBoolean("USE_URDU", false)
        if (isUrdu) {
            tts?.setLanguage(Locale("ur", "PK"))
            tts?.speak("Khush aamdeed. Ye aap ka dashboard hai.", TextToSpeech.QUEUE_FLUSH, null, "DashboardID")
        } else {
            tts?.setLanguage(Locale.US)
            tts?.speak("Welcome to your dashboard. You can apply for pension or track your status here.", TextToSpeech.QUEUE_FLUSH, null, "DashboardID")
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        isVoiceEnabled = sharedPref.getBoolean("VOICE_ENABLED", true)
        updateVoiceIcon()
        updateApplicationStatus()
    }

    override fun onPause() { 
        tts?.stop()
        super.onPause() 
    }
    
    override fun onDestroy() { 
        scrollHandler.removeCallbacksAndMessages(null)
        tts?.shutdown()
        super.onDestroy() 
    }

    private fun updateApplicationStatus() {
        val tvUpdateTitle = findViewById<TextView>(R.id.tvUpdateTitle)
        val tvUpdateId = findViewById<TextView>(R.id.tvUpdateId)
        val tvHistoryHeader = findViewById<TextView>(R.id.tvHistoryHeader)
        
        val latestApp = userCnic?.let { dbHelper.getPensionApplication(it) }
        if (latestApp != null) {
            tvUpdateTitle.text = "Pension Application"
            tvUpdateId.text = "#${latestApp["appId"]} - ${latestApp["status"]}"
        } else {
            tvUpdateTitle.text = "No current application"
            tvUpdateId.text = "Apply for pension to see status here."
        }

        userCnic?.let { cnic ->
            val allApps = dbHelper.getAllPensionApplications(cnic)
            if (allApps.isNotEmpty()) {
                tvHistoryHeader.visibility = View.VISIBLE
                rvHistory.visibility = View.VISIBLE
                historyList.clear()
                historyList.addAll(allApps)
                rvHistory.adapter = HistoryAdapter(historyList) { appId ->
                    val intent = Intent(this, TrackApplicationActivity::class.java)
                    intent.putExtra("USER_CNIC", cnic)
                    intent.putExtra("APP_ID", appId)
                    startActivity(intent)
                }
            } else {
                tvHistoryHeader.visibility = View.GONE
                rvHistory.visibility = View.GONE
            }
        }
    }

    class HistoryAdapter(
        private val list: List<Map<String, String>>,
        private val onItemClick: (String) -> Unit
    ) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvType: TextView = view.findViewById(R.id.tvAppType)
            val tvStatus: TextView = view.findViewById(R.id.tvAppStatus)
            val tvId: TextView = view.findViewById(R.id.tvAppId)
            val tvDate: TextView = view.findViewById(R.id.tvAppDate)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_application_history, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = list[position]
            holder.tvType.text = app["type"] ?: "Service Pension"
            holder.tvStatus.text = app["status"]
            holder.tvId.text = "#${app["appId"]}"
            
            val timestamp = app["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            holder.tvDate.text = "Submitted on ${sdf.format(Date(timestamp))}"

            holder.itemView.setOnClickListener {
                onItemClick(app["appId"] ?: "")
            }
        }

        override fun getItemCount() = list.size
    }
}
