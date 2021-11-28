<p>Demo中的布局、图片和文字(<a href='https://www.jianshu.com/p/fa1c8deeaa57'>来自</a>)，本Demo只研究Activity/Fragment场景下的SharedElement过渡处理</p>

<h3>关于自定义Activity SharedElement遇到的问题</h3>
<div>
    <br/>
    <br/>
    <br/>
</div>

<h3>关于自定义Fragment SharedElement遇到的问题</h3>
<p>因为Fragment本质就是往FrameLayout中的布局addView，所以Fragment SharedElement过渡会很简单,内部其实直接调用 TransitionManager.beginDelayedTransition[具体代码在FragmentTransition.startTransitions方法中]</p>
<p>当所有东西都准备好，运行如图一所示，第一条目的过渡没有像理想中一样，而第二和第三符合预期</p>
<div style='display: box;'>
  <!-- 动态图1 -->
  <img src="./images/Fragment_invalid_shared_element_resize.gif" style="width: 300px;"/>
  &nbsp;&nbsp;&nbsp;&nbsp;
  <!-- 动态图2 -->
  <img src="./images/Fragment_valid_shared_element resize.gif" style="width: 300px;"/>
</div>
<br/>

<p>问题排查过程：</p>
<p>一、猜测是RecyclerView的问题</p>
我把列表容器从RecyclerView -> ListView，发现效果还是一样，猜测不是因为布局导致的
<br/>
<br/>

<p>二、猜测是不是由于Item条目的约束导致的【其实这个方向没有抱太大希望，因为除第一外都是好的，而且过渡动画是使用GhostView去操作的，按理不会存在影响(此时还没找到GhostView应用的代码)】</p>
然后分别给RecyclerView和Item父布局设置了 clipChildren="false", 让裁剪子view，发现还是效果一直
<br/>
<br/>

<p>三、从 Fragment 的切换和 FragmentTransition 过渡动画入手</p>
读 Fragment 源码后，发现了一些事务提交的过程【纯粹为了读一下Fragment，看能不能找到蛛丝马迹】，也找到与想象中的FrameLayout.addView和removeView的地方
<br/>
读了 FragmentTransition 源码，最终居然碰到了TransitionManager.beginDelayedTransition，但实际还是没看到有关于问题的蛛丝马迹
<br/>
<br/>

<p>四、参考源码</p>
当目前排查的路所有堵死之后，决定找找网络上的demo，但实际上，能找到的关于Fragment的SharedElement的Demo文章还是比较少的，然后我去搜了下<a href='https://github.com/android/animation-samples/tree/main/GridToPager'>官方Demo</a>
<br/>
发现 GridToPager 效果是跟我预期想过是一致的，然后pull下来，开始找不同之旅
<br/>
<br/>

<p>五、找到切入口</p>
我开始比对跟 GridToPager 不同的地方；令我万万没想到的是，居然是一行我想不到的代码起了效果， xml 文件中的 paddingStart="8dp" 可以让上述图二运行
<br/>
我分别在 RecyclerView 中，试了paddingStart、paddingLeft、...、paddingEnd，只有paddingLeft(paddingStart) 或者 paddingTop 是有效果 
<br/>
<br/>

<p>六、开始找padding相关的地方</p>
当前与padding相关的，首当其冲的肯定是 View 和 ViewGroup，找在初始化的时候对 padding 的处理，发现并没有做一些特殊处理
<br/>
<br/>

<p>七、开始从Transition开始下手</p>
终于在对每个Transition下手的时候，终于发现第一个Item，少触发了一次 ChangeTransform；然后就断点过程，为什么会少触发一次；
最终定位到其中一个属性 String PROPNAME_PARENT_MATRIX = "android:changeTransform:parentMatrix";
第一个Item和其他Item不同，它的 parentMatrix 是等于结束的，而其他的是不等于，最后在 ChangeTransform.createAnimator 之前，会判断有没有发生变更
没有变更则不生成 ChangeTransform animator，导致此效果
<br/>
<br/>
<pre>ChangeTransform 看到 GhostView 的运用，还看到 mReparent 和 mUseOverlay 这些变量和运用，能解答猜测二</pre>

