package com.zxj.animation.fragment

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.zxj.animation.R
import com.zxj.animation.view.CircleView
import com.zxj.common.dp

class FirstFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflate = inflater.inflate(R.layout.fragment_first, container, false)
        inflate.findViewById<CircleView>(R.id.view).apply {
            val animator = ObjectAnimator.ofFloat(this, "radius", 150.dp)
            animator.startDelay = 1000L
            animator.start()
        }
        return inflate
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    companion object {
        @JvmStatic
        fun newInstance() = FirstFragment()
    }
}