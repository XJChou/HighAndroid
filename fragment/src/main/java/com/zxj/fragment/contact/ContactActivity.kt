package com.zxj.fragment.contact

import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.zxj.common.commit
import com.zxj.fragment.R

class ContactActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState)
//        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
//        window.sharedElementsUseOverlay = true
        setContentView(R.layout.activity_contact)

        val contactFragment = ContactFragment()
        contactFragment.allowReturnTransitionOverlap = true
        contactFragment.allowEnterTransitionOverlap = true
        supportFragmentManager.commit {
            replace(R.id.fl_content, contactFragment)
        }
    }

}