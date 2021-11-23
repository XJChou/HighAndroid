package com.zxj.fragment.contact

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.transition.*
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.zxj.fragment.R
import com.zxj.fragment.bean.SharedElementItem
import com.zxj.fragment.databinding.ActivityDetailImplBinding
import com.zxj.fragment.transition.ChangeOnlineImageTransition
import com.zxj.fragment.transition.ChangeText

class DetailImplActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityDetailImplBinding.inflate(layoutInflater)
    }

    private val enterSharedElementCallback = object : SharedElementCallback() {

        private var isEnter = true

        /**
         * 在这里会把ContactImplActivity 传过来的 Parcelable 数据，重新生成一个 View
         * 这个 View 的大小和位置会与 ContactImplActivity 里的 ShareElement一致，
         */
        override fun onCreateSnapshotView(context: Context, snapshot: Parcelable): View {
            val item = snapshot as SharedElementItem
            val view = super.onCreateSnapshotView(context, item.originParcelable)
            view.setTag(R.id.shared_element_snapshot_start, snapshot)
            return view
        }

        // 生成开始属性【captureStartValues】
        override fun onSharedElementStart(
            sharedElementNames: MutableList<String>,
            sharedElements: MutableList<View>,
            sharedElementSnapshots: MutableList<View>
        ) {
            super.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots)
            sharedElements.forEachIndexed { i, view ->
                // 开始便利
                val snapshot = sharedElementSnapshots[i]
                if (isEnter) {
                    val start = snapshot.getTag(R.id.shared_element_snapshot_start)
                    view.setTag(R.id.shared_element_snapshot_start, start)

                    val end = SharedElementItem().save(view)
                    view.setTag(R.id.shared_element_snapshot_end, end)
                }
            }
        }

        // 生成结束属性【captureEndValues】
        override fun onSharedElementEnd(
            sharedElementNames: MutableList<String>,
            sharedElements: MutableList<View>,
            sharedElementSnapshots: MutableList<View>
        ) {
            super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots)
            sharedElements.forEachIndexed { i, view ->
                if (!isEnter) {
                    // 返回的时候交换回去
                    val end = view.getTag(R.id.shared_element_snapshot_start)
                    val start = view.getTag(R.id.shared_element_snapshot_end)

                    view.setTag(R.id.shared_element_snapshot_start, start)
                    view.setTag(R.id.shared_element_snapshot_end, end)
                }
            }
            isEnter = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fillData()
        setEnterSharedElementCallback(enterSharedElementCallback)
        initTransition()
    }

    private fun initTransition() {
        window.enterTransition = Fade()

        val transitionSet = TransitionSet()
        transitionSet
            .addTransition(ChangeClipBounds())
            .addTransition(ChangeTransform())
            .addTransition(ChangeBounds())
            .addTransition(ChangeText())
        if (true) {
            transitionSet.addTransition(ChangeImageTransform())
        } else {
            transitionSet.addTransition(ChangeOnlineImageTransition())
        }
        window.sharedElementEnterTransition = transitionSet
    }

    private fun fillData() {
        val contacts = intent.getParcelableExtra<Contacts>("contacts") ?: return

        // 从 intent 取内容给 view 设置 transitionName
        ViewCompat.setTransitionName(binding.avatar, "avatar:" + contacts.name)
        ViewCompat.setTransitionName(binding.name, "name:" + contacts.name)

        binding.name.text = contacts.name
        binding.desc.text = contacts.desc
        Glide
            .with(binding.avatar).load(contacts.avatarRes)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.avatar)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finishAfterTransition()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

