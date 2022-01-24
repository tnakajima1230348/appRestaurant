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

class RestaurantAccountInfoChange : AppCompatActivity() {
    companion object{
        const val changeRestaurantInfoId: Int = 5
    }

    private val uri = WsClient.serverRemote
    private var client = ChangeUserInfoWsClient(this, uri)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_account_info_change)
    }

    override fun onResume() {
        super.onResume()
        client.connect()
        val currentRestaurantId = intent.getIntExtra("restaurantId", 0)
        val currentRestaurantName = intent.getStringExtra("restaurantName")
        val currentEmail = intent.getStringExtra("emailAddr")
        val currentAddress = intent.getStringExtra("address")
        val token = Restaurant.globalToken
        var restaurantName = Restaurant.globalRestaurantName

        val etxtRestaurantName: EditText = findViewById(R.id.textBoxRestaurantName)
        val etxtRestaurantEmail: EditText = findViewById(R.id.textBoxRestaurantEmail)
        val etxtRestaurantAddress: EditText = findViewById(R.id.textBoxRestaurantAddress)
        val buttonSubmit: Button = findViewById(R.id.buttonSubmit)
        val buttonResign: Button = findViewById(R.id.buttonResign)
        val errorDisplay: TextView = findViewById(R.id.errorDisplay)

        etxtRestaurantName.setText(currentRestaurantName)
        etxtRestaurantEmail.setText(currentEmail)
        etxtRestaurantAddress.setText(currentAddress)

        buttonSubmit.setOnClickListener {

            val params = JSONObject()
            params.put("restaurant_name", etxtRestaurantName.text.toString())
            params.put("email_addr", etxtRestaurantEmail.text.toString())
            params.put("address", etxtRestaurantAddress.text.toString())
            params.put("token", token)

            //restaurantName = etxtRestaurantName.text.toString()

            val request = client.createJsonrpcReq("updateInfo/user/basic", changeRestaurantInfoId, params)

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

        buttonResign.setOnClickListener {
            //val intent = Intent(this@RestaurantAccountInfoChange, ::class.java)
            //intent.putExtra("token", token)
            //client.close(WsClient.NORMAL_CLOSURE)
            //startActivity(intent)
            TODO("not yet implemented")
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        client.close(WsClient.NORMAL_CLOSURE)
    }
}

class ChangeUserInfoWsClient(private val activity: Activity, uri: URI) : WsClient(uri){

    private val errorDisplay: TextView by lazy {
        activity.findViewById(R.id.errorDisplay)
    }

    private val etxtRestaurantName: EditText by lazy {
        activity.findViewById(R.id.textBoxRestaurantName)
    }

    override fun onMessage(message: String?) {
        super.onMessage(message)
        Log.i(javaClass.simpleName, "msg arrived")
        Log.i(javaClass.simpleName, "$message")

        val wholeMsg = JSONObject("$message")
        val resId: Int = wholeMsg.getInt("id")
        val result: JSONObject = wholeMsg.getJSONObject("result")
        val status: String = result.getString("status")

        if(resId == RestaurantAccountInfoChange.changeRestaurantInfoId){
            if(status == "success"){
                Restaurant.globalRestaurantName = etxtRestaurantName.text.toString()
                val intent = Intent(activity, ShowResult::class.java)
                intent.putExtra("message", "アカウント情報を変更しました")
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