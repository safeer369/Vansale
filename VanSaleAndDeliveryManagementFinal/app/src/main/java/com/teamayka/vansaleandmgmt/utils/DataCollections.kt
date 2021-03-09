package com.teamayka.vansaleandmgmt.utils

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.teamayka.vansaleandmgmt.ui.filedforce.models.RvListModel
import com.teamayka.vansaleandmgmt.ui.main.models.CustomerModel
import com.teamayka.vansaleandmgmt.ui.main.models.ProductModel
import com.teamayka.vansaleandmgmt.ui.main.models.SupplierModel
import com.teamayka.vansaleandmgmt.ui.main.models.UserModel
import com.teamayka.vansaleandmgmt.ui.purchase.models.PurchaseInvoiceModel
import com.teamayka.vansaleandmgmt.ui.purchasereturn.models.PurchaseReturnInvoiceModel
import com.teamayka.vansaleandmgmt.ui.sales.models.SalesInvoiceModel
import com.teamayka.vansaleandmgmt.ui.salesorder.models.SalesOrderInvoiceModel
import com.teamayka.vansaleandmgmt.ui.salesreturn.models.SalesReturnInvoiceModel

/**
 * Created by User on 24-02-2018.
 */
class DataCollections(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "db1_data"
        private const val DATABASE_VERSION = 1

        private const val TABLE_NAME_USER = "tb1_user"
        const val TABLE_NAME_PRODUCTS = "tb1_products"
        const val TABLE_NAME_CUSTOMERS = "tb1_customers"
        const val TABLE_NAME_SUPPLIERS = "tb1_suppliers"
        const val TABLE_NAME_SALES_INVOICE = "tb1_sales_invoice"
        const val TABLE_NAME_SALES_ORDER_INVOICE = "tb1_sales_order_invoice"
        const val TABLE_NAME_PURCHASE_INVOICE = "tb1_purchase_invoice"
        const val TABLE_NAME_PURCHASE_RETURN_INVOICE = "tb1_purchase_return_invoice"
        const val TABLE_NAME_SALES_RETURN_INVOICE = "tb1_sales_return_invoice"
        const val TABLE_NAME_IN_PROGRESS_LIST = "tb1_in_progress_list"
        const val TABLE_NAME_FAILED_LIST = "tb1_failed_list"
        const val TABLE_NAME_FCM_ID_UPDATE_STATUS = "tb1_fcm_id_update_status"

        private var database: DataCollections? = null

        fun getInstance(context: Context): DataCollections {
            if (database == null)
                database = DataCollections(context)
            return database as DataCollections
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_NAME_USER (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "_user_id VARCHAR(500)," +
                "_user_role VARCHAR(2)," +
                "_user_name VARCHAR(500)," +
                "_image_url VARCHAR(3000)," +
                "_amount_type INTEGER," +
                "_header VARCHAR(5000)," +
                "_footer VARCHAR(5000))")
        db?.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_NAME_PRODUCTS (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "_product_id VARCHAR(500)," +
                "_product_code VARCHAR(500)," +
                "_product_name VARCHAR(5000)," +
                "_display_name VARCHAR(500)," +
                "_unit VARCHAR(500)," +
                "_unit_id VARCHAR(500)," +
                "_c_gst VARCHAR(500)," +
                "_s_gst VARCHAR(500)," +
                "_rate_1 VARCHAR(500)," +
                "_rate_2 VARCHAR(500)," +
                "_rate_3 VARCHAR(500)," +
                "_tax_inclusive VARCHAR(10)," +
                "_image_url VARCHAR(3000)" +
                ")")
        db?.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_NAME_CUSTOMERS (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "_customer_id VARCHAR(500)," +
                "_code VARCHAR(500)," +
                "_name VARCHAR(5000)," +
                "_address1 VARCHAR(500)," +
                "_city VARCHAR(500)," +
                "_telephone VARCHAR(500)," +
                "_rate_plan VARCHAR(500)" +
                ")")
        db?.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_NAME_SUPPLIERS (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "_supplier_id VARCHAR(500)," +
                "_code VARCHAR(500)," +
                "_name VARCHAR(5000)," +
                "_address1 VARCHAR(500)," +
                "_city VARCHAR(500)," +
                "_telephone VARCHAR(500)," +
                "_rate_plan VARCHAR(500)" +
                ")")
        db?.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_NAME_SALES_INVOICE (_id INTEGER PRIMARY KEY AUTOINCREMENT,_invoice_id VARCHAR(100),_invoice_no VARCHAR(1000),_invoice_status VARCHAR(1))")
        db?.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_NAME_SALES_ORDER_INVOICE (_id INTEGER PRIMARY KEY AUTOINCREMENT,_invoice_id VARCHAR(100),_invoice_no VARCHAR(1000),_invoice_status VARCHAR(1))")
        db?.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_NAME_PURCHASE_INVOICE (_id INTEGER PRIMARY KEY AUTOINCREMENT,_invoice_id VARCHAR(100),_invoice_no VARCHAR(1000),_invoice_status VARCHAR(1))")
        db?.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_NAME_SALES_RETURN_INVOICE (_id INTEGER PRIMARY KEY AUTOINCREMENT,_invoice_id VARCHAR(100),_invoice_no VARCHAR(1000),_invoice_status VARCHAR(1))")
        db?.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_NAME_PURCHASE_RETURN_INVOICE (_id INTEGER PRIMARY KEY AUTOINCREMENT,_invoice_id VARCHAR(100),_invoice_no VARCHAR(1000),_invoice_status VARCHAR(1))")
        db?.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_NAME_FCM_ID_UPDATE_STATUS (_id INTEGER PRIMARY KEY AUTOINCREMENT,_fcm_id VARCHAR(5000),_sync_status INTEGER)")
        db?.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_NAME_IN_PROGRESS_LIST (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "_ticket_id_no VARCHAR(1000)," +
                "_ticket_id VARCHAR(1000)," +
                "_status_id VARCHAR(1000)," +
                "_service_type VARCHAR(1000)," +
                "_service_name VARCHAR(1000)," +
                "_customer VARCHAR(1000)," +
                "_phone_1 VARCHAR(1000)," +
                "_phone_2 VARCHAR(1000)," +
                "_address VARCHAR(5000)," +
                "_ticket_description VARCHAR(5000)," +
                "_order_info VARCHAR(5000)," +
                "_longitude VARCHAR(500)," +
                "_latitude VARCHAR(500)," +
                "_remarks VARCHAR(5000)," +
                "_received_by VARCHAR(5000)," +
                "_time VARCHAR(50)" +
                ")")
        db?.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_NAME_FAILED_LIST (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "_ticket_id_no VARCHAR(1000)," +
                "_ticket_id VARCHAR(1000)," +
                "_status_id VARCHAR(1000)," +
                "_service_type VARCHAR(1000)," +
                "_service_name VARCHAR(1000)," +
                "_customer VARCHAR(1000)," +
                "_phone_1 VARCHAR(1000)," +
                "_phone_2 VARCHAR(1000)," +
                "_address VARCHAR(5000)," +
                "_ticket_description VARCHAR(5000)," +
                "_order_info VARCHAR(5000)," +
                "_longitude VARCHAR(500)," +
                "_latitude VARCHAR(500)," +
                "_remarks VARCHAR(5000)," +
                "_received_by VARCHAR(5000)," +
                "_time VARCHAR(5000)" +
                ")")
    }

    fun setUser(userId: String, userRole: String, userName: String, imageUrl: String, amountType: Int, header: String, footer: String) {
        val db = writableDatabase

        db.execSQL("DELETE FROM $TABLE_NAME_USER")

        val values = ContentValues()
        values.put("_user_id", userId)
        values.put("_user_role", userRole)
        values.put("_user_name", userName)
        values.put("_image_url", imageUrl)
        values.put("_amount_type", amountType)
        values.put("_header", header)
        values.put("_footer", footer)

        db.insert(TABLE_NAME_USER, null, values)
    }

    fun clearUserTable() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME_USER")
    }

    fun getUser(): UserModel? {
        val db = readableDatabase
        val c = db.rawQuery("SELECT * FROM $TABLE_NAME_USER", null)
        val user = if (c.moveToFirst()) {
            UserModel(c.getString(c.getColumnIndex("_user_id")),
                    c.getString(c.getColumnIndex("_user_role")),
                    c.getString(c.getColumnIndex("_user_name")),
                    c.getString(c.getColumnIndex("_image_url")),
                    c.getString(c.getColumnIndex("_header")),
                    c.getString(c.getColumnIndex("_footer")),
                    c.getInt(c.getColumnIndex("_amount_type")))
        } else {
            null
        }
        c.close()
        return user
    }

    fun addProduct(productId: String,
                   productCode: String,
                   productName: String,
                   displayName: String,
                   unit: String,
                   unitId: String,
                   cGST: String,
                   sGST: String,
                   rate1: String,
                   rate2: String,
                   rate3: String,
                   taxInclusive: String,
                   imageUrl: String) {
        val db = writableDatabase

        val values = ContentValues()
        values.put("_product_id", productId)
        values.put("_product_code", productCode)
        values.put("_product_name", productName)
        values.put("_display_name", displayName)
        values.put("_unit", unit)
        values.put("_unit_id", unitId)
        values.put("_c_gst", cGST)
        values.put("_s_gst", sGST)
        values.put("_rate_1", rate1)
        values.put("_rate_2", rate2)
        values.put("_rate_3", rate3)
        values.put("_tax_inclusive", taxInclusive)
        values.put("_image_url", imageUrl)

        db.insert(TABLE_NAME_PRODUCTS, null, values)
    }

    fun getProducts(): ArrayList<ProductModel> {
        val db = readableDatabase
        val c = db.rawQuery("SELECT * FROM $TABLE_NAME_PRODUCTS", null)
        val list = ArrayList<ProductModel>()
        while (c.moveToNext()) {
            list.add(ProductModel(
                    c.getString(c.getColumnIndex("_product_id")),
                    c.getString(c.getColumnIndex("_product_code")),
                    c.getString(c.getColumnIndex("_product_name")),
                    c.getString(c.getColumnIndex("_display_name")),
                    c.getString(c.getColumnIndex("_unit")),
                    c.getString(c.getColumnIndex("_unit_id")),
                    c.getString(c.getColumnIndex("_c_gst")).toDouble(),
                    c.getString(c.getColumnIndex("_s_gst")).toDouble(),
                    c.getString(c.getColumnIndex("_rate_1")).toDouble(),
                    c.getString(c.getColumnIndex("_rate_2")).toDouble(),
                    c.getString(c.getColumnIndex("_rate_3")).toDouble(),
                    c.getString(c.getColumnIndex("_tax_inclusive")),
                    c.getString(c.getColumnIndex("_image_url")),
                    1.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0
            ))
        }
        c.close()
        return list
    }

    fun clearProducts() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME_PRODUCTS")
    }

    fun addCustomer(
            supplierId: String,
            code: String,
            name: String,
            address1: String,
            city: String,
            telephone: String,
            ratePlan: String) {
        val db = writableDatabase

        val values = ContentValues()
        values.put("_customer_id", supplierId)
        values.put("_code", code)
        values.put("_name", name)
        values.put("_address1", address1)
        values.put("_city", city)
        values.put("_telephone", telephone)
        values.put("_rate_plan", ratePlan)

        db.insert(TABLE_NAME_CUSTOMERS, null, values)
    }

    fun getCustomers(): ArrayList<CustomerModel> {
        val db = readableDatabase
        val c = db.rawQuery("SELECT * FROM $TABLE_NAME_CUSTOMERS", null)
        val list = ArrayList<CustomerModel>()
        while (c.moveToNext()) {
            list.add(CustomerModel(
                    c.getString(c.getColumnIndex("_customer_id")),
                    c.getString(c.getColumnIndex("_code")),
                    c.getString(c.getColumnIndex("_name")),
                    c.getString(c.getColumnIndex("_address1")),
                    c.getString(c.getColumnIndex("_city")),
                    c.getString(c.getColumnIndex("_telephone")),
                    c.getString(c.getColumnIndex("_rate_plan"))
            ))
        }
        c.close()
        return list
    }

    fun clearCustomers() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME_CUSTOMERS")
    }

    fun addSupplier(
            supplierId: String,
            code: String,
            name: String,
            address1: String,
            city: String,
            telephone: String,
            ratePlan: String) {
        val db = writableDatabase

        val values = ContentValues()
        values.put("_supplier_id", supplierId)
        values.put("_code", code)
        values.put("_name", name)
        values.put("_address1", address1)
        values.put("_city", city)
        values.put("_telephone", telephone)
        values.put("_rate_plan", ratePlan)

        db.insert(TABLE_NAME_SUPPLIERS, null, values)
    }

    fun getSupplier(): ArrayList<SupplierModel> {
        val db = readableDatabase
        val c = db.rawQuery("SELECT * FROM $TABLE_NAME_SUPPLIERS", null)
        val list = ArrayList<SupplierModel>()
        while (c.moveToNext()) {
            list.add(SupplierModel(
                    c.getString(c.getColumnIndex("_supplier_id")),
                    c.getString(c.getColumnIndex("_code")),
                    c.getString(c.getColumnIndex("_name")),
                    c.getString(c.getColumnIndex("_address1")),
                    c.getString(c.getColumnIndex("_city")),
                    c.getString(c.getColumnIndex("_telephone")),
                    c.getString(c.getColumnIndex("_rate_plan"))
            ))
        }
        c.close()
        return list
    }

    fun clearSuppliers() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME_SUPPLIERS")
    }

    fun addSalesInvoice(invoiceId: String, invoiceNo: String, invoiceStatus: String) {
        val db = writableDatabase

        val values = ContentValues()
        values.put("_invoice_id", invoiceId)
        values.put("_invoice_no", invoiceNo)
        values.put("_invoice_status", invoiceStatus)

        db.insert(TABLE_NAME_SALES_INVOICE, null, values)
    }

    fun getSalesInvoices(): ArrayList<SalesInvoiceModel> {
        val db = readableDatabase
        val c = db.rawQuery("SELECT * FROM $TABLE_NAME_SALES_INVOICE", null)
        val list = ArrayList<SalesInvoiceModel>()
        while (c.moveToNext()) {
            list.add(SalesInvoiceModel(c.getString(c.getColumnIndex("_invoice_id")), c.getString(c.getColumnIndex("_invoice_no")), c.getString(c.getColumnIndex("_invoice_status"))))
        }
        c.close()
        return list
    }

    fun clearSalesInvoiceTable() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME_SALES_INVOICE")
    }

    fun addSalesOrderInvoice(invoiceId: String, invoiceNo: String, invoiceStatus: String) {
        val db = writableDatabase

        val values = ContentValues()
        values.put("_invoice_id", invoiceId)
        values.put("_invoice_no", invoiceNo)
        values.put("_invoice_status", invoiceStatus)

        db.insert(TABLE_NAME_SALES_ORDER_INVOICE, null, values)
    }

    fun getSalesOrderInvoices(): ArrayList<SalesOrderInvoiceModel> {
        val db = readableDatabase
        val c = db.rawQuery("SELECT * FROM $TABLE_NAME_SALES_ORDER_INVOICE", null)
        val list = ArrayList<SalesOrderInvoiceModel>()
        while (c.moveToNext()) {
            list.add(SalesOrderInvoiceModel(c.getString(c.getColumnIndex("_invoice_id")), c.getString(c.getColumnIndex("_invoice_no")), c.getString(c.getColumnIndex("_invoice_status"))))
        }
        c.close()
        return list
    }

    fun clearSalesOrderInvoiceTable() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME_SALES_ORDER_INVOICE")
    }

    fun addPurchaseInvoice(invoiceId: String, invoiceNo: String, invoiceStatus: String) {
        val db = writableDatabase

        val values = ContentValues()
        values.put("_invoice_id", invoiceId)
        values.put("_invoice_no", invoiceNo)
        values.put("_invoice_status", invoiceStatus)

        db.insert(TABLE_NAME_PURCHASE_INVOICE, null, values)
    }

    fun getPurchaseInvoices(): ArrayList<PurchaseInvoiceModel> {
        val db = readableDatabase
        val c = db.rawQuery("SELECT * FROM $TABLE_NAME_PURCHASE_INVOICE", null)
        val list = ArrayList<PurchaseInvoiceModel>()
        while (c.moveToNext()) {
            list.add(PurchaseInvoiceModel(c.getString(c.getColumnIndex("_invoice_id")), c.getString(c.getColumnIndex("_invoice_no")), c.getString(c.getColumnIndex("_invoice_status"))))
        }
        c.close()
        return list
    }

    fun clearPurchaseInvoiceTable() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME_PURCHASE_INVOICE")
    }

    fun addPurchaseReturnInvoice(invoiceId: String, invoiceNo: String, invoiceStatus: String) {
        val db = writableDatabase

        val values = ContentValues()
        values.put("_invoice_id", invoiceId)
        values.put("_invoice_no", invoiceNo)
        values.put("_invoice_status", invoiceStatus)

        db.insert(TABLE_NAME_PURCHASE_RETURN_INVOICE, null, values)
    }

    fun getPurchaseReturnInvoices(): ArrayList<PurchaseReturnInvoiceModel> {
        val db = readableDatabase
        val c = db.rawQuery("SELECT * FROM $TABLE_NAME_PURCHASE_RETURN_INVOICE", null)
        val list = ArrayList<PurchaseReturnInvoiceModel>()
        while (c.moveToNext()) {
            list.add(PurchaseReturnInvoiceModel(c.getString(c.getColumnIndex("_invoice_id")), c.getString(c.getColumnIndex("_invoice_no")), c.getString(c.getColumnIndex("_invoice_status"))))
        }
        c.close()
        return list
    }

    fun clearPurchaseReturnInvoiceTable() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME_PURCHASE_RETURN_INVOICE")
    }

    fun addSalesReturnInvoice(invoiceId: String, invoiceNo: String, invoiceStatus: String) {
        val db = writableDatabase

        val values = ContentValues()
        values.put("_invoice_id", invoiceId)
        values.put("_invoice_no", invoiceNo)
        values.put("_invoice_status", invoiceStatus)

        db.insert(TABLE_NAME_SALES_RETURN_INVOICE, null, values)
    }

    fun getSalesReturnInvoices(): ArrayList<SalesReturnInvoiceModel> {
        val db = readableDatabase
        val c = db.rawQuery("SELECT * FROM $TABLE_NAME_SALES_RETURN_INVOICE", null)
        val list = ArrayList<SalesReturnInvoiceModel>()
        while (c.moveToNext()) {
            list.add(SalesReturnInvoiceModel(c.getString(c.getColumnIndex("_invoice_id")), c.getString(c.getColumnIndex("_invoice_no")), c.getString(c.getColumnIndex("_invoice_status"))))
        }
        c.close()
        return list
    }

    fun clearSalesReturnInvoiceTable() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME_SALES_RETURN_INVOICE")
    }

    fun updateInProgressList(list: ArrayList<RvListModel>, date: String) {
        if (date != CalendarUtils.getCurrentDate())
            return

        val db = writableDatabase

        clearInProgressListTable()

        for (i in 0 until list.size) {
            val values = ContentValues()
            values.put("_ticket_id_no", list[i].ticketIDNo)
            values.put("_ticket_id", list[i].ticketId)
            values.put("_status_id", list[i].statusId)
            values.put("_service_type", list[i].serviceType)
            values.put("_service_name", list[i].serviceName)
            values.put("_customer", list[i].customer)
            values.put("_phone_1", list[i].phone1)
            values.put("_phone_2", list[i].phone2)
            values.put("_address", list[i].address)
            values.put("_ticket_description", list[i].ticketDescription)
            values.put("_order_info", list[i].orderInfo)
            values.put("_longitude", list[i].longitude)
            values.put("_latitude", list[i].latitude)
            values.put("_remarks", list[i].remarks)
            values.put("_received_by", list[i].receivedBy)
            values.put("_time", list[i].time)

            db.insert(TABLE_NAME_IN_PROGRESS_LIST, null, values)
        }
    }

    fun getInProgressList(): ArrayList<RvListModel> {
        val db = readableDatabase
        val c = db.rawQuery("SELECT * FROM $TABLE_NAME_IN_PROGRESS_LIST", null)
        val list = ArrayList<RvListModel>()
        while (c.moveToNext()) {
            list.add(RvListModel(
                    c.getString(c.getColumnIndex("_ticket_id_no")),
                    c.getString(c.getColumnIndex("_ticket_id")),
                    c.getString(c.getColumnIndex("_status_id")),
                    c.getString(c.getColumnIndex("_service_type")),
                    c.getString(c.getColumnIndex("_service_name")),
                    c.getString(c.getColumnIndex("_customer")),
                    c.getString(c.getColumnIndex("_phone_1")),
                    c.getString(c.getColumnIndex("_phone_2")),
                    c.getString(c.getColumnIndex("_address")),
                    c.getString(c.getColumnIndex("_ticket_description")),
                    c.getString(c.getColumnIndex("_order_info")),
                    c.getString(c.getColumnIndex("_longitude")),
                    c.getString(c.getColumnIndex("_latitude")),
                    c.getString(c.getColumnIndex("_remarks")),
                    c.getString(c.getColumnIndex("_received_by")),
                    c.getString(c.getColumnIndex("_time"))
            ))
        }
        c.close()
        return list
    }

    fun deleteItemFromInProgress(ticketId: String) {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME_IN_PROGRESS_LIST  WHERE _ticket_id=$ticketId")
    }

    fun clearInProgressListTable() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME_IN_PROGRESS_LIST")
    }

    fun updateFailedList(list: ArrayList<RvListModel>, date: String) {
        if (date != CalendarUtils.getCurrentDate())
            return

        val db = writableDatabase

        clearFailedListTable()

        for (i in 0 until list.size) {
            val values = ContentValues()
            values.put("_ticket_id_no", list[i].ticketIDNo)
            values.put("_ticket_id", list[i].ticketId)
            values.put("_status_id", list[i].statusId)
            values.put("_service_type", list[i].serviceType)
            values.put("_service_name", list[i].serviceName)
            values.put("_customer", list[i].customer)
            values.put("_phone_1", list[i].phone1)
            values.put("_phone_2", list[i].phone2)
            values.put("_address", list[i].address)
            values.put("_ticket_description", list[i].ticketDescription)
            values.put("_order_info", list[i].orderInfo)
            values.put("_longitude", list[i].longitude)
            values.put("_latitude", list[i].latitude)
            values.put("_remarks", list[i].remarks)
            values.put("_received_by", list[i].receivedBy)
            values.put("_time", list[i].time)

            db.insert(TABLE_NAME_FAILED_LIST, null, values)
        }
    }

    fun getFailedList(): ArrayList<RvListModel> {
        val db = readableDatabase
        val c = db.rawQuery("SELECT * FROM $TABLE_NAME_FAILED_LIST", null)
        val list = ArrayList<RvListModel>()
        while (c.moveToNext()) {
            list.add(RvListModel(
                    c.getString(c.getColumnIndex("_ticket_id_no")),
                    c.getString(c.getColumnIndex("_ticket_id")),
                    c.getString(c.getColumnIndex("_status_id")),
                    c.getString(c.getColumnIndex("_service_type")),
                    c.getString(c.getColumnIndex("_service_name")),
                    c.getString(c.getColumnIndex("_customer")),
                    c.getString(c.getColumnIndex("_phone_1")),
                    c.getString(c.getColumnIndex("_phone_2")),
                    c.getString(c.getColumnIndex("_address")),
                    c.getString(c.getColumnIndex("_ticket_description")),
                    c.getString(c.getColumnIndex("_order_info")),
                    c.getString(c.getColumnIndex("_longitude")),
                    c.getString(c.getColumnIndex("_latitude")),
                    c.getString(c.getColumnIndex("_remarks")),
                    c.getString(c.getColumnIndex("_received_by")),
                    c.getString(c.getColumnIndex("_time"))
            ))
        }
        c.close()
        return list
    }

    fun deleteItemFromFailedList(ticketId: String) {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME_FAILED_LIST WHERE _ticket_id=$ticketId")
    }

    fun clearFailedListTable() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME_FAILED_LIST")
    }

    fun updateFcmSyncStatus(fcm: String, isUpdated: Boolean) {
        val db = writableDatabase

        val intIsUpdated = if (isUpdated) 1 else 0

        if (isTableEmpty(TABLE_NAME_FCM_ID_UPDATE_STATUS)) {
            val values = ContentValues()
            values.put("_fcm_id", fcm)
            values.put("_sync_status", intIsUpdated)
            db.insert(TABLE_NAME_FCM_ID_UPDATE_STATUS, null, values)
        }

        val values = ContentValues()
        values.put("_fcm_id", fcm)
        values.put("_sync_status", intIsUpdated)
        db.update(TABLE_NAME_FCM_ID_UPDATE_STATUS, values, "_id=?", arrayOf("1"))
    }

    fun isFcmUpdated(): Boolean {
        val db = readableDatabase
        val c = db.rawQuery("SELECT * FROM $TABLE_NAME_FCM_ID_UPDATE_STATUS", null)
        val isUpdated: Boolean
        isUpdated = if (c.moveToFirst()) {
            val syncStatus = c.getInt(c.getColumnIndex("_sync_status"))
            syncStatus == 1
        } else {
            false
        }
        c.close()
        return isUpdated
    }

    fun getFcm(): String? {
        val db = readableDatabase
        val c = db.rawQuery("SELECT * FROM $TABLE_NAME_FCM_ID_UPDATE_STATUS", null)
        val fcm = if (c.moveToFirst()) {
            c.getString(c.getColumnIndex("_fcm_id"))
        } else {
            null
        }
        c.close()
        return fcm
    }

    fun clearFcmSyncStatusTable() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME_FCM_ID_UPDATE_STATUS")
    }


    fun isTableEmpty(tableName: String): Boolean {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT count(*) FROM $tableName", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count <= 0
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        dropTables(db)
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        super.onDowngrade(db, oldVersion, newVersion)
        dropTables(db)
    }

    private fun dropTables(db: SQLiteDatabase?) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_USER")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_PRODUCTS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_CUSTOMERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_SUPPLIERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_SALES_INVOICE")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_SALES_ORDER_INVOICE")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_PURCHASE_INVOICE")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_SALES_RETURN_INVOICE")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_PURCHASE_RETURN_INVOICE")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_IN_PROGRESS_LIST")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_FAILED_LIST")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_FCM_ID_UPDATE_STATUS")
//        onCreate(db)
    }
}