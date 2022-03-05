# View触控

### 色块移动

相关技术：

1. ViewDragHelper：用于拖拽指定的子view，并可以监听拖拽过程做出对应改变，一般常用于应用内的拖拽

2. 属性动画：让子View【移动/交换】的时候有过渡动画，看起来体验会好一点 Tips: 不能使用 translationX/translationY 做动画，是因为
   ViewDrawHelper.findTopChildUnder 查找view的时候，没有加上translationX/translationY,导致查找的view和当前抓起不是同一个

### ViewCompat.startDragAndDrop 应用

相关技术：

View.OnDragListener：View自带的拖拽，主要用于跨应用传输数据【扔老师：如果是分屏的时候，可以把相册的内容，直接拖到微信聊天框，可以直接发照片】

<img src='https://github.com/XJChou/HighAndroid/blob/master/touch/src/main/assets/DragHelper.gif'/>

### 多点触控类型
扔老师总结多点触控为三种类型：
1. [接力型](./src/main/java/com/zxj/touch/multitouch/RelayView.kt): 新手指 承接 老手指 的职责

   <img src='./resources/multitouch_relay.gif' style="width: 400px"/>
   
   Tip: 当有新手指加入的时候，新手指抢过控制权，当新手指抬起，控制权重新回到原手指上；


2. [合作型](./src/main/java/com/zxj/touch/multitouch/CooperationView.kt): 多手指互相配合
   
   <img src='./resources/multitouch_cooperation.gif' style="width: 400px"/>
   
   Tip: 一只手指全速运行；一只手指在下，一只手指在下，半速运行；

3. [各自为战型](./src/main/java/com/zxj/touch/multitouch/SelfView.kt): 每个手指都有独立的职责
 
   <img src='./resources/multitouch_self.gif' style="width: 400px"/>

   Tip: 每个手指都是一直画笔，可以独立绘制路线；

### ScalableImageView

### 简易ViewPager
