package com.ash.customerapp

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ash.customerapp.adaptor.CartRecycleViewAdaptor
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_open_cart.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class OpenCartActivity : AppCompatActivity() {

    private lateinit var recyclerviewAdaptor:CartRecycleViewAdaptor

    private var cartTotal = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_cart)

        initRecycleView()
        init()
    }

    private fun init()
    {
        cart_btn_PlaceOrder.setOnClickListener {
                placeOrder()
        }
        cart_btn_PlaceOrder.isEnabled = true
    }

    private fun placeOrder()
    {
        if( recyclerviewAdaptor.itemCount==0)
        {
            showSnakeBar("Cart Empty!",R.color.black)
        } else

        if(MainActivity.ordersReferenceID==null)
        {

            showSnakeBar("You are not logged in!",R.color.black)
        }else
        {


            CoroutineScope(Dispatchers.IO).launch {
                val cartItems = recyclerviewAdaptor.productList
                //Setting user
                MainActivity.profileReferenceID!!.setValue(MainActivity.user)


                for(cartItem in cartItems)
                {
                    val str = StringBuilder()
                    str.append("${cartItem.quantity}x")
                    str.append("\t")
                    str.append("'${cartItem.name}'")
                    str.append("\t")
                    str.append("(${cartItem.category})")
                    str.append("\t")
                    str.append("[Rs.${cartItem.price.toFloat() * cartItem.quantity}]")
                   // val str = "(${cartItem.quantity}x)\t${cartItem.name}\t(${cartItem.category})\t[Rs.${cartItem.price.toFloat() * cartItem.quantity}]"
                    MainActivity.ordersReferenceID!!.child(MainActivity.ordersReferenceID!!.push().key!!).setValue(str.toString())
                }


                MainActivity.cartDatabase.cartDao().clearCart()
                CoroutineScope(Dispatchers.Main).launch {
                    showSnakeBar("Order for [+${cartItems.size}] items placed!", R.color.pink)
                    recyclerviewAdaptor.clear()
                }
            }







//Old method
/*            CoroutineScope(Dispatchers.IO).launch {
                val cartItems = recyclerviewAdaptor.productList
                MainActivity.profileReferenceID!!.setValue(MainActivity.user)
                for(cartItem in cartItems)
                {
                    val order = Order(cartItem.key,cartItem.quantity)
                    MainActivity.ordersReferenceID!!.child(MainActivity.ordersReferenceID!!.push().key!!).setValue(order)
                }

                MainActivity.cartDatabase.cartDao().clearCart()
                CoroutineScope(Dispatchers.Main).launch {
                    showSnakeBar("Order for [+${cartItems.size}] items placed!", R.color.pink)
                    recyclerviewAdaptor.clear()
                }
            }*/

        }


    }

    private fun initRecycleView()
    {

        recyclerviewAdaptor = CartRecycleViewAdaptor()

        cart_recycleview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        cart_recycleview.adapter = recyclerviewAdaptor

        CoroutineScope(Dispatchers.Default).launch {
            val cartItems = MainActivity.cartDatabase.cartDao().getAllCartItems()
            for(cartItem in cartItems)
            {
                cartTotal += cartItem.price.toFloat()

                CoroutineScope(Dispatchers.Main).launch {
                    recyclerviewAdaptor.addProduct(cartItem,0)
                }

            }

            CoroutineScope(Dispatchers.Main).launch {
                cart_textViewTotal.text = cartTotal.toString()
                cart_textViewQuantity.text = cartItems.size.toString()
            }

        }




    }



    private fun showSnakeBar(msg: String, res: Int)
    {
        val snackbar = Snackbar.make(cart_recycleview, msg, Snackbar.LENGTH_LONG)
        val sbView: View = snackbar.view

        sbView.setBackgroundColor(ContextCompat.getColor(this, res))

        val textView = sbView.findViewById(R.id.snackbar_text) as TextView
        textView.textSize = 15f
        textView.setTextColor(Color.WHITE)
        snackbar.show()
    }

}