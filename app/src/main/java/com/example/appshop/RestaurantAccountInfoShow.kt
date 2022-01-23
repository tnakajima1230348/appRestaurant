package com.example.appshop

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import org.json.JSONObject
import java.lang.Exception
import java.net.URI
import java.util.*
import kotlin.concurrent.schedule

class RestaurantAccountInfoShow : AppCompatActivity() {

    companion object{
        const val getRestaurantInfoId: Int = 4
    }

    private val uri = WsClient.serverRemote
    private var client = GetRestaurantInfoWsClient(this, uri)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_account_info_show)
        client.connect()
    }

    override fun onResume() {
        super.onResume()

        val errorDisplay: TextView = findViewById(R.id.errorDisplay)
        val buttonAccountInfoChange: Button = findViewById(R.id.buttonAccountInfoChange)


        val token = Restaurant.globalToken
        val restaurantName = Restaurant.globalRestaurantName

        val getInfoParams = JSONObject()
        getInfoParams.put("searchBy", "restaurant_name")
        getInfoParams.put("restaurant_name", restaurantName)
        getInfoParams.put("token", token)
        val getInfoRequest = client.createJsonrpcReq("getInfo/restaurant/basic", getRestaurantInfoId, getInfoParams)

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

        buttonAccountInfoChange.setOnClickListener {
            if(client.isReceived){
                val intent = Intent(this@RestaurantAccountInfoShow, RestaurantAccountInfoChange::class.java)
                intent.putExtra("restaurantId", client.restaurantId)
                intent.putExtra("restaurantName", client.restaurantName)
                intent.putExtra("emailAddr", client.emailAddr)
                intent.putExtra("address", client.address)
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
        client = GetRestaurantInfoWsClient(this, uri)
    }


}

class GetRestaurantInfoWsClient(private val activity: Activity, uri: URI) : WsClient(uri) {
    var restaurantId: Int = -1
    var restaurantName = ""
    var emailAddr = ""
    var address = ""
    var isReceived = false

    private val errorDisplay: TextView by lazy { activity.findViewById(R.id.errorDisplay) }
    private val txtRestaurantName: TextView by lazy { activity.findViewById(R.id.textBoxRestaurantName) }
    private val txtEmail: TextView by lazy { activity.findViewById(R.id.textBoxRestaurantEmail) }
    private val txtAddress: TextView by lazy { activity.findViewById(R.id.textBoxRestaurantAddress) }

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
                this.restaurantId = result.getInt("user_id")
                this.restaurantName = result.getString("user_name")
                this.emailAddr = result.getString("email_addr")
                this.address = result.getString("address")

                activity.runOnUiThread {
                    txtRestaurantName.text = this.restaurantName
                    txtEmail.text = this.emailAddr
                    txtAddress.text = this.address
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
