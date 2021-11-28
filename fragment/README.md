<p>Demo中的布局、图片和文字<a href=‘https://www.jianshu.com/p/fa1c8deeaa57’>来源</a>，本Demo只研究Activity/Fragment场景下的SharedElement过渡处理</p>

<pre>
    <p>关于自定义Activity SharedElement</p>
</pre>

<pre>
    <p>关于自定义Fragment SharedElement</p>
    因为Fragment本质就是切换，FrameLayout中的布局[就是addView]，所以Fragment过渡会简单，就是切换完成后，等待onPreDraw执行，
    内部的过渡也是调用了TransitionManager.beginDelayedTransition
    <div>
      <!-- 动态图1 -->
      <img src="./images/Fragment_invalid_shared_element_resize.gif" style="width: 300px;"/>
      <!-- 动态图2 -->
      <img src="./images/Fragment_valid_shared_element resize.gif" style="width: 300px;"/>
    </div>
</pre>