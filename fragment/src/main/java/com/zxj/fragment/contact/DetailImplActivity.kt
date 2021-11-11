package com.zxj.fragment.contact

import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.ChangeTransform
import android.transition.Fade
import android.transition.TransitionSet
import androidx.appcompat.app.AppCompatActivity
import com.zxj.fragment.databinding.ActivityDetailImplBinding

class DetailImplActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityDetailImplBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        window.enterTransition = Fade()
        window.exitTransition = Fade()

        val transitionSet = TransitionSet()
        transitionSet.ordering = TransitionSet.ORDERING_TOGETHER
//            transitionSet.addTransition(ChangeClipBounds())
        transitionSet.addTransition(ChangeBounds())
        transitionSet.addTransition(ChangeTransform())
//        transitionSet.addTransition(ChangeTextSize())
//        transitionSet.addTransition(ChangeTextColor())
        window.sharedElementEnterTransition = transitionSet
    }
}