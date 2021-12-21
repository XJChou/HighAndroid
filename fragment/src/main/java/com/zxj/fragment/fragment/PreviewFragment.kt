package com.zxj.fragment.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.transition.*
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivPreview.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 共享元素的处理
        sharedElementEnterTransition = TransitionSet().apply {
            addTransition(ChangeBounds())
            addTransition(ChangePosition())
            addTransition(ChangeTransform())
            addTransition(ChangeImageTransform())
        }
    }
}