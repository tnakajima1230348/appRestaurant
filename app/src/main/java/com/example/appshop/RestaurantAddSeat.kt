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
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.net.URI
import java.util.*
import kotlin.concurrent.schedule

class RestaurantAddSeat : AppCompatActivity() {

    companion object{
        const val addSeatInfoId: Int = 13
    }

    private val uri = WsClient.serverRemote
    private var client = AddSeatWsClient(this, uri)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_add_seat)
        client.connect()
    }

    override fun onResume() {
        super.onResume()

        val errorDisplay: TextView = findViewById(R.id.errorDisplay)
        val buttonAddSeat: Button = findViewById(R.id.buttonAddSeat)

        val etxtSeatName: EditText = findViewById(R.id.textBoxSeatName)
        val etxtCapacity: EditText = findViewById(R.id.textBoxCapacity)
        val etxtSeatFeature: EditText = findViewById(R.id.textBoxSeatFeature)

        val token = Restaurant.globalToken
        val restaurantId = Restaurant.globalRestaurantId

        buttonAddSeat.setOnClickListener {
            val seatInfo = JSONObject()
            seatInfo.put("seat_name", etxtSeatName.text.toString())
            seatInfo.put("capacity", etxtCapacity.text.toString())
            seatInfo.put("feature", etxtSeatFeature.text.toString())

            val params = JSONObject()
            params.put("token", token)
            params.put("type", "new")
            params.put("seatInfo", seatInfo)

            val request = client.createJsonrpcReq("updateInfo/restaurant/seat",
                addSeatInfoId, params)
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

    override fun onRestart() {
        super.onRestart()
        client = AddSeatWsClient(this, uri)
    }
}

class AddSeatWsClient(private val activity: Activity, uri: URI) : WsClient(uri) {
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

        if(resId == RestaurantAddSeat.addSeatInfoId){
            if(status == "success"){
                val intent = Intent(activity, ShowResult::class.java)
                intent.putExtra("message", "座席を追加しました")
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