package com.zxj.fragment.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.zxj.fragment.MainActivity
import com.zxj.fragment.databinding.FragmentListBinding

class ListFragment : Fragment() {

    private val binding by lazy {
        FragmentListBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewCompat.setTransitionName(binding.ivIcon1, "list_item_view_1")
        ViewCompat.setTransitionName(binding.ivIcon2, "list_item_view_2")
        ViewCompat.setTransitionName(binding.ivIcon3, "list_item_view_3")
        ViewCompat.setTransitionName(binding.ivIcon4, "list_item_view_4")
        ViewCompat.setTransitionName(binding.ivIcon5, "list_item_view_5")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val onClickListener = View.OnClickListener {
            it.elevation = 100f
            (requireActivity() as? MainActivity)?.transitionPreview(it)
        }

        binding.ivIcon1.setOnClickListener(onClickListener)
        binding.ivIcon2.setOnClickListener(onClickListener)
        binding.ivIcon3.setOnClickListener(onClickListener)
        binding.ivIcon4.setOnClickListener(onClickListener)
        binding.ivIcon5.setOnClickListener(onClickListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("LifeScope", "ListFragment onCreate")
    }

    override fun onStart() {
        super.onStart()
        Log.e("LifeScope", "ListFragment onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.e("LifeScope", "ListFragment onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.e("LifeScope", "ListFragment onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.e("LifeScope", "ListFragment onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("LifeScope", "ListFragment onDestroy")
    }
}