package com.zxj.fragment

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.zxj.common.commit
import com.zxj.fragment.fragment.ListFragment
import com.zxj.fragment.fragment.PreviewFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.commit {
            add(R.id.fl_content, ListFragment())
        }
    }

    fun transitionPreview(view: View) {
        supportFragmentManager.commit {
            // startView -> targetName
//            setCustomAnimations(R.transition.transition_image, R.transition.transition_image)
            addSharedElement(view, "item_view")
            replace(R.id.fl_content, PreviewFragment())
            addToBackStack(null)
        }
    }
}

interface Test {
    suspend fun test()
}