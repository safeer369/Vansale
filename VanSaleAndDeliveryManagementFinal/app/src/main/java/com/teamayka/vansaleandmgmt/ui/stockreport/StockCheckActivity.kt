package com.teamayka.vansaleandmgmt.ui.stockreport

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.ui.main.PublicUrls
import com.teamayka.vansaleandmgmt.ui.main.models.CategoryDataModel
import com.teamayka.vansaleandmgmt.ui.main.models.GroupDataModel
import com.teamayka.vansaleandmgmt.ui.main.models.ProductDataModel
import com.teamayka.vansaleandmgmt.utils.*
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class StockCheckActivity : AppCompatActivity() {

    private lateinit var layoutContent: View
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvRetry: TextView
    private lateinit var tvMessage: TextView
    private lateinit var tvGroups: AutoCompleteTextView
    private lateinit var tvCategories: AutoCompleteTextView
    private lateinit var tvProducts: AutoCompleteTextView
    private lateinit var tvValueDate: TextView
    private lateinit var ivSearch: ImageView

    private var callGetData: Call? = null

    private var groupId = "-1"
    private var categoryId = "-1"
    private var productId = "-1"

    private var groupName = ""
    private var categoryName = ""
    private var productName = ""

    val listAllCategories = ArrayList<CategoryDataModel>()
    val listAllProducts = ArrayList<ProductDataModel>()

    private val listAllCategoryNames = ArrayList<String>()
    private val listAllProductNames = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_check)
        init()

        tvGroups.threshold = 1
        tvCategories.threshold = 1
        tvProducts.threshold = 1

        getData()
    }

    private fun getData() {
        layoutContent.visibility = View.GONE
        tvRetry.visibility = View.GONE
        tvMessage.visibility = View.GONE
        pbLoading.visibility = View.VISIBLE

        tvRetry.setOnClickListener {
            getData()
        }

        val client = OkHttpUtils.getOkHttpClient()

        val body = FormBody.Builder()
                .add("UserID", DataCollections.getInstance(this).getUser()?.id ?: "")
                .build()

        val request = Request.Builder()
                .url(PublicUrls.URL_GET_PRODUCT_DATA)
                .post(body)
                .build()

        callGetData = client.newCall(request)
        callGetData?.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                if (call == null || call.isCanceled)
                    return
                runOnUiThread {
                    pbLoading.visibility = View.GONE
                    layoutContent.visibility = View.GONE
                    tvRetry.visibility = View.VISIBLE
                    tvMessage.visibility = View.VISIBLE
                    if (e is UnknownHostException)
                        tvMessage.text = getString(R.string.error_message_connect_error)
                    else
                        tvMessage.text = getString(R.string.error_message_went_wront)
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                if (call == null || call.isCanceled)
                    return
                val resp = response?.body()?.string()
                Log.e("_____________resp", "_get_data : $resp")

                try {
//                    val jo = XML.toJSONObject(resp)
                    val jo = JSONObject(resp)
                    val joGroups = jo.getJSONObject("Groups")
                    val jaGroups = joGroups.getJSONArray("Group")
                    val listGroups = ArrayList<GroupDataModel>()
                    val listGroupNames = ArrayList<String>()
                    for (i in 0 until jaGroups.length()) {
                        val joGroup = jaGroups.getJSONObject(i)
                        val groupId = joGroup.getString("GroupID")
                        val groupName = joGroup.getString("GROUP_NAME")
                        val jaCategories = joGroup.getJSONArray("Category")
                        val listCategories = ArrayList<CategoryDataModel>()
                        for (j in 0 until jaCategories.length()) {
                            val joCategory = jaCategories.getJSONObject(j)
                            val categoryId = joCategory.getString("CategoryID")
                            val categoryName = joCategory.getString("CATEGORY")
                            val jaProducts = joCategory.getJSONArray("Products")
                            val listProducts = ArrayList<ProductDataModel>()
                            for (k in 0 until jaProducts.length()) {
                                val joProduct = jaProducts.getJSONObject(k)
                                val productId = joProduct.getString("ProductID")
                                val productName = joProduct.getString("NAME")
                                listProducts.add(ProductDataModel(productId, productName))
                            }
                            listAllProducts.addAll(listProducts)
                            listCategories.add(CategoryDataModel(categoryId, categoryName, listProducts))
                        }
                        listAllCategories.addAll(listCategories)
                        listGroups.add(GroupDataModel(groupId, groupName, listCategories))
                        listGroupNames.add(groupName)
                    }

                    runOnUiThread {
                        pbLoading.visibility = View.GONE

                        if (listGroups.size <= 0) {
                            layoutContent.visibility = View.GONE
                            tvRetry.visibility = View.VISIBLE
                            tvMessage.visibility = View.VISIBLE

                            tvMessage.text = getString(R.string.empty_list)
                        } else {
                            tvMessage.visibility = View.GONE
                            tvRetry.visibility = View.GONE
                            layoutContent.visibility = View.VISIBLE

                            showContents(listGroups, listGroupNames)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("______exc_get_data", e.message)
                    runOnUiThread {
                        pbLoading.visibility = View.GONE
                        layoutContent.visibility = View.GONE
                        tvRetry.visibility = View.VISIBLE
                        tvMessage.visibility = View.VISIBLE
                        tvMessage.text = getString(R.string.error_message_went_wront)
                    }
                }
            }
        })
    }

    private fun showContents(listGroups: ArrayList<GroupDataModel>, listGroupNames: ArrayList<String>) {
        val currentDate = Calendar.getInstance().time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        tvValueDate.text = df.format(currentDate)
        tvValueDate.setOnClickListener {
            val cal = Calendar.getInstance()
            val pickerDialog = DatePickerDialog.newInstance({ view, year, monthOfYear, dayOfMonth ->
                val date = "$year-${monthOfYear + 1}-$dayOfMonth"
                tvValueDate.text = date
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            pickerDialog.accentColor = ResourcesCompat.getColor(resources, R.color.colorBgDefaultBlue, null)
            pickerDialog.show(fragmentManager, "DatePickerDialog")

        }

        for (i in 0 until listAllCategories.size) {
            listAllCategoryNames.add(listAllCategories[i].categoryName)
        }

        for (i in 0 until listAllProducts.size) {
            listAllProductNames.add(listAllProducts[i].productName)
        }

        tvGroups.setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listGroupNames))
        tvCategories.setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listAllCategoryNames))
        tvProducts.setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listAllProductNames))

        tvGroups.setOnClickListener {
            tvGroups.showDropDown()
        }

        tvCategories.setOnClickListener {
            tvCategories.showDropDown()
        }

        tvProducts.setOnClickListener {
            tvProducts.showDropDown()
        }

        tvCategories.setOnItemClickListener { categoryAdapterView, categoryView, i1Category, lCategory ->
            tvProducts.setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ArrayList<String>()))
            tvProducts.setText("")
            this.productId = "-1"
            this.productName = ""
            val vlCategory = categoryAdapterView.getItemAtPosition(i1Category) as String
            val positionCategory = listAllCategoryNames.indexOf(vlCategory)
            this.categoryId = listAllCategories[positionCategory].categoryId
            this.categoryName = listAllCategories[positionCategory].categoryName
            val listProductNames = ArrayList<String>()
            for (i in 0 until listAllCategories[positionCategory].listProducts.size) {
                listProductNames.add(listAllCategories[positionCategory].listProducts[i].productName)
            }
            tvProducts.setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listProductNames))

            tvProducts.setOnItemClickListener { productAdapterView, productView, i1Product, lProduct ->
                val vlProduct = productAdapterView.getItemAtPosition(i1Product) as String
                val positionProduct = listProductNames.indexOf(vlProduct)
                this.productId = listAllCategories[positionCategory].listProducts[positionProduct].productId
                this.productName = listAllCategories[positionCategory].listProducts[positionProduct].productName
                Tools.hideSoftKeyboard(this)
            }
            Tools.hideSoftKeyboard(this)
        }

        tvProducts.setOnItemClickListener { productAdapterView, productView, i1Product, lProduct ->
            val vlProduct = productAdapterView.getItemAtPosition(i1Product) as String
            val positionProduct = listAllProductNames.indexOf(vlProduct)
            this.productId = listAllProducts[positionProduct].productId
            this.productName = listAllProducts[positionProduct].productName
            Tools.hideSoftKeyboard(this)
        }

        tvGroups.setOnItemClickListener { groupAdapterView, groupView, i1Group, lGroup ->
            tvCategories.setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ArrayList<String>()))
            tvProducts.setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ArrayList<String>()))
            tvCategories.setText("")
            tvProducts.setText("")
            this.categoryId = "-1"
            this.categoryName = ""
            this.productId = "-1"
            this.productName = ""
            val vlGroup = groupAdapterView.getItemAtPosition(i1Group) as String
            val positionGroup = listGroupNames.indexOf(vlGroup)
            this.groupId = listGroups[positionGroup].groupId
            this.groupName = listGroups[positionGroup].groupName
            val listCategoryNames = ArrayList<String>()
            for (i in 0 until listGroups[positionGroup].listCategories.size) {
                listCategoryNames.add(listGroups[positionGroup].listCategories[i].categoryName)
            }
            tvCategories.setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listCategoryNames))
            tvCategories.setOnItemClickListener { categoryAdapterView, categoryView, i1Category, lCategory ->
                tvProducts.setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ArrayList<String>()))
                tvProducts.setText("")
                this.productId = "-1"
                this.productName = ""
                val vlCategory = categoryAdapterView.getItemAtPosition(i1Category) as String
                val positionCategory = listCategoryNames.indexOf(vlCategory)
                this.categoryId = listGroups[positionGroup].listCategories[positionCategory].categoryId
                this.categoryName = listGroups[positionGroup].listCategories[positionCategory].categoryName
                val listProductNames = ArrayList<String>()
                for (i in 0 until listGroups[positionGroup].listCategories[positionCategory].listProducts.size) {
                    listProductNames.add(listGroups[positionGroup].listCategories[positionCategory].listProducts[i].productName)
                }
                tvProducts.setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listProductNames))

                tvProducts.setOnItemClickListener { productAdapterView, productView, i1Product, lProduct ->
                    val vlProduct = productAdapterView.getItemAtPosition(i1Product) as String
                    val positionProduct = listProductNames.indexOf(vlProduct)
                    this.productId = listGroups[positionGroup].listCategories[positionCategory].listProducts[positionProduct].productId
                    this.productName = listGroups[positionGroup].listCategories[positionCategory].listProducts[positionProduct].productName
                    Tools.hideSoftKeyboard(this)
                }
                Tools.hideSoftKeyboard(this)
            }
            Tools.hideSoftKeyboard(this)
        }

        tvGroups.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (TextUtils.isEmpty(p0.toString().trim())) {
                    this@StockCheckActivity.groupId = "-1"
                    this@StockCheckActivity.groupName = ""
                    tvCategories.setAdapter(ArrayAdapter<String>(this@StockCheckActivity, android.R.layout.simple_list_item_1, listAllCategoryNames))
                    tvProducts.setAdapter(ArrayAdapter<String>(this@StockCheckActivity, android.R.layout.simple_list_item_1, listAllProductNames))
                    tvCategories.setOnItemClickListener { categoryAdapterView, categoryView, i1Category, lCategory ->
                        tvProducts.setAdapter(ArrayAdapter<String>(this@StockCheckActivity, android.R.layout.simple_list_item_1, ArrayList<String>()))
                        tvProducts.setText("")
                        this@StockCheckActivity.productId = "-1"
                        this@StockCheckActivity.productName = ""
                        val vlCategory = categoryAdapterView.getItemAtPosition(i1Category) as String
                        val positionCategory = listAllCategoryNames.indexOf(vlCategory)
                        this@StockCheckActivity.categoryId = listAllCategories[positionCategory].categoryId
                        this@StockCheckActivity.categoryName = listAllCategories[positionCategory].categoryName
                        val listProductNames = ArrayList<String>()
                        for (i in 0 until listAllCategories[positionCategory].listProducts.size) {
                            listProductNames.add(listAllCategories[positionCategory].listProducts[i].productName)
                        }
                        tvProducts.setAdapter(ArrayAdapter<String>(this@StockCheckActivity, android.R.layout.simple_list_item_1, listProductNames))

                        tvProducts.setOnItemClickListener { productAdapterView, productView, i1Product, lProduct ->
                            val vlProduct = productAdapterView.getItemAtPosition(i1Product) as String
                            val positionProduct = listProductNames.indexOf(vlProduct)
                            this@StockCheckActivity.productId = listAllCategories[positionCategory].listProducts[positionProduct].productId
                            this@StockCheckActivity.productName = listAllCategories[positionCategory].listProducts[positionProduct].productName
                            Tools.hideSoftKeyboard(this@StockCheckActivity)
                        }
                        Tools.hideSoftKeyboard(this@StockCheckActivity)
                    }

                    tvProducts.setOnItemClickListener { productAdapterView, productView, i1Product, lProduct ->
                        val vlProduct = productAdapterView.getItemAtPosition(i1Product) as String
                        val positionProduct = listAllProductNames.indexOf(vlProduct)
                        this@StockCheckActivity.productId = listAllProducts[positionProduct].productId
                        this@StockCheckActivity.productName = listAllProducts[positionProduct].productName
                        Tools.hideSoftKeyboard(this@StockCheckActivity)
                    }
                }
            }

        })

        tvCategories.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (TextUtils.isEmpty(p0.toString().trim())) {
                    this@StockCheckActivity.categoryId = "-1"
                    this@StockCheckActivity.categoryName = ""
                    tvProducts.setAdapter(ArrayAdapter<String>(this@StockCheckActivity, android.R.layout.simple_list_item_1, listAllProductNames))
                    tvProducts.setOnItemClickListener { productAdapterView, productView, i1Product, lProduct ->
                        val vlProduct = productAdapterView.getItemAtPosition(i1Product) as String
                        val positionProduct = listAllProductNames.indexOf(vlProduct)
                        this@StockCheckActivity.productId = listAllProducts[positionProduct].productId
                        this@StockCheckActivity.productName = listAllProducts[positionProduct].productName
                        Tools.hideSoftKeyboard(this@StockCheckActivity)
                    }
                }
            }
        })

        tvProducts.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (TextUtils.isEmpty(p0.toString().trim())) {
                    this@StockCheckActivity.productId = "-1"
                    this@StockCheckActivity.productName = ""
                }
            }
        })

        ivSearch.setOnClickListener { _ ->
            if (this.groupId == "-1" && this.categoryId == "-1" && this.productId == "-1") {
                SnackBarUtils.showSnackBar(this, "Choose fields")
                return@setOnClickListener
            }

            val groupName = tvGroups.text.toString().trim()
            val categoryName = tvCategories.text.toString().trim()
            val productName = tvProducts.text.toString().trim()

            if (this.groupId != "-1") {
                if (this.groupName != groupName) {
                    SnackBarUtils.showSnackBar(this, "Choose fields")
                    return@setOnClickListener
                }
            }

            if (this.categoryId != "-1") {
                if (this.categoryName != categoryName) {
                    SnackBarUtils.showSnackBar(this, "Choose fields")
                    return@setOnClickListener
                }
            }

            if (this.productId != "-1") {
                if (this.productName != productName) {
                    SnackBarUtils.showSnackBar(this, "Choose fields")
                    return@setOnClickListener
                }
            }

//            if (this.groupName != groupName || this.categoryName != categoryName || this.productName != productName) {
//                SnackBarUtils.showSnackBar(this, "Choose fields")
//                return@setOnClickListener
//            }

            val intent = Intent(this, StockReportActivity::class.java)
            intent.putExtra("KEY_GROUP_ID", this.groupId)
            intent.putExtra("KEY_CATEGORY_ID", this.categoryId)
            intent.putExtra("KEY_PRODUCT_ID", this.productId)
            intent.putExtra("KEY_DATE", tvValueDate.text.toString())
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        OkHttpUtils.cancelCalls(callGetData)
    }


    private fun init() {
        layoutContent = findViewById(R.id.layoutContent)
        pbLoading = findViewById(R.id.pbLoading)
        tvRetry = findViewById(R.id.tvRetry)
        tvMessage = findViewById(R.id.tvMessage)
        tvGroups = findViewById(R.id.tvGroups)
        tvCategories = findViewById(R.id.tvCategories)
        tvProducts = findViewById(R.id.tvProducts)
        tvValueDate = findViewById(R.id.tvValueDate)
        ivSearch = findViewById(R.id.ivSearch)
    }
}
