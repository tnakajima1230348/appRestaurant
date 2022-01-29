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
import kotlin.concurrent.schedule

class RestaurantSeatShow : AppCompatActivity() {

    companion object{
        const val getSeatInfoId: Int = 10
    }

    private val uri = WsClient.serverRemote
    private var client = SeatInfoWsClient(this, uri)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_seat_show)
        client.connect()
    }

    override fun onResume() {
        super.onResume()

        val errorDisplay: TextView = findViewById(R.id.errorDisplay)
        val buttonSeatInfoChange: Button = findViewById(R.id.buttonSeatInfoChange)


        val arrayIndex = intent.getIntExtra("arrayIndex", 0)
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

        buttonSeatInfoChange.setOnClickListener {
            if(client.isReceived){
                val intent = Intent(this@RestaurantSeatShow, RestaurantSeatShow::class.java)
                intent.putExtra("seatId", client.seatId)
                intent.putExtra("seatName", client.seatName)
                intent.putExtra("restaurantId", client.restaurantId)
                intent.putExtra("capacity", client.capacity)
                intent.putExtra("is_filled", client.isFilled)
                intent.putExtra("time_start", client.timeStart)
                intent.putExtra("staying_times", client.stayingTimes)
                intent.putExtra("avg_staying_time", client.avgStayTime)
                intent.putExtra("feature", client.feature)
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
        client = SeatInfoWsClient(this, uri)
    }


}

class SeatInfoWsClient(private val activity: Activity, uri: URI) : WsClient(uri) {
    var seatId: Int = -1
    var seatName = ""
    var restaurantId: Int = -1
    var capacity = ""
    var isFilled: Boolean = false
    var fill = "空"
    var timeStart = ""
    var stayingTimes: String = ""
    var avgStayTime: String = ""
    var feature: String = ""
    var isReceived = false

    private val errorDisplay: TextView by lazy { activity.findViewById(R.id.errorDisplay) }
    private val txtSeatName: TextView by lazy { activity.findViewById(R.id.textBoxSeatName) }
    private val txtCapacity: TextView by lazy { activity.findViewById(R.id.textBoxCapacity) }
    private val txtIsFilled: TextView by lazy { activity.findViewById(R.id.textBoxIsFilled) }
    private val txtTimeStart: TextView by lazy { activity.findViewById(R.id.textBoxTimeStart) }
    private val txtStayingTimes: TextView by lazy { activity.findViewById(R.id.textBoxStayingTime) }
    private val txtavgStayTime: TextView by lazy { activity.findViewById(R.id.textBoxAvgStayingTime) }
    private val txtFeature: TextView by lazy { activity.findViewById(R.id.textBoxSeatFeature) }

    override fun onMessage(message: String?) {
        super.onMessage(message)
        Log.i(javaClass.simpleName, "msg arrived")
        Log.i(javaClass.simpleName, "$message")

        val wholeMsg = JSONObject("$message")
        val resId: Int = wholeMsg.getInt("id")
        val result: JSONObject = wholeMsg.getJSONObject("result")
        val status: String = result.getString("status")
        val seats: JSONArray = result.getJSONArray("seats")
        val seat: JSONObject = seats.getJSONObject(RestaurantSeatShow.arrayIndex)


        if (resId == RestaurantSeatShow.getSeatInfoId) {
            this.isReceived = true
            if (status == "success") {
                this.seatId = seat.getInt("seat_id")
                this.seatName = seat.getString("seat_name")
                this.restaurantId = seat.getInt("restaurant_id")
                this.capacity = seat.getString("capacity")
                this.isFilled = seat.getBoolean("is_filled")
                this.timeStart = seat.getString("time_start")
                this.stayingTimes = seat.getString("staying_times")
                this.avgStayTime = seat.getString("avg_staying_time")
                this.feature = seat.getString("feature")

                activity.runOnUiThread {
                    txtSeatName.text = this.seatName
                    txtCapacity.text = this.capacity
                    txtTimeStart.text = this.timeStart
                    txtStayingTimes.text = this.stayingTimes
                    txtavgStayTime.text = this.avgStayTime
                    txtFeature.text = this.feature

                    if(this.isFilled == true) {
                        txtIsFilled.text = this.fill
                    } else {
                        this.fill = "満"
                        txtIsFilled.text = this.fill
                    }
                }
            } else if (status == "error") {
                activity.runOnUiThread {
                    errorDisplay.text = "座席情報を取得できません"
                    errorDisplay.visibility = View.INVISIBLE
                }
            }
        }
    }
}