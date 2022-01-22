package com.example.appshop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Restaurant : AppCompatActivity() {
    companion object{
        const val RestaurantReqId: Int = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant)

        val buttonToHome: Button = findViewById(R.id.buttonHome)
        val buttonToAccount: Button = findViewById(R.id.buttonAccount)
        val buttonToSeat: Button = findViewById(R.id.buttonSeat)
        val buttonToReserve: Button = findViewById(R.id.buttonReserve)

        //bottom footer event listeners
        buttonToHome.setOnClickListener {
            val intent = Intent(this@Restaurant, Restaurant::class.java)
            startActivity(intent)
        }

        buttonToAccount.setOnClickListener{
            val intent = Intent(this@Restaurant, RestaurantAccountSelect::class.java)
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