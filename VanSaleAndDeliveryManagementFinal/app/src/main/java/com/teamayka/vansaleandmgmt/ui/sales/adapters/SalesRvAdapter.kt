package com.teamayka.vansaleandmgmt.ui.sales.adapters

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.ui.main.models.ProductModel
import com.teamayka.vansaleandmgmt.ui.sales.activities.SalesInvoiceEditActivity
import com.teamayka.vansaleandmgmt.ui.sales.fragments.SalesFragment
import com.teamayka.vansaleandmgmt.utils.DataCollections
import com.teamayka.vansaleandmgmt.utils.MathUtils
import com.teamayka.vansaleandmgmt.utils.Tools
import java.util.*


class SalesRvAdapter(private val parent: Any,
                     private val list: ArrayList<ProductModel>,
                     private val productList: ArrayList<ProductModel>,
                     private val productListNames: java.util.ArrayList<String>,
                     private val tvItemCount: TextView,
                     private val etInvoiceNo: AutoCompleteTextView,
                     private val etCustomerName: AutoCompleteTextView,
                     private val etCustomerAddress: EditText,
                     private val etPhoneNo: EditText,
                     val cbCredit: CheckBox) : RecyclerView.Adapter<SalesRvAdapter.ViewHolder>() {

    companion object {
        const val TYPE_QUANTITY = 1
        const val TYPE_DISCOUNT_PERCENTAGE = 2
        const val TYPE_DISCOUNT_AMOUNT = 3
        const val TYPE_RATE = 4
    }

    private var isItemSelected = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_rv_sales, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        tvItemCount.text = list.size.toString()
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder.adapterPosition == -1)
            return

        if (parent is SalesFragment) {
            isEditMode(parent.isRecyclerViewEnabled, holder)
        } else if (parent is SalesInvoiceEditActivity) {
            isEditMode(true, holder)
        }

        holder.tvSerialNo.text = "%s".format(holder.adapterPosition + 1)
        holder.etItemName.setText(list[holder.adapterPosition].displayName)
        holder.tvCode.text = list[holder.adapterPosition].productCode
        holder.tvQuantity.text = ("%.3f").format(list[holder.adapterPosition].quantity)
        holder.tvUnit.text = list[holder.adapterPosition].unit
        holder.tvSalesRate.text = ("%." + DataCollections.getInstance(holder.itemView.context).getUser()!!.amountType + "f").format(list[holder.adapterPosition].rate1) // me picked rate1 field for rate storing in item list
        holder.tvDiscount.setText(("%." + DataCollections.getInstance(holder.itemView.context).getUser()!!.amountType + "f").format(list[holder.adapterPosition].discountPercentage))
        holder.tvDiscountAmount.setText(("%." + DataCollections.getInstance(holder.itemView.context).getUser()!!.amountType + "f").format(list[holder.adapterPosition].discountAmount))// me picked rate1 field for rate storing in item list
        holder.layoutParent.requestFocus()

        holder.ivDelete.setOnClickListener {
            if (holder.adapterPosition == -1)
                return@setOnClickListener
            if (TextUtils.isEmpty(holder.etItemName.text.toString().trim())) {
                list.removeAt(holder.adapterPosition)
                notifyItemRemoved(holder.adapterPosition)
                notifyItemRangeChanged(position, list.size)
                if (parent is SalesFragment) {
                    parent.calculateTotal()
                } else if (parent is SalesInvoiceEditActivity) {
                    parent.calculateTotal()
                }
                return@setOnClickListener
            } else {
                AlertDialog.Builder(holder.itemView.context)
                        .setMessage("Delete item?")
                        .setPositiveButton("YES") { p0, p1 ->
                            list.removeAt(holder.adapterPosition)
                            notifyItemRemoved(holder.adapterPosition)
                            notifyItemRangeChanged(position, list.size)
                            if (parent is SalesFragment) {
                                parent.calculateTotal()
                            } else if (parent is SalesInvoiceEditActivity) {
                                parent.calculateTotal()
                            }
                        }
                        .setNegativeButton("CANCEL") { p0, p1 -> }
                        .show()
            }
        }

        holder.etItemName.threshold = 1
        holder.etItemName.setAdapter(ArrayAdapter<String>(holder.itemView.context, android.R.layout.simple_list_item_1, productListNames))
        isItemSelected = false

        holder.etItemName.setOnItemClickListener { adapterView, view, i1, l ->
            // where the position i1 is invalid so this technique was used
            val vl = adapterView.getItemAtPosition(i1) as String
            val i = productListNames.indexOf(vl)
            list[position].productId = productList[i].productId
            list[position].productCode = productList[i].productCode
            list[position].productName = productList[i].productName
            list[position].displayName = productList[i].displayName
            list[position].unit = productList[i].unit
            list[position].unitId = productList[i].unitId
            list[position].cGSTPercentage = productList[i].cGSTPercentage
            list[position].sGSTPercentage = productList[i].sGSTPercentage
            list[position].rate1 = productList[i].rate1
            list[position].rate2 = productList[i].rate2
            list[position].rate3 = productList[i].rate3
            list[position].taxInclusive = productList[i].taxInclusive
            list[position].imageUrl = productList[i].imageUrl
            list[position].quantity = productList[i].quantity
            list[position].discountPercentage = productList[i].discountPercentage
            list[position].discountAmount = productList[i].discountAmount
            list[position].totalAmount = productList[i].totalAmount
            list[position].taxAmount = productList[i].taxAmount
            list[position].grandTotal = productList[i].grandTotal

            holder.tvCode.text = list[position].productCode
            holder.tvUnit.text = list[position].unit
            holder.tvQuantity.text = ("%.3f").format(list[position].quantity)
            holder.tvDiscount.setText(("%." + DataCollections.getInstance(holder.itemView.context).getUser()!!.amountType + "f").format(list[position].discountPercentage))
            holder.tvDiscountAmount.setText(("%." + DataCollections.getInstance(holder.itemView.context).getUser()!!.amountType + "f").format(list[position].discountAmount))

            val ratePlan = (parent as? SalesFragment)?.ratePlan
                    ?: if (parent is SalesInvoiceEditActivity) {
                        parent.ratePlan
                    } else
                        -1
            when (ratePlan) {
                0 -> {
                    list[position].rate1 = productList[i].rate1
                }
                1 -> {
                    list[position].rate1 = productList[i].rate2
                }
                2 -> {
                    list[position].rate1 = productList[i].rate3
                }
            }
            holder.tvSalesRate.text = ("%." + DataCollections.getInstance(holder.itemView.context).getUser()!!.amountType + "f").format(list[position].rate1)
            calculateAmountsByRate(list[position], list[position].rate1, holder)

            if (parent is SalesFragment) {
                parent.calculateTotal()
            } else if (parent is SalesInvoiceEditActivity) {
                parent.calculateTotal()
            }

            val ro = RequestOptions()
            .placeholder(R.drawable.ic_image_placeholder)
            .centerCrop()
            Glide.with(holder.ivItem).load(list[position].imageUrl).apply(ro).into(holder.ivItem)

//            Picasso.get().load(list[position].imageUrl).into(holder.ivItem)

            if (parent is SalesFragment) {
                Tools.hideSoftKeyboard(parent.mContext as Activity)
            } else if (parent is SalesInvoiceEditActivity) {
                Tools.hideSoftKeyboard(parent)
            }
            isItemSelected = false
        }

        holder.etItemName.setOnDismissListener {
            if (!isItemSelected && !productList.any { it -> it.displayName.toLowerCase() == holder.etItemName.text.toString().toLowerCase() }) {
                holder.etItemName.setText("")
            }
        }

        holder.etItemName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                // clear text if not in item list
                val text = p0.toString().toLowerCase()
//                if (!TextUtils.isEmpty(text) && !productListNames.any { it ->
//                            text.length <= it.length && it.substring(0, text.length).toLowerCase() == text
//                        }) {
//                    holder.etItemName.setText("")
//                }

                if (!TextUtils.isEmpty(text) && !productListNames.any { it ->
                            it.toLowerCase().contains(text)
                        }) {
                    holder.etItemName.setText("")
                }

                if (holder.adapterPosition == -1)
                    return
                list[holder.adapterPosition].displayName = p0.toString()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        holder.tvQuantity.addTextChangedListener(QuantityChangedListener(holder))
        holder.tvDiscount.addTextChangedListener(DiscountPercentageChangedListener(holder))
        holder.tvDiscountAmount.addTextChangedListener(DiscountAmountChangedListener(holder))
        holder.tvSalesRate.addTextChangedListener(RateChangedListener(holder))
    }

    private fun isEditMode(isRecyclerViewEnabled: Boolean, holder: ViewHolder) {
        holder.etItemName.isEnabled = isRecyclerViewEnabled
        holder.ivDelete.isEnabled = isRecyclerViewEnabled
        holder.tvSalesRate.isEnabled = isRecyclerViewEnabled
        holder.tvDiscount.isEnabled = isRecyclerViewEnabled
        holder.tvDiscountAmount.isEnabled = isRecyclerViewEnabled
        holder.tvQuantity.isEnabled = isRecyclerViewEnabled
    }

    private fun calculateAmountsByQuantity(productModel: ProductModel, quantity: Double, holder: ViewHolder) {
        val totalRate = productModel.rate1 * quantity // calculate rate as quantity
        val discountAmount = totalRate * productModel.discountPercentage / 100
        val totalAmount = totalRate - discountAmount
        val taxAmount = totalAmount * (productModel.sGSTPercentage + productModel.cGSTPercentage) / 100
        val grandTotal = totalAmount + taxAmount
        list[holder.adapterPosition].discountAmount = discountAmount
        list[holder.adapterPosition].totalAmount = totalAmount
        list[holder.adapterPosition].taxAmount = taxAmount
        list[holder.adapterPosition].grandTotal = grandTotal
        holder.tvDiscountAmount.setText(("%." + DataCollections.getInstance(holder.itemView.context).getUser()!!.amountType + "f").format(discountAmount))
    }

    private fun calculateAmountsByRate(productModel: ProductModel, rate: Double, holder: ViewHolder) {
        val totalRate = rate * productModel.quantity
        val discountAmount = totalRate * productModel.discountPercentage / 100
        val totalAmount = totalRate - discountAmount
        val taxAmount = totalAmount * (productModel.sGSTPercentage + productModel.cGSTPercentage) / 100
        val grandTotal = totalAmount + taxAmount
        list[holder.adapterPosition].discountAmount = discountAmount
        list[holder.adapterPosition].totalAmount = totalAmount
        list[holder.adapterPosition].taxAmount = taxAmount
        list[holder.adapterPosition].grandTotal = grandTotal
        holder.tvDiscountAmount.setText(("%." + DataCollections.getInstance(holder.itemView.context).getUser()!!.amountType + "f").format(discountAmount))
    }

    private fun calculateAmountsByDiscountPercentage(productModel: ProductModel, discountPercentage: Double, holder: ViewHolder) {
        val totalRate = productModel.rate1 * productModel.quantity // calculate rate as quantity
        val discountAmount = totalRate * discountPercentage / 100
        val totalAmount = totalRate - discountAmount
        val taxAmount = totalAmount * (productModel.sGSTPercentage + productModel.cGSTPercentage) / 100
        val grandTotal = totalAmount + taxAmount
        list[holder.adapterPosition].discountAmount = discountAmount
        list[holder.adapterPosition].totalAmount = totalAmount
        list[holder.adapterPosition].taxAmount = taxAmount
        list[holder.adapterPosition].grandTotal = grandTotal
        holder.tvDiscountAmount.setText(("%." + DataCollections.getInstance(holder.itemView.context).getUser()!!.amountType + "f").format(discountAmount))
    }

    private fun calculateAmountsByDiscountAmount(productModel: ProductModel, discountAmount: Double, holder: ViewHolder) {
        val totalRate = productModel.rate1 * productModel.quantity // calculate rate as quantity
        val discountPercentage = 100 * discountAmount / totalRate
        val totalAmount = totalRate - discountAmount
        val taxAmount = totalAmount * (productModel.sGSTPercentage + productModel.cGSTPercentage) / 100
        val grandTotal = totalAmount + taxAmount
        list[holder.adapterPosition].discountPercentage = discountPercentage
        list[holder.adapterPosition].totalAmount = totalAmount
        list[holder.adapterPosition].taxAmount = taxAmount
        list[holder.adapterPosition].grandTotal = grandTotal
        holder.tvDiscount.setText(("%." + DataCollections.getInstance(holder.itemView.context).getUser()!!.amountType + "f").format(discountPercentage))
    }

    private inner class QuantityChangedListener(val holder: ViewHolder) : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            if (holder.adapterPosition == -1)
                return
            if (TextUtils.isEmpty(holder.etItemName.text.toString()))
                return
            val quantity = MathUtils.toDouble(p0)
            list[holder.adapterPosition].quantity = quantity
            calculateAmountsByQuantity(list[holder.adapterPosition], quantity, holder)
            if (parent is SalesFragment) {
                parent.calculateTotal()
            } else if (parent is SalesInvoiceEditActivity) {
                parent.calculateTotal()
            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }
    }

    private inner class RateChangedListener(val holder: ViewHolder) : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            if (holder.adapterPosition == -1)
                return
            if (TextUtils.isEmpty(holder.etItemName.text.toString()))
                return
            val rate = MathUtils.toDouble(p0)
            list[holder.adapterPosition].rate1 = rate
            calculateAmountsByRate(list[holder.adapterPosition], rate, holder)
            if (parent is SalesFragment) {
                parent.calculateTotal()
            } else if (parent is SalesInvoiceEditActivity) {
                parent.calculateTotal()
            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }
    }

    private inner class DiscountPercentageChangedListener(val holder: ViewHolder) : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            if (holder.adapterPosition == -1)
                return
            if (TextUtils.isEmpty(holder.etItemName.text.toString()))
                return
            val discountPercentage = MathUtils.toDouble(p0)
            list[holder.adapterPosition].discountPercentage = discountPercentage
            calculateAmountsByDiscountPercentage(list[holder.adapterPosition], discountPercentage, holder)
            if (parent is SalesFragment) {
                parent.calculateTotal()
            } else if (parent is SalesInvoiceEditActivity) {
                parent.calculateTotal()
            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }
    }

    private inner class DiscountAmountChangedListener(val holder: ViewHolder) : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            if (holder.adapterPosition == -1)
                return
            if (TextUtils.isEmpty(holder.etItemName.text.toString()))
                return
            val discountAmount = MathUtils.toDouble(p0)
            list[holder.adapterPosition].discountAmount = discountAmount
            calculateAmountsByDiscountAmount(list[holder.adapterPosition], discountAmount, holder)
            if (parent is SalesFragment) {
                parent.calculateTotal()
            } else if (parent is SalesInvoiceEditActivity) {
                parent.calculateTotal()
            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }
    }

//    private inner class TextChangedListener(val type: Int, val holder: ViewHolder) : TextWatcher {
//        override fun afterTextChanged(p0: Editable?) {
//            if (holder.adapterPosition == -1)
//                return
//            when (type) {
//                TYPE_QUANTITY -> {
//                    val quantity = MathUtils.toDouble(p0)
//                    list[holder.adapterPosition].quantity = quantity
//                    calculateAmountsByQuantity(list[holder.adapterPosition], quantity, holder)
//                    if (parent is SalesFragment) {
//                        parent.calculateTotal()
//                    } else if (parent is SalesInvoiceEditActivity) {
//                        parent.calculateTotal()
//                    }
//                }
//                TYPE_DISCOUNT_PERCENTAGE -> {
//                    val discountPercentage = MathUtils.toDouble(p0)
//                    list[holder.adapterPosition].discountPercentage = discountPercentage
//                    calculateAmountsByDiscountPercentage(list[holder.adapterPosition], discountPercentage, holder, this)
//                    if (parent is SalesFragment) {
//                        parent.calculateTotal()
//                    } else if (parent is SalesInvoiceEditActivity) {
//                        parent.calculateTotal()
//                    }
//                }
//                TYPE_DISCOUNT_AMOUNT -> {
//                    val discountAmount = MathUtils.toDouble(p0)
//                    list[holder.adapterPosition].discountAmount = discountAmount
//                    calculateAmountsByDiscountAmount(list[holder.adapterPosition], discountAmount, holder, this)
//                    if (parent is SalesFragment) {
//                        parent.calculateTotal()
//                    } else if (parent is SalesInvoiceEditActivity) {
//                        parent.calculateTotal()
//                    }
//                }
//                TYPE_RATE -> {
//                    val rate = MathUtils.toDouble(p0)
//                    list[holder.adapterPosition].rate1 = rate
//                    calculateAmountsByRate(list[holder.adapterPosition], rate, holder)
//                    if (parent is SalesFragment) {
//                        parent.calculateTotal()
//                    } else if (parent is SalesInvoiceEditActivity) {
//                        parent.calculateTotal()
//                    }
//                }
//            }
//        }
//
//        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//        }
//
//        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//
//        }
//    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var layoutParent: View = itemView.findViewById(R.id.layoutParent)
        var tvSerialNo: TextView = itemView.findViewById(R.id.tvSerialNo)
        var etItemName: AutoCompleteTextView = itemView.findViewById(R.id.etItemName)
        var ivDelete: ImageView = itemView.findViewById(R.id.ivDelete)
        var ivItem: ImageView = itemView.findViewById(R.id.ivItem)
        var tvUnit: TextView = itemView.findViewById(R.id.tvUnit)
        var tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        var tvCode: TextView = itemView.findViewById(R.id.tvCode)
        var tvSalesRate: TextView = itemView.findViewById(R.id.tvSalesRate)
        var tvDiscount: EditText = itemView.findViewById(R.id.tvDiscount)
        var tvDiscountAmount: EditText = itemView.findViewById(R.id.tvDiscountAmount)
    }
}
