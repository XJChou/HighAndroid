package com.zxj.fragment.gridtodetail.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.ViewModelProvider
import androidx.transition.ChangeBounds
import androidx.transition.ChangeClipBounds
import androidx.transition.ChangePosition
import androidx.transition.TransitionSet
import androidx.viewpager.widget.ViewPager
import com.zxj.fragment.databinding.FragmentPageBinding
import com.zxj.fragment.gridtodetail.adapter.ImagePageAdapter
import com.zxj.fragment.gridtodetail.fragment.viewmodel.ImageViewModel


class PageFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = PageFragment()
    }

    private val imageViewModel by lazy {
        ViewModelProvider(requireActivity())[ImageViewModel::class.java]
    }

    private val adapter by lazy {
        ImagePageAdapter(this).also {
            it.dataList = imageViewModel.dataArray.asList()
        }
    }

    private val sharedElementTransitionSet = TransitionSet().also {
        it.duration = 375L
        it.ordering = TransitionSet.ORDERING_TOGETHER
        it.interpolator = FastOutSlowInInterpolator()
        it.addTransition(ChangeBounds())
//        it.addTransition(ChangeImageTransform())
//        it.addTransition(ChangeTransform())
        it.addTransition(ChangePosition())
        it.addTransition(ChangeClipBounds())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPageBinding.inflate(layoutInflater)
        binding.root.adapter = adapter
        binding.root.currentItem = imageViewModel.imagePosition
        binding.root.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            // 当 position 改变的时候
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                imageViewModel.imagePosition = position
            }
        })

        /* 防止页面重建时候的延迟，等待 ViewPager绘制 */
        if (savedInstanceState == null) {
            postponeEnterTransition()
        }

        sharedElementEnterTransition = sharedElementTransitionSet

        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>,
                sharedElements: MutableMap<String, View>
            ) {
                super.onMapSharedElements(names, sharedElements)
                // 更新 target view
                val fragment = adapter
                    .instantiateItem(binding.root, imageViewModel.imagePosition) as? Fragment
                val image = fragment?.view as? ImageView ?: return
                sharedElements[names[0]] = image
            }
        })


        return binding.root
    }

}