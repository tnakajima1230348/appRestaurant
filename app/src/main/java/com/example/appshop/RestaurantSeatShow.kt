package com.example.appshop

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.net.URI
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class RestaurantSeatShow : AppCompatActivity() {

    companion object{
        const val getSeatInfoId: Int = 8
    }

    private val uri = WsClient.serverRemote
    private var client = RestaurantInfoWsClient(this, uri)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_seat_show)
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
        val getInfoRequest = client.createJsonrpcReq("getInfo/restaurant/basic", getSeatInfoId, getInfoParams)

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
            if(client.isReceived){
                errorDisplay.visibility = View.INVISIBLE
                this.cancel()
            }
        }

        val wholeMsg = JSONObject()
        val result: JSONObject = wholeMsg.getJSONObject("result")
        val seats:JSONArray = result.getJSONArray("seats")
        val names = arrayOf<String>()

        for (seat in seats){
            names[seat.index] = seat.getString("seat_name")
        }

        buttonAccountInfoChange.setOnClickListener {
            if(client.isReceived){
                val intent = Intent(this@RestaurantAccountInfoShow, RestaurantAccountInfoChange::class.java)
                intent.putExtra("restaurantId", client.restaurantId)
                intent.putExtra("restaurantName", client.restaurantName)
                intent.putExtra("emailAddr", client.emailAddr)
                intent.putExtra("address", client.address)
                intent.putExtra("time_open", client.time_open)
                intent.putExtra("time_close", client.time_close)
                intent.putExtra("features", client.features)
                intent.putExtra("token", token)
                startActivity(intent)
                client.close(WsClient.NORMAL_CLOSURE)
            }else{
                return@setOnClickListener
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        client = RestaurantInfoWsClient(this, uri)
    }


}

class RestaurantInfoWsClient(private val activity: Activity, uri: URI) : WsClient(uri) {
    var restaurantId: Int = -1
    var restaurantName = ""
    var emailAddr = ""
    var address = ""
    var time_open: String = ""
    var time_close: String = ""
    var features: String = ""
    var isReceived = false

    private val errorDisplay: TextView by lazy { activity.findViewById(R.id.errorDisplay) }
    private val txtRestaurantName: TextView by lazy { activity.findViewById(R.id.textBoxRestaurantName) }
    private val txtEmail: TextView by lazy { activity.findViewById(R.id.textBoxRestaurantEmail) }
    private val txtAddress: TextView by lazy { activity.findViewById(R.id.textBoxRestaurantAddress) }
    private val txtTimeOpen: TextView by lazy { activity.findViewById(R.id.textBoxRestaurantTimeOpen) }
    private val txtTimeClose: TextView by lazy { activity.findViewById(R.id.textBoxRestaurantTimeColse) }
    private val txtFeatures: TextView by lazy { activity.findViewById(R.id.textBoxRestaurantFeatures) }

    override fun onMessage(message: String?) {
        super.onMessage(message)
        Log.i(javaClass.simpleName, "msg arrived")
        Log.i(javaClass.simpleName, "$message")

        val wholeMsg = JSONObject("$message")
        val resId: Int = wholeMsg.getInt("id")
        val result: JSONObject = wholeMsg.getJSONObject("result")
        val status: String = result.getString("status")

        if (resId == RestaurantAccountInfoShow.getRestaurantInfoId) {
            this.isReceived = true
            if (status == "success") {
                this.restaurantId = result.getInt("restaurant_id")
                this.restaurantName = result.getString("restaurant_name")
                this.emailAddr = result.getString("email_addr")
                this.address = result.getString("address")
                this.time_open = result.getString("time_open")
                this.time_close = result.getString("time_close")
                this.features = result.getString("features")

                activity.runOnUiThread {
                    txtRestaurantName.text = this.restaurantName
                    txtEmail.text = this.emailAddr
                    txtAddress.text = this.address
                    txtTimeOpen.text = this.time_open
                    txtTimeClose.text = this.time_close
                    txtFeatures.text = this.features
                }
            } else if (status == "error") {
                activity.runOnUiThread {
                    errorDisplay.text = "アカウント情報を取得できません"
                    errorDisplay.visibility = View.INVISIBLE
                }
            }
        }
    }
}


