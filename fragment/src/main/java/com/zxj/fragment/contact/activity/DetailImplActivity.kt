package com.zxj.fragment.contact.activity

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.transition.*
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.zxj.common.bean.SharedElementItem
import com.zxj.common.transition.ChangeText
import com.zxj.fragment.R
import com.zxj.fragment.contact.Contacts
import com.zxj.fragment.databinding.ActivityDetailImplBinding
import com.zxj.fragment.transition.ChangeOnlineImageTransition

/**
 * 在Activity 中 Transition相互要独立，不能影响到其他属性，
 * 比如修改TextView的字体大小，在Wrap_content模式下，会影响宽度和高度，最终导致ChangeBound的获取
 */
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
                val snapshot = sharedElementSnapshots[i]
                if (isEnter) {
                    val start = snapshot.getTag(R.id.shared_element_snapshot_start)
                    view.setTag(R.id.shared_element_snapshot_start, start)

                    val end = SharedElementItem.create(view)
                    view.setTag(R.id.shared_element_snapshot_end, end)
                }
            }
//            sharedElementSnapshots.forEachIndexed { index, item ->
//                item.setTag(
//                    R.id.shared_element_snapshot_end,
//                    SharedElementItem.create(sharedElements[index])
//                )
//                val snapshot = item.getTag(R.id.shared_element_snapshot_start)
//                if (snapshot != null && snapshot is SharedElementItem) {
//                    val startColor = snapshot.getInt(SharedElementItem.TEXT_VIEW_COLOR)
//                    val textSize = snapshot.getFloat(SharedElementItem.TEXT_VIEW_SIZE)
//                    (sharedElements[index] as? TextView)?.setTextColor(startColor)
//                    (sharedElements[index] as? TextView)?.setTextSize(
//                        TypedValue.COMPLEX_UNIT_PX,
//                        textSize
//                    )
//                }
//            }
        }

        // 生成结束属性【captureEndValues】
        override fun onSharedElementEnd(
            sharedElementNames: MutableList<String>,
            sharedElements: MutableList<View>,
            sharedElementSnapshots: MutableList<View>
        ) {
            super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots)

//            sharedElementSnapshots.forEachIndexed { index, item ->
//                val snapshot = item.getTag(R.id.shared_element_snapshot_end)
//                if (snapshot != null && snapshot is SharedElementItem) {
//                    val startColor = snapshot.getInt(SharedElementItem.TEXT_VIEW_COLOR)
//                    val textSize = snapshot.getFloat(SharedElementItem.TEXT_VIEW_SIZE)
//                    (sharedElements[index] as? TextView)?.setTextColor(startColor)
//                    (sharedElements[index] as? TextView)?.setTextSize(
//                        TypedValue.COMPLEX_UNIT_PX,
//                        textSize
//                    )
//                }
//            }
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

