package com.teamayka.vansaleandmgmt.ui.main

/**
 * Created by seydbasil on 3/30/2018.
 */
object PublicUrls {
    // !!! WILL LOSS DYNAMICALLY ASSIGNED VALUES OF VARIABLES IN THE OBJECT !!!

    // private const val URL_BASE = "http://www.digisuitevansale.a2zprojects.com/vansale.asmx"
    private const val URL_BASE = "http://apiservices.digisuitesolutions.com/Vansale.asmx"

    const val URL_USER_REGISTER = "$URL_BASE/RegisterApp" // User Login
    const val URL_USER_LOGIN = "$URL_BASE/Login" // User Login
    const val URL_GET_PRODUCTS = "$URL_BASE/GetProducts" // Get list of products
    const val URL_GET_CUSTOMERS = "$URL_BASE/GetCustomers" // Get list of customers
    const val URL_GET_SUPPLIERS = "$URL_BASE/GetSuppliers" // Get list of suppliers

    // SALES
    const val URL_GET_SALES_INVOICES = "$URL_BASE/GetInvoices" // Get list of invoices
    const val URL_SAVE_SALES_INVOICE = "$URL_BASE/SaveInvoice" // Save invoice
    const val URL_EDIT_SALES_INVOICE = "$URL_BASE/EditInvoice" // EDIT invoice
    const val URL_GET_SALES_INVOICE_DETAILS = "$URL_BASE/GetInvoiceDetails" // invoice details

    // SALES ORDER
    const val URL_GET_SALES_ORDER_INVOICES = "$URL_BASE/GetSalesOrderInvoices" // Get list of invoices
    const val URL_SAVE_SALES_ORDER_INVOICE = "$URL_BASE/SaveSalesOrder" // Save invoice
    const val URL_EDIT_SALES_ORDER_INVOICE = "$URL_BASE/EditSalesOrder" // EDIT invoice
    const val URL_GET_SALES_ORDER_INVOICE_DETAILS = "$URL_BASE/GetSalesOrderDetails" // invoice details

    // PURCHASE
    const val URL_GET_PURCHASE_INVOICES = "$URL_BASE/GetPurchaseInvoices" // Get list of invoices
    const val URL_SAVE_PURCHASE_INVOICE = "$URL_BASE/SavePurchase" // Save invoice
    const val URL_EDIT_PURCHASE_INVOICE = "$URL_BASE/EditPurchase" // EDIT invoice
    const val URL_GET_PURCHASE_INVOICE_DETAILS = "$URL_BASE/GetPurchaseInvoiceDetails" // invoice details

    //SALES RETURN
    const val URL_GET_SALES_RETURN_INVOICES = "$URL_BASE/GetSalesReturnInvoices" // Get list of invoices
    const val URL_SAVE_SALES_RETURN_INVOICE = "$URL_BASE/SaveSalesReturn" // Save invoice
    const val URL_EDIT_SALES_RETURN_INVOICE = "$URL_BASE/EditSalesReturn" // EDIT invoice
    const val URL_GET_SALES_RETURN_INVOICE_DETAILS = "$URL_BASE/GetSalesReturnInvoiceDetails" // invoice details

    // PURCHASE RETURN
    const val URL_GET_PURCHASE_RETURN_INVOICES = "$URL_BASE/GetPurchaseReturnInvoices" // Get list of invoices
    const val URL_SAVE_PURCHASE_RETURN_INVOICE = "$URL_BASE/SavePurchaseReturn" // Save invoice
    const val URL_EDIT_PURCHASE_RETURN_INVOICE = "$URL_BASE/EditPurchaseReturn" // EDIT invoice
    const val URL_GET_PURCHASE_RETURN_INVOICE_DETAILS = "$URL_BASE/GetPurchaseReturnInvoiceDetails" // invoice details

    // FILED FORCE
    const val URL_GET_DASHBOARD = "$URL_BASE/GetFieldserviceDashBoard"
    const val URL_GET_FIELD_FORCE_LIST = "$URL_BASE/GetFieldserviceDashBoardDetails"
    const val URL_UPDATE_FIELD_FORCE_STATUS = "$URL_BASE/UpdateFieldServiceStatus"
    const val URL_GET_FIELD_FORCE_STATUS = "$URL_BASE/GetFieldServiceUpdateStatus"
    const val URL_FIELD_FORCE_CREATE_CHECK_IN = "$URL_BASE/CreateCheckinDetailsFieldForce"
    const val URL_FIELD_FORCE_GET_CHECK_IN_LIST = "$URL_BASE/GetCheckinDetailsFieldForce"
    const val URL_FIELD_FORCE_REFRESH = "$URL_BASE/GetFieldserviceDashBoardDetailsWithRouteOptimised"

    const val URL_GET_SALES_REPORT = "$URL_BASE/GetSalesReport" // sales report
    const val URL_GET_PRODUCT_DATA = "$URL_BASE/GetProductDetailsForReport" // data for stock report search
    const val URL_GET_STOCK_REPORT = "$URL_BASE/GetStockReport" // stock report list on process data

    const val URL_UPDATE_FCM_ID = "$URL_BASE/UpdateFCMID"
    const val URL_SAVE_AS_PDF = "$URL_BASE/EmailInvoicePDF"
    const val URL_EMAIL = "$URL_BASE/SendEmailSalesReport"
}
