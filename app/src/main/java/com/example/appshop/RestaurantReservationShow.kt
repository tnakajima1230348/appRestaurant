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

class RestaurantReservationShow : AppCompatActivity() {

    companion object{
        const val deleteReservationId = 18
    }

    private val uri = WsClient.serverRemote
    private var client = ReservationInfoWsClient(this, uri)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_reservation_show)
    }

    override fun onResume() {
        super.onResume()
        client.connect()

        val errorDisplay: TextView = findViewById(R.id.errorDisplay)
        val buttonDeleteReservation: Button = findViewById(R.id.buttonDeleteReservation)

        val reservationId = intent.getIntExtra("reservationId",0)
        val userId = intent.getIntExtra("userId", 0)
        val timeStart = intent.getStringExtra("timeStart")
        val timeEnd = intent.getStringExtra("timeEnd")

        val token = Restaurant.globalToken
        val restaurantId = Restaurant.globalRestaurantId

        val etxtReservationId: TextView = findViewById(R.id.textBoxReservationId)
        val etxtUserId: TextView = findViewById(R.id.textBoxUserId)
        val etxtTimeStart: TextView = findViewById(R.id.textBoxTimeStart)
        val etxtTimeEnd: TextView = findViewById(R.id.textBoxTimeEnd)

        etxtReservationId.setText(reservationId.toString())
        etxtUserId.setText(userId.toString())
        etxtTimeStart.setText(timeStart)
        etxtTimeEnd.setText(timeEnd)

        buttonDeleteReservation.setOnClickListener {
            val params = JSONObject()
            params.put("token", token)
            params.put("type", "delete")
            params.put("reservation_id", reservationId)

            val request = client.createJsonrpcReq("updateInfo/reservation",
                RestaurantSeatShow.deleteSeatInfoId, params)
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
        client = ReservationInfoWsClient(this, uri)
    }
}

class ReservationInfoWsClient(private val activity: Activity, uri: URI) : WsClient(uri) {

    private val errorDisplay: TextView by lazy { activity.findViewById(R.id.errorDisplay) }

    override fun onMessage(message: String?) {
        super.onMessage(message)
        Log.i(javaClass.simpleName, "msg arrived")
        Log.i(javaClass.simpleName, "$message")

        val wholeMsg = JSONObject("$message")
        val resId: Int = wholeMsg.getInt("id")
        val result: JSONObject = wholeMsg.getJSONObject("result")
        val status: String = result.getString("status")

        if(resId == RestaurantReservationShow.deleteReservationId){
            if(status == "success"){
                val intent = Intent(activity, ShowResult::class.java)
                intent.putExtra("message", "予約を削除しました")
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
    }
}