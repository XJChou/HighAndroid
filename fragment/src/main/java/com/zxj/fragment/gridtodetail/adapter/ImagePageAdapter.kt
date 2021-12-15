package com.zxj.fragment.gridtodetail.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.zxj.fragment.gridtodetail.fragment.ImageFragment

class ImagePageAdapter(fragment: Fragment) : FragmentStatePagerAdapter(
    fragment.childFragmentManager,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
) {

    var dataList: List<Int> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getCount() = dataList.size

    override fun getItem(position: Int) =
        ImageFragment.newInstance(dataList[position], "shared_element_${position}")
}