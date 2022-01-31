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


class RestaurantReservationList : AppCompatActivity() {

    companion object{
        const val getSeatInfoId: Int = 17
        var arrayIndex: Int = -1
    }

    private val uri = WsClient.serverRemote
    private var client = ReservationListWsClient(this, uri)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_seat_list)
    }

    override fun onResume() {
        super.onResume()
        client.connect()

        val errorDisplay: TextView = findViewById(R.id.errorDisplay)
        val buttonAddSeat: Button = findViewById(R.id.buttonAddSeat)

        arrayIndex = intent.getIntExtra("arrayIndex", 0)

        val token = Restaurant.globalToken
        val restaurantId = Restaurant.globalRestaurantId

        val getInfoParams = JSONObject()
        getInfoParams.put("searchBy", "restaurant_id")
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
        client = ReservationListWsClient(this, uri)
    }
}

class ReservationListWsClient(private val activity: Activity, uri: URI) : WsClient(uri) {
    var isReceived = false

    override fun onMessage(message: String?) {
        super.onMessage(message)
        Log.i(javaClass.simpleName, "msg arrived")
        Log.i(javaClass.simpleName, "$message")

        val wholeMsg = JSONObject("$message")
        val resId: Int = wholeMsg.getInt("id")
        val result: JSONObject = wholeMsg.getJSONObject("result")
        val seats: JSONArray = result.getJSONArray("seats")
        val seat: JSONObject = seats.getJSONObject(RestaurantReservationList.arrayIndex)
        val reservations:JSONArray = seat.getJSONArray("reservations")
        val number = arrayOf<Int>()
        var i = 1

        for (index in 0 until reservations.length()) {
            number[index] = i
            ++i
        }

        activity.runOnUiThread{
            val listView = activity.findViewById<ListView>(R.id.listView)

            //ArrayAdapter
            val adapter = ArrayAdapter<Int>(activity, android.R.layout.simple_list_item_1, number)

            listView.adapter = adapter

            listView.setOnItemClickListener { parent, view, position, id ->

                if(this.isReceived){
                    val intent = Intent(activity, RestaurantReservationShow::class.java)
                    intent.putExtra("arrayIndex", RestaurantReservationList.arrayIndex)
                    intent.putExtra("arrayIndex2", position)
                    intent.putExtra("token", Restaurant.globalToken)
                    activity.startActivity(intent)
                    this.close(WsClient.NORMAL_CLOSURE)
                }else{
                    return@setOnItemClickListener
                }
            }
        }
        if (resId == RestaurantSeatList.getSeatInfoId) {
            this.isReceived = true
        }
    }
}
