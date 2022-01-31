package com.example.appshop

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.DOMStringList
import java.lang.Exception
import java.net.URI
import java.util.*
import kotlin.concurrent.schedule


class RestaurantHolidayList : AppCompatActivity() {

    companion object{
        const val getHolidayInfoId: Int = 18
        var arrayIndex: Int = -1
    }

    private val uri = WsClient.serverRemote
    private var client = HolidayListWsClient(this, uri)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_holiday_list)
    }

    override fun onResume() {
        super.onResume()
        client.connect()

        val errorDisplay: TextView = findViewById(R.id.errorDisplay)

        arrayIndex = intent.getIntExtra("arrayIndex", 0)

    }

    override fun onRestart() {
        super.onRestart()
        client = HolidayListWsClient(this, uri)
    }
}

class HolidayListWsClient(private val activity: Activity, uri: URI) : WsClient(uri) {
    var isReceived = false

    private fun sendReqGetRestaurantInfo(){
        val getInfoParams = JSONObject()
        getInfoParams.put("searchBy", "restaurant_id")
        getInfoParams.put("restaurant_id", Restaurant.globalRestaurantId)
        getInfoParams.put("token", Restaurant.globalToken)
        val getInfoRequest =
            this.createJsonrpcReq("getInfo/restaurant/basic",
                RestaurantHolidayList.getHolidayInfoId, getInfoParams)

        this.send(getInfoRequest.toString())
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        super.onOpen(handshakedata)
        this.sendReqGetRestaurantInfo()
    }

    override fun onMessage(message: String?) {
        super.onMessage(message)
        Log.i(javaClass.simpleName, "msg arrived")
        Log.i(javaClass.simpleName, "$message")

        val wholeMsg = JSONObject("$message")
        val resId: Int = wholeMsg.getInt("id")
        val result: JSONObject = wholeMsg.getJSONObject("result")
        if (resId == RestaurantHolidayList.getHolidayInfoId) {
            val holidays: JSONArray = result.getJSONArray("holidays")

            val name = mutableListOf<String>()
            for (index in 0 until holidays.length()) {
                name.add(holidays.get(index).toString())
            }

            activity.runOnUiThread {
                val listView = activity.findViewById<ListView>(R.id.listView)

                //ArrayAdapter
                val adapter =
                    ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, name)

                listView.adapter = adapter
            }
            if (resId == RestaurantSeatList.getSeatInfoId) {
                this.isReceived = true
            }
        }

    }
}