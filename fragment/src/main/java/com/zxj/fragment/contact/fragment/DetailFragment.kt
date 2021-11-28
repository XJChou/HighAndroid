package com.zxj.fragment.contact.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.transition.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.zxj.common.transitionx.ChangeText
import com.zxj.fragment.R
import com.zxj.fragment.contact.Contacts
import com.zxj.fragment.databinding.FragmentDetailBinding

/**
 * 联系猫版
 * [1] 遇到问题：列表第一条目被遮挡
 */
class DetailFragment : Fragment(R.layout.fragment_detail) {

    companion object {
        private val TRANSITION = TransitionSet().apply {
            addTransition(ChangeClipBounds())
            addTransition(ChangeBounds())
            addTransition(ChangeTransform())
            addTransition(ChangeText())
            addTransition(ChangeImageTransform())
        }

        fun newInstance(contacts: Contacts): DetailFragment {
            val detailFragment = DetailFragment()
            detailFragment.arguments = Bundle().also {
                it.putParcelable("Contacts", contacts)
            }
            detailFragment.sharedElementEnterTransition = TRANSITION
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
