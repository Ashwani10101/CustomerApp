package com.ash.customerapp.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.ash.customerapp.MainActivity
import com.ash.customerapp.R
import com.ash.customerapp.interfaces.MainRecycleViewInterface
import com.ash.customerapp.firebase.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


import kotlin.collections.ArrayList


class MainRecycleViewAdaptorFirebase(val mainRecycleViewInterface: MainRecycleViewInterface) : RecyclerView.Adapter<MainRecycleViewAdaptorFirebase.MyViewHolder>(), Filterable
{


    private val productList = ArrayList<Product>()
    private var productListAll = ArrayList<Product>()// Used for filtering in search bar

    fun addProductList(ProductList: ArrayList<Product>)
    {
        productList.addAll(ProductList)
        productListAll = ArrayList<Product>().apply { addAll(ProductList) }
        notifyDataSetChanged()
    }

    fun addProduct(product: Product)
    {
        productList.add(product)
        productListAll.add(product)
        //notifyDataSetChanged()
        notifyItemInserted(itemCount)
    }

    fun removeProduct(product: Product)
    {
        var removedFrom:Int? = null
        for(i in 0..productListAll.size)
        {
            val p = productListAll[i]
            if(p.key == product.key)
            {
                productListAll.removeAt(i)
                productList.remove(p)
                removedFrom = i
                break
            }
        }


        //trimming
        productList.trimToSize()
        productListAll.trimToSize()

        // notifyDataSetChanged()
        if(removedFrom!=null)
        {
            notifyItemRemoved(removedFrom)
        } else
        {
            notifyDataSetChanged()
        }

    }

    fun removeAll()
    {
        productList.clear()
        productListAll.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.main_card_product, parent, false)
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

        private val cardView: CardView= view.findViewById(R.id.main_card)
        private val productImage: ImageView = view.findViewById<ImageView>(R.id.imageView_ProductImage)
        private val productName: TextView = view.findViewById<TextView>(R.id.textView_ProductName)
        private val productDetails: TextView = view.findViewById<TextView>(R.id.textView_ProductDetails)

        private val productQuantity: TextView = view.findViewById<TextView>(R.id.textView_ProductQuantity)
        private val btn_productQuantityAdd: Button = view.findViewById<Button>(R.id.btn_ProductQuantityAdd)
        private val btn_productQuantitySubtract: Button = view.findViewById<Button>(R.id.btn_ProductQuantitySubtract)

        private val productCategory: TextView = view.findViewById<TextView>(R.id.textView_ProductCategory)
        private val productPrice: TextView = view.findViewById<TextView>(R.id.textView_ProductPrice)

        private val btn_AddToCart: Button = view.findViewById(R.id.btn_AddToCart)
       // private val btn_BuyNow: Button = view.findViewById(R.id.btn_BuyNow)

        fun bindData(product: Product)
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

            }

            btn_productQuantitySubtract.setOnClickListener {

                if( product.quantity <= 1)
                {
                    btn_productQuantitySubtract.isEnabled = false
                } else
                {
                    product.quantity--
                    productQuantity.text = product.quantity.toString()
                }


            }



            btn_AddToCart.setOnClickListener {
                mainRecycleViewInterface.onAddToCartClick(product)
                product.quantity = 1
                productQuantity.text = product.quantity.toString()
            }


            cardView.setOnClickListener {
                mainRecycleViewInterface.onClick(product)
            }



        }
    }

    override fun getFilter(): Filter
    {
        return filter()
    }


    private fun filter(): Filter
    {
        return object : Filter()
        {
            //Run in background thread
            override fun performFiltering(charSequence: CharSequence?): FilterResults
            {
                val filterList = ArrayList<Product>()


                if (charSequence == null || charSequence.toString().isEmpty() || charSequence.toString() == "")
                {
                    filterList.addAll(productListAll)
                } else
                {
                    for (productEntity in productListAll)
                    {
                        if (productEntity.name.toLowerCase().trim().contains(charSequence.toString().toLowerCase().trim()) || productEntity.details.toLowerCase().trim().contains(charSequence.toString().toLowerCase().trim()))
                        {
                            filterList.add(productEntity)
                        }

                        if (productEntity.category.toLowerCase().trim().contains(charSequence.toString().toLowerCase().trim()))
                        {
                            filterList.add(productEntity)
                        }


                    }
                }

                val filterResult = FilterResults()
                filterResult.values = filterList

                return filterResult
            }

            //Run in UI thread
            override fun publishResults(constraint: CharSequence?, filterResult: FilterResults?)
            {
                productList.clear()
                productList.addAll(filterResult!!.values as Collection<Product>)
                notifyDataSetChanged()
            }
        }
    }


}