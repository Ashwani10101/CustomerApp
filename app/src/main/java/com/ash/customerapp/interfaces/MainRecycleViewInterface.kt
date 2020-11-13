package com.ash.customerapp.interfaces

import com.ash.customerapp.firebase.Product


interface MainRecycleViewInterface
{
    fun onClick(product: Product)
    fun onAddToCartClick(product: Product)

}