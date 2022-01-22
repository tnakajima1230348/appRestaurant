package com.example.appshop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.Toast

class RestaurantHoliday : AppCompatActivity() {
    companion object{
        const val HolidayReqId: Int = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_holiday)

        val buttonToHome: Button = findViewById(R.id.buttonHome)
        val buttonToAccount: Button = findViewById(R.id.buttonAccount)
        val buttonToSeat: Button = findViewById(R.id.buttonSeat)
        val buttonToReserve: Button = findViewById(R.id.buttonReserve)
        val calender: CalendarView = findViewById(R.id.calendar)

        calender.setOnDateChangeListener { view, year, month, dayOfMonth ->
            Toast.makeText(this, "" + year + "/" + (month+1) + "/" + dayOfMonth, Toast.LENGTH_LONG).show()
        }

        //bottom footer event listeners
        buttonToHome.setOnClickListener {
            val intent = Intent(this@RestaurantHoliday, Restaurant::class.java)
            startActivity(intent)
        }

        buttonToAccount.setOnClickListener{
            val intent = Intent(this@RestaurantHoliday, RestaurantAccountSetting::class.java)
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