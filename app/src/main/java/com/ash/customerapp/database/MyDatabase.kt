package com.ash.customerapp.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [(CartItem::class)] ,version = 1 )
abstract class MyDatabase : RoomDatabase()
{
    abstract fun cartDao() : CartItemDataAccessObject
}
