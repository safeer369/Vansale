package com.teamayka.vansaleandmgmt.ui.salesreport

class SalesReportDataModel(
        val date: String,
        val discountAmt: String,
        val grossAmt: String,
        val netAmt: String,
        val username: String,
        val list: ArrayList<SalesReportModel>
)