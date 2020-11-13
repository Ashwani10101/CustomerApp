package com.ash.customerapp.firebase

import android.graphics.Bitmap
import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Ignore
import androidx.room.PrimaryKey

class Product
{
    var key :String = ""

    var name :String = ""

    var price :String = ""

    var quantity :Int = 1

    var details :String = ""

    var category :String = ""

    var image: Uri? = null

}