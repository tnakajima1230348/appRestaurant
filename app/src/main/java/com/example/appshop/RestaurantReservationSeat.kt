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
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.net.URI
import java.util.*
import kotlin.concurrent.schedule


class RestaurantReservationSeat : AppCompatActivity() {

    companion object{
        const val getSeatInfoId: Int = 16
    }

    private val uri = WsClient.serverRemote
    private var client = ReservationSeatWsClient(this, uri)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_reservation_seat)
    }

    override fun onResume() {
        super.onResume()
        client.connect()

        val errorDisplay: TextView = findViewById(R.id.errorDisplay)

        val token = Restaurant.globalToken
        val restaurantId = Restaurant.globalRestaurantId

        val getInfoParams = JSONObject()
        getInfoParams.put("restaurant_id", restaurantId)
        getInfoParams.put("token", token)
        val getInfoRequest =
            client.createJsonrpcReq("getInfo/restaurant/seats", getSeatInfoId, getInfoParams)

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
    }

    override fun onRestart() {
        super.onRestart()
        client = ReservationSeatWsClient(this, uri)
    }
}

class ReservationSeatWsClient(private val activity: Activity, uri: URI) : WsClient(uri) {
    var isReceived = false

    override fun onMessage(message: String?) {
        super.onMessage(message)
        Log.i(javaClass.simpleName, "msg arrived")
        Log.i(javaClass.simpleName, "$message")

        val wholeMsg = JSONObject("$message")
        val resId: Int = wholeMsg.getInt("id")
        val result: JSONObject = wholeMsg.getJSONObject("result")

        if(resId == RestaurantReservationSeat.getSeatInfoId){
            this.isReceived = true
            val seats: JSONArray = result.getJSONArray("seats")
            val name = mutableListOf<String>()

            if(seats.length() != 0){
                for (index in 0 until seats.length()) {
                    val seat:JSONObject = seats.getJSONObject(index)
                    name.add(seat.getString("seat_name"))
                }

                activity.runOnUiThread{
                    val listView = activity.findViewById<ListView>(R.id.listView)

                    //ArrayAdapter
                    val adapter = ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, name)

                    listView.adapter = adapter

                    listView.setOnItemClickListener { parent, view, position, id ->

                        if(this.isReceived){
                            val intent = Intent(activity, RestaurantReservationList::class.java)
                            intent.putExtra("seatId", seats.getJSONObject(position).getInt("seat_id"))
                            intent.putExtra("token", Restaurant.globalToken)
                            activity.startActivity(intent)
                            this.close(WsClient.NORMAL_CLOSURE)
                        }else{
                            return@setOnItemClickListener
                        }
                    }
                }
            }
        }
    }
}
