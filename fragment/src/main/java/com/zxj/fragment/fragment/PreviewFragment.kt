package com.zxj.fragment.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import com.zxj.fragment.R
import com.zxj.fragment.databinding.FragmentPreviewBinding

class PreviewFragment : Fragment() {

    private val binding by lazy {
        FragmentPreviewBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewCompat.setTransitionName(binding.ivPreview, "item_view")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivPreview.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 共享元素的处理
        sharedElementEnterTransition = TransitionInflater
            .from(requireContext())
            .inflateTransition(R.transition.transition_image)

        sharedElementReturnTransition = TransitionInflater
            .from(requireContext())
            .inflateTransition(R.transition.transition_image)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("LifeScope", "PreviewFragment onCreate")
    }

    override fun onStart() {
        super.onStart()
        Log.e("LifeScope", "PreviewFragment onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.e("LifeScope", "PreviewFragment onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.e("LifeScope", "PreviewFragment onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.e("LifeScope", "PreviewFragment onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("LifeScope", "PreviewFragment onDestroy")
    }
}