# View的系统拖拽[OnDragListener]

### View Drag ACTION
* ACTION_DRAG_STARTED 和 ACTION_DRAG_ENDED: 分别代表拖拽的事件开始和结束[与触摸事件ACTION_DOWN和ACTION_UP作用类似]
* ACTION_DRAG_ENTERED 和 ACTION_DRAG_EXITED: 分别代表拖拽的内容进入和退出当前view
* ACTION_DRAG_LOCATION: 代表拖拽内容当前正在当前view上移动[与触摸事件ACTION_MOVE作用类似]
* ACTION_DROP: 代表拖拽内容在当前view放下了

### 拖拽调用栈
ViewRootImpl.handleDragEvent
    -> ViewGroup.dispatchDragEvent
        -> View.dispatchDragEvent
            -> ViewRootImpl.setDragFocus
        -> super.dispatchDragEvent
        -> ViewRootImpl.setDragFocus

### 源码分析[基于Android7.0及以上代码分析]
```java
public abstract class ViewGroup extends View implements ViewParent, ViewManager {

    @Override
    public boolean dispatchDragEvent(DragEvent event) {
        // 代表此事件是否有view处理
        boolean retval = false;
        
        final float tx = event.mX;
        final float ty = event.mY;
        final ClipData td = event.mClipData;
        final PointF localPoint = getLocalPoint();

        switch (event.mAction) {
            
            case DragEvent.ACTION_DRAG_STARTED: {
                // step1: 清空所有工作数据
                mCurrentDragChild = null;
                mCurrentDragStartEvent = DragEvent.obtain(event);
                if (mChildrenInterestedInDrag == null) {
                    mChildrenInterestedInDrag = new HashSet<View>();
                } else {
                    mChildrenInterestedInDrag.clear();
                }

                // step2: 分发 child view ACTION_DRAG_STARTED 事件，所有child都可以接受到
                final int count = mChildrenCount;
                final View[] children = mChildren;
                for (int i = 0; i < count; i++) {
                    final View child = children[i];
                    child.mPrivateFlags2 &= ~View.DRAG_MASK;
                    if (child.getVisibility() == VISIBLE) {
                        /**
                         * 1. 将坐标系转换为child的坐标系
                         * 2. 检测子view是否可以drag，即在 ACTION_DRAG_STARTED 返回true，与ACTION_DOWN相同
                         *      * 加入 mChildrenInterestedInDrag 集合，用于分发 ACTION_DRAG_ENDED 事件
                         *      * 设置 child view PFLAG2_DRAG_CAN_ACCEPT，canAcceptDrag返回true，用于 ACTION_DRAG_LOCATION | ACTION_DROP 判断
                         */
                        if (notifyChildOfDragStart(children[i])) {
                            retval = true;
                        }
                    }
                }

                // step3: 判断自身是否消费 ACTION_DRAG_STARTED 时间
                mIsInterestedInDrag = super.dispatchDragEvent(event);
                if (mIsInterestedInDrag) {
                    retval = true;
                }

                // step4: 没有人消费，则回收event
                if (!retval) {
                    mCurrentDragStartEvent.recycle();
                    mCurrentDragStartEvent = null;
                }
            } break;

            case DragEvent.ACTION_DRAG_ENDED: {
                // step1: 从ACTION_DRAG_STARTED记录响应drag的child view，全部分发 ACTION_DRAG_ENDED 事件
                final HashSet<View> childrenInterestedInDrag = mChildrenInterestedInDrag;
                if (childrenInterestedInDrag != null) {
                    for (View child : childrenInterestedInDrag) {
                        if (child.dispatchDragEvent(event)) {
                            retval = true;
                        }
                    }
                    childrenInterestedInDrag.clear();
                }
                
                // step2: 回收event事件
                if (mCurrentDragStartEvent != null) {
                    mCurrentDragStartEvent.recycle();
                    mCurrentDragStartEvent = null;
                }

                // step3: 如果自身响应drag，则对应的也分发一次
                if (mIsInterestedInDrag) {
                    if (super.dispatchDragEvent(event)) {
                        retval = true;
                    }
                    mIsInterestedInDrag = false;
                }
            } break;

            case DragEvent.ACTION_DRAG_LOCATION:
            case DragEvent.ACTION_DROP: {
                /**
                 * step1: 根据当前坐标查找倒序child view, 需要具备2个条件
                 *      1. child.canAcceptDrag() 为true，即判断是否含有 PFLAG2_DRAG_CAN_ACCEPT
                 *      2. 当前坐标需要在child view上
                 */
                View target = findFrontmostDroppableChildAt(event.mX, event.mY, localPoint);

                // step2: 不相等则切换当前拖拽目标
                if (target != mCurrentDragChild) {
                    mCurrentDragChild = target;
                }

                // step3: 如果child view不响应，且 parent view能够处理，则交给parent处理
                if (target == null && mIsInterestedInDrag) {
                    target = this;
                }

                // step4: 分发事件
                if (target != null) {
                    if (target != this) {
                        event.mX = localPoint.x;
                        event.mY = localPoint.y;

                        retval = target.dispatchDragEvent(event);

                        event.mX = tx;
                        event.mY = ty;

                        if (mIsInterestedInDrag) {
                            final boolean eventWasConsumed;
                            if (sCascadedDragDrop) {
                                eventWasConsumed = retval;
                            } else {
                                // view.dispatchDragEvent 消费事件就为true
                                eventWasConsumed = event.mEventHandlerWasCalled;
                            }
                            
                            // 没有child消费则给自身消费
                            if (!eventWasConsumed) {
                                retval = super.dispatchDragEvent(event);
                            }
                        }
                    } else {
                        retval = super.dispatchDragEvent(event);
                    }
                }
            } break;
        }

        return retval;
    }
    
}
```

```java
public class View implements Drawable.Callback, KeyEvent.Callback, AccessibilityEventSourc {
    
    // ...
    
    public boolean dispatchDragEvent(DragEvent event) {
        // step1: 标记着有child view处理此event
        event.mEventHandlerWasCalled = true;
        
        // step2: 通知 ViewRootImpl 切换当前拖拽view
        if (event.mAction == DragEvent.ACTION_DRAG_LOCATION || event.mAction == DragEvent.ACTION_DROP) {
            getViewRootImpl().setDragFocus(this, event);
        }
        
        // step3: 触发拖拽回调
        return callDragEventHandler(event);
    }

    // ...
}
```

### 总结
1. drag事件序列 与 touch类似，从 ACTION_DRAG_STARTED(ACTION_DOWN) 开始，从 ACTION_DRAG_ENDED(ACTION_UP) 结束
2. drag事件(ACTION_DRAG_LOCATION | ACTION_DROP)当child view响应的时候，parent view是不具有抢夺的，只有当无 child view响应拖拽，自身才可以处理
3. ACTION_DRAG_STARTED 和 ACTION_DRAG_ENDED 事件是可以多个view接收，但 ACTION_DROP 和 ACTION_DRAG_LOCATION 有且只有一个view能响应
4. 基于 findFrontmostDroppableChildAt 倒序特性，当2个child view重合的时候，每次都是最后add的接收 ACTION_DROP 或者 ACTION_DRAG_LOCATION 事件