package com.zxj.common

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction


inline fun FragmentManager.commit(block: FragmentTransaction.() -> Unit) {
    val transaction = beginTransaction()
    transaction.block()
    transaction.commit()
}