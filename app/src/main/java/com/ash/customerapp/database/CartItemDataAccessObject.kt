package com.ash.customerapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface CartItemDataAccessObject
{
    @Insert
    fun addNewCartItem(cartItem: CartItem)

    @Query("UPDATE CartItem SET quantity = :newQuantity WHERE `key` = :cartItemKey")
    fun updateCartItem(newQuantity :String,cartItemKey: String)


    @Query("select * from CartItem")
    fun getAllCartItems() :List<CartItem>

    @Query("DELETE FROM CartItem")
    fun clearCart()

}