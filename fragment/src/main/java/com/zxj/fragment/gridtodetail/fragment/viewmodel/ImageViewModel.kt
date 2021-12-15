package com.zxj.fragment.gridtodetail.fragment.viewmodel

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import com.zxj.fragment.R

class ImageViewModel : ViewModel() {

    /**
     * * 图片位置
     */
    var imagePosition = 0

    /**
     * * 图片数据
     */
    @DrawableRes
    val dataArray = arrayOf(
        R.drawable.animal_2024172,
        R.drawable.beetle_562035,
        R.drawable.bug_189903,
        R.drawable.butterfly_417971,
        R.drawable.butterfly_dolls_363342,
        R.drawable.dragonfly_122787,
        R.drawable.dragonfly_274059,
        R.drawable.dragonfly_689626,
        R.drawable.grasshopper_279532,
        R.drawable.hover_fly_61682,
        R.drawable.hoverfly_546692,
        R.drawable.insect_278083,
        R.drawable.morpho_43483,
        R.drawable.nature_95365
    )

}