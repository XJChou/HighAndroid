package com.zxj.path

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {
    private lateinit var mPieView: View
    private lateinit var mDashBoardView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mPieView = findViewById(R.id.view_pie)
        mDashBoardView = findViewById(R.id.view_dash_board)

        findViewById<View>(R.id.btn).setOnClickListener {
            if (mPieView.visibility == View.GONE) {
                mPieView.visibility = View.VISIBLE
                mDashBoardView.visibility = View.GONE
            } else {
                mPieView.visibility = View.GONE
                mDashBoardView.visibility = View.VISIBLE
            }
        }
    }
}