package com.ash.customerapp.database

import android.os.Parcel
import android.os.Parcelable

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity //Table
class CartItem() :Parcelable
{

    @PrimaryKey
    var key :String = ""
    @ColumnInfo
    var name :String = ""
    @ColumnInfo
    var price :String = ""
    @ColumnInfo
    var quantity :Int = 1
    @ColumnInfo
    var details :String = ""
    @ColumnInfo
    var category :String = ""

    constructor(parcel: Parcel) : this()
    {
        key = parcel.readString().toString()
        name = parcel.readString().toString()
        price = parcel.readString().toString()
        quantity = parcel.readInt()
        details = parcel.readString().toString()
        category = parcel.readString().toString()
    }
/*    @ColumnInfo
    var image: Uri? = null*/
    override fun writeToParcel(parcel: Parcel, flags: Int)
    {
        parcel.writeString(key)
        parcel.writeString(name)
        parcel.writeString(price)
        parcel.writeInt(quantity)
        parcel.writeString(details)
        parcel.writeString(category)
    }

    override fun describeContents(): Int
    {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CartItem>
    {
        override fun createFromParcel(parcel: Parcel): CartItem
        {
            return CartItem(parcel)
        }

        override fun newArray(size: Int): Array<CartItem?>
        {
            return arrayOfNulls(size)
        }
    }


}