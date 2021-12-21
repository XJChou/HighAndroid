package com.zxj.fragment.contact.fragment

import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.zxj.common.commit
import com.zxj.fragment.R

class ContactActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        val contactFragment = ContactFragment()
        contactFragment.allowReturnTransitionOverlap = true
        contactFragment.allowEnterTransitionOverlap = true
        supportFragmentManager.commit {
            replace(R.id.fl_content, contactFragment)
        }
    }

}