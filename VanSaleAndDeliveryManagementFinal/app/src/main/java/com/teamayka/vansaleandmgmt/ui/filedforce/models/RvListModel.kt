package com.teamayka.vansaleandmgmt.ui.filedforce.models

import android.os.Parcel
import android.os.Parcelable

class RvListModel(
        val ticketIDNo: String,
        val ticketId: String,
        val statusId: String,
        val serviceType: String,
        val serviceName: String,
        val customer: String,
        val phone1: String,
        val phone2: String,
        val address: String,
        val ticketDescription: String,
        val orderInfo: String,
        val longitude: String,
        val latitude: String,
        val remarks: String,
        val receivedBy: String,
        val time: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(ticketIDNo)
        parcel.writeString(ticketId)
        parcel.writeString(statusId)
        parcel.writeString(serviceType)
        parcel.writeString(serviceName)
        parcel.writeString(customer)
        parcel.writeString(phone1)
        parcel.writeString(phone2)
        parcel.writeString(address)
        parcel.writeString(ticketDescription)
        parcel.writeString(orderInfo)
        parcel.writeString(longitude)
        parcel.writeString(latitude)
        parcel.writeString(remarks)
        parcel.writeString(receivedBy)
        parcel.writeString(time)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RvListModel> {
        override fun createFromParcel(parcel: Parcel): RvListModel {
            return RvListModel(parcel)
        }

        override fun newArray(size: Int): Array<RvListModel?> {
            return arrayOfNulls(size)
        }
    }
}