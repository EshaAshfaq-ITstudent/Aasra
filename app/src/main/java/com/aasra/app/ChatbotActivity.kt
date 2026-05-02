package com.aasra.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ChatbotActivity : AppCompatActivity() {

    private lateinit var rvChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: FloatingActionButton
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        rvChat = findViewById(R.id.rvChat)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)

        adapter = ChatAdapter(messages)
        rvChat.layoutManager = LinearLayoutManager(this)
        rvChat.adapter = adapter

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        addMessage("Bot", "Hello! I am your Aasra Assistant. I can guide you on how to use the app, track applications, or submit complaints. You can ask me 'What is this app for?' or 'How to apply?'.")

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                addMessage("User", text)
                etMessage.setText("")
                generateBotResponse(text)
            }
        }
    }

    private fun addMessage(sender: String, message: String) {
        messages.add(ChatMessage(sender, message))
        adapter.notifyItemInserted(messages.size - 1)
        rvChat.scrollToPosition(messages.size - 1)
    }

    private fun generateBotResponse(userText: String) {
        val query = userText.lowercase()
        val response = when {
            query.contains("app for") || query.contains("purpose") || query.contains("kya hai") -> 
                "Aasra is a pension support application designed to help senior citizens apply for their pension easily, track its status, and resolve issues through support tickets."
            
            query.contains("stuck") || query.contains("contact") || query.contains("help") || query.contains("raabta") -> 
                "If you are stuck or need human assistance, you can contact our helpline at 0800-12345 or email us at support@aasra.com. You can also visit our Help Center in the app."
            
            query.contains("how to apply") || query.contains("pension apply") -> 
                "To apply for pension:\n1. Open the Dashboard.\n2. Click on the 'Apply for Pension' card.\n3. Fill in your details and upload your CNIC images.\n4. Click Submit."
            
            query.contains("track") || query.contains("status") -> 
                "You can track your pension application from the 'Track Application' card on the Dashboard. For complaints, go to Help > Track Ticket."
            
            query.contains("document") || query.contains("required") -> 
                "You will need your 13-digit CNIC card (front and back), Retirement Order, and Service details for the application."
            
            query.contains("hello") || query.contains("hi") || query.contains("hey") -> 
                "Hi there! I'm your Aasra guide. How can I help you with your pension or support today?"

            query.contains("thank") || query.contains("shukriya") -> 
                "You're very welcome! I'm always here if you need more help."

            else -> "I'm sorry, I'm still learning. You can ask about 'app purpose', 'how to apply', 'helpline number', or 'tracking status'."
        }
        
        rvChat.postDelayed({
            addMessage("Bot", response)
        }, 800)
    }

    data class ChatMessage(val sender: String, val message: String)

    class ChatAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

        class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val layoutBot: View = view.findViewById(R.id.layoutBot)
            val layoutUser: View = view.findViewById(R.id.layoutUser)
            val tvBot: TextView = view.findViewById(R.id.tvBotMessage)
            val tvUser: TextView = view.findViewById(R.id.tvUserMessage)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
            return ChatViewHolder(view)
        }

        override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
            val msg = messages[position]
            if (msg.sender == "Bot") {
                holder.layoutBot.visibility = View.VISIBLE
                holder.layoutUser.visibility = View.GONE
                holder.tvBot.text = msg.message
            } else {
                holder.layoutBot.visibility = View.GONE
                holder.layoutUser.visibility = View.VISIBLE
                holder.tvUser.text = msg.message
            }
        }

        override fun getItemCount() = messages.size
    }
}
