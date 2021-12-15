<p>Demo中的布局、图片和文字(<a href='https://www.jianshu.com/p/fa1c8deeaa57'>来自</a>)，本Demo只研究Activity/Fragment场景下的SharedElement过渡处理</p>

<h3>关于自定义 Activity SharedElement 遇到的问题</h3>
<p>自定义 Activity 的 SharedElement 比 Fragment SharedElement 要复杂一点，要存储前一个 Activity 的状态，然后在当前 Activity 动画的时候带上</p>
<p>假设从 A(activity) -> B(activity)</p>
<ol>
    <li> A.setExitSharedElementCallback 中 onCaptureSharedElementSnapshot 需要存储 A界面 需要自定义元素的信息，在 Demo 中是存储了字体的颜色和大小</li>
    <li> B.setEnterSharedElementCallback 中 onCreateSnapshotView 将 A界面 传输过来的快照，保存起来 </li>
    <li> B.setEnterSharedElementCallback 中 onSharedElementStart 和 onSharedElementEnd 根据 isEnter 分别处理进入和离开的情况</li>
</ol>
<div style='display: box;'>
  <!-- 动态图1 -->
  <img src="./images/Activity_invalid_shared_elment.gif" style="width: 300px;"/>
  &nbsp;&nbsp;&nbsp;&nbsp;
  <!-- 动态图2 -->
  <img src="./images/Activity_valid_shared_elment.gif" style="width: 300px;"/>
</div>
<br/>

<pre>
<p/>
注意：自定义SharedElement的时候，是不能修改其他属性的（比如left,right,top,bottom等）否则，动画不是你预期的;<br/>
我开始的理解在 onSharedElementStart 的时候，从snapshot 信息弄到 sharedElement 上; 在 onSharedElementEnd 恢复原界面的信息；<br/>
但对于 textColor 是可以的，因为它不会关联[left, right，top，bottom等]相关属性，动画是正常的；<br/>
但 textSize 是会 影响 right 和 bottom 的，导致最后的动画会有跳动效果<br/>
最后我在 view 上存储了A界面和B界面信息，根据不同状态来组成动画；暂时没想到有其他更好的方法<br/>
Tips: Activity SharedElement 是自带 GhostView 的在最上层的，所以不用考虑 Fragment 被覆盖问题
</pre>

```java
// 核心流程代码 EnterTransitionCoordinator.startSharedElementTransition 中
class EnterTransitionCoordinator {

    private void startSharedElementTransition(Bundle sharedElementState) {
        // ...

        // 根据前一个数据生成快照
        ArrayList<View> sharedElementSnapshots = createSnapshots(sharedElementState,
                mSharedElementNames);
        showViews(mSharedElements, true);
        // 下一个 onPreDraw 实际调用 onSharedElementEnd
        // 在 onSharedElementStart 中改动的属性，会在 onSharedElementEnd调用前测量，所以不能改变原有属性
        scheduleSetSharedElementEnd(sharedElementSnapshots);
        // 取出 SharedElements 相关属性存储，并调用 onSharedElementEnd
        ArrayList<SharedElementOriginalState> originalImageViewState =
                setSharedElementState(sharedElementState, sharedElementSnapshots);

        // ...

        // 实际调用 TransitionManager.beginDelayedTransition        
        Transition transition = beginTransition(decorView, startEnterTransition,
                startSharedElementTransition);

        // 相关属性恢复到 sharedElement 中
        setOriginalSharedElementState(mSharedElements, originalImageViewState);
    }

}
```

<h3>关于自定义 Fragment SharedElement 遇到的问题</h3>
<p>因为 Fragment 本质就是往 FrameLayout 中的布局 addView，所以 Fragment SharedElement 过渡会很简单,内部其实直接调用 TransitionManager.beginDelayedTransition [具体代码在 FragmentTransition.startTransitions 方法中]</p>
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
<p>一、猜测是 RecyclerView 的问题</p>
列表容器从 RecyclerView -> ListView，发现效果还是一样，可以排除布局导致的
<br/>
<br/>

<p>二、猜测是不是由于 Item 条目的约束导致的</p>
其实这个方向没有抱太大希望，因为除第一外都是好的，而且过渡动画是使用 GhostView 去操作的，按理不会存在影响(此时还没找到 GhostView 应用的代码)
然后分别给 RecyclerView 和 Item 父布局设置了 clipChildren="false", 使得 child view 可以超出 parent view，发现还是效果相同
<br/>
<br/>

<p>三、从 Fragment 的切换和 FragmentTransition 过渡动画入手</p>
读 Fragment 源码后，发现了一些事务提交的过程【纯粹为了读一下Fragment，看能不能找到蛛丝马迹】，也找到与想象中的 FrameLayout.addView 和 removeView 的地方
<br/>
读了 FragmentTransition 源码，最终居然碰到了 TransitionManager.beginDelayedTransition~~但与问题没有相关性
<br/>
<br/>

<p>四、参考源码</p>
当目前排查的路所有堵死之后，决定找找网络上的demo，但实际上，能找到的关于 Fragment 的 SharedElement 的 Demo 文章还是比较少的，然后我去搜了下<a href='https://github.com/android/animation-samples/tree/main/GridToPager'>官方Demo</a>
<br/>
发现 GridToPager 效果是跟我预期想过是一致的，然后pull下来，开始找不同之旅
<br/>
<br/>

<p>五、找到切入口</p>
我开始比对跟 GridToPager 不同的地方；令我万万没想到的是，居然是一行我想不到的代码起了效果， xml 文件中的 paddingStart="8dp" 可以让上述图二运行
<br/>
我分别在 RecyclerView 中，试了 paddingStart、paddingLeft、...、paddingEnd，只有 paddingLeft(paddingStart) 或者 paddingTop 是有效果 
<br/>
<br/>

<p>六、开始找padding相关的地方</p>
当前与 padding 相关的，首当其冲的肯定是 View 和 ViewGroup，找在初始化的时候对 padding 的处理，发现并没有做一些特殊处理
<br/>
<br/>

<p>七、开始从 Transition 开始下手</p>
终于在对每个 Transition 下手的时候，终于发现第一个Item，少触发了一次 ChangeTransform；然后就断点过程，为什么会少触发一次；
最终定位到其中一个属性 ChangeTransform.PROPNAME_PARENT_MATRIX = "android:changeTransform:parentMatrix";
第一个 Item 和其他 Item 不同，它的 parentMatrix 开始等于结束的，而其他的是不等于，最后在 ChangeTransform.createAnimator 之前，会判断有没有发生变更
没有变更则不生成 ChangeTransform animator，从而没生成GhostView做跨view动画
<br/>
<br/>

<h4>解决办法</h4>
<ul>
    <li>ChangeTransform.parentMatrix 实际的值是 view.parent 相对于整个屏幕的 left 和 top; 让 view.parent 的 left 和 top 在2个场景下发生变化即可触发 ChangeTransform 的 GhostView 跨view位移[也就是官方对 RecyclerView 中的 Item 增加 paddingStart，同时回答了第五点start、left和top可以, right 和 bottom不行]</li>
    <li>对 ChangeTransform 进行修改；通过看源码可知，实际干活的是 ChangeTransform.createGhostView; 让 GhostView 干活即可; [<a href='../common/src/main/java/androidx/transition/ChangePosition.kt'>ChangePosition</a>]</li>
    <li>让 FrameLayout 加入到 GhostView 中，自身管理动画过程中的 Transition, 如 Activity Transition 一致
</ul>


### 关于切换回来 sharedElement 改变

当返回的时候想改变 sharedElement 元素的时候，类似 [官方demo](https://github.com/android/animation-samples/tree/main/GridToPager) 效果的话

<img src="./images/recyclerview_to_viewpager_small.gif" style="width: 300px;"/>

我阅读了官方demo代码，发现是在 setExitSharedElementCallback 和 setEnterSharedElementCallback 中的 onMapSharedElements 分别进行了 view 的替换

如果在 onMapSharedElements 没有更改 name 和 view 的话，正常情况下 name.equals(ViewCompat.getTransitionName(view)) 条件是为 true，则不会触发此分支

反之 exitingCall 会使 sharedElementNameMapping 的 key 发生改变，enteringCallback 会使 sharedElementNameMapping 的 value 发生改变，具体工作代码如下

```java
class DefaultSpecialEffectsController {
    @NonNull
    private Map<Operation, Boolean> startTransitions(@NonNull List<TransitionInfo> transitionInfos,
                                                     final boolean isPop, @Nullable final Operation firstOut,
                                                     @Nullable final Operation lastIn) {
        // ...
        if (exitingCallback != null) {
            // Give the SharedElementCallback a chance to override the default mapping
            exitingCallback.onMapSharedElements(exitingNames, firstOutViews);
            for (int i = exitingNames.size() - 1; i >= 0; i--) {
                String name = exitingNames.get(i);
                View view = firstOutViews.get(name);
                if (view == null) {
                    sharedElementNameMapping.remove(name);
                } else if (!name.equals(ViewCompat.getTransitionName(view))) {
                    String targetValue = sharedElementNameMapping.remove(name);
                    sharedElementNameMapping.put(ViewCompat.getTransitionName(view),
                            targetValue);
                }
            }
        }
        // ...
        if (enteringCallback != null) {
            // Give the SharedElementCallback a chance to override the default mapping
            enteringCallback.onMapSharedElements(enteringNames, lastInViews);
            for (int i = enteringNames.size() - 1; i >= 0; i--) {
                String name = enteringNames.get(i);
                View view = lastInViews.get(name);
                if (view == null) {
                    String key = FragmentTransition.findKeyForValue(
                            sharedElementNameMapping, name);
                    if (key != null) {
                        sharedElementNameMapping.remove(key);
                    }
                } else if (!name.equals(ViewCompat.getTransitionName(view))) {
                    String key = FragmentTransition.findKeyForValue(
                            sharedElementNameMapping, name);
                    if (key != null) {
                        sharedElementNameMapping.put(key,
                                ViewCompat.getTransitionName(view));
                    }
                }
            }
        }
        // ...
    }
}
```
