package com.example.appshop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import java.lang.Exception

class ShowResult : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_result)
    }

    override fun onResume() {
        super.onResume()

        val message = intent.getStringExtra("message")
        val transitionBtnMessage = intent.getStringExtra("transitionBtnMessage")
        val isBeforeLogin = intent.getBooleanExtra("isBeforeLogin", true)

        val txtMessage: TextView = findViewById(R.id.message)
        val transitionBtn: Button = findViewById(R.id.transitionButton)

        txtMessage.text = message
        transitionBtn.text = transitionBtnMessage

        transitionBtn.setOnClickListener {
            intent = if(isBeforeLogin){
                Intent(this@ShowResult, RestaurantLogin::class.java)
            }else{
                Intent(this@ShowResult, Restaurant::class.java)
            }

            startActivity(intent)
            finish()
        }
    }
}
