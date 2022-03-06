# View触控

### 拖拽

1. [ViewDragHelper(应用内拖拽)](./src/main/java/com/zxj/touch/drag/DragBlockView.kt)
   <img src='./resources/DrahHelper.gif' style='width: 400px' />

2. [OnDragListener(应用间拖拽)](./src/main/java/com/zxj/touch/drag/fragment/DragTransportFragment.kt) 

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

### [简易ViewPager](./src/main/java/com/zxj/touch/viewgroup/ZViewPager.kt)

* 技术点
    1. VelocityTracker：用于计算速度
    2. OverScroller：滚动帮助类

    <img src='./resources/viewpager.gif' style="width: 400px"/>

### ScalableImageView


### ViewGroup.dispatchTouchEvent解析

```
```