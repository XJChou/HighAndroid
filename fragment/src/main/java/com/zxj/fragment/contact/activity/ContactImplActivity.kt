package com.zxj.fragment.contact.activity

import android.content.Intent
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.transition.Slide
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.zxj.fragment.R
import com.zxj.common.bean.SharedElementItem
import com.zxj.fragment.contact.Contacts
import com.zxj.fragment.databinding.ActivityContactImplBinding
import com.zxj.fragment.databinding.ItemContactsBinding

/**
 * activity.setExitSharedElementCallback
 * activity.setEnterSharedElementCallback
 */
class ContactImplActivity : AppCompatActivity() {

    private val mContactsList = initContacts()

    private fun initContacts(): List<Contacts> {
        val result = mutableListOf<Contacts>()
        result.add(
            Contacts(
                "Ragdoll", R.drawable.ragdoll,
                """  布偶猫又叫布拉多尔猫、布娃娃猫、玩偶猫，拉丁学名为Ragdoll。虽然布偶猫的体型和体重在整个猫类中都是最大，但它们确是十足的温柔大块头。它们对人类非常友善，即便面对孩子们的打闹也能大度包容，因此布偶猫受到许多家庭的青睐。
       布偶猫的原产地是美国，由家住加州的一名叫Ann Baker的妇女在上世纪60年代所培育，1965年在美国获得认可，然后逐渐进入其他国家。布偶猫进入中国较晚，至今不过10年时间，但布偶猫却在很短时间内就在中国流行开来，这与它们出众的外貌和极佳的性格密不可分。
       如果你喜欢粘人的猫咪，那么布偶猫绝对是一个不错的选择。它们很喜欢待在主人身边，如果你有事在忙，它们也不会吵到你，因为布偶猫的叫声很轻柔，而且大部分情况下，它们都会保持安静。与此同时，布偶猫除了对主人很友善外，它们对陌生人、小孩子和其他动物都非常友好，因此，主人不必担心布偶猫是否会伤害到家作客的客人或其他动物等。"""
            )
        )
        result.add(
            Contacts(
                "British Shorthair", R.drawable.british_shorthair,
                """英国短毛猫有悠久的历史，但直到20世纪初才引起人们的宠爱。该猫体形圆胖，外型 由中型至大型。其骨架及肌肉很发达，短而肥的颈与及阔而平的肩膊相配合。头部圆而阔， 体粗短发达，被毛短而密，头大脸圆，大而圆的眼睛根据被毛不同而呈现各种颜色。最大的特征是支耳的距离很接近身。该猫温柔平静，对人友善，很容易饲养。
       英国短毛猫的祖先们可以说是“战功赫赫”，早在2000多年前的古罗马帝国时期，它们就曾跟随凯撒大帝到处征战。在战争中，它们靠着超强的捕鼠能力，保护罗马大军的粮草不被老鼠偷吃，充分保障了军需后方的稳定。从此，这些猫在人们心中得到了很高的地位。就在那个时候，它们被带到了英国境内，靠着极强的适应能力，逐渐演变成为英国的土著猫。它不仅被公认为捕鼠高手，那英俊外形也被更多人所喜爱。"""
            )
        )
        result.add(
            Contacts(
                "Dragon Li", R.drawable.dragon_li,
                """中国狸花猫以前被视为难登大雅之堂的土猫，但近年来，许多爱猫人士都致力于中国纯种狸花猫的培育，2010年2月8日经过CFA中国长城猫俱乐部六年的努力，CFA终于认可是狸花这一中国本土自然品种。中国长城猫俱乐部为尊重中国人习惯，公布了狸花猫在国际上的标准名字为：Chinese LiHua。
       中国是狸花猫的源产地，它属于自然猫，因为是在千百年中经过许多品种的自然淘汰而保留下来的品种。中国狸花猫多品种的自然淘汰而保留下来的品种。人们最熟悉的就要算是“狸猫换太子”（宋朝）的故事了，但中国关于狸花这一品种的介绍却远在此之前。2003CFA中国长城猫俱乐部向国际申请，将中国狸花猫做为中国特有猫种向世界展示，经过七年的努力，于2010年2月8日，CFA休斯顿董事会议上通过。狸花猫正式进入CFA。狸花猫在国际的名称依然延用中国名及发音“Chinese Li HUa Mao”。从此中国拥有了自己本土的纯种猫。"""
            )
        )
        result.add(
            Contacts(
                "Exotic Shorthair", R.drawable.exotic_shorthair,
                """  大家喜欢叫它加菲猫，憨憨的样子极其可爱。加菲猫(Garfield)是由吉姆·戴维斯(Jim Davis)所创，第一次出现在美国报纸上是在1978年6月19日。它是一只爱说风凉话、好吃、爱睡觉，以及爱捉弄人的大肥猫。无论成人还是孩子都被它的魅力所倾倒。        
       异国短毛猫（加菲猫）起源
       异国短毛猫（加菲猫）是在六十年代的美国，以人工方式将波斯猫等长毛种的猫与美国短毛猫、缅甸猫等交配繁殖出来的品种。当初进行繁殖计划时，异国短毛猫的体形还很瘦弱，波斯猫的饲养者担心纯种波斯会被杂种化，有些繁殖者甚至强烈批评它“有损纯种猫”，因而在1968年禁止交配。当然，还是有人暗暗努力，最后发现用美国短毛猫配种，形体才渐渐成型。直到八十年代，异国短毛猫的品种正式确立，并获得猫协会的认可。外来种猫由于经过很多人的反复繁育，毛色品种很多，几乎猫中所有的毛色都有。在外观上基本继承了波斯猫滑稽造型。除了毛短之外，其它体型、四肢、头脸眼均与波斯猫一样。"""
            )
        )
        result.add(
            Contacts(
                "American ShortHair", R.drawable.american_shorthair,
                """>美国短毛猫（英语：American Shorthair）是原产美国的一种猫，其祖先为欧洲早期移民带到北美的猫种，与英国短毛猫和欧洲短毛猫同类。据记载，五月花号上携带了数只猫以帮助除鼠。该品种的猫是在街头巷尾收集来的猫当中选种、并和进口品种如英国短毛猫、缅甸猫和波斯猫杂交培育而成。美国短毛猫在欧洲很罕见，但在日本颇受好评，在美国国内也较受欢迎。
        美国短毛猫的特点是外观漂亮，活泼，性格温柔，容易和人类相处，很适合家庭饲养，寿命一般为15到20年。美国短毛猫约有80多种毛色和外观。包括褐虎斑、银斑等等。其中较著名的是银虎斑，这种猫毛发背景为银色，覆盖有浓厚的黑色斑纹。美国短毛猫在1966年正式定名，以纪念其原产地美国。"""
            )
        )
        result.add(
            Contacts(
                "Siamese",
                R.drawable.siamese,
                "   暹罗猫，又叫泰国猫、西母猫，其英文名为Siamese，起源于14世纪，祖先为非纯种亚洲猫。虽然科学家手中并没有握着暹罗猫来源的证据，但他们相信暹罗猫是从泰国而来。据记载，暹罗猫早在几百年前就生活在泰国的皇宫和寺庙中，之后它们被作为外交礼物而送给其他国家。\n暹罗猫被很多人看作贵族，但跟另外一种猫中贵族波斯猫相比，它们则明显外向得多，它们活泼好动，一听到主人的声音就会立马回应。如果你希望身边有一只机灵的猫咪能打破无聊又平静的生活，那么暹罗猫一定不会让你失望。至于网上有人问，暹罗猫需不需要遛来消耗它们的体力，专家则表示暹罗猫在家的运动就可以满足身体运动的需求，如果担心暹罗猫活动不够，那就每天多陪它们来点游戏吧。"
            )
        )
        return result
    }

    private val binding by lazy {
        ActivityContactImplBinding.inflate(layoutInflater)
    }

    private val exitSharedElementCallback = object : SharedElementCallback() {
        /**
         * 最先调用，用于动画开始前替换ShareElements，比如在Activity B翻过若干页大图之后，返回Activity A
         * 的时候需要缩小回到对应的小图，就需要在这里进行替换
         */
        override fun onMapSharedElements(
            names: MutableList<String>,
            sharedElements: MutableMap<String, View>
        ) {
            super.onMapSharedElements(names, sharedElements)
        }


        /**
         * 在这里会把ShareElement里值得记录的信息存到为Parcelable格式，以发送到Activity B
         * 默认处理规则是ImageView会特殊记录Bitmap、ScaleType、Matrix，其它View只记录大小和位置
         */
        override fun onCaptureSharedElementSnapshot(
            sharedElement: View,
            viewToGlobalMatrix: Matrix,
            screenBounds: RectF
        ): Parcelable {
            val originParcelable = super.onCaptureSharedElementSnapshot(
                sharedElement,
                viewToGlobalMatrix,
                screenBounds
            )
            return SharedElementItem.create(sharedElement,originParcelable)
        }

        /**
         * 表示ShareElement已经全部就位，可以开始动画了
         */
        override fun onSharedElementsArrived(
            sharedElementNames: MutableList<String>?,
            sharedElements: MutableList<View>?,
            listener: OnSharedElementsReadyListener?
        ) {
            super.onSharedElementsArrived(sharedElementNames, sharedElements, listener)
        }

        /**
         * 在之前的步骤里(onMapSharedElements)被从ShareElements列表里除掉的View会在此回调，
         * 不处理的话默认进行alpha动画消失
         */
        override fun onRejectSharedElements(rejectedSharedElements: MutableList<View>?) {
            super.onRejectSharedElements(rejectedSharedElements)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        /* 默认打开 */
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        window.exitTransition = Slide(Gravity.LEFT)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.contactsRecycler.layoutManager = LinearLayoutManager(this)
        binding.contactsRecycler.adapter = ContactsAdapter()

        this.setExitSharedElementCallback(exitSharedElementCallback)

    }

    private fun gotoDetailActivity(contacts: Contacts, avatarImg: View, nameTxt: View) {
        val intent = Intent(this, DetailImplActivity::class.java)
        intent.putExtra("contacts", contacts)
        val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this,
            androidx.core.util.Pair(avatarImg, avatarImg.transitionName),
            androidx.core.util.Pair(nameTxt, nameTxt.transitionName)
        ).toBundle()

        // 退出的时候回调
        ActivityCompat.startActivity(this, intent, bundle)
    }

    inner class ContactsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = ItemContactsBinding.inflate(layoutInflater).root
            return object : RecyclerView.ViewHolder(view) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val binding = ItemContactsBinding.bind(holder.itemView)

            val avatarImg = binding.contactsAvatar
            val nameTxt = binding.contactsName
            val item: Contacts = mContactsList[position]

            Glide
                .with(avatarImg).load(item.avatarRes)
                .apply(RequestOptions.circleCropTransform())
                .into(avatarImg)

            nameTxt.text = item.name
            holder.itemView.setOnClickListener {
                gotoDetailActivity(item, avatarImg, nameTxt)
            }
            ViewCompat.setTransitionName(avatarImg, "avatar:" + item.name)
            ViewCompat.setTransitionName(nameTxt, "name:" + item.name)
        }

        override fun getItemCount() = mContactsList.size
    }
}