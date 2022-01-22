package com.example.appshop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class RestaurantAccountInfoChange : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_account_info_change)

        val buttonToHome: Button = findViewById(R.id.buttonHome)
        val buttonToAccount: Button = findViewById(R.id.buttonAccount)
        val buttonToSeat: Button = findViewById(R.id.buttonSeat)
        val buttonToReserve: Button = findViewById(R.id.buttonReserve)

        //bottom footer event listeners
        buttonToHome.setOnClickListener {
            val intent = Intent(this@RestaurantAccountInfoChange, Restaurant::class.java)
            startActivity(intent)
        }

        buttonToAccount.setOnClickListener{
            val intent = Intent(this@RestaurantAccountInfoChange, RestaurantAccountSetting::class.java)
            startActivity(intent)
        }

        buttonToSeat.setOnClickListener {
            TODO("not yet implemented")
        }

        buttonToReserve.setOnClickListener {
            TODO("not yet implemented")
        }
    }
}