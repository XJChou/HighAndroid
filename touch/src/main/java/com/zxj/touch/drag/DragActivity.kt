package com.zxj.touch.drag

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.zxj.touch.R
import com.zxj.touch.drag.fragment.DragBlockFragment
import com.zxj.touch.drag.fragment.DragTransportFragment

class DragActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drag)

        var index = 0
        findViewById<View>(R.id.btn_switch).setOnClickListener {
            nextPage(index++)
        }
        nextPage(index++)
    }

    private fun nextPage(index: Int) {
        val page = index % 2
        val fragment: Fragment = if (page == 0) {
            DragBlockFragment.newInstance()
        } else {
            DragTransportFragment.newInstance()
        }

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fl_content, fragment)
        transaction.commit()
    }

}