package com.example.appshop

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import java.net.URI
import org.json.*

class RestaurantLogin : AppCompatActivity() {
    companion object{
        const val loginReqId: Int = 1
    }

    private val uri = WsClient.serverRemote
    private var client = LoginWsClient(this, uri)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}