package com.zxj.fragment.gridtodetail.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.transition.Fade
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.zxj.fragment.databinding.FragmentImageBinding


private const val ARG_IMAGE_RESOURCE = "ARG_IMAGE_RESOURCE"
private const val ARG_TRANSITION_NAME = "ARG_TRANSITION_NAME"

class ImageFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(resource: Int, transitionName: String) =
            ImageFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_IMAGE_RESOURCE, resource)
                    putString(ARG_TRANSITION_NAME, transitionName)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val imageResource = arguments?.getInt(ARG_IMAGE_RESOURCE)
        val transitionName = arguments?.getString(ARG_TRANSITION_NAME)

        val binding = FragmentImageBinding.inflate(inflater)
        binding.root.transitionName = transitionName

        Glide
            .with(this)
            .load(imageResource)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    parentFragment?.startPostponedEnterTransition()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    parentFragment?.startPostponedEnterTransition()
                    return false
                }

            })
            .into(binding.root)
        return binding.root
    }
}