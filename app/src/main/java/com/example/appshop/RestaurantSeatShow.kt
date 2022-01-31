package com.example.appshop

import android.app.Activity
import android.content.Intent
import android.hardware.camera2.params.Capability
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
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
        const val deleteSeatInfoId: Int = 12
        const val seatUseInfoId: Int = 19
    }

    private val uri = WsClient.serverRemote
    private var client = SeatInfoWsClient(this, uri)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_seat_show)
    }

    override fun onResume() {
        super.onResume()
        client.connect()

        val errorDisplay: TextView = findViewById(R.id.errorDisplay)
        val buttonSeatInfoChange: Button = findViewById(R.id.buttonSeatInfoChange)
        val buttonDeleteSeat: Button = findViewById(R.id.buttonDeleteSeat)
        val buttonSeatUseChange: Button = findViewById(R.id.buttonSeatUseChange)


        val seatId = intent.getIntExtra("seatId",0)
        val seatName = intent.getStringExtra("seatName")
        val capacity = intent.getIntExtra("capacity", 0)
        val isFilled = intent.getBooleanExtra("isFilled", false)
        val feature = intent.getStringExtra("feature")
        var fill: String = "空"

        val token = Restaurant.globalToken
        val restaurantId = Restaurant.globalRestaurantId

        val etxtSeatId: TextView = findViewById(R.id.textBoxSeatId)
        val etxtSeatName: TextView = findViewById(R.id.textBoxSeatName)
        val etxtCapacity: TextView = findViewById(R.id.textBoxCapacity)
        val etxtIsFilled: TextView = findViewById(R.id.textBoxIsFilled)
        val etxtFeature: TextView = findViewById(R.id.textBoxSeatFeature)

        etxtSeatId.setText(seatId.toString())
        etxtSeatName.setText(seatName)
        etxtCapacity.setText(capacity.toString())
        etxtFeature.setText(feature)

        if(isFilled == true) {
            fill = "満"
            etxtIsFilled.setText(fill)
        } else if (isFilled == false){
            etxtIsFilled.setText(fill)
        }

        buttonSeatInfoChange.setOnClickListener {
            val intent = Intent(this@RestaurantSeatShow, RestaurantSeatInfoChange::class.java)
            intent.putExtra("seatId", seatId)
            intent.putExtra("seatName", seatName)
            intent.putExtra("capacity", capacity)
            intent.putExtra("feature", feature)
            intent.putExtra("token", token)
            startActivity(intent)
            client.close(WsClient.NORMAL_CLOSURE)
        }

        buttonDeleteSeat.setOnClickListener {
            val params = JSONObject()
            params.put("token", token)
            params.put("type", "delete")
            params.put("seat_id", seatId)

            val request = client.createJsonrpcReq("updateInfo/restaurant/seat", deleteSeatInfoId, params)
            try {
                if(client.isClosed){
                    client.reconnect()
                }
                client.send(request.toString())
            }catch (ex:Exception){
                Log.i(javaClass.simpleName, "send failed")
                Log.i(javaClass.simpleName, "$ex")
                errorDisplay.text = "インターネットに接続されていません"
                errorDisplay.visibility = View.VISIBLE
            }
        }

        buttonSeatUseChange.setOnClickListener {
            val seats = JSONArray()
            val seat = JSONObject()
            seat.put("seat_id", seatId)
            if(isFilled == true) {
                seat.put("is_filled", false)
            } else if(isFilled == false) {
                seat.put("is_filled", true)
            }

            seats.put(seat)

            val params = JSONObject()
            params.put("token", token)
            params.put("seats", seats)

            val request = client.createJsonrpcReq("updateInfo/restaurant/seatsAvailability", seatUseInfoId, params)
            try {
                if(client.isClosed){
                    client.reconnect()
                }
                client.send(request.toString())
            }catch (ex:Exception){
                Log.i(javaClass.simpleName, "send failed")
                Log.i(javaClass.simpleName, "$ex")
                errorDisplay.text = "インターネットに接続されていません"
                errorDisplay.visibility = View.VISIBLE
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        client = SeatInfoWsClient(this, uri)
    }


}

class SeatInfoWsClient(private val activity: Activity, uri: URI) : WsClient(uri) {
    var isReceived = false

    private val errorDisplay: TextView by lazy { activity.findViewById(R.id.errorDisplay) }
    private val txtSeatId: TextView by lazy { activity.findViewById(R.id.textBoxSeatId) }
    private val txtSeatName: TextView by lazy { activity.findViewById(R.id.textBoxSeatName) }
    private val txtCapacity: TextView by lazy { activity.findViewById(R.id.textBoxCapacity) }
    private val txtIsFilled: TextView by lazy { activity.findViewById(R.id.textBoxIsFilled) }
    private val txtFeature: TextView by lazy { activity.findViewById(R.id.textBoxSeatFeature) }

    override fun onMessage(message: String?) {
        super.onMessage(message)
        Log.i(javaClass.simpleName, "msg arrived")
        Log.i(javaClass.simpleName, "$message")

        val wholeMsg = JSONObject("$message")
        val resId: Int = wholeMsg.getInt("id")
        val result: JSONObject = wholeMsg.getJSONObject("result")
        val status: String = result.getString("status")



        if(resId == RestaurantSeatShow.deleteSeatInfoId){
            if(status == "success"){
                val intent = Intent(activity, ShowResult::class.java)
                intent.putExtra("message", "座席を削除しました")
                intent.putExtra("transitionBtnMessage", "ホームへ")
                intent.putExtra("isBeforeLogin", false)
                this.close(NORMAL_CLOSURE)
                activity.startActivity(intent)

            }else if(status == "error"){
                activity.runOnUiThread {
                    errorDisplay.text = result.getString("reason")
                    errorDisplay.visibility = View.VISIBLE
                }
            }
        }

        if(resId == RestaurantSeatShow.seatUseInfoId) {
            if (status == "success") {
                val intent = Intent(activity, ShowResult::class.java)
                intent.putExtra("message", "座席利用状況を変更しました")
                intent.putExtra("transitionBtnMessage", "ホームへ")
                intent.putExtra("isBeforeLogin", false)
                this.close(NORMAL_CLOSURE)
                activity.startActivity(intent)

            } else if (status == "error") {
                activity.runOnUiThread {
                    errorDisplay.text = result.getString("reason")
                    errorDisplay.visibility = View.VISIBLE
                }
            }
        }
    }
}