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

class ShowRestaurantReview : AppCompatActivity() {

    companion object{
        const val getReviewInfoId: Int = 10
        const val reviewDeleteReqId = 8
        var arrayIndex: Int = -1
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

        val homeBtn: Button = findViewById(R.id.buttonHome)
        val deleteBtn: Button = findViewById(R.id.buttonDeleteReview)

        arrayIndex = intent.getIntExtra("arrayIndex", 0)
        val token = Restaurant.globalToken
        val restaurantId = Restaurant.globalRestaurantId

        val getInfoParams = JSONObject()
        getInfoParams.put("restaurant_id", restaurantId)
        getInfoParams.put("token", token)
        val getReviewInfoRequest = client.createJsonrpcReq("getInfo/restaurant/evaluations", getReviewInfoId, getInfoParams)
        Timer().schedule(50, 200) {
            Log.i(javaClass.simpleName, "set req ${getReviewInfoRequest.toString()}")
            try {
                if (client.isClosed) {
                    client.reconnect()
                }
                client.send(getReviewInfoRequest.toString())
                //errorDisplay.text = "情報取得中..."
                //errorDisplay.visibility = View.VISIBLE
            } catch (ex: Exception) {
                Log.i(javaClass.simpleName, "send failed")
                Log.i(javaClass.simpleName, "$ex")
            }
            // if msg arrived
            if(client.isReceived){
                //errorDisplay.visibility = View.INVISIBLE
                this.cancel()
            }
        }

        deleteBtn.setOnClickListener {
            val resignParams = JSONObject()
            resignParams.put("type", "delete")
            resignParams.put("token", token)
            resignParams.put("evaluation_id", restaurantId)
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

    var message1 = ""
    var message2 = ""
    var isReceived = false

    private val errorDisplay: TextView by lazy {
        activity.findViewById(R.id.errorDisplay)
    }
    private val txtMessage1: TextView by lazy { activity.findViewById(R.id.message1) }
    private val txtMessage2: TextView by lazy { activity.findViewById(R.id.message2) }

    override fun onMessage(message: String?) {
        super.onMessage(message)
        Log.i(javaClass.simpleName, "msg arrived")
        Log.i(javaClass.simpleName, "$message")

        val wholeMsg = JSONObject("$message")
        val resId: Int = wholeMsg.getInt("id")
        val result: JSONObject = wholeMsg.getJSONObject("result")
        val status: String = result.getString("status")
        val evaluation: JSONArray = result.getJSONArray("status")
        val review: JSONObject = evaluation.getJSONObject(ShowRestaurantReview.arrayIndex)

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
        if (resId == ShowRestaurantReview.getReviewInfoId) {
            this.isReceived = true
            if (status == "success") {
                if(result.getJSONArray("evaluations").length() == 0){
                    val intent = Intent(activity, ShowResult::class.java)
                    var message = "この店舗のレビューはまだありません"
                    val transitionBtnMessage = "Home画面へ"
                    val isBeforeLogin = false

                    intent.putExtra("message", message)
                    intent.putExtra("transitionBtnMessage", transitionBtnMessage)
                    intent.putExtra("isBeforeLogin", isBeforeLogin)
                    activity.startActivity(intent)
                    activity.finish()
                    this.close(NORMAL_CLOSURE)
                }else {
                    this.message1 = review.getString("seat_id")
                    this.message2 = review.getString("seat_name")

                    activity.runOnUiThread {
                        txtMessage1.text = this.message1
                        txtMessage2.text = this.message2
                    }
                }
            } else if (status == "error") {
                activity.runOnUiThread {
                    errorDisplay.text = "レビュー情報を取得できません"
                    errorDisplay.visibility = View.INVISIBLE
                }
            }
        }
    }
}