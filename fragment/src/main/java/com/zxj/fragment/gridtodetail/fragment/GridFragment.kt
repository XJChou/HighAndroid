package com.zxj.fragment.gridtodetail.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.ViewModelProvider
import androidx.transition.Fade
import androidx.transition.Transition
import com.zxj.fragment.R
import com.zxj.fragment.databinding.FragmentGridBinding
import com.zxj.fragment.gridtodetail.adapter.GridAdapter
import com.zxj.fragment.gridtodetail.adapter.ImageViewHolder
import com.zxj.fragment.gridtodetail.adapter.ViewHolderListener
import com.zxj.fragment.gridtodetail.fragment.viewmodel.ImageViewModel
import java.util.concurrent.atomic.AtomicBoolean

class GridFragment : Fragment(), ViewHolderListener {

    companion object {
        fun newInstance() = GridFragment()
    }

    private val imageViewModel by lazy {
        ViewModelProvider(requireActivity())[ImageViewModel::class.java]
    }

    private val adapter by lazy {
        GridAdapter(this).also {
            it.dataList = imageViewModel.dataArray.asList()
        }
    }

    private val binding by lazy {
        FragmentGridBinding.inflate(layoutInflater)
    }

    private lateinit var isTransition: AtomicBoolean

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.root.adapter = adapter

        isTransition = AtomicBoolean()

        exitTransition = Fade().also {
            it.startDelay = 25L
            it.duration = 375
            it.interpolator = FastOutSlowInInterpolator()
        }

        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>,
                sharedElements: MutableMap<String, View>
            ) {
                super.onMapSharedElements(names, sharedElements)

                val viewHolder = binding
                    .root
                    .findViewHolderForAdapterPosition(imageViewModel.imagePosition) as? ImageViewHolder

                // 更新 target view
                viewHolder?.binding?.cardImage?.let {
                    sharedElements[names[0]] = it
                }
            }
        })

        postponeEnterTransition()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scrollToPosition(imageViewModel.imagePosition)
    }

    /**
     * 根据情况滚动到指定位置
     */
    private fun scrollToPosition(position: Int) {
        binding.root.addOnOnceLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            val layoutManager = binding.root.layoutManager ?: return@addOnOnceLayoutChangeListener
            val viewAtPosition = layoutManager.findViewByPosition(position)
            // isViewPartiallyVisible
            if (viewAtPosition == null
                || layoutManager.isViewPartiallyVisible(viewAtPosition, false, true)
            ) {
                binding.root.post { layoutManager.scrollToPosition(position) }
            }
        }
    }


    /**
     * * 当 RecyclerView item 被点击的时候
     */
    override fun onItemClicked(view: View, position: Int) {
        /* 修改position */
        imageViewModel.imagePosition = position

        (exitTransition as Transition)?.excludeTarget(view.parent as View, true)

        /* 切换fragment */
        parentFragmentManager.commit {
            setReorderingAllowed(true)
            addSharedElement(view, view.transitionName)
            replace(
                R.id.fl_content,
                PageFragment.newInstance(),
                PageFragment::class.java.simpleName
            )
            addToBackStack(null)
        }
    }

    /**
     * 条目加载完成回调
     */
    override fun onLoadFinish(position: Int) {
        if (imageViewModel.imagePosition != position) {
            return
        }

        if (isTransition.getAndSet(true)) {
            return
        }

        startPostponedEnterTransition()
    }

}


fun View.addOnOnceLayoutChangeListener(
    block: (
        view: View,
        left: Int, top: Int, right: Int, bottom: Int,
        oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
    ) -> Unit
) {
    this.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
        override fun onLayoutChange(
            v: View,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            oldLeft: Int,
            oldTop: Int,
            oldRight: Int,
            oldBottom: Int
        ) {
            this@addOnOnceLayoutChangeListener.removeOnLayoutChangeListener(this)
            block.invoke(
                v,
                left, top, right, bottom,
                oldLeft, oldTop, oldRight, oldBottom
            )
        }

    })
}