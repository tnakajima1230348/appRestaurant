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
        const val getSeatInfoId: Int = 18
        const val deleteReservationId = 19
        var arrayIndex: Int = -1
        var arrayIndex2: Int = -1
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


        arrayIndex = intent.getIntExtra("arrayIndex", 0)
        arrayIndex2 = intent.getIntExtra("arrayIndex2", 0)
        val token = Restaurant.globalToken
        val restaurantId = Restaurant.globalRestaurantId

        val getInfoParams = JSONObject()
        getInfoParams.put("restaurant_id", restaurantId)
        getInfoParams.put("token", token)
        val getInfoRequest = client.createJsonrpcReq("getInfo/restaurant/seats", getSeatInfoId, getInfoParams)

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

        buttonDeleteReservation.setOnClickListener {
            val params = JSONObject()
            params.put("token", token)
            params.put("type", "delete")
            params.put("reservation_id", client.reservationId)

            val request = client.createJsonrpcReq("updateInfo/restaurant/seat", deleteReservationId, params)
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
    var reservationId:Int = -1
    var userId: Int = -1
    var timeStart = ""
    var timeEnd = ""
    var isReceived = false

    private val errorDisplay: TextView by lazy { activity.findViewById(R.id.errorDisplay) }
    private val txtReservationId: TextView by lazy { activity.findViewById(R.id.textBoxReservationId) }
    private val txtUserId: TextView by lazy { activity.findViewById(R.id.textBoxUserId) }
    private val txtTimeStart: TextView by lazy { activity.findViewById(R.id.textBoxTimeStart) }
    private val txtTimeEnd: TextView by lazy { activity.findViewById(R.id.textBoxTimeEnd) }

    override fun onMessage(message: String?) {
        super.onMessage(message)
        Log.i(javaClass.simpleName, "msg arrived")
        Log.i(javaClass.simpleName, "$message")

        val wholeMsg = JSONObject("$message")
        val resId: Int = wholeMsg.getInt("id")
        val result: JSONObject = wholeMsg.getJSONObject("result")
        val status: String = result.getString("status")
        val seats: JSONArray = result.getJSONArray("seats")
        val seat: JSONObject = seats.getJSONObject(RestaurantReservationShow.arrayIndex)
        val reservations:JSONArray = seat.getJSONArray("reservations")
        val reservation: JSONObject = reservations.getJSONObject(RestaurantReservationShow.arrayIndex2)


        if (resId == RestaurantSeatShow.getSeatInfoId) {
            this.isReceived = true
            if (status == "success") {
                this.reservationId = reservation.getInt("reservation_id")
                this.userId = reservation.getInt("user_id")
                this.timeStart = reservation.getString("time_start")
                this.timeEnd = reservation.getString("time_end")

                activity.runOnUiThread {
                    txtReservationId.text = this.reservationId.toString()
                    txtUserId.text = this.userId.toString()
                    txtTimeStart.text = this.timeStart
                    txtTimeEnd.text = this.timeEnd
                }
            } else if (status == "error") {
                activity.runOnUiThread {
                    errorDisplay.text = "座席情報を取得できません"
                    errorDisplay.visibility = View.INVISIBLE
                }
            }
        }

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