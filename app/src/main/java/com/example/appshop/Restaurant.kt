package com.example.appshop

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Button
import org.json.JSONObject
import java.net.URI

class Restaurant : AppCompatActivity() {
    private var restaurant_id: Int = -1
    private var restaurant_name: String = ""
    private var email_addr: String = ""
    private var address: String = ""

    companion object{
        const val logoutReqId: Int = 3
        const val getRestaurantInfoId: Int = 7
    }

    private val uri = WsClient.serverRemote
    private var client = RestaurantTopWsClient(this, uri)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant)
    }

    private fun fetchClientInfo(client:RestaurantTopWsClient){
        this.restaurant_id = client.user_id
        this.restaurant_name = client.user_name
        this.email_addr = client.email_addr
        this.address = client.address
    }

    override fun onResume() {
        super.onResume()
        client.connect()

        val token = intent.getStringExtra("token")!!
        val tokenExpiry = intent.getStringExtra("expire")
        val restaurantName = intent.getStringExtra("restaurantName")
        this.restaurant_name = restaurantName!!

        val getRestaurantInfo = Runnable {
            while (!client.isOpen){
                //do nothing
                //wait until websocket open
            }
            //the connection openings is guaranteed -> attach no error handler
            client.sendReqGetRestaurantInfoByName(token, this.restaurant_name)
            return@Runnable
        }
        Thread( getRestaurantInfo ).start()

        Log.i(javaClass.simpleName, "token recved $token")
        Log.i(javaClass.simpleName, "token expiry $tokenExpiry")
        Log.i(javaClass.simpleName, "restaurantName: $restaurantName")

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