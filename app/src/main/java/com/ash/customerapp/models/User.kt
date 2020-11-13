package com.ash.customerapp.models

import android.os.Parcel
import android.os.Parcelable

class User() : Parcelable
{
    var name:String = "null"
    var phone:String = "null"
    var address:String = "null"

    constructor(parcel: Parcel) : this()
    {
        name = parcel.readString().toString()
        phone = parcel.readString().toString()
        address = parcel.readString().toString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int)
    {
        parcel.writeString(name)
        parcel.writeString(phone)
        parcel.writeString(address)
    }

    override fun describeContents(): Int
    {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User>
    {
        override fun createFromParcel(parcel: Parcel): User
        {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?>
        {
            return arrayOfNulls(size)
        }
    }
}