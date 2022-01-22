package com.example.appshop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class RestaurantAccountSetting : AppCompatActivity() {
    companion object{
        const val SelectReqId: Int = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_account_setting)

        val buttonToHome: Button = findViewById(R.id.buttonHome)
        val buttonToAccount: Button = findViewById(R.id.buttonAccount)
        val buttonToSeat: Button = findViewById(R.id.buttonSeat)
        val buttonToReserve: Button = findViewById(R.id.buttonReserve)
        val buttonToAccountInfoChange: Button = findViewById(R.id.buttonAccountInfoChange)
        val buttonToHoliday: Button = findViewById(R.id.buttonHoliday)

        //bottom footer event listeners
        buttonToHome.setOnClickListener {
            val intent = Intent(this@RestaurantAccountSetting, Restaurant::class.java)
            startActivity(intent)
        }

        buttonToAccount.setOnClickListener {
            val intent = Intent(this@RestaurantAccountSetting, RestaurantAccountSetting::class.java)
            startActivity(intent)
        }

        buttonToSeat.setOnClickListener {
            TODO("not yet implemented")
        }

        buttonToReserve.setOnClickListener {
            TODO("not yet implemented")
        }

        //center event listeners
        buttonToAccountInfoChange.setOnClickListener{
            val intent = Intent(this@RestaurantAccountSetting, RestaurantAccountInfoChange)
            startActivity(intent)
        }

        buttonToHoliday.setOnClickListener {
            val intent = Intent(this@RestaurantAccountSetting, RestaurantHoliday)
            startActivity(intent)
        }
    }
}