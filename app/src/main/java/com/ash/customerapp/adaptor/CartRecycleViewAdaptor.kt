package com.ash.customerapp.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.ash.customerapp.MainActivity
import com.ash.customerapp.R
import com.ash.customerapp.database.CartItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


import kotlin.collections.ArrayList


class CartRecycleViewAdaptor : RecyclerView.Adapter<CartRecycleViewAdaptor.MyViewHolder>()
{


    val productList = ArrayList<CartItem>()


    fun addProductList(cartItemList: ArrayList<CartItem>)
    {
        productList.addAll(cartItemList)
    }

    fun addProduct(product: CartItem, position: Int)
    {
        productList.add(position, product)
        notifyItemInserted(position)
    }

    fun deleteProduct(product: CartItem, position: Int)
    {
        productList.remove(product)

        //trimming
        productList.trimToSize()


        notifyItemRemoved(position)
    }

    fun clear()
    {
        productList.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_card_product, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int)
    {
        return holder.bindData(productList[position])
    }

    override fun getItemCount(): Int
    {
        return productList.size
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
      //  private var PRODUCT_QUANTITY = 1

       // private val productImage: ImageView = view.findViewById<ImageView>(R.id.imageView_ProductImage)
        private val productName: TextView = view.findViewById<TextView>(R.id.cart_textView_ProductName)
        private val productDetails: TextView = view.findViewById<TextView>(R.id.cart_textView_ProductDetails)
        private val productCategory: TextView = view.findViewById<TextView>(R.id.cart_textView_ProductCategory)
        private val productPrice: TextView = view.findViewById<TextView>(R.id.cart_textView_ProductPrice)

        private val productQuantity: TextView = view.findViewById<TextView>(R.id.cart_textView_ProductQuantity)
        private val btn_productQuantityAdd: Button = view.findViewById<Button>(R.id.cart_btn_ProductQuantityAdd)
        private val btn_productQuantitySubtract: Button = view.findViewById<Button>(R.id.cart_btn_ProductQuantitySubtract)

        fun bindData(product: CartItem)
        {
/*            if (product.image != null)
            {
                productImage.setImageBitmap(product.image)
            } else
            {
                productImage.setImageResource(R.drawable.no_image_selected)
            }*/

            productName.text = product.name
            productDetails.text = product.details
            productPrice.text = product.price
            productCategory.text = product.category



            productQuantity.text = product.quantity.toString()

            btn_productQuantityAdd.setOnClickListener {
                product.quantity++
                productQuantity.text = product.quantity.toString()
                btn_productQuantitySubtract.isEnabled = true


                CoroutineScope(Dispatchers.IO).launch {
                    MainActivity.cartDatabase.cartDao().updateCartItem(product.quantity.toString(),product.key)
                }
            }


/*            btn_productQuantitySubtract.setOnClickListener {
                product.quantity--
                productQuantity.text = product.quantity.toString()
                btn_productQuantitySubtract.isEnabled = product.quantity > 1
                CoroutineScope(Dispatchers.IO).launch {
                    MainActivity.cartDatabase.cartDao().updateCartItem(product.quantity.toString(),product.key)
                }
            }*/


            btn_productQuantitySubtract.setOnClickListener {
                if( product.quantity <= 1)
                {
                    btn_productQuantitySubtract.isEnabled = false
                } else
                {
                    product.quantity--
                    productQuantity.text = product.quantity.toString()
                    CoroutineScope(Dispatchers.IO).launch {
                        MainActivity.cartDatabase.cartDao().updateCartItem(product.quantity.toString(),product.key)
                    }
                }
            }
        }
    }




}