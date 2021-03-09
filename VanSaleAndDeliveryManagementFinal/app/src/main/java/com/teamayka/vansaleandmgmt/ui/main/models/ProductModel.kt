package com.teamayka.vansaleandmgmt.ui.main.models

import android.os.Parcel
import android.os.Parcelable

class ProductModel(var productId: String,
                   var productCode: String,
                   var productName: String,
                   var displayName: String,
                   var unit: String,
                   var unitId: String,
                   var cGSTPercentage: Double,
                   var sGSTPercentage: Double,
                   var rate1: Double,
                   var rate2: Double,
                   var rate3: Double,
                   var taxInclusive: String,
                   var imageUrl: String,
                   var quantity: Double,
                   var discountPercentage: Double,
                   var discountAmount: Double,
                   var totalAmount: Double,
                   var taxAmount: Double,
                   var grandTotal: Double
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readString(),
            parcel.readString(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble()
            ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(productId)
        parcel.writeString(productCode)
        parcel.writeString(productName)
        parcel.writeString(displayName)
        parcel.writeString(unit)
        parcel.writeString(unitId)
        parcel.writeDouble(cGSTPercentage)
        parcel.writeDouble(sGSTPercentage)
        parcel.writeDouble(rate1)
        parcel.writeDouble(rate2)
        parcel.writeDouble(rate3)
        parcel.writeString(taxInclusive)
        parcel.writeString(imageUrl)
        parcel.writeDouble(quantity)
        parcel.writeDouble(discountPercentage)
        parcel.writeDouble(discountAmount)
        parcel.writeDouble(totalAmount)
        parcel.writeDouble(taxAmount)
        parcel.writeDouble(grandTotal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProductModel> {
        override fun createFromParcel(parcel: Parcel): ProductModel {
            return ProductModel(parcel)
        }

        override fun newArray(size: Int): Array<ProductModel?> {
            return arrayOfNulls(size)
        }
    }
}