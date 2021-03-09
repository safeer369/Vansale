package com.teamayka.vansaleandmgmt.utils

import android.os.AsyncTask
import android.util.Log
import com.teamayka.vansaleandmgmt.ui.sales.models.SalesDataModel
import com.teamayka.vansaleandmgmt.ui.salesreport.SalesReportDataModel
import com.teamayka.vansaleandmgmt.ui.stockreport.StockReportDataModel
import com.zebra.sdk.comm.Connection
import com.zebra.sdk.printer.ZebraPrinter

/**
 * Created by seydbasil on 4/26/2018.
 */
class PrintTask(private val sampleInterface: ZebraPrinterNotify, private val connection: Connection, private val header: List<String>, private val footer: List<String>, private val data: Any, private val zebraPrinter: ZebraPrinter, val format: Int) : AsyncTask<String, String, Unit>() {
    companion object {
        const val FORMAT_STOCK_REPORT = 1
        const val FORMAT_SALES_REPORT = 2
        const val FORMAT_SALES = 3
    }

    override fun doInBackground(vararg params: String?) {
        val zpl = when (format) {
            FORMAT_SALES -> {
                val a = data as SalesDataModel
                PaperCreator.getSales(header,footer,a.date,a.ivNo,a.name,a.list,a.total,a.taxAmount,a.netTotal,a.cash,a.card,a.amtType,a.discountAmt,a.balanceAmt)
            }
            FORMAT_SALES_REPORT -> {
                val a = data as SalesReportDataModel
                PaperCreator.getSalesReport(header, footer, a.date, a.username, a.list, a.discountAmt, a.grossAmt, a.netAmt)
            }
            FORMAT_STOCK_REPORT -> {
                val a = data as StockReportDataModel
                PaperCreator.getStockReport(header, footer, a.date, a.list)
            }
            else -> ""
        }
        Log.e("_____________", "zpl : $zpl")
        zebraPrinter.printStoredFormat(zpl, java.util.HashMap<Int, String>())
    }

    override fun onPostExecute(result: Unit?) {
        super.onPostExecute(result)
        connection.close()
        sampleInterface.notifyChange()
    }

    interface ZebraPrinterNotify {
        fun notifyChange()
    }
}