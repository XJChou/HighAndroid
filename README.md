# HighAndroid
Android进阶Demo【HenCoder 第八期】

<h3><a href='https://github.com/XJChou/HighAndroid/blob/master/touch/src/main/java/com/zxj/touch/drag/DragBlockView.kt'>DragBlockView【简易色块移动】</a></h3>
</p>
相关技术：</p>
    1、ViewDragHelper：用于拖拽指定的子view，并可以监听拖拽过程做出对应改变，一般常用于应用内的拖拽</p>
    2、属性动画：让子View【移动/交换】的时候有过渡动画，看起来体验会好一点</p>
<p>Tips: 不能使用 translationX/translationY 做动画，是因为 ViewDrawHelper.findTopChildUnder 查找view的时候，没有加上translationX/translationY,导致查找的view和当前抓起不是同一个<p/>
<img src='https://github.com/XJChou/HighAndroid/blob/master/touch/src/main/assets/DragHelper.gif'/>
</p>

<h3><a href='https://github.com/XJChou/HighAndroid/blob/master/touch/src/main/java/com/zxj/touch/drag/fragment/DragTransportFragment.kt'>DragTransportFragment</a></h3>
</p>
相关技术：</p>
    View.OnDragListener：View自带的拖拽，主要用于跨应用传输数据【扔老师：如果是分屏的时候，可以把相册的内容，直接拖到微信聊天框，可以直接发照片】 </p>

<h3>ConstraintLayout</h3>
</p>
<h4><a href='https://github.com/XJChou/HighAndroid/blob/master/constraint/README.md'>关于ConstraintLayout中TextView省略号问题</a></h4>
<div>
    <img width="300px" src='https://github.com/XJChou/HighAndroid/blob/master/constraint/images/Constraint_validate.gif'/>
    <img width="300px" src='https://github.com/XJChou/HighAndroid/blob/master/constraint/images/Constraint_invalidate.gif'/>
</div>

<h3>SharedElement 过渡相关</h3>
<h4><a href='./fragment/README.md'>关于 Activity/Fragment.SharedElement 过渡相关问题</a></h4>
<div>
    <img width="300px" src='./fragment/images/Fragment_invalid_shared_element_resize.gif'/>
    <img width="300px" src='./fragment/images/Fragment_valid_shared_element resize.gif'/>
</div>

### 其他
* [手动打包简单APK流程](https://github.com/XJChou/HighAndroid/tree/master/buildapk)
* [简单插件化和热更新](https://github.com/XJChou/HighAndroid/tree/master/component)
* [简易Android Processor使用](butterknife)
