package com.aasra.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "AasraDB"
        private const val DATABASE_VERSION = 5 
        
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_CNIC = "cnic"

        private const val TABLE_PENSION = "pension_applications"
        private const val COLUMN_P_ID = "p_id"
        private const val COLUMN_APP_ID = "app_id"
        private const val COLUMN_P_USER_CNIC = "user_cnic"
        private const val COLUMN_P_NAME = "p_name"
        private const val COLUMN_P_PHONE = "p_phone"
        private const val COLUMN_P_DOB = "p_dob"
        private const val COLUMN_P_GENDER = "p_gender"
        private const val COLUMN_P_TYPE = "p_type"
        private const val COLUMN_P_DEPT = "p_dept"
        private const val COLUMN_P_RETIRE_DATE = "p_retire_date"
        private const val COLUMN_P_STATUS = "p_status"
        private const val COLUMN_P_TIMESTAMP = "p_timestamp"

        // Tickets Table
        private const val TABLE_TICKETS = "tickets"
        private const val COLUMN_T_ID = "t_id"
        private const val COLUMN_TICKET_ID = "ticket_id"
        private const val COLUMN_T_USER_CNIC = "user_cnic"
        private const val COLUMN_T_ISSUE_TYPE = "issue_type"
        private const val COLUMN_T_DESCRIPTION = "description"
        private const val COLUMN_T_ATTACHMENT = "attachment_path"
        private const val COLUMN_T_STATUS = "status"
        private const val COLUMN_T_TIMESTAMP = "timestamp"

        // Documents Table (Vault)
        private const val TABLE_DOCUMENTS = "documents"
        private const val COLUMN_D_ID = "d_id"
        private const val COLUMN_D_USER_CNIC = "user_cnic"
        private const val COLUMN_D_NAME = "doc_name"
        private const val COLUMN_D_PATH = "doc_path"
        private const val COLUMN_D_TYPE = "doc_type"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createUsersTable = ("CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_PHONE + " TEXT,"
                + COLUMN_CNIC + " TEXT UNIQUE" + ")")
        db?.execSQL(createUsersTable)

        val createPensionTable = ("CREATE TABLE " + TABLE_PENSION + "("
                + COLUMN_P_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_APP_ID + " TEXT,"
                + COLUMN_P_USER_CNIC + " TEXT,"
                + COLUMN_P_NAME + " TEXT,"
                + COLUMN_P_PHONE + " TEXT,"
                + COLUMN_P_DOB + " TEXT,"
                + COLUMN_P_GENDER + " TEXT,"
                + COLUMN_P_TYPE + " TEXT,"
                + COLUMN_P_DEPT + " TEXT,"
                + COLUMN_P_RETIRE_DATE + " TEXT,"
                + COLUMN_P_STATUS + " TEXT,"
                + COLUMN_P_TIMESTAMP + " INTEGER" + ")")
        db?.execSQL(createPensionTable)

        createTicketsTable(db)
        createDocumentsTable(db)
    }

    private fun createTicketsTable(db: SQLiteDatabase?) {
        val createTicketsTable = ("CREATE TABLE IF NOT EXISTS " + TABLE_TICKETS + "("
                + COLUMN_T_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TICKET_ID + " TEXT,"
                + COLUMN_T_USER_CNIC + " TEXT,"
                + COLUMN_T_ISSUE_TYPE + " TEXT,"
                + COLUMN_T_DESCRIPTION + " TEXT,"
                + COLUMN_T_ATTACHMENT + " TEXT,"
                + COLUMN_T_STATUS + " TEXT,"
                + COLUMN_T_TIMESTAMP + " INTEGER" + ")")
        db?.execSQL(createTicketsTable)
    }

    private fun createDocumentsTable(db: SQLiteDatabase?) {
        val createDocsTable = ("CREATE TABLE IF NOT EXISTS " + TABLE_DOCUMENTS + "("
                + COLUMN_D_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_D_USER_CNIC + " TEXT,"
                + COLUMN_D_NAME + " TEXT,"
                + COLUMN_D_PATH + " TEXT,"
                + COLUMN_D_TYPE + " TEXT" + ")")
        db?.execSQL(createDocsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db?.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_PENSION ($COLUMN_P_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_APP_ID TEXT, $COLUMN_P_USER_CNIC TEXT, $COLUMN_P_NAME TEXT, $COLUMN_P_PHONE TEXT, $COLUMN_P_DOB TEXT, $COLUMN_P_GENDER TEXT, $COLUMN_P_TYPE TEXT, $COLUMN_P_DEPT TEXT, $COLUMN_P_RETIRE_DATE TEXT, $COLUMN_P_STATUS TEXT)")
        }
        if (oldVersion < 3) {
            db?.execSQL("ALTER TABLE $TABLE_PENSION ADD COLUMN $COLUMN_P_TIMESTAMP INTEGER DEFAULT 0")
        }
        if (oldVersion < 4) {
            createTicketsTable(db)
        }
        if (oldVersion < 5) {
            createDocumentsTable(db)
        }
    }

    fun addUser(name: String, phone: String, cnic: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME, name)
        values.put(COLUMN_PHONE, phone)
        values.put(COLUMN_CNIC, cnic)
        val result = db.insert(TABLE_USERS, null, values)
        return result != -1L
    }

    fun checkUser(cnic: String): String? {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_USERS, arrayOf(COLUMN_NAME), "$COLUMN_CNIC=?", arrayOf(cnic), null, null, null)
        var name: String? = null
        if (cursor.moveToFirst()) name = cursor.getString(0)
        cursor.close()
        return name
    }

    fun addPensionApplication(appId: String, userCnic: String, name: String, phone: String, dob: String, gender: String, type: String, dept: String, retireDate: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_APP_ID, appId)
        values.put(COLUMN_P_USER_CNIC, userCnic)
        values.put(COLUMN_P_NAME, name)
        values.put(COLUMN_P_PHONE, phone)
        values.put(COLUMN_P_DOB, dob)
        values.put(COLUMN_P_GENDER, gender)
        values.put(COLUMN_P_TYPE, type)
        values.put(COLUMN_P_DEPT, dept)
        values.put(COLUMN_P_RETIRE_DATE, retireDate)
        values.put(COLUMN_P_STATUS, "Submitted")
        values.put(COLUMN_P_TIMESTAMP, System.currentTimeMillis())
        val result = db.insert(TABLE_PENSION, null, values)
        return result != -1L
    }

    fun getPensionApplication(cnic: String): Map<String, String>? {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_PENSION, arrayOf(COLUMN_APP_ID, COLUMN_P_STATUS, COLUMN_P_TIMESTAMP), "$COLUMN_P_USER_CNIC=?", arrayOf(cnic), null, null, "$COLUMN_P_ID DESC", "1")
        var appData: Map<String, String>? = null
        if (cursor.moveToFirst()) appData = mapOf("appId" to cursor.getString(0), "status" to cursor.getString(1), "timestamp" to cursor.getLong(2).toString())
        cursor.close()
        return appData
    }

    // Document Vault Methods
    fun addDocument(cnic: String, name: String, path: String, type: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_D_USER_CNIC, cnic)
        values.put(COLUMN_D_NAME, name)
        values.put(COLUMN_D_PATH, path)
        values.put(COLUMN_D_TYPE, type)
        return db.insert(TABLE_DOCUMENTS, null, values) != -1L
    }

    fun getDocuments(cnic: String): List<Map<String, String>> {
        val docs = mutableListOf<Map<String, String>>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_DOCUMENTS, null, "$COLUMN_D_USER_CNIC=?", arrayOf(cnic), null, null, null)
        if (cursor.moveToFirst()) {
            do {
                docs.add(mapOf(
                    "name" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_D_NAME)),
                    "path" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_D_PATH)),
                    "type" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_D_TYPE))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return docs
    }

    // Ticket Methods
    fun addTicket(ticketId: String, userCnic: String, issueType: String, description: String, attachment: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_TICKET_ID, ticketId)
        values.put(COLUMN_T_USER_CNIC, userCnic)
        values.put(COLUMN_T_ISSUE_TYPE, issueType)
        values.put(COLUMN_T_DESCRIPTION, description)
        values.put(COLUMN_T_ATTACHMENT, attachment)
        values.put(COLUMN_T_STATUS, "Received")
        values.put(COLUMN_T_TIMESTAMP, System.currentTimeMillis())
        val result = db.insert(TABLE_TICKETS, null, values)
        return result != -1L
    }

    fun getLatestTicket(cnic: String): Map<String, String>? {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_TICKETS, arrayOf(COLUMN_TICKET_ID, COLUMN_T_ISSUE_TYPE, COLUMN_T_STATUS, COLUMN_T_TIMESTAMP), "$COLUMN_T_USER_CNIC=?", arrayOf(cnic), null, null, "$COLUMN_T_ID DESC", "1")
        var ticketData: Map<String, String>? = null
        if (cursor.moveToFirst()) {
            ticketData = mapOf(
                "ticketId" to cursor.getString(0),
                "issueType" to cursor.getString(1),
                "status" to cursor.getString(2),
                "timestamp" to cursor.getLong(3).toString()
            )
        }
        cursor.close()
        return ticketData
    }

    fun getAllTickets(cnic: String): List<Map<String, String>> {
        val ticketList = mutableListOf<Map<String, String>>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_TICKETS, null, "$COLUMN_T_USER_CNIC=?", arrayOf(cnic), null, null, "$COLUMN_T_ID DESC")
        
        if (cursor.moveToFirst()) {
            do {
                val ticket = mapOf(
                    "ticketId" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TICKET_ID)),
                    "issueType" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_T_ISSUE_TYPE)),
                    "description" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_T_DESCRIPTION)),
                    "status" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_T_STATUS)),
                    "timestamp" to cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_T_TIMESTAMP)).toString()
                )
                ticketList.add(ticket)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return ticketList
    }

    fun updateTicketStatus(ticketId: String, newStatus: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_T_STATUS, newStatus)
        db.update(TABLE_TICKETS, values, "$COLUMN_TICKET_ID=?", arrayOf(ticketId))
    }
}
