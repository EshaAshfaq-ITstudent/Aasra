package com.aasra.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class DocumentVaultActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var userCnic: String? = null
    private val docList = mutableListOf<Map<String, String>>()
    private lateinit var adapter: DocumentAdapter
    private lateinit var rvDocuments: RecyclerView
    private lateinit var layoutEmpty: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_document_vault)

        dbHelper = DatabaseHelper(this)
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userCnic = sharedPref.getString("USER_CNIC", "")

        rvDocuments = findViewById(R.id.rvDocuments)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnAddDoc = findViewById<MaterialButton>(R.id.btnAddDoc)

        setupRecyclerView()
        loadDocuments()

        btnBack.setOnClickListener { finish() }

        val pickFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null && userCnic != null) {
                    saveDocument(uri)
                }
            }
        }

        btnAddDoc.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            val mimeTypes = arrayOf("image/*", "application/pdf")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            pickFileLauncher.launch(intent)
        }
    }

    private fun setupRecyclerView() {
        adapter = DocumentAdapter(docList)
        rvDocuments.layoutManager = LinearLayoutManager(this)
        rvDocuments.adapter = adapter
    }

    private fun loadDocuments() {
        if (userCnic == null) return
        docList.clear()
        docList.addAll(dbHelper.getDocuments(userCnic!!))
        adapter.notifyDataSetChanged()
        
        layoutEmpty.visibility = if (docList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun saveDocument(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            
            val name = uri.lastPathSegment ?: "Document_${System.currentTimeMillis()}"
            val type = contentResolver.getType(uri) ?: "unknown"
            
            val success = dbHelper.addDocument(userCnic!!, name, uri.toString(), type)
            if (success) {
                Toast.makeText(this, "Document saved to vault", Toast.LENGTH_SHORT).show()
                loadDocuments()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save document: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    class DocumentAdapter(private val items: List<Map<String, String>>) : RecyclerView.Adapter<DocumentAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.tvDocName)
            val subtitle: TextView = view.findViewById(R.id.tvDocType)
            val icon: ImageView = view.findViewById(R.id.ivDocIcon)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_document, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val type = item["type"] ?: ""
            holder.title.text = item["name"]
            holder.subtitle.text = type
            
            // Set icon based on type
            if (type.contains("image")) {
                // You might want to use a specific icon for images
                // holder.icon.setImageResource(R.drawable.ic_image) 
            } else if (type.contains("pdf")) {
                // holder.icon.setImageResource(R.drawable.ic_pdf)
            }

            holder.itemView.setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(Uri.parse(item["path"]), item["type"])
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    holder.itemView.context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(holder.itemView.context, "Cannot open file", Toast.LENGTH_SHORT).show()
                }
            }
        }

        override fun getItemCount() = items.size
    }
}