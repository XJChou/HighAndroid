package com.zxj.fragment.contact

import android.os.Bundle
import android.transition.ChangeImageTransform
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.transition.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.zxj.common.transition.ChangeTextColor
import com.zxj.common.transition.ChangeTextSize
import com.zxj.fragment.R
import com.zxj.fragment.databinding.FragmentDetailBinding

class DetailFragment : Fragment(R.layout.fragment_detail) {


    companion object {
        fun newInstance(contacts: Contacts): DetailFragment {
            val detailFragment = DetailFragment()
            detailFragment.arguments = Bundle().also {
                it.putParcelable("Contacts", contacts)
            }
            val transitionSet = TransitionSet()
            transitionSet.ordering = TransitionSet.ORDERING_TOGETHER
//            transitionSet.addTransition(ChangeClipBounds())
            transitionSet.addTransition(ChangeBounds())
            transitionSet.addTransition(ChangeTransform())
            transitionSet.addTransition(ChangeTextSize())
            transitionSet.addTransition(ChangeTextColor())
//            transitionSet.addTransition(androidx.transition.ChangeImageTransform())

            detailFragment.allowEnterTransitionOverlap = true
            detailFragment.allowReturnTransitionOverlap = true
            detailFragment.sharedElementEnterTransition = transitionSet

            detailFragment.enterTransition = Fade()
            return detailFragment
        }
    }

    private lateinit var binding: FragmentDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        if (view != null) {
            binding = FragmentDetailBinding.bind(view)
            setData()
        }
        return view
    }


    private fun setData() {
        val item: Contacts = arguments?.getParcelable("Contacts") ?: return
        Glide
            .with(binding.avatar).load(item.avatarRes)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.avatar)
        binding.name.text = item.name
        binding.desc.text = item.desc
        ViewCompat.setTransitionName(binding.avatar, "avatar:" + item.name)
        ViewCompat.setTransitionName(binding.name, "name:" + item.name)
    }

}
