package com.example.mediplus.uii

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mediplus.R
import com.example.mediplus.uii.auth.LoginActivity
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

data class ChatMessage(val message: String, val isUser: Boolean)

class ChatbotActivity : AppCompatActivity() {
    private val apiKey = "AIzaSyC_OXkg5qHwUqx0ZOhBOqih0_lE8hx_qfk"
    private lateinit var rvChatHistory: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageView
    private lateinit var layoutWelcome: LinearLayout
    private lateinit var progressBar: ProgressBar

    // Tambahan variabel Profile
    private lateinit var ivProfile: ImageView

    private val chatList = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = apiKey,
            systemInstruction = content {
                text("Kamu adalah asisten medis SehatYuk. Jawablah pertanyaan hanya seputar kesehatan dengan singkat dan jelas. Tolak pertanyaan di luar topik kesehatan")
            }
        )
    }

    private lateinit var chatSession: com.google.ai.client.generativeai.Chat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        try {
            chatSession = generativeModel.startChat()
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal memulai AI: ${e.message}", Toast.LENGTH_LONG).show()
        }

        initViews()
        setupRecyclerView()
        setupBottomNav()

        // --- SETUP LISTENER ---
        btnSend.setOnClickListener {
            val message = etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
            } else {
                Toast.makeText(this, "Ketik sesuatu dulu ya", Toast.LENGTH_SHORT).show()
            }
        }

        // Klik Profile -> Muncul Menu
        ivProfile.setOnClickListener { view ->
            showProfileMenu(view)
        }
    }

    private fun initViews() {
        rvChatHistory = findViewById(R.id.rvChatHistory)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        layoutWelcome = findViewById(R.id.layoutWelcome)
        progressBar = findViewById(R.id.progressBar)
        // Inisialisasi ivProfile (sesuai ID di XML baru)
        ivProfile = findViewById(R.id.ivProfile)
    }

    // --- LOGIKA POPUP MENU ---
    private fun showProfileMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_profile, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.action_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(chatList)
        rvChatHistory.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        rvChatHistory.adapter = chatAdapter
    }

    private fun sendMessage(userMessage: String) {
        layoutWelcome.visibility = View.GONE
        rvChatHistory.visibility = View.VISIBLE

        chatList.add(ChatMessage(userMessage, true))
        chatAdapter.notifyItemInserted(chatList.size - 1)
        rvChatHistory.scrollToPosition(chatList.size - 1)

        etMessage.text.clear()
        progressBar.visibility = View.VISIBLE

        btnSend.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = chatSession.sendMessage(userMessage)
                val botReply = response.text ?: "Maaf, respon kosong."

                progressBar.visibility = View.GONE
                chatList.add(ChatMessage(botReply, false))
                chatAdapter.notifyItemInserted(chatList.size - 1)
                rvChatHistory.scrollToPosition(chatList.size - 1)

            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Log.e("MediPlusError", "Error Gemini: ${e.message}", e)

                val errorMessage = if (e.message?.contains("API key") == true) {
                    "API Key salah/tidak valid."
                } else {
                    "Gagal memuat balasan: ${e.localizedMessage}"
                }

                chatList.add(ChatMessage("⚠️ $errorMessage", false))
                chatAdapter.notifyItemInserted(chatList.size - 1)
                rvChatHistory.scrollToPosition(chatList.size - 1)
            } finally {
                btnSend.isEnabled = true
            }
        }
    }

    private fun setupBottomNav() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_chatbot
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_appointment -> {
                    startActivity(Intent(this, AppointmentActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_chatbot -> true
                else -> false
            }
        }
    }

    inner class ChatAdapter(private val messages: List<ChatMessage>) :
        RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

        inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvUser: TextView = view.findViewById(R.id.tvUserMessage)
            val tvBot: TextView = view.findViewById(R.id.tvBotMessage)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_bubble, parent, false)
            return ChatViewHolder(view)
        }

        override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
            val chat = messages[position]
            if (chat.isUser) {
                holder.tvUser.text = chat.message
                holder.tvUser.visibility = View.VISIBLE
                holder.tvBot.visibility = View.GONE
            } else {
                holder.tvBot.text = chat.message
                holder.tvBot.visibility = View.VISIBLE
                holder.tvUser.visibility = View.GONE
            }
        }

        override fun getItemCount() = messages.size
    }
}