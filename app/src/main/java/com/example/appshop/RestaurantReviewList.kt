package com.example.appshop

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.net.URI
import java.util.*
import kotlin.concurrent.schedule


class RestaurantReviewList : AppCompatActivity() {

    companion object{
        const val getReviewInfoId: Int = 10
    }

    private val uri = WsClient.serverRemote
    private var client = ReviewListWsClient(this, uri)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_review_list)
    }

    override fun onResume() {
        super.onResume()
        client.connect()

        val errorDisplay: TextView = findViewById(R.id.errorDisplay)
        val buttonReviewManage: Button = findViewById(R.id.buttonReviewManage)

        val token = Restaurant.globalToken
        val restaurantId = Restaurant.globalRestaurantId

        val getInfoParams = JSONObject()
        getInfoParams.put("restaurant_id", restaurantId)
        getInfoParams.put("token", token)
        val getInfoRequest =
            client.createJsonrpcReq("getInfo/restaurant/evaluations", getReviewInfoId, getInfoParams)

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
            if (client.isReceived) {
                errorDisplay.visibility = View.INVISIBLE
                this.cancel()
            }
        }

        buttonReviewManage.setOnClickListener {
            if(client.isReceived){
                val intent = Intent(this@RestaurantReviewList, ShowRestaurantReview::class.java)
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
        client = ReviewListWsClient(this, uri)
    }
}

class ReviewListWsClient(private val activity: Activity, uri: URI) : WsClient(uri) {
    var isReceived = false

    override fun onMessage(message: String?) {
        super.onMessage(message)
        Log.i(javaClass.simpleName, "msg arrived")
        Log.i(javaClass.simpleName, "$message")

        val wholeMsg = JSONObject("$message")
        val resId: Int = wholeMsg.getInt("id")
        val result: JSONObject = wholeMsg.getJSONObject("result")
        if (resId == RestaurantReviewList.getReviewInfoId) {
            this.isReceived = true
            val evaluations: JSONArray = result.getJSONArray("evaluations")
            val name = mutableListOf<String>()

            for (index in 0 until evaluations.length()) {
                val evaluation:JSONObject = evaluations.getJSONObject(index)
                name.add(evaluation.getString("evaluation_grade"))
            }

            activity.runOnUiThread{
                val listView = activity.findViewById<ListView>(R.id.listView)

                //ArrayAdapter
                val adapter = ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, name)

                listView.adapter = adapter

                listView.setOnItemClickListener { parent, view, position, id ->

                    if(this.isReceived){
                        val intent = Intent(activity, ShowRestaurantReview::class.java)
                        intent.putExtra("evaluationId", evaluations.getJSONObject(position).getInt("ecaluation_id"))
                        intent.putExtra("restaurantId", evaluations.getJSONObject(position).getInt("restaurant_id"))
                        intent.putExtra("userId", evaluations.getJSONObject(position).getInt("user_id"))
                        intent.putExtra("evaluationGrade", evaluations.getJSONObject(position).getString("evaluation_grade"))
                        intent.putExtra("evaluationComment", evaluations.getJSONObject(position).getString("evaluation_comment"))
                        intent.putExtra("token", Restaurant.globalToken)
                        activity.startActivity(intent)
                        this.close(WsClient.NORMAL_CLOSURE)
                    }else{
                        return@setOnItemClickListener
                    }
                }
            }
        }
    }
}