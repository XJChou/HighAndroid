/**
 * ViewGroup dispatch 解析 [基于 android30 源码]
 *
 * 事件分发流程：
 * dispatchTouchEvent[Activity] ->
 * superDispatchTouchEvent[PhoneWindow] ->
 * superDispatchTouchEvent[DecorView] ->
 * dispatchTouchEvent[ViewGroup] ->
 * dispatchTouchEvent[View]
 *
 * 关键属性说明：
 * 【1】 mFirstTouchTarget 是用来标记当前viewGroup，有多少个子view相应了点击
 * 【2】 TouchTarget 核心属性是 [child, pointerIdBits, next]，其中pointerIdBits 是用来标记那几根手指在当前view上
 * 【3】
 *
 * 重点：
 * 【1】 mGroupFlags & FLAG_DISALLOW_INTERCEPT) != 0
 */
public boolean dispatchTouchEvent(MotionEvent ev) {
    boolean handled = false;

    // 标识是否是安全点击
    if (onFilterTouchEventForSecurity(ev)) {
        final int action = ev.getAction();
        final int actionMasked = action & MotionEvent.ACTION_MASK;

        // 如果是down事件【Finish】
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            // 由于app切换、ANR或者一些状态改变导致数据没有正常置空，所以在down事件再次进行清空

            // 如果 down 事件发生的时候还有 mFirstTouchTarget[当前响应事件的节点] 链条

            // 当前ViewGroup，强调清空 mFirstTouchTarget 链条上的 view
            // [1] 重置 当前ViewGroup  链条下的所有 view.mPrivateFlags 的 PFLAG_CANCEL_NEXT_UP_EVENT{临时忽略当次事件，标记存在则代表事件转换为cancel} 标志位
            // [2] 链条下的所有 view 全部派发cancel事件 [child.dispatchTouchEvent{Cancel}]
            // [3] 清空TouchTarget 下所有节点

            // 递归到子ViewGroup {类似的流程，只是走的代码逻辑不一致}
            // [1] 重置 当前ViewGroup  链条下的所有 view.mPrivateFlags 的 PFLAG_CANCEL_NEXT_UP_EVENT{临时忽略当次事件，标记存在则代表事件转换为cancel} 标志位
            // [2] 链条下的所有 view 全部派发cancel事件 [child.dispatchTouchEvent{Cancel}]
            // [3] 清空TouchTarget 下所有节点
            cancelAndClearTouchTargets(ev);

            // 这里是强调清空自己
            // [1] 清空 当前ViewGroup mFirstTouchTarget 链条
            // [2] 重置 当前ViewGroup 链条下的所有 view.mPrivateFlags 的 PFLAG_CANCEL_NEXT_UP_EVENT{临时忽略当次事件，标记存在则代表事件转换为cancel} 标志位
            // [3] 恢复 当前ViewGroup 拦截子view功能
            // [4] mNestedScrollAxes 置为 SCROLL_AXIS_NONE
            resetTouchState();
        }


        final boolean intercepted;
        // 如果当前是down事件 || 当前触摸链条【有子view响应】不为空
        // 第二个条件判断是因为 父view 是具备任何时间点取抢夺子view的事件序列而不往传给子view的
        if (actionMasked == MotionEvent.ACTION_DOWN || mFirstTouchTarget != null) {
            // disallowIntercept 为 true的情况是，子View 设置了 requestDisallowIntercept
            // requestDisallowIntercept是递归调用到所有父view将不会拦截子view
            final boolean disallowIntercept = (mGroupFlags & FLAG_DISALLOW_INTERCEPT) != 0;
            if (!disallowIntercept) {
                intercepted = onInterceptTouchEvent(ev);
                // restore action in case it was changed
                // 防止onInterceptTouchEvent改变了Action
                ev.setAction(action);
            } else {
                intercepted = false;
            }
        } else {
            // 能进入到此分支是由于当前view拦截过事件
            // 导致 mFirstTouchTarget 被清空，且事件序列不为 down
            // 拦截也是，此view拦截，在不出现任何情况下，都是此view处理后续的事件序列
            intercepted = true;
        }


        // 重置Cancel状态会判断当前是否PFLAG_CANCEL_NEXT_UP_EVENT 或者 事件序列 MotionEvent.CANCEL
        final boolean canceled = resetCancelNextUpFlag(this) || actionMasked == MotionEvent.ACTION_CANCEL;
        // 判断是否是鼠标事件
        final boolean isMouseEvent = ev.getSource() == InputDevice.SOURCE_MOUSE;
        // 如果目标 android version >= 3.0 默认含有多指事件 => 只要是触摸永为true
        final boolean split = (mGroupFlags & FLAG_SPLIT_MOTION_EVENTS) != 0 && !isMouseEvent;
        // 当次 事件序列 TouchTarget [可能会使用原有的，也有可能使用新的]
        TouchTarget newTouchTarget = null;
        // 当次 MotionEvent 是否产生了新的 TouchTarget [新的child view]
        boolean alreadyDispatchedToNewTouchTarget = false;

        // 不是取消事件 且 当前ViewGroup不拦截的时候
        if (!canceled && !intercepted) {

            // 单指的down 或者 多指的pointer_down{对于当前view来说，对子view不一定}，还有一个不是touch事件
            if (actionMasked == MotionEvent.ACTION_DOWN
                    || (split && actionMasked == MotionEvent.ACTION_POINTER_DOWN)
                    || actionMasked == MotionEvent.ACTION_HOVER_MOVE) {
                // 获取当前 pointer 的索引，索引是会变的，转成id是不变的，id能跟手指的对应
                // 对于down事件来说，actionIndex == 0
                final int actionIndex = ev.getActionIndex();
                // 其实就是当前手指的标识[只要手指放屏幕，不抬起，标识就不变]
                final int idBitsToAssign = split ? 1 << ev.getPointerId(actionIndex)
                        : TouchTarget.ALL_POINTER_IDS;

                // 做个异常预防处理，touchTargets含有此手指全移除
                removePointersFromTouchTargets(idBitsToAssign);

                final int childrenCount = mChildrenCount;
                // newTouchTarget永null 且 子view数目不为空的时候
                if (newTouchTarget == null && childrenCount != 0) {
                    // 获取当前手指的坐标
                    final float x = ev.getX(actionIndex);
                    final float y = ev.getY(actionIndex);

                    // 找到一个子view可以接受这事件；从前往后扫描所有子view

                    // buildTouchDispatchChildList【子view需要大于等于1 且 Z方向不为空的时候，才能返回非null】
                    // isChildrenDrawingOrderEnabled()默认ViewGroup为false，所以返回按照Z轴大小来排序【从小到大的顺序】｛使用的插入排序算法｝
                    final ArrayList<View> preorderedList = buildTouchDispatchChildList();

                    // 一般情况下，preorderedList必为null 且 isChildrenDrawingOrderEnabled()也是为false，则custom => false
                    final boolean customOrder = preorderedList == null && isChildrenDrawingOrderEnabled();

                    final View[] children = mChildren;
                    for (int i = childrenCount - 1; i >= 0; i--) {
                            // 如果custom等于false的时候 => int childIndex = i
                            final int childIndex = getAndVerifyPreorderedIndex(childrenCount, i, customOrder);
                            // 就是对应位置的child  => View child = children[childIndex]
                            final View child = getAndVerifyPreorderedView(preorderedList, children, childIndex);

                            // canReceivePointerEvents() 只要控件是VISIBLE状态 || 设置了R.anim的动画 就不会为false
                            // isTransformedTouchPointInView 经过坐标转换后判断是否在指定的子View内 这里使用的是屏幕坐标点一层一层子view减下去
                            if (!child.canReceivePointerEvents() || !isTransformedTouchPointInView(x, y, child, null)) {
                                continue;
                            }

                            // 查找当前是否存在过点击此view
                            newTouchTarget = getTouchTarget(child);
                            // 存在过，则view加上当前的pointerId
                            if (newTouchTarget != null) {
                                newTouchTarget.pointerIdBits |= idBitsToAssign;
                                break;
                            }
                            // 找到一个子view，在点击返回，但是未在此mFirstTouchTarget链上，则尝试分发一下

                            resetCancelNextUpFlag(child);   // 重置当前子view 取消状态，让他接受事件

                            // 对于单点来说,down事件
                            // 对于多点事件来说，相对于child控件来说，pointer_down -> down
                            if (dispatchTransformedTouchEvent(ev, false, child, idBitsToAssign)) {
                                // 能进来代表有子view响应处理

                                mLastTouchDownTime = ev.getDownTime();
                                ...
                                // 从上面可知，preorderedList必为null,所以省略为 mLastTouchDownIndex = childIndex;
                                mLastTouchDownIndex = childIndex;
                                // 因为为down事件可以使用 ev.getX() 和 ev.getY()
                                mLastTouchDownX = ev.getX();
                                mLastTouchDownY = ev.getY();

                                // 头插法增加进mFirstTouchTarget
                                newTouchTarget = addTouchTarget(child, idBitsToAssign);
                                alreadyDispatchedToNewTouchTarget = true;
                                break;
                            }
                    }
                    ...
                }

                // 没有找到可以分发此事件的子view && 之前有FirstTouchTarget
                if (newTouchTarget == null && mFirstTouchTarget != null) {

                    // Did not find a child to receive the event.
                    // Assign the pointer to the least recently added target.

                    // 因为使用的是头插法，最开始响应的view，在链表的最后，默认让 尾节点默认接收【】
                    newTouchTarget = mFirstTouchTarget;
                    while (newTouchTarget.next != null) {
                        newTouchTarget = newTouchTarget.next;
                    }
                    newTouchTarget.pointerIdBits |= idBitsToAssign;
                }
            }

        }


        // 没有子view响应事件，则自己处理
        if (mFirstTouchTarget == null) {
            // 没有子view响应的时候,分发给自己,可以接受所有pointer的event
            // newPointerIdBits == oldPointerIdBits => super.dispatchTouchEvent(event) 直接分发给自己
            handled = dispatchTransformedTouchEvent(ev, canceled, null, TouchTarget.ALL_POINTER_IDS);
        } else {
            // 当有点击链的时候，代表有子view响应过，则遍历当前每个响应的view做处理

            // Dispatch to touch targets, excluding the new touch target if we already
            // dispatched to it.  Cancel touch targets if necessary.
            TouchTarget predecessor = null;
            TouchTarget target = mFirstTouchTarget;

            while (target != null) {
                final TouchTarget next = target.next;

                // 当次已经分发过down事件则不分发了
                if (alreadyDispatchedToNewTouchTarget && target == newTouchTarget) {
                    handled = true;

                } else {

                    // resetCancelNextUpFlag一般情况下都是false
                    // intercepted == true 代表父view要拦截，则取消当前所有的点击子view
                    // intercepted == false 代表没有子view响应当前位置的点击事件
                    final boolean cancelChild = resetCancelNextUpFlag(target.child) || intercepted;

                    // 分发当前链条下的view
                    if (dispatchTransformedTouchEvent(ev, cancelChild, target.child, target.pointerIdBits)) {
                        handled = true;
                    }

                    // 如果是拦截事件，则取消当前所有子view的点击链条并释放
                    if (cancelChild) {
                        if (predecessor == null) {
                            mFirstTouchTarget = next;
                        } else {
                            predecessor.next = next;
                        }
                        target.recycle();
                        target = next;
                        continue;
                    }
                }
                predecessor = target;
                target = next;
            }
        }

        // 主要对 ACTION_UP 或者 ACTION_POINTER_UP 做回收处理
        if (canceled
                || actionMasked == MotionEvent.ACTION_UP
                || actionMasked == MotionEvent.ACTION_HOVER_MOVE) {
            // 清空Target链条
            resetTouchState();
        } else if (split && actionMasked == MotionEvent.ACTION_POINTER_UP) {
            final int actionIndex = ev.getActionIndex();
            final int idBitsToRemove = 1 << ev.getPointerId(actionIndex);
            //  移除指定的pointer[因为这个手指抬起了]
            removePointersFromTouchTargets(idBitsToRemove);
        }
    }
    return handled;
}