package com.example.appshop

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI




class Restaurant : AppCompatActivity() {
    private var restaurant_id: Int = -1
    private var restaurant_name: String = ""
    private var email_addr: String = ""
    private var address: String = ""
    private var time_open: String = ""
    private var time_close: String = ""
    private var features: String = ""

    companion object{
        const val logoutReqId: Int = 3
        const val getReviewInfoId: Int = 6
        const val getRestaurantInfoId: Int = 7
        var globalToken = ""
        var globalRestaurantId = -1
        var globalRestaurantName = ""
        var globalTokenExpiry = ""
    }

    private val uri = WsClient.serverRemote
    private var client = RestaurantTopWsClient(this, uri)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant)
    }

    private fun fetchClientInfo(client:RestaurantTopWsClient){
        this.restaurant_id = client.restaurant_id
        this.restaurant_name = client.restaurant_name
        this.email_addr = client.email_addr
        this.address = client.address
        this.time_open = client.time_open
        this.time_close = client.time_close
        this.features = client.features
    }

    override fun onResume() {
        super.onResume()
        client.connect()

        val getRestaurantInfo = Runnable {
            while (!client.isOpen){
                //do nothing
                //wait until websocket open
            }
            //the connection openings is guaranteed -> attach no error handler
            client.sendReqGetRestaurantInfoByName(Restaurant.globalToken, this.restaurant_name)
            return@Runnable
        }
        Thread( getRestaurantInfo ).start()

        Log.i(javaClass.simpleName, "restaurantId ${Restaurant.globalRestaurantId}")
        Log.i(javaClass.simpleName, "token recved ${Restaurant.globalToken}")
        Log.i(javaClass.simpleName, "token expiry ${Restaurant.globalTokenExpiry}")
        Log.i(javaClass.simpleName, "restaurantName: ${Restaurant.globalRestaurantName}")

        val buttonToHome: Button = findViewById(R.id.buttonHome)
        val buttonToSetting: Button = findViewById(R.id.buttonSetting)
        val buttonToSeat: Button = findViewById(R.id.buttonSeat)
        val buttonLogout: Button = findViewById(R.id.buttonLogout)
        val buttonToCalender: Button = findViewById(R.id.buttonCalender)
        val buttonReview: Button = findViewById(R.id.buttonReview)
        val buttonAddSeat: Button = findViewById(R.id.buttonAddSeat)

        //bottom footer event listeners
        buttonToHome.setOnClickListener {
            //doNothing
        }

        buttonToSeat.setOnClickListener {
            val intent = Intent(this@Restaurant, RestaurantSeatList::class.java)
            intent.putExtra("token", Restaurant.globalToken)
            startActivity(intent)
            client.close(WsClient.NORMAL_CLOSURE)
        }

        buttonToCalender.setOnClickListener {
            val intent = Intent(this@Restaurant, RestaurantCalendar::class.java)
            intent.putExtra("token", Restaurant.globalToken)
            startActivity(intent)
            client.close(WsClient.NORMAL_CLOSURE)
        }

        buttonAddSeat.setOnClickListener {
            val intent = Intent(this@Restaurant, RestaurantAccountInfoShow::class.java)
            intent.putExtra("restaurantName", Restaurant.globalRestaurantName)
            intent.putExtra("token", Restaurant.globalToken)
            startActivity(intent)
            client.close(WsClient.NORMAL_CLOSURE)
        }

        buttonReview.setOnClickListener {
            val shopId = globalRestaurantId
            val token = globalToken

            val getInfoParams = JSONObject()
            getInfoParams.put("restaurant_id", shopId)
            getInfoParams.put("token", token)
            val getInfoRequest = client.createJsonrpcReq("getInfo/restaurant/evaluations", getReviewInfoId, getInfoParams)
            try {
                if (client.isClosed) {
                    client.reconnect()
                }
                client.send(getInfoRequest.toString())
                //errorDisplay.text = "情報取得中..."
                //errorDisplay.visibility = View.VISIBLE
            } catch (ex: Exception) {
                Log.i(javaClass.simpleName, "send failed")
                Log.i(javaClass.simpleName, "$ex")
            }
        }

        buttonLogout.setOnClickListener {
            val logoutParams = JSONObject()
            logoutParams.put("token", Restaurant.globalToken)
            val logoutRequest = client.createJsonrpcReq("logout", logoutReqId, logoutParams)

            try{
                if(client.isClosed) {
                    client.reconnect()
                }
                client.send(logoutRequest.toString())
            } catch (ex: Exception){
                Log.i(javaClass.simpleName, "send failed $ex")
                val intent = Intent(this@Restaurant, ShowResult::class.java)
                val message = "ログアウトしました"
                val transitionBtnMessage = "ログインページへ"
                val isBeforeLogin = true
                Log.i(javaClass.simpleName, "logout with no request")
                intent.putExtra("message", message)
                intent.putExtra("transitionBtnMessage", transitionBtnMessage)
                intent.putExtra("isBeforeLogin", isBeforeLogin)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        client = RestaurantTopWsClient(this, uri)
    }
}

class RestaurantTopWsClient(private val activity: Activity, uri: URI) : WsClient(uri){

    var restaurant_id: Int = -1
    var restaurant_name: String = ""
    var email_addr: String = ""
    var address: String = ""
    var time_open: String = ""
    var time_close: String = ""
    var features: String = ""

    fun isRestaurantInfoArrived(): Boolean{
        if(this.restaurant_id == -1){
            return false
        }
        return true
    }

    /**
     * this method will send request about getting user information
     */
    fun sendReqGetRestaurantInfoByName(token: String, clientName: String){
        Log.i(javaClass.simpleName, "send request to get user information")
        val params = JSONObject()
        params.put("searchBy", "user_name")
        params.put("user_name", Restaurant.globalRestaurantName)
        params.put("token", Restaurant.globalToken)
        val request = this.createJsonrpcReq("getInfo/user/basic", Restaurant.getRestaurantInfoId, params)
        this.send(request.toString())
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        super.onOpen(handshakedata)
        this.sendReqGetRestaurantInfoByName(Restaurant.globalToken, Restaurant.globalRestaurantName)
    }

    override fun onMessage(message: String?) {
        super.onMessage(message)
        Log.i(javaClass.simpleName, "msg arrived")
        Log.i(javaClass.simpleName, "$message")

        val wholeMsg = JSONObject("$message")
        val resId: Int = wholeMsg.getInt("id")
        val result: JSONObject = wholeMsg.getJSONObject("result")
        val status: String = result.getString("status")

        //if message is about reviewManagement
        if(resId == Restaurant.getReviewInfoId){
            if(status == "success"){
                val intent = Intent(activity, ShowRestaurantReview::class.java) //画面遷移
                intent.putExtra("shopId", result.getInt("restaurant_id"))
                intent.putExtra("message1", result.getString("evaluation_grade"))
                intent.putExtra("message2", result.getString("evaluation_comment"))
                activity.startActivity(intent)
                this.close(WsClient.NORMAL_CLOSURE)

            }else if(status == "error"){
                Log.i(javaClass.simpleName, "no user matched")
                activity.runOnUiThread{
                    //errorDisplay.text = "アカウント情報を取得できません"
                    //errorDisplay.visibility = View.INVISIBLE
                }
            }
        }

        //if message is about logout
        if(resId == Restaurant.logoutReqId){

            val intent = Intent(activity, ShowResult::class.java)
            var message = ""
            val transitionBtnMessage = "ログインページへ"
            val isBeforeLogin = true

            //if logout successes
            if(status == "success"){
                message = "ログアウトに成功しました"

            }else if(status == "error"){
                message = "ログアウトに失敗しました"
            }

            intent.putExtra("message", message)
            intent.putExtra("transitionBtnMessage", transitionBtnMessage)
            intent.putExtra("isBeforeLogin", isBeforeLogin)

            activity.startActivity(intent)
            activity.finish()
            this.close(NORMAL_CLOSURE)

            //if msg is about getInfo/restaurant/basic
        }else if(resId == Restaurant.getRestaurantInfoId){
            if(status == "success"){
                this.restaurant_id = result.getInt("restaurant_id")
                this.restaurant_name = result.getString("restaurant_name")
                this.email_addr = result.getString("email_addr")
                this.address = result.getString("address")
                this.time_open = result.getString("time_open")
                this.time_close = result.getString("time_close")
                this.features = result.getString("features")
            }else if(status == "error"){
                Log.i(javaClass.simpleName, "getInfo failed")
            }
        }
    }
}