package com.ash.customerapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteConstraintException
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.room.Room
import com.ash.customerapp.adaptor.MainRecycleViewAdaptorFirebase
import com.ash.customerapp.database.CartItem
import com.ash.customerapp.database.MyDatabase
import com.ash.customerapp.firebase.Product
import com.ash.customerapp.interfaces.MainRecycleViewInterface
import com.ash.customerapp.models.User
import com.facebook.stetho.Stetho
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), MainRecycleViewInterface
{

    private lateinit var recyclerviewAdaptor: MainRecycleViewAdaptorFirebase
    private lateinit var spinner: Spinner
    private lateinit var searchView: SearchView //SearchView query needed by fragment tab change listener

    private val LOGIN_CODE = 11
    lateinit var sharedPreferences: SharedPreferences

    companion object
    {
        val USER_INFO_SHARE_PREFERENCE = "userInfo"
        const val USER_ID = "userID"
        const val USER_NAME = "userName"
        const val USER_PHONE = "userPhone"
        const val USER_ADDRESS = "userAddress"
        const val USER_LOGGED_IN = "userLogged"
        const val NULL = "null"

        var user :User? = null
        private lateinit var userID :String
        lateinit var cartDatabase: MyDatabase
        val ALL_CATEGORYS = "All, Atta, Masale, Grains, Pulses, Rice, Dry Fruits, Sweetners, Satto, Others"



       /* val firebaseStorageRef = FirebaseStorage.getInstance().reference
        val firebaseStorageRefAllProductImages = FirebaseStorage.getInstance().reference.child("product_images")*/

        val firebaseDatabaseRefAllProducts = FirebaseDatabase.getInstance().reference.child("AllProducts")
        private val _firebaseDatabaseRefAllOrders = FirebaseDatabase.getInstance().reference.child("AllOrders")

        var ordersReferenceID: DatabaseReference? = null
        var profileReferenceID: DatabaseReference? = null


/*        fun getOrderInCarts():DatabaseReference
        {
            return  getCartRefID().child("Orders")
        }*/


    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        setTheme(R.style.MainPageTheme) //Setting splash screen
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar_MainActivity)


        Stetho.initializeWithDefaults(applicationContext)

        init()

        initSharePreferences()
        initDatabase()
        initDrawer()
        initSpinner()
        initRecycleView()
        Handler().postDelayed({ initFirebase() },500)

    }

    private fun init()
    {
        drawer_textview_options_history.setOnClickListener {
            initFirebase()
        }
    }

    private fun initSharePreferences()
    {

        sharedPreferences = getSharedPreferences(USER_INFO_SHARE_PREFERENCE, Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean(USER_LOGGED_IN,false)
        if(!isLoggedIn)
        {
            val intent = Intent(this,LoginActivity::class.java)
            startActivityForResult(intent,LOGIN_CODE)
        } else
        {

            val _userID = sharedPreferences.getString(USER_ID, NULL)
            val userName = sharedPreferences.getString(USER_NAME, NULL)
            val userPhone = sharedPreferences.getString(USER_PHONE, NULL)
            val userAddress = sharedPreferences.getString(USER_ADDRESS, NULL)

            user = User()
            userID = _userID!!
            user!!.name = userName!!
            user!!.phone= userPhone!!
            user!!.address = userAddress!!

            setFirebaseCartReference(userID)
//            ordersReferenceID = _firebaseDatabaseRefAllOrders.child(userID).child("Orders")
//            profileReferenceID =  _firebaseDatabaseRefAllOrders.child(userID).child("Profile")//.setValue(user)


            drawer_textviewUserName.text = userName
            drawer_textviewUserPhone.text = userPhone
            drawer_textviewLoginBtn.text = "Logout"
        }




    }




    private fun initFirebase()
    {
        recyclerviewAdaptor.removeAll()

        firebaseDatabaseRefAllProducts.orderByKey().addChildEventListener(object : ChildEventListener
        {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?)
            {

                val product = snapshot.getValue(Product::class.java)

                recyclerviewAdaptor.addProduct(product!!)
                reycleviewMain.smoothScrollToPosition(recyclerviewAdaptor.itemCount-1)


                /*val url = firebaseStorageRefAllProductImages.child("${product!!.key}.jpg").path
                 show(url)
                 firebaseStorageRefAllProductImages.child("${product.key}.jpg").downloadUrl.addOnCompleteListener {
                 val fileLink = it.result
                 show("URL : ${fileLink.toString()}")
                 product.image =  fileLink//bitmap//getBitmapFromURL(fileLink.toString())

                }*/
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?)
            {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot)
            {
                val product = snapshot.getValue(Product::class.java)
                recyclerviewAdaptor.removeProduct(product!!)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?)
            {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError)
            {
                TODO("Not yet implemented")
            }
        })


    }




    private lateinit var drawer: DrawerLayout
    private fun initDrawer()
    {
        drawer = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawer, toolbar_MainActivity, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()


    }


   private fun initDatabase()
    {
        cartDatabase = Room.databaseBuilder(
            applicationContext,
            MyDatabase::class.java,
            "CartDatabase"
        ).build()
    }

    private fun initRecycleView()
    {
        recyclerviewAdaptor = MainRecycleViewAdaptorFirebase(this)

       /// reycleviewMain.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        reycleviewMain.adapter = recyclerviewAdaptor




    }

    private fun initSpinner()
    {
        spinner = findViewById(R.id.main_spinnerCategorySelector)

        val spinnerData: ArrayList<String> = ArrayList()


        val list = ALL_CATEGORYS.split(',')
        spinnerData.addAll(list)

        val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(applicationContext, android.R.layout.simple_spinner_dropdown_item, spinnerData)
        {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View
            {
                return setCentered(super.getView(position, convertView, parent))!!
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View
            {
                return setCentered(super.getDropDownView(position, convertView, parent))!!
            }

            private fun setCentered(view: View): View?
            {
                val textView = view.findViewById<View>(android.R.id.text1) as TextView
                textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                textView.isAllCaps = true


                return view
            }
        }


        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        //Lister
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
        {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View, position: Int, id: Long)
            {
                val name = selectedItemView.findViewById<TextView>(android.R.id.text1).text.toString()
                show("Spinner Selected : $name")
                if (name == "-ALL-")
                {

                    recyclerviewAdaptor.filter.filter("")
                } else
                {
                    //   var str = recyclerviewAdaptor.filter
                    recyclerviewAdaptor.filter.filter(name)
                }

            }

            override fun onNothingSelected(parentView: AdapterView<*>?)
            {
                recyclerviewAdaptor.filter.filter("")
            }
        }

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_activity_toolbar, menu)
        val item = menu!!.findItem(R.id.MainMenuSearchButton)
        searchView = item.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener
        {
            override fun onQueryTextSubmit(query: String?): Boolean
            {
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean
            {   //Coroutine added
/*
                if (query.toString().isNotEmpty() || query.toString() != "")
                {
                    showToast(query.toString())
                    recyclerviewAdaptor.filter.filter(query)
                } else
                {
                    recyclerviewAdaptor.filter.filter(spinner.selectedItem.toString())
                }
*/

                if (query.toString().isNotEmpty() || query.toString() != "")
                {
                    recyclerviewAdaptor.filter.filter(query)
                } else if (spinner.selectedItem.toString() != "-ALL-")
                {
                    recyclerviewAdaptor.filter.filter(spinner.selectedItem.toString())
                } else
                {
                    recyclerviewAdaptor.filter.filter("")
                }


                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {

        return when (item.itemId)
        {
            R.id.MainMenuSearchButton ->
            {

                showToast("Search Clicked!!")
                return true
            }

            R.id.MainMenuCartButton ->
            {
                val intent = Intent(this, OpenCartActivity::class.java)
                startActivity(intent)

                return true
            }

            else -> super.onOptionsItemSelected(item)


        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK)
        {
            when(requestCode)
            {
                LOGIN_CODE ->
                {

                    val _user = data!!.getParcelableExtra<User>("USER")

                    if(_user!=null)
                    {

                        val editor: SharedPreferences.Editor = sharedPreferences.edit()
                        user = _user
                        val _userID = _firebaseDatabaseRefAllOrders.push().key!!
                        userID = "${_user.name.trim()}&${_user.phone.trim()}@$_userID"
                        editor.putString(USER_ID, userID)
                        editor.putString(USER_NAME, _user.name)
                        editor.putString(USER_PHONE, _user.phone)
                        editor.putString(USER_ADDRESS, _user.phone)
                        editor.putBoolean(USER_LOGGED_IN, true)
                        editor.apply()
                        editor.commit()

                        setFirebaseCartReference(userID)

/*                        ordersReferenceID = _firebaseDatabaseRefAllOrders.child(userID).child("orders")
                        ordersReferenceID!!.child("Profile").setValue(user)*/


                        drawer_textviewUserName.text = user!!.name
                        drawer_textviewUserPhone.text = user!!.phone
                        drawer_textviewLoginBtn.text = "Logout"
                    }
                }
            }
        }
    }

    private fun setFirebaseCartReference(userID: String)
    {
        ordersReferenceID = _firebaseDatabaseRefAllOrders.child(userID).child("Orders")
        profileReferenceID =  _firebaseDatabaseRefAllOrders.child(userID).child("Profile")
    }


/*
    private fun refresh()
    {

        // val data = "M.P SHARBATI DELUXE ATTA ; ATTA ; 40 ; No Details@MP SHARBATI ATTA ; ATTA ; 38 ; No Details@HARYANA DESI ATTA ; ATTA ; 30 ; No Details@NORMAL FRESH ATTA ; ATTA ; 24 ; No Details@MULTI GRAIN ATTA ; ATTA ; 50 ; No Details@JO ATTA (BARLEY) ; ATTA ; 80 ; No Details@JWAR ATM CHANA ATTA SOYABEAN ATTA ; ATTA ; 80 ; No Details@MAKKI ATTA ; ATTA ; 40 ; No Details@BAJRA ATTA ; ATTA ; 30 ; No Details@RICE POWDER ; ATTA ; 40 ; No Details@URAD DAL ATTA ; ATTA ; 140 ; No Details@MOONS DAL ATTA ; ATTA ; 130 ; No Details@RAGI ATTA ; ATTA ; 80 ; No Details@BESAN ; ATTA ; 90 ; No Details@BESAN (MOTA) ; ATTA ; 90 ; No Details@MAIDA ; ATTA ; 40 ; No Details@SUJI ; ATTA ; 40 ; No Details@DALIA ; ATTA ; 40 ; No Details@OATS ; ATTA ; 40 ; No Details@ARHAR (TOOK) ; PULSES ; 79 ; No Details@MOONS DHULI ; PULSES ; 82 ; No Details@MOONS CHILKA ; PULSES ; 80 ; No Details@MOONS SABUT ; PULSES ; 78 ; No Details@URAD DHULI ; PULSES ; 110 ; No Details@URAD CHILKA ; PULSES ; 87 ; No Details@SABUT MASUR BLACK ; PULSES ; 43 ; No Details@MASUR MALKA ; PULSES ; 53 ; No Details@RAJMA CHITRA, JAMMU ; PULSES ; 52 ; No Details@CHANA DAL ; PULSES ; 53 ; No Details@MIX DAL ; PULSES ; 86 ; No Details@MOTH DAL ; PULSES ; 24 ; No Details@LAL LOBIA, SAFED LOBIA ; PULSES ; 245 ; No Details@MATAR ; PULSES ; 23 ; No Details@MASUR DALLI ; PULSES ; 26 ; No Details@KALA CHANA ; PULSES ; 73 ; No Details@BULI CHANA ; PULSES ; 73 ; No Details@SABUT GARAM MASALA MIND MOTO ; MASALA ; 78 ; No Details@JEERA ; MASALA ; 75 ; No Details@LONG ; MASALA ; 783 ; No Details@KALANOJI ; MASALA ; 34 ; No Details@JAI FAL,JAVITRI ; MASALA ; 43 ; No Details@SUPARI ; MASALA ; 65 ; No Details@TEJ PATTA , KADI PATTA ; MASALA ; 756 ; No Details@KALI MIRCH SABUT ; MASALA ; 768 ; No Details@HALDI, DHANIA, AMCHOOR ; MASALA ; 54 ; No Details@LAL MIRCH SABUT ; MASALA ; 44 ; No Details@SARSO YELLOW, BLACK ; MASALA ; 43 ; No Details@RAI, METH' DANA ; MASALA ; 43 ; No Details@SAUNF MOT', BAREEK ; MASALA ; 53 ; No Details@ORIGANOS ; MASALA ; 36 ; No Details@IMLI,SAUNTH,GOOND ; MASALA ; 63 ; No Details@EXTRA LONG GRAIN 1121 XXXL BASMATI ROYAL RICE ; RICE ; 57 ; No Details@LONG GRAIN 1121 XXL BASMATI DELIGHT RICE ; RICE ; 57 ; No Details@PREMIUM 1121 XL BASMATI RICE ; RICE ; 57 ; No Details@ROZANA BASMATI RICE ; RICE ; 57 ; No Details@BASMATI TIBAR(1121) ; RICE ; 57 ; No Details@BASMATI DUBAR (1121) ; RICE ; 57 ; No Details@SAUNA MASORI RICE ; RICE ; 57 ; No Details@PARMAL DOUBLE SILKY RICE ; RICE ; 57 ; No Details@GOLDEN SELLA XXXL 1121 ; RICE ; 57 ; No Details@GOLDEN SELLA XXL 1121 ; RICE ; 57 ; No Details@BASMATI SUPER MOGRA ; RICE ; 57 ; No Details@IDLI RICE ; RICE ; 57 ; No Details@BROWN RICE ; RICE ; 57 ; No Details@KAJU ; DRY FRUITS ; 76 ; No Details@BADAM ; DRY FRUITS ; 76 ; No Details@PISTA,GREEN PISTA ; DRY FRUITS ; 76 ; No Details@AKROT GIRT, SABUT ; DRY FRUITS ; 76 ; No Details@KHUMANI ANJEER ; DRY FRUITS ; 76 ; No Details@KHARBUJ MAGAJ , TARBOJ MAGAJ ; DRY FRUITS ; 76 ; No Details@CHUWARA ; DRY FRUITS ; 76 ; No Details@GOLA, GOLA POWDER ; DRY FRUITS ; 76 ; No Details@KHASKHAS ; DRY FRUITS ; 76 ; No Details@BAJRA JWAR; GRAINS ; 89 ; No Details@MAKKA .10(BARLEY) ; GRAINS ; 89 ; No Details@RAGI ; GRAINS ; 89 ; No Details@FLEX SEEDS(ALSI) ; GRAINS ; 89 ; No Details@SATANAJ MIX ; GRAINS ; 89 ; No Details@WHEAT SOYABEAN ; GRAINS ; 89 ; No Details@VINEGAR ; OTHERS ; 90; No Details@BHUNA CHANA,CHILKA CHANA STARCH ; OTHERS ; 90; No Details@BOONDI,SOYA BARI ; OTHERS ; 90; No Details@BAKERY BISCUITS , NAMKEENS ; OTHERS ; 90; No Details@PASTA, MACRON'@MITHASODA, BAKING POWDER VERMICELLI,MURMURE ; OTHERS ;45 ; No Details@CORN FLAKES, AMERICAN CORN POHA MOTA, BAREEK ; OTHERS ;45 ; No Details@MOONS FALI DANA ; OTHERS ;45 ; No Details@ANARDANA,SABUT DANA ARAROT ; OTHERS ;45 ; No Details@URAD BARI, MOONS BARI ; OTHERS ;45 ; No Details@TATA SALT ; SALT ; 72 ; No Details@LITE SALT ; SALT ; 72 ; No Details@BLACK SALT ; SALT ; 72 ; No Details@ROCK SALT (SENDHA) ; SALT ; 72 ; No Details@DOUBLE FILTER PREMIUM SUGAR BURR ; SWEETNERS ; 56; No Details@KARARA ; SWEETNERS ; 56; No Details@GUD ; SWEETNERS ; 56; No Details@SHAKKAR ; SWEETNERS ; 56; No Details"
        //  val data = "m.p sharbati deluxe atta ; atta ; 40 ; no details@mp sharbati atta ; atta ; 38 ; no details@haryana desi atta ; atta ; 30 ; no details@normal fresh atta ; atta ; 24 ; no details@multi grain atta ; atta ; 50 ; no details@jo atta (barley) ; atta ; 80 ; no details@jwar atm chana atta soyabean atta ; atta ; 80 ; no details@makki atta ; atta ; 40 ; no details@bajra atta ; atta ; 30 ; no details@rice powder ; atta ; 40 ; no details@urad dal atta ; atta ; 140 ; no details@moons dal atta ; atta ; 130 ; no details@ragi atta ; atta ; 80 ; no details@besan ; atta ; 90 ; no details@besan (mota) ; atta ; 90 ; no details@maida ; atta ; 40 ; no details@suji ; atta ; 40 ; no details@dalia ; atta ; 40 ; no details@oats ; atta ; 40 ; no details@arhar (took) ; pulses ; 79 ; no details@moons dhuli ; pulses ; 82 ; no details@moons chilka ; pulses ; 80 ; no details@moons sabut ; pulses ; 78 ; no details@urad dhuli ; pulses ; 110 ; no details@urad chilka ; pulses ; 87 ; no details@sabut masur black ; pulses ; 43 ; no details@masur malka ; pulses ; 53 ; no details@rajma chitra, jammu ; pulses ; 52 ; no details@chana dal ; pulses ; 53 ; no details@mix dal ; pulses ; 86 ; no details@moth dal ; pulses ; 24 ; no details@lal lobia, safed lobia ; pulses ; 245 ; no details@matar ; pulses ; 23 ; no details@masur dalli ; pulses ; 26 ; no details@kala chana ; pulses ; 73 ; no details@buli chana ; pulses ; 73 ; no details@sabut garam masala mind moto ; masala ; 78 ; no details@jeera ; masala ; 75 ; no details@long ; masala ; 783 ; no details@kalanoji ; masala ; 34 ; no details@jai fal,javitri ; masala ; 43 ; no details@supari ; masala ; 65 ; no details@tej patta , kadi patta ; masala ; 756 ; no details@kali mirch sabut ; masala ; 768 ; no details@haldi, dhania, amchoor ; masala ; 54 ; no details@lal mirch sabut ; masala ; 44 ; no details@sarso yellow, black ; masala ; 43 ; no details@rai, meth' dana ; masala ; 43 ; no details@saunf mot', bareek ; masala ; 53 ; no details@origanos ; masala ; 36 ; no details@imli,saunth,goond ; masala ; 63 ; no details@extra long grain 1121 xxxl basmati royal rice ; rice ; 57 ; no details@long grain 1121 xxl basmati delight rice ; rice ; 57 ; no details@premium 1121 xl basmati rice ; rice ; 57 ; no details@rozana basmati rice ; rice ; 57 ; no details@basmati tibar(1121) ; rice ; 57 ; no details@basmati dubar (1121) ; rice ; 57 ; no details@sauna masori rice ; rice ; 57 ; no details@parmal double silky rice ; rice ; 57 ; no details@golden sella xxxl 1121 ; rice ; 57 ; no details@golden sella xxl 1121 ; rice ; 57 ; no details@basmati super mogra ; rice ; 57 ; no details@idli rice ; rice ; 57 ; no details@brown rice ; rice ; 57 ; no details@kaju ; dry fruits ; 76 ; no details@badam ; dry fruits ; 76 ; no details@pista,green pista ; dry fruits ; 76 ; no details@akrot girt, sabut ; dry fruits ; 76 ; no details@khumani anjeer ; dry fruits ; 76 ; no details@kharbuj magaj , tarboj magaj ; dry fruits ; 76 ; no details@chuwara ; dry fruits ; 76 ; no details@gola, gola powder ; dry fruits ; 76 ; no details@khaskhas ; dry fruits ; 76 ; no details@bajra jwar; grains ; 89 ; no details@makka .10(barley) ; grains ; 89 ; no details@ragi ; grains ; 89 ; no details@flex seeds(alsi) ; grains ; 89 ; no details@satanaj mix ; grains ; 89 ; no details@wheat soyabean ; grains ; 89 ; no details@vinegar ; others ; 90; no details@bhuna chana,chilka chana starch ; others ; 90; no details@boondi,soya bari ; others ; 90; no details@bakery biscuits , namkeens ; others ; 90; no details@pasta, macron'@mithasoda, baking powder vermicelli,murmure ; others ;45 ; no details@corn flakes, american corn poha mota, bareek ; others ;45 ; no details@moons fali dana ; others ;45 ; no details@anardana,sabut dana ararot ; others ;45 ; no details@urad bari, moons bari ; others ;45 ; no details@tata salt ; salt ; 72 ; no details@lite salt ; salt ; 72 ; no details@black salt ; salt ; 72 ; no details@rock salt (sendha) ; salt ; 72 ; no details@double filter premium sugar burr ; sweetners ; 56; no details@karara ; sweetners ; 56; no details@gud ; sweetners ; 56; no details@shakkar ; sweetners ; 56; no details"
        val data = "M.p Sharbati Deluxe Atta ; Atta ; 40 ; No Details@mp Sharbati Atta ; Atta ; 38 ; No Details@haryana Desi Atta ; Atta ; 30 ; No Details@normal Fresh Atta ; Atta ; 24 ; No Details@multi Grain Atta ; Atta ; 50 ; No Details@jo Atta (Barley) ; Atta ; 80 ; No Details@jwar Atm Chana Atta Soyabean Atta ; Atta ; 80 ; No Details@makki Atta ; Atta ; 40 ; No Details@bajra Atta ; Atta ; 30 ; No Details@rice Powder ; Atta ; 40 ; No Details@urad Dal Atta ; Atta ; 140 ; No Details@moons Dal Atta ; Atta ; 130 ; No Details@ragi Atta ; Atta ; 80 ; No Details@besan ; Atta ; 90 ; No Details@besan (Mota) ; Atta ; 90 ; No Details@maida ; Atta ; 40 ; No Details@suji ; Atta ; 40 ; No Details@dalia ; Atta ; 40 ; No Details@oats ; Atta ; 40 ; No Details@arhar (Took) ; Pulses ; 79 ; No Details@moons Dhuli ; Pulses ; 82 ; No Details@moons Chilka ; Pulses ; 80 ; No Details@moons Sabut ; Pulses ; 78 ; No Details@urad Dhuli ; Pulses ; 110 ; No Details@urad Chilka ; Pulses ; 87 ; No Details@sabut Masur Black ; Pulses ; 43 ; No Details@masur Malka ; Pulses ; 53 ; No Details@rajma Chitra, Jammu ; Pulses ; 52 ; No Details@chana Dal ; Pulses ; 53 ; No Details@mix Dal ; Pulses ; 86 ; No Details@moth Dal ; Pulses ; 24 ; No Details@lal Lobia, Safed Lobia ; Pulses ; 245 ; No Details@matar ; Pulses ; 23 ; No Details@masur Dalli ; Pulses ; 26 ; No Details@kala Chana ; Pulses ; 73 ; No Details@buli Chana ; Pulses ; 73 ; No Details@sabut Garam Masala Mind Moto ; Masala ; 78 ; No Details@jeera ; Masala ; 75 ; No Details@long ; Masala ; 783 ; No Details@kalanoji ; Masala ; 34 ; No Details@jai Fal,javitri ; Masala ; 43 ; No Details@supari ; Masala ; 65 ; No Details@tej Patta , Kadi Patta ; Masala ; 756 ; No Details@kali Mirch Sabut ; Masala ; 768 ; No Details@haldi, Dhania, Amchoor ; Masala ; 54 ; No Details@lal Mirch Sabut ; Masala ; 44 ; No Details@sarso Yellow, Black ; Masala ; 43 ; No Details@rai, Meth' Dana ; Masala ; 43 ; No Details@saunf Mot', Bareek ; Masala ; 53 ; No Details@origanos ; Masala ; 36 ; No Details@imli,saunth,goond ; Masala ; 63 ; No Details@extra Long Grain 1121 Xxxl Basmati Royal Rice ; Rice ; 57 ; No Details@long Grain 1121 Xxl Basmati Delight Rice ; Rice ; 57 ; No Details@premium 1121 Xl Basmati Rice ; Rice ; 57 ; No Details@rozana Basmati Rice ; Rice ; 57 ; No Details@basmati Tibar(1121) ; Rice ; 57 ; No Details@basmati Dubar (1121) ; Rice ; 57 ; No Details@sauna Masori Rice ; Rice ; 57 ; No Details@parmal Double Silky Rice ; Rice ; 57 ; No Details@golden Sella Xxxl 1121 ; Rice ; 57 ; No Details@golden Sella Xxl 1121 ; Rice ; 57 ; No Details@basmati Super Mogra ; Rice ; 57 ; No Details@idli Rice ; Rice ; 57 ; No Details@brown Rice ; Rice ; 57 ; No Details@kaju ; Dry Fruits ; 76 ; No Details@badam ; Dry Fruits ; 76 ; No Details@pista,green Pista ; Dry Fruits ; 76 ; No Details@akrot Girt, Sabut ; Dry Fruits ; 76 ; No Details@khumani Anjeer ; Dry Fruits ; 76 ; No Details@kharbuj Magaj , Tarboj Magaj ; Dry Fruits ; 76 ; No Details@chuwara ; Dry Fruits ; 76 ; No Details@gola, Gola Powder ; Dry Fruits ; 76 ; No Details@khaskhas ; Dry Fruits ; 76 ; No Details@bajra Jwar; Grains ; 89 ; No Details@makka .10(Barley) ; Grains ; 89 ; No Details@ragi ; Grains ; 89 ; No Details@flex Seeds(Alsi) ; Grains ; 89 ; No Details@satanaj Mix ; Grains ; 89 ; No Details@wheat Soyabean ; Grains ; 89 ; No Details@vinegar ; Others ; 90; No Details@bhuna Chana,chilka Chana Starch ; Others ; 90; No Details@boondi,soya Bari ; Others ; 90; No Details@bakery Biscuits , Namkeens ; Others ; 90; No Details@pasta, Macron'@mithasoda, Baking Powder Vermicelli,murmure ; Others ;45 ; No Details@corn Flakes, American Corn Poha Mota, Bareek ; Others ;45 ; No Details@moons Fali Dana ; Others ;45 ; No Details@anardana,sabut Dana Ararot ; Others ;45 ; No Details@urad Bari, Moons Bari ; Others ;45 ; No Details@tata Salt ; Salt ; 72 ; No Details@lite Salt ; Salt ; 72 ; No Details@black Salt ; Salt ; 72 ; No Details@rock Salt (Sendha) ; Salt ; 72 ; No Details@double Filter Premium Sugar Burr ; Sweetners ; 56; No Details@karara ; Sweetners ; 56; No Details@gud ; Sweetners ; 56; No Details@shakkar ; Sweetners ; 56; No Details"
        val productEntityList = ArrayList<CartItem>()

        val productLines = data.split('@')
        for (productLine in productLines)
        {
            show("Data => $productLine")
            val productData = productLine.split(';')

            show("Working=> ${productData[0]}")
            val productEntity = CartItem()
            try
            {
                productEntity.name = productData[0]
                productEntity.image = getColorBitmap()
                productEntity.category = productData[1]
                productEntity.price = productData[2]
                productEntity.details = productData[3]
                productEntityList.add(productEntity)

                CoroutineScope(Dispatchers.IO).launch {
                    myDatabase!!.productDao().addNewProduct(productEntity)
                    show("Database Added - ${productEntity.name}")
                    CoroutineScope(Dispatchers.Main).launch {
                        recyclerviewAdaptor.addProduct(productEntity, 0)
                        show("RecycleView  Added - ${productEntity.name}")
                    }
                }

            } catch (e: IndexOutOfBoundsException)
            {
                productEntity.name = productData[0]
                productEntityList.add(productEntity)
            }

        }


        // recyclerviewAdaptor.addProductList(productEntityList)


    }
*/

    private fun getColorBitmap(): Bitmap
    {
        val random = Random()
        val color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))

        val bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(color)

        return bitmap

    }

    private fun show(message: String)
    {
        Log.i("###", message)
    }

    private fun showToast(msg: String)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }


    private fun showSnakeBar(msg: String, res: Int)
    {
        val snackbar = Snackbar.make(reycleviewMain, msg, Snackbar.LENGTH_LONG)
        val sbView: View = snackbar.view

        sbView.setBackgroundColor(ContextCompat.getColor(this, res))

        val textView = sbView.findViewById(R.id.snackbar_text) as TextView
        textView.textSize = 15f
        textView.setTextColor(Color.WHITE)
        snackbar.show()
    }

    override fun onClick(product: Product)
    {

    }

    override fun onAddToCartClick(product: Product)
    {
        addProductToCart(product)


    }

    private fun addProductToCart(product: Product)
    {
        val cartItem = CartItem()
        cartItem.name = product.name
        cartItem.category = product.category
        cartItem.details = product.details
        cartItem.key = product.key
        cartItem.price = product.price
        cartItem.quantity = product.quantity
        CoroutineScope(Dispatchers.IO).launch {
            try
            {
                cartDatabase.cartDao().addNewCartItem(cartItem)
                showSnakeBar("[+${cartItem.quantity}] Product Added to Cart!",R.color.green)
            } catch (e:SQLiteConstraintException)
            {
                showSnakeBar("Product already Added, Check Cart",R.color.black)
            }

        }

    }


}