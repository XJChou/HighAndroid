<div>
  <!-- 动态图1 -->
  <img src="./images/Constraint_invalidate.gif" style="width: 300px;"/>
  <!-- 动态图2 -->
  <img src="./images/Constraint_validate.gif" style="width: 300px;"/>
</div>

问题的由来，是在HencoderPlus听 ConstraintLayout 课程的时候，模仿了下高老师的效果，然后发现我写的并不能出现省略号，然后我就调呀调，调呀调。最后调出来了,当把黄色背景的 TextView 的高度设为android:layout_height="wrap_content"的时候，就可以正常显示了，但为什么呢？？？
<br/>
<br/>

先说结论：这个问题是由于TextView在 measure 过程的宽度和 layout 过程的宽度不是同一个，而TextView的绘制文本的layout[用于控制省略号宽度]是在 measure 过程创建的，layout的时候没有判断在measure过程中宽度是否与layout给予的宽度一致，导致这个问题
<br/>
<br/>

<p>过程分析：</p>

<p>第一步：</p>
首先我先弄了个 LinearLayout 套 TextView，然后给此 TextView 设置成[ match_parent || wrap_content || 固定值 ]，都是可以正常显示的省略号的，那么可以确定由于 ConstraintLayout 触发的
<br/>
<br/>

<p>第二步：</p>
观察每次 ConstraintLayout measure 指定TextView的时候，观察宽度改变，最后发现 measure 过程后 和 layout 过程 在前面2个动图的宽高测量是一致的，此时陷入了迷茫；
<br/>
<br/>

<pre>
<p style='margin-bottom: 0px;'>这里我看了 ConstraintLayout 的 大概measure过程，有些可以分享的</p>

<p style='margin-bottom: 0px;'>1、View 和 widget 的关系 </p>
<p style='margin-bottom: 0px;'>widget 和 view 都独有宽高尺寸，widget 是主要承担 view 的测量工作的。widget 的尺寸赋值中，有四种测量模式；</p>
<p style='margin-bottom: 0px;'>分别是【ConstraintWidget.DimensionBehaviour类中的FIXED, WRAP_CONTENT, MATCH_CONSTRAINT, MATCH_PARENT】</p>
<p style='margin-bottom: 0px;'>下面宽度为例，以为由下面的六种模式View尺寸转换而来的</p>
<ul>
  <li>当 android:layout_width="100dp" 的时候，此时为固定值,对于 view 的 ConstraintWidget 中标记此模式为 ConstraintWidget.DimensionBehaviour.FIXED</li>
  <li>当 android:layout_width="0dp" 的时候，对于 view 的 ConstraintWidget 中标记此模式为 ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT</li>     
  <li>当 android:layout_width="wrap_content" 和 app:layout_constrainedWidth="false" 的时候，对于 view 的 ConstraintWidget 中标记此模式ConstraintWidget.DimensionBehaviour.WRAP_CONTENT</li>
  <li>当 android:layout_width="wrap_content" 和 app:layout_constrainedWidth="true" 的时候，对于 view 的 ConstraintWidget 中标记此模式为 ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT</li>
  <li>当 android:layout_width="match_parent" 和 app:layout_constrainedWidth="true" 的时候，对于 view 的 ConstraintWidget 中标记此模式为 ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT </li>
  <li>当 android:layout_width="match_parent" 和 app:layout_constrainedWidth="false" 的时候，对于 view 的 ConstraintWidget 中标记此模式为 ConstraintWidget.DimensionBehaviour.MATCH_PARENT</li>
</ul>
<p style='margin-bottom: 0px;'>2、ConstraintLayout 可变尺寸的测量</p>
<p style='margin-bottom: 0px;'>Widget 的宽或者高的尺寸为 ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT 或者 ConstraintWidget.DimensionBehaviour.MATCH_PARENT的时候，称为可变尺寸；</p>
<p style='margin-bottom: 0px;'>可变尺寸在一次 measure 过程中，会测量3次子view</p>
</pre>
<br/>

<p>第三步：</p>
因为从测量后的过程和layout过程已经找不出问题了；那么只能去源头找，找TextView中对于省略号的控制的代码在哪里；<br/>
发现在onMeasure过程，然后断点调试，最后发现，在第三次测量的时候，ConstraintLayout 使用了父布局宽度去测量，但最后通过 LinearSystem 这个类矫正回我们肉眼看到的尺寸了,所以最后的measure和layout都是对的;<br/>
但由于 TextView 特性，是在 measure 过程建立好省略号所需要的宽度了，所以不会再次更新 layout过程 实际宽度的了【Tips: 如果你设置了android:autoSizeTextType="uniform"属性，是会在layout过程强制在刷新一次的】
<br/>
<br/>

<pre>
实际出现问题的代码在 ConstraintLayout 中的 Measurer 内部类中，measure方法中 <br/>
<img src="./images/ConstraintLayout_error.jpg" />
<p/>
而当高度设置为 android:layout_height="wrap_content"的时候，didVerticalWrap为true，widget.wrapMeasure[VERTICAL + 2] != 0，所以不会出现此情况第三次的时候使用父容器宽度测量<br/>
<p/>

还有另外一个知识点：
然后在翻查 ConstraintLayout 代码的时候，发现有个另外一个方法 forceLayout，这个方法跟 requestLayout 一样，是用来标记此view需要重新布局的，但是有个区别是，forceLayout 只用来标记，不会向 父view传递，则只是用来标记的；这个用法，我猜测是，操控指定的子view刷新而不刷新全部子view，做一些精细化的控制；因为关于 forceLayout 资料其实蛮少的。
</pre>


<!-- 解决方法 -->

<!-- Constraint主要继承图 -->

<!-- 测量 -->
