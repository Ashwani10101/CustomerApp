package com.ash.customerapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.ash.customerapp.models.User
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity()
{

    private lateinit var editTextName: TextInputEditText
    private lateinit var editTextPhone: TextInputEditText
    private lateinit var editTextAddress: TextInputEditText
    private lateinit var btnskip: Button
    private lateinit var btnlogin: Button
    private lateinit var imageViewMain: ImageView



    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        CoroutineScope(Dispatchers.Default).launch {
            //initSharePreferences()
            initGUI()
            CoroutineScope(Dispatchers.Main).launch {
                btnskip.isEnabled = true
                btnlogin.isEnabled = true
            }

        }



    }

    private fun initGUI()
    {
        editTextName = findViewById(R.id.loginActivity_textviewName)
        editTextPhone = findViewById(R.id.loginActivity_textviewPhone)
        editTextAddress = findViewById(R.id.loginActivity_textviewAddress)

        btnskip = findViewById(R.id.loginActivity_btnSkip)
        btnlogin = findViewById(R.id.loginActivity_btnLogin)

        imageViewMain = findViewById(R.id.loginActivity_avMain)


        btnlogin.setOnClickListener { login() }
        btnskip.setOnClickListener { skip() }

    }



    private fun skip()
    {
        finish()
    }

    private fun login()
    {
        var allowLogin = true
        val user = User()

        user.address =  editTextAddress.text.toString()
        if(editTextName.text.toString().trim().isNotEmpty())
        {
            user.name =  editTextName.text.toString()
        } else
        {
            editTextName.error = "Enter user name"
            allowLogin = false
        }

        if(editTextPhone.text.toString().trim().isNotEmpty())
        {
            user.phone =  editTextPhone.text.toString()
        } else
        {
            editTextPhone.error = "Enter phone number"
            allowLogin = false
        }

        if(editTextAddress.text.toString().trim().isNotEmpty())
        {
            user.address =  editTextAddress.text.toString()
        } else
        {
            editTextAddress.error = "Enter full address"
            allowLogin = false
        }

        if(allowLogin)
        {

            val intent = Intent()
            intent.putExtra("USER", user)
            setResult(Activity.RESULT_OK, intent)
            finish()




        }


    }


/*    private fun initSharePreferences()
    {
        sharedPreferences = getSharedPreferences(USER_INFO_SHARE_PREFERENCE, Context.MODE_PRIVATE)

        val isUserLoggedIn = sharedPreferences.getBoolean(USER_LOGGED_IN, false)
        if(isUserLoggedIn)
        {
            skip()
        }


    }*/

}