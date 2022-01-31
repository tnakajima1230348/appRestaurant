package com.example.appshop

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import org.json.JSONObject
import java.lang.Exception
import java.net.URI
import java.util.*
import kotlin.concurrent.schedule

class RestaurantCalendar : AppCompatActivity() {

    companion object{
        const val addHolidayInfoId: Int = 8
        const val deleteHolidayInfoId: Int = 14
    }

    private val uri = WsClient.serverRemote
    private var client = HolidayInfoWsClient(this, uri)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_calendar)
    }

    override fun onResume() {
        super.onResume()
        client.connect()

        val errorDisplay: TextView = findViewById(R.id.errorDisplay)
        val buttonAccountAddHoliday: Button = findViewById(R.id.buttonAccountAddHoliday)
        val buttonAccountDeleteHoliday: Button = findViewById(R.id.buttonAccountDeleteHoliday)
        val buttonAccountHolidayList:Button = findViewById(R.id.buttonAccountHolidayList)

        val token = Restaurant.globalToken
        val restaurantName = Restaurant.globalRestaurantName

        //val getInfoParams = JSONObject()
        //getInfoParams.put("searchBy", "restaurant_name")
        //getInfoParams.put("restaurant_name", restaurantName)
        //getInfoParams.put("token", token)
        //val getInfoRequest = client.createJsonrpcReq("getInfo/restaurant/basic", getHolidayInfoId, getInfoParams)

        //attempt to send until connection established

        val calenderView: CalendarView = findViewById(R.id.calendarView)
        calenderView.date = System.currentTimeMillis()

        calenderView.setOnDateChangeListener{ view, year, month, dayOfMonth ->
            val date = "$year/$month/$dayOfMonth"

            buttonAccountAddHoliday.setOnClickListener {
                val dates = arrayOf<String>(date)
                val params = JSONObject()
                params.put("holidays", dates)
                params.put("type", "new")
                params.put("token", token)

                val request = client.createJsonrpcReq("updateInfo/restaurant/holidays",
                    addHolidayInfoId, params)

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

            buttonAccountDeleteHoliday.setOnClickListener {
                val dates = arrayOf<String>(date)
                val params = JSONObject()
                params.put("holidays", dates)
                params.put("type", "delete")
                params.put("token", token)

                val request = client.createJsonrpcReq("updateInfo/restaurant/holidays",
                    deleteHolidayInfoId, params)

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

            buttonAccountHolidayList.setOnClickListener {
                val intent = Intent(this@RestaurantCalendar, RestaurantHolidayList::class.java)
                intent.putExtra("token", token)
                startActivity(intent)
                client.close(WsClient.NORMAL_CLOSURE)
            }

        }
    }

    override fun onRestart() {
        super.onRestart()
        client = HolidayInfoWsClient(this, uri)
    }

}

class HolidayInfoWsClient(private val activity: Activity, uri: URI) : WsClient(uri) {
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

        if(resId == RestaurantCalendar.addHolidayInfoId){
            if(status == "success"){
                val intent = Intent(activity, ShowResult::class.java)
                intent.putExtra("message", "休日を追加しました")
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

        if(resId == RestaurantCalendar.deleteHolidayInfoId){
            if(status == "success"){
                val intent = Intent(activity, ShowResult::class.java)
                intent.putExtra("message", "休日を削除しました")
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
