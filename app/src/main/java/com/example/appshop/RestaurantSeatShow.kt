package com.example.appshop

import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import java.lang.String


fun loadData(mTableLayout: Any) {
    val leftRowMargin = 0
    val topRowMargin = 0
    val rightRowMargin = 0
    val bottomRowMargin = 0
    var textSize = 0
    var smallTextSize = 0
    var mediumTextSize = 0
    "20dp".also { textSize = it }
    "20dp".also { smallTextSize = it }
    "20dp".also { mediumTextSize = it }
    val players = Player()
    val data: Array<PlayerData> = players.getPlayers()
    val rows = data.size
    var textSpacer: TextView? = null

    // -1 はヘッダー行
    for (i in -1 until rows) {
        var row: PlayerData? = null
        if (i > -1) row = data[i] else {
            textSpacer = TextView(this)
            textSpacer.text = ""
        }
        // 1列目(No)
        val tv = TextView(this)
        tv.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        tv.gravity = Gravity.LEFT
        tv.setPadding(5, 15, 0, 15)
        if (i == -1) {
            tv.text = "No"
            tv.setBackgroundColor(Color.parseColor("#f0f0f0"))
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize.toFloat())
        } else {
            tv.setText(String.valueOf(row.getNo()))
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
        }
        // 2列目(Name)
        val tv2 = TextView(this)
        tv2.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        tv2.gravity = Gravity.LEFT
        tv2.setPadding(5, 15, 0, 15)
        if (i == -1) {
            tv2.text = "Name"
            tv2.setBackgroundColor(Color.parseColor("#f0f0f0"))
            tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize.toFloat())
        } else {
            tv2.setText(row.getName())
        }
        // 3列目(Position)
        val tv3 = TextView(this)
        tv3.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        tv3.gravity = Gravity.LEFT
        tv3.setPadding(5, 15, 0, 15)
        if (i == -1) {
            tv3.text = "Position"
            tv3.setBackgroundColor(Color.parseColor("#f0f0f0"))
            tv3.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize.toFloat())
        } else {
            tv3.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
            tv3.setText(row.getPosition())
        }
        // 4列目(Birth)
        val tv4 = TextView(this)
        tv4.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        tv4.gravity = Gravity.LEFT
        tv4.setPadding(5, 15, 0, 15)
        if (i == -1) {
            tv4.text = "Birth"
            tv4.setBackgroundColor(Color.parseColor("#f0f0f0"))
            tv4.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize.toFloat())

            // テーブルに行を追加
            val tr = TableRow(this)
            tr.id = i + 1
            val trParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            trParams.setMargins(leftRowMargin, topRowMargin, rightRowMargin, bottomRowMargin)
            tr.setPadding(0, 0, 0, 0)
            tr.layoutParams = trParams
            tr.addView(tv)
            tr.addView(tv2)
            tr.addView(tv3)
            tr.addView(tv4)
            mTableLayout.addView(tr, trParams)
            // 罫線を追加
            if (i > -1) {
                val trSep = TableRow(this)
                val trParamsSep = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
                trParamsSep.setMargins(leftRowMargin, topRowMargin, rightRowMargin, bottomRowMargin)
                trSep.layoutParams = trParamsSep
                val tvSep = TextView(this)
                val tvSepLay = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
                tvSepLay.span = 4
                tvSep.layoutParams = tvSepLay
                tvSep.setBackgroundColor(Color.parseColor("#d9d9d9"))
                tvSep.height = 1
                trSep.addView(tvSep)
                mTableLayout.addView(trSep, trParamsSep)
            }
        }
    }
}

private fun Any.addView(tr: TableRow, trParams: TableLayout.LayoutParams) {

}


