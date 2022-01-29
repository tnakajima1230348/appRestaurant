package com.example.appshop

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.net.URI
import java.util.*
import kotlin.concurrent.schedule




class RestaurantSeatList : AppCompatActivity() {

    companion object{
        const val getSeatInfoId: Int = 8
    }

    private val uri = WsClient.serverRemote
    private var client = SeatListWsClient(this, uri)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_seat_list)
        client.connect()
    }

    override fun onResume() {
        super.onResume()

        val errorDisplay: TextView = findViewById(R.id.errorDisplay)

        val token = Restaurant.globalToken
        val restaurantId = Restaurant.globalRestaurantId

        val getInfoParams = JSONObject()
        getInfoParams.put("restaurant_id", restaurantId)
        getInfoParams.put("token", token)
        val getInfoRequest =
            client.createJsonrpcReq("getInfo/restaurant/basic", getSeatInfoId, getInfoParams)

        //attempt to send until connection established
        Timer().schedule(50, 200) {
            Log.i(javaClass.simpleName, "set req ${getInfoRequest.toString()}")
            try {
                if (client.isClosed) {
                    client.reconnect()
                }
                client.send(getInfoRequest.toString())
                errorDisplay.text = "情報取得中..."
                errorDisplay.visibility = View.VISIBLE
            } catch (ex: Exception) {
                Log.i(javaClass.simpleName, "send failed")
                Log.i(javaClass.simpleName, "$ex")
            }
            // if msg arrived
            if (client.isReceived) {
                errorDisplay.visibility = View.INVISIBLE
                this.cancel()
            }
        }

        val wholeMsg = JSONObject()
        val result: JSONObject = wholeMsg.getJSONObject("result")
        val seats: JSONArray = result.getJSONArray("seats")
        val names = arrayOf<String>()

        for (index in 0 until seats.length()) {
            val seat: JSONObject = seats.getJSONObject(index)
            names[index] = seat.getString("seat_name")
        }

        val listView = findViewById<ListView>(R.id.list_view)

        //ArrayAdapter
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names)

        listView.adapter = adapter

        listView.setOnItemClickListener { parent, view, position, id ->

            if(client.isReceived){
                val intent = Intent(this@RestaurantSeatList, RestaurantSeatShow::class.java)
                intent.putExtra("arrayIndex", position)
                intent.putExtra("token", token)
                startActivity(intent)
                client.close(WsClient.NORMAL_CLOSURE)
            }else{
                return@setOnItemClickListener
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        client = SeatListWsClient(this, uri)
    }
}

class SeatListWsClient(private val activity: Activity, uri: URI) : WsClient(uri) {
    var isReceived = false

    override fun onMessage(message: String?) {
        super.onMessage(message)
        Log.i(javaClass.simpleName, "msg arrived")
        Log.i(javaClass.simpleName, "$message")

        val wholeMsg = JSONObject("$message")
        val resId: Int = wholeMsg.getInt("id")

        if (resId == RestaurantSeatList.getSeatInfoId) {
            this.isReceived = true
        }
    }
}


