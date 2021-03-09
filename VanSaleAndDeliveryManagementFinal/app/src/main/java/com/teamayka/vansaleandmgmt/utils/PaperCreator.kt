package com.teamayka.vansaleandmgmt.utils

import com.teamayka.vansaleandmgmt.ui.main.models.ProductModel
import com.teamayka.vansaleandmgmt.ui.salesreport.SalesReportModel
import com.teamayka.vansaleandmgmt.ui.stockreport.StockReportModel
import kotlin.collections.ArrayList

object PaperCreator {

    private const val numberOfPrints = 1
    private const val pageMarginTop = 50
    private const val defaultPageWidth = 575
    private const val defaultFontSize = 23
    private const val defaultLineHeight = 2
    private const val defaultMarginLeft = 5
    private const val marginLeftSmall = 10
    private const val defaultMarginTop = 10

    private const val initializer = "^XA^CWZ,E:TT0003M_.FNT^FS^XZ^XA^MNN"
    private const val unicodeSupporter = "^PA1,1,1,1"
    private const val start = initializer + unicodeSupporter

    private var y = 0

    fun getSales(header: List<String>,
                 footer: List<String>,
                 date: String,
                 ivNo: String,
                 name: String,
                 list1: ArrayList<ProductModel>,
                 total: String,
                 taxAmount: String,
                 netTotal: String,
                 cash: String,
                 card: String,
                 amtType: Int,
                 discountAmt: String,
                 balanceAmt: String): String {
        val builder = StringBuilder()
        y = pageMarginTop

        builder.append(start)

        for (i in 0 until header.size) {
            builder.append(drawTextCenterEnglish(header[i]))
        }

        y += defaultMarginTop
        y += defaultMarginTop
        y += defaultMarginTop
        builder.append(drawTextCenterEnglish("INVOICE"))

        y += defaultMarginTop
        builder.append(drawTextLeft("Invoice No"))
        builder.append(drawTextLeft(ivNo, defaultPageWidth / 2))

        builder.append(drawTextLeft("Invoice Date"))
        builder.append(drawTextLeft(date, defaultPageWidth / 2))

        builder.append(drawTextLeft("Customer"))
        builder.append(drawTextLeft(if (name.length > 18) name.substring(0, 17) else name, defaultPageWidth / 2))

        y += defaultMarginTop
        y += defaultMarginTop
        builder.append(drawTextLeft("Item"))
        builder.append(drawTextRight("Amount"))
        builder.append(drawTextRight("Rate", defaultPageWidth - 130))
        builder.append(drawTextRight("Qty", defaultPageWidth - 260))

        builder.append(drawLine())

        for (i in 0 until list1.size) {
            val dName = list1[i].displayName
            builder.append(drawTextLeft(if (dName.length > 17) dName.substring(0, 16) else dName))
            builder.append(drawTextRight(("%." + amtType + "f").format(MathUtils.toDouble(list1[i].totalAmount))))
            builder.append(drawTextRight(("%." + amtType + "f").format(MathUtils.toDouble(list1[i].rate1)), defaultPageWidth - 130))
            builder.append(drawTextRight(("%." + amtType + "f").format(MathUtils.toDouble(list1[i].quantity)), defaultPageWidth - 260))
        }

        builder.append(drawLine())

        y += defaultMarginTop + defaultFontSize
        builder.append(drawTextRight("Total", defaultPageWidth - 200))
        builder.append(drawTextRight(total))

        if (MathUtils.toDouble(discountAmt) != 0.0) {
            y += defaultMarginTop + defaultFontSize
            builder.append(drawTextRight("Discount", defaultPageWidth - 200))
            builder.append(drawTextRight(discountAmt))
        }

        y += defaultMarginTop + defaultFontSize
        builder.append(drawTextRight("GST Amount", defaultPageWidth - 200))
        builder.append(drawTextRight(taxAmount))

        y += defaultMarginTop + defaultFontSize
        builder.append(drawTextRight("Net Amount", defaultPageWidth - 200))
        builder.append(drawTextRight(netTotal))

        y += defaultMarginTop + defaultFontSize
        builder.append(drawTextRight("Card Paid", defaultPageWidth - 200))
        builder.append(drawTextRight(card))

        y += defaultMarginTop + defaultFontSize
        builder.append(drawTextRight("Cash Paid", defaultPageWidth - 200))
        builder.append(drawTextRight(cash))

        y += defaultMarginTop + defaultFontSize
        builder.append(drawTextRight("Balance Amount :", defaultPageWidth - 200))
        builder.append(drawTextRight(balanceAmt))


        builder.append(drawLine())

        for (i in 0 until footer.size) {
            builder.append(drawTextCenterEnglish(footer[i]))
        }

        y += pageMarginTop
        y += pageMarginTop
        y += pageMarginTop
        builder.insert(initializer.length, "^LL$y") // set length of paper according to print content(set ^LL before first ^FS)

        builder.append("^PQ$numberOfPrints,1,0,Y,Y^XZ")
        return builder.toString()
    }

    fun getSalesReport(header: List<String>, footer: List<String>, date: String, username: String, list1: ArrayList<SalesReportModel>, discountAmt: String, grossAmt: String, netAmt: String): String {
        val builder = StringBuilder()
        y = pageMarginTop

        builder.append(start)

        for (i in 0 until header.size) {
            builder.append(drawTextCenterEnglish(header[i]))
        }

        builder.append(drawTextCenterEnglish(date))
        builder.append(drawTextCenterEnglish(username))

        builder.append(drawLine())

        for (i in 0 until list1.size) {
            builder.append(drawTextLeft("NAME :"))
            builder.append(drawTextRight(list1[i].customerName))

            builder.append(drawTextLeft("INVOICE NO :"))
            builder.append(drawTextRight(list1[i].invoiceNo))

            builder.append(drawTextLeft("DATE :"))
            builder.append(drawTextRight(list1[i].invoiceDate))

            builder.append(drawTextLeft("GROSS AMT :"))
            builder.append(drawTextRight(list1[i].grossAmount))

            builder.append(drawTextLeft("DISCOUNT AMT:"))
            builder.append(drawTextRight(list1[i].discount))

            builder.append(drawTextLeft("TOTAL AMT:"))
            builder.append(drawTextRight(list1[i].netAmount))

            if (i != list1.size - 1)
                builder.append(drawTextCenterEnglish("------------------------------------------"))
        }

        builder.append(drawLine())

        builder.append(drawTextLeft("TOTAL GROSS AMT :"))
        builder.append(drawTextRight(grossAmt))

        builder.append(drawTextLeft("TOTAL DISCOUNT AMT :"))
        builder.append(drawTextRight(discountAmt))

        builder.append(drawTextLeft("NET AMT :"))
        builder.append(drawTextRight(netAmt))

        builder.append(drawLine())

        for (i in 0 until footer.size) {
            builder.append(drawTextCenterEnglish(footer[i]))
        }

        y += pageMarginTop
        y += pageMarginTop
        y += pageMarginTop
        builder.insert(initializer.length, "^LL$y") // set length of paper according to print content(set ^LL before first ^FS)

        builder.append("^PQ$numberOfPrints,1,0,Y,Y^XZ")
        return builder.toString()
    }

    fun getStockReport(header: List<String>, footer: List<String>, date: String, list1: ArrayList<StockReportModel>): String {
        val builder = StringBuilder()
        y = pageMarginTop

        builder.append(start)

        for (i in 0 until header.size) {
            builder.append(drawTextCenterEnglish(header[i]))
        }

        builder.append(drawTextCenterEnglish(date))
        builder.append(drawLine())

        for (i in 0 until list1.size) {
            builder.append(drawTextLeft("NAME :"))
            builder.append(drawTextRight(list1[i].itemName))
            builder.append(drawTextLeft("CATEGORY :"))
            builder.append(drawTextRight(list1[i].category))
            builder.append(drawTextLeft("CODE :"))
            builder.append(drawTextRight(list1[i].barcode, defaultPageWidth / 2))
            builder.append(drawTextLeft("UNIT :", defaultPageWidth / 2 + marginLeftSmall))
            builder.append(drawTextRight(list1[i].unit))
            builder.append(drawTextLeft("STOCK :"))
            builder.append(drawTextRight(list1[i].stock, defaultPageWidth / 2))
            builder.append(drawTextLeft("WCP :", defaultPageWidth / 2 + marginLeftSmall))
            builder.append(drawTextRight(list1[i].WCP))
            builder.append(drawTextLeft("VALUE :"))
            builder.append(drawTextRight(list1[i].value))
            if (i != list1.size - 1)
                builder.append(drawTextCenterEnglish("------------------------------------------"))
        }

        builder.append(drawLine())

        for (i in 0 until footer.size) {
            builder.append(drawTextCenterEnglish(footer[i]))
        }

        y += pageMarginTop
        y += pageMarginTop
        y += pageMarginTop
        builder.insert(initializer.length, "^LL$y") // set length of paper according to print content(set ^LL before first ^FS)

        builder.append("^PQ$numberOfPrints,1,0,Y,Y^XZ")
        return builder.toString()
    }

    private fun drawTextLeft(text: String): String {
        y += defaultFontSize + defaultMarginTop
        return "^FO$defaultMarginLeft,$y^CI28^AZN,$defaultFontSize,$defaultFontSize^FD$text^FS"
    }

    private fun drawTextLeft(text: String, x: Int): String {
        return "^FO$x,$y^CI28^AZN,$defaultFontSize,$defaultFontSize^FD$text^FS"
    }

    //for latest firmware with unicode support
    private fun drawTextRight(text: String): String {
        return "^FO$defaultPageWidth,$y,1^CI28^AZN,$defaultFontSize,$defaultFontSize^FD$text^FS"
    }

    //for latest firmware with unicode support
    private fun drawTextRight(text: String, x: Int): String {
        return "^FO$x,$y,1^CI28^AZN,$defaultFontSize,$defaultFontSize^FD$text^FS"
    }

    // for all firmware. but no unicode support
//    fun drawTextRight(text : String): String {
//        return "^FO$defaultMarginLeft,$y^FB$defaultPageWidth,1,0,R^CI28^AZN,$defaultFontSize,$defaultFontSize^FD$text^FS"
//    }

    private fun drawTextCenterEnglish(text: String): String {
        y += defaultFontSize + defaultMarginTop
        return "^FO$defaultMarginLeft,$y^FB$defaultPageWidth,1,0,C^CI28^AZN,$defaultFontSize,$defaultFontSize^FD$text^FS"
    }

    private fun drawLine(): String {
        y += defaultMarginTop + defaultFontSize
        val text = "^FO$defaultMarginLeft,$y^FB$defaultPageWidth,1,0,C^GB576,0,$defaultLineHeight^FS"
        y += defaultMarginTop
        return text
    }
}