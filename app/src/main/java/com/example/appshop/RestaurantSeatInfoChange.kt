package com.example.appshop

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import org.json.JSONObject
import java.lang.Exception
import java.net.URI

class RestaurantSeatInfoChange : AppCompatActivity() {
    companion object{
        const val changeSeatInfoId: Int = 15
    }

    private val uri = WsClient.serverRemote
    private var client = ChangeSeatInfoWsClient(this, uri)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_seat_info_change)
    }

    override fun onResume() {
        super.onResume()
        client.connect()

        val seatId = intent.getIntExtra("seatId", 0)
        val currentSeatName = intent.getStringExtra("seatName")
        val currentCapacity = intent.getStringExtra("capacity")
        val currentFeature = intent.getStringExtra("feature")
        val token = Restaurant.globalToken

        val etxtSeatName: EditText = findViewById(R.id.textBoxSeatName)
        val etxtCapacity: EditText = findViewById(R.id.textBoxCapacity)
        val etxtSeatFeature: EditText = findViewById(R.id.textBoxSeatFeature)
        val buttonChange: Button = findViewById(R.id.buttonChange)
        val errorDisplay: TextView = findViewById(R.id.errorDisplay)

        etxtSeatName.setText(currentSeatName)
        etxtCapacity.setText(currentCapacity)
        etxtSeatFeature.setText(currentFeature)



        buttonChange.setOnClickListener {
            val seatInfo = JSONObject()
            seatInfo.put("seat_id", seatId)
            seatInfo.put("seat_name", etxtSeatName.text.toString())
            seatInfo.put("capacity", etxtCapacity.text.toString())
            seatInfo.put("feature", etxtSeatFeature.text.toString())

            val params = JSONObject()
            params.put("token", token)
            params.put("type", "change")
            params.put("seatInfo", seatInfo)

            val request = client.createJsonrpcReq("updateInfo/restaurant/seat",
                changeSeatInfoId, params)
            try {
                if (client.isClosed) {
                    client.reconnect()
                }
                client.send(request.toString())
            } catch (ex: Exception) {
                Log.i(javaClass.simpleName, "send failed")
                Log.i(javaClass.simpleName, "$ex")
                errorDisplay.text = "インターネットに接続されていません"
                errorDisplay.visibility = View.VISIBLE
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        client.close(WsClient.NORMAL_CLOSURE)
    }
}

class ChangeSeatInfoWsClient(private val activity: Activity, uri: URI) : WsClient(uri){

    private val errorDisplay: TextView by lazy {
        activity.findViewById(R.id.errorDisplay)
    }

    override fun onMessage(message: String?) {
        super.onMessage(message)
        Log.i(javaClass.simpleName, "msg arrived")
        Log.i(javaClass.simpleName, "$message")

        val wholeMsg = JSONObject("$message")
        val resId: Int = wholeMsg.getInt("id")
        val result: JSONObject = wholeMsg.getJSONObject("result")
        val status: String = result.getString("status")

        if(resId == RestaurantSeatInfoChange.changeSeatInfoId){
            if(status == "success"){
                val intent = Intent(activity, ShowResult::class.java)
                intent.putExtra("message", "座席情報を変更しました")
                intent.putExtra("transitionBtnMessage", "ホームへ")
                intent.putExtra("isBeforeLogin", false)
                this.close(NORMAL_CLOSURE)
                activity.startActivity(intent)

            }else if(status == "error"){
                activity.runOnUiThread{
                    errorDisplay.text = result.getString("reason")
                    errorDisplay.visibility = View.VISIBLE
                }
            }
        }

    }
}