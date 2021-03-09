package com.teamayka.vansaleandmgmt.ui.sales.models

import com.teamayka.vansaleandmgmt.ui.main.models.ProductModel

class SalesDataModel(val date: String,
                     val ivNo: String,
                     val name: String,
                     val list: ArrayList<ProductModel>,
                     val total: String,
                     val taxAmount: String,
                     val netTotal: String,
                     val cash: String,
                     val card: String,
                     val amtType: Int,
                     val discountAmt: String,
                     val balanceAmt: String)