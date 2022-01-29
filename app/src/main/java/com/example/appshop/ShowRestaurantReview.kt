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

class ShowRestaurantReview : AppCompatActivity() {

    companion object{
        const val reviewDeleteReqId = 6
    }

    private val uri = WsClient.serverRemote
    private val client = ReviewDeleteWsClient(this, uri)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_restaurant_review)
    }

    override fun onResume() {
        super.onResume()
        client.connect()
        val token = Restaurant.globalToken

        val currentShopId = intent.getIntExtra("shopId", 0)
        val message1 = intent.getStringExtra("message1")
        val message2 = intent.getStringExtra("message2")


        val txtMessage1: TextView = findViewById(R.id.message1)
        val txtMessage2: TextView = findViewById(R.id.message2)

        val deleteBtn: Button = findViewById(R.id.buttonDeleteReview)
        val homeBtn: Button = findViewById(R.id.buttonHome)

        txtMessage1.text = message1
        txtMessage2.text = message2


        deleteBtn.setOnClickListener {
            val resignParams = JSONObject()
            resignParams.put("type", "delete")
            resignParams.put("token", token)
            resignParams.put("evaluation_id", currentShopId)
            val resignReq = client.createJsonrpcReq("updateInfo/evaluation", reviewDeleteReqId, resignParams)

            try {
                if (client.isClosed) {
                    client.reconnect()
                }
                client.send(resignReq.toString())
            } catch (ex: Exception) {
                Log.i(javaClass.simpleName, "send failed")
                Log.i(javaClass.simpleName, "$ex")
                //errorDisplay.text = "インターネットに接続されていません"
                //errorDisplay.visibility = View.VISIBLE
            }
        }
        homeBtn.setOnClickListener {
            val intent = Intent(this,Restaurant::class.java)
            startActivity(intent)
        }
    }
}
class ReviewDeleteWsClient(private val activity: Activity, uri: URI) : WsClient(uri){

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

        if (resId == ShowRestaurantReview.reviewDeleteReqId){
            if(status == "success"){
                val intent = Intent(activity, ShowResult::class.java)
                intent.putExtra("message", "レビュー削除が完了しました")
                intent.putExtra("transitionBtnMessage", "Home画面へ")
                intent.putExtra("isBeforeLogin", false)
                activity.startActivity(intent)
                this.close(NORMAL_CLOSURE)

            }else if(status == "error"){
                activity.runOnUiThread {
                    errorDisplay.text = "削除できません"
                    errorDisplay.visibility = View.VISIBLE
                }
            }
        }
    }
}