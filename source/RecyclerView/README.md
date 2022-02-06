# RecyclerView 源码解析


### RecyclerView 基本组件
* LayoutManager：布局管理器
* Recycler: 复用ViewHolder
* Adapter: 数据转ViewHolder
* ChildHelper: 管理当前RecyclerView 的 Child View


### 简单工作原理图
<img src="./resources/RecyclerView基本工作原理.png" width="80%">


### pre-layout 和 post-layout
RecyclerView 使用 pre-layout 和 post-layout 来获取动画前后状态


### RecyclerView暂存区/缓存区
* Recycler.mChangedScrap: 暂存已绘制的但将要改变的的ViewHolder，主要用于 pre-layout 中布局
* Recycler.mAttachedScrap: 暂存已绘制但非局部刷新标记的ViewHolder，在下次 onLayout 的时候，可以快速复用，无须 onCreateViewHolder 和 onBindViewHolder

  source：
  1. LinearLayoutManager.onLayoutChildren - detachAndScrapAttachedViews(recycler), 当 !viewHolder.isInvalid() || viewHolder.isRemoved() || mRecyclerView.mAdapter.hasStableIds()
  2. recycler.scrapView 根据 holder.isUpdated() 决定, 如果 holder.isUpdated() == true，则进入 mChangedScrap，反之进入 mAttachedScrap

* Recycler.mCacheView: 缓存最近 detach 的 ViewHolder, 可以设置通过 Recycler.setViewCacheSize 设置缓存大小

  source：RecyclerView.LayoutManager.(removeAndRecycleView/removeAndRecycleViewAt)

* Recycler.RecyclerViewPool：按照 ViewType为索引 缓存指定个数的ViewHolder

  source：CacheView超出cacheSize的条目 || holder.isUpdated() == true || holder.isRemoved() == true

use：RecyclerView.Recycler.tryGetViewHolderForPositionByDeadline


### 常见问题
1. 使用 notifyItemChanged() 不带 payloads参数，对应条目发生闪烁原因
   * 是因为ViewHolder 打上了 ViewHolder.FLAG_UPDATE，只会进入 changeScrap 暂存区
   * pre-layout 会从 changeScrap 取出 原ViewHolder A
   * post-layout 会从 attachScrap/cacheView/recyclerViewPool/adapter 中取出ViewHolder B
   * 但此时 ViewHolder A 和 ViewHolder B，不是同一个，则 ViewHolder A 进行淡出，ViewHolder B进行淡入，整体表现为闪烁 

2. notifyItemChanged() 使用 payloads 不闪烁原因

3. 设置 mAdapter.setHasStableIds(true) 并重写 Adapter.getItemId，执行 notifyDatasetChanged(), 为什么不全部刷新
   * 


### RecyclerView 与 ListView 对比
1. 缓存内容从View -> ViewHolder，增加View的重用性，使复用成本降低
2. 将 getView 拆分成 onCreateViewHolder 和 onBindViewHolder, 可以操控更精细化
3. viewType可以不连续，RecyclerView是使用LongSpareArray，而ListView使用了数组
4. 制作动画简易， RecyclerView针对动画做了指定的接口，并使用 pre-layout 和 post-layout 获取动画前后状态
5. 增加了局部刷新，而无需全部更新
6. ListView第一级缓存效率很低，只有在无数据变更情况下触发了onLayout才能使用，而RecyclerView的第一级缓存mCacheView是记录最近退出的view【源码地方】
7. 不同RecyclerView可以使用同一个RecyclerViewPool，减少内存损耗
8. 缓存策略修改，提高击中缓存区的概率，增加缓存的重用性 (mAttachScrap/mChangeScrap) -> mCacheViews -> RecyclerViewPool ->
   Adapter.onCreateViewHolder
9. 可以自由调整 mCacheViews 和 RecyclerView Pool大小，增加在不同场景的性能


### 部分源码解析

#### 缓存区
```java
public final class Recycler {
    final ArrayList<ViewHolder> mAttachedScrap = new ArrayList<>();
    ArrayList<ViewHolder> mChangedScrap = null;

    final ArrayList<ViewHolder> mCachedViews = new ArrayList<ViewHolder>();
    RecycledViewPool mRecyclerPool;

    @NonNull
    public View getViewForPosition(int position) {
        return getViewForPosition(position, false);
    }

    View getViewForPosition(int position, boolean dryRun) {
        return tryGetViewHolderForPositionByDeadline(position, dryRun, FOREVER_NS).itemView;
    }

    ViewHolder tryGetViewHolderForPositionByDeadline(int position, boolean dryRun, long deadlineNs) {
        if (position < 0 || position >= mState.getItemCount()) {
            throw new IndexOutOfBoundsException("Invalid item position " + position
                    + "(" + position + "). Item count:" + mState.getItemCount()
                    + exceptionLabel());
        }
        boolean fromScrapOrHiddenOrCache = false;
        ViewHolder holder = null;
        // 0) If there is a changed scrap, try to find from there
        if (mState.isPreLayout()) {
            holder = getChangedScrapViewForPosition(position);
            fromScrapOrHiddenOrCache = holder != null;
        }
        // 1) Find by position from scrap/hidden list/cache
        if (holder == null) {
            holder = getScrapOrHiddenOrCachedHolderForPosition(position, dryRun);
            if (holder != null) {
                if (!validateViewHolderForOffsetPosition(holder)) {
                    // recycle holder (and unscrap if relevant) since it can't be used
                    if (!dryRun) {
                        // we would like to recycle this but need to make sure it is not used by
                        // animation logic etc.
                        holder.addFlags(ViewHolder.FLAG_INVALID);
                        if (holder.isScrap()) {
                            removeDetachedView(holder.itemView, false);
                            holder.unScrap();
                        } else if (holder.wasReturnedFromScrap()) {
                            holder.clearReturnedFromScrapFlag();
                        }
                        recycleViewHolderInternal(holder);
                    }
                    holder = null;
                } else {
                    fromScrapOrHiddenOrCache = true;
                }
            }
        }
        if (holder == null) {
            final int offsetPosition = mAdapterHelper.findPositionOffset(position);
            if (offsetPosition < 0 || offsetPosition >= mAdapter.getItemCount()) {
                throw new IndexOutOfBoundsException("Inconsistency detected. Invalid item "
                        + "position " + position + "(offset:" + offsetPosition + ")."
                        + "state:" + mState.getItemCount() + exceptionLabel());
            }

            final int type = mAdapter.getItemViewType(offsetPosition);
            // 2) Find from scrap/cache via stable ids, if exists
            if (mAdapter.hasStableIds()) {
                holder = getScrapOrCachedViewForId(mAdapter.getItemId(offsetPosition),
                        type, dryRun);
                if (holder != null) {
                    // update position
                    holder.mPosition = offsetPosition;
                    fromScrapOrHiddenOrCache = true;
                }
            }
            if (holder == null && mViewCacheExtension != null) {
                // We are NOT sending the offsetPosition because LayoutManager does not
                // know it.
                final View view = mViewCacheExtension
                        .getViewForPositionAndType(this, position, type);
                if (view != null) {
                    holder = getChildViewHolder(view);
                    if (holder == null) {
                        throw new IllegalArgumentException("getViewForPositionAndType returned"
                                + " a view which does not have a ViewHolder"
                                + exceptionLabel());
                    } else if (holder.shouldIgnore()) {
                        throw new IllegalArgumentException("getViewForPositionAndType returned"
                                + " a view that is ignored. You must call stopIgnoring before"
                                + " returning this view." + exceptionLabel());
                    }
                }
            }
            if (holder == null) { // fallback to pool
                if (DEBUG) {
                    Log.d(TAG, "tryGetViewHolderForPositionByDeadline("
                            + position + ") fetching from shared pool");
                }
                holder = getRecycledViewPool().getRecycledView(type);
                if (holder != null) {
                    holder.resetInternal();
                    if (FORCE_INVALIDATE_DISPLAY_LIST) {
                        invalidateDisplayListInt(holder);
                    }
                }
            }
            if (holder == null) {
                long start = getNanoTime();
                if (deadlineNs != FOREVER_NS
                        && !mRecyclerPool.willCreateInTime(type, start, deadlineNs)) {
                    // abort - we have a deadline we can't meet
                    return null;
                }
                holder = mAdapter.createViewHolder(RecyclerView.this, type);
                if (ALLOW_THREAD_GAP_WORK) {
                    // only bother finding nested RV if prefetching
                    RecyclerView innerView = findNestedRecyclerView(holder.itemView);
                    if (innerView != null) {
                        holder.mNestedRecyclerView = new WeakReference<>(innerView);
                    }
                }

                long end = getNanoTime();
                mRecyclerPool.factorInCreateTime(type, end - start);
                if (DEBUG) {
                    Log.d(TAG, "tryGetViewHolderForPositionByDeadline created new ViewHolder");
                }
            }
        }

        // This is very ugly but the only place we can grab this information
        // before the View is rebound and returned to the LayoutManager for post layout ops.
        // We don't need this in pre-layout since the VH is not updated by the LM.
        if (fromScrapOrHiddenOrCache && !mState.isPreLayout() && holder
                .hasAnyOfTheFlags(ViewHolder.FLAG_BOUNCED_FROM_HIDDEN_LIST)) {
            holder.setFlags(0, ViewHolder.FLAG_BOUNCED_FROM_HIDDEN_LIST);
            if (mState.mRunSimpleAnimations) {
                int changeFlags = ItemAnimator
                        .buildAdapterChangeFlagsForAnimations(holder);
                changeFlags |= ItemAnimator.FLAG_APPEARED_IN_PRE_LAYOUT;
                final ItemHolderInfo info = mItemAnimator.recordPreLayoutInformation(mState,
                        holder, changeFlags, holder.getUnmodifiedPayloads());
                recordAnimationInfoIfBouncedHiddenView(holder, info);
            }
        }

        boolean bound = false;
        if (mState.isPreLayout() && holder.isBound()) {
            // do not update unless we absolutely have to.
            holder.mPreLayoutPosition = position;
        } else if (!holder.isBound() || holder.needsUpdate() || holder.isInvalid()) {
            if (DEBUG && holder.isRemoved()) {
                throw new IllegalStateException("Removed holder should be bound and it should"
                        + " come here only in pre-layout. Holder: " + holder
                        + exceptionLabel());
            }
            final int offsetPosition = mAdapterHelper.findPositionOffset(position);
            bound = tryBindViewHolderByDeadline(holder, offsetPosition, position, deadlineNs);
        }

        final ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        final LayoutParams rvLayoutParams;
        if (lp == null) {
            rvLayoutParams = (LayoutParams) generateDefaultLayoutParams();
            holder.itemView.setLayoutParams(rvLayoutParams);
        } else if (!checkLayoutParams(lp)) {
            rvLayoutParams = (LayoutParams) generateLayoutParams(lp);
            holder.itemView.setLayoutParams(rvLayoutParams);
        } else {
            rvLayoutParams = (LayoutParams) lp;
        }
        rvLayoutParams.mViewHolder = holder;
        rvLayoutParams.mPendingInvalidate = fromScrapOrHiddenOrCache && bound;
        return holder;
    }

    ViewHolder getChangedScrapViewForPosition(int position) {
        // If pre-layout, check the changed scrap for an exact match.
        final int changedScrapSize;
        if (mChangedScrap == null || (changedScrapSize = mChangedScrap.size()) == 0) {
            return null;
        }
        // find by position
        for (int i = 0; i < changedScrapSize; i++) {
            final ViewHolder holder = mChangedScrap.get(i);
            if (!holder.wasReturnedFromScrap() && holder.getLayoutPosition() == position) {
                holder.addFlags(ViewHolder.FLAG_RETURNED_FROM_SCRAP);
                return holder;
            }
        }
        // find by id
        if (mAdapter.hasStableIds()) {
            final int offsetPosition = mAdapterHelper.findPositionOffset(position);
            if (offsetPosition > 0 && offsetPosition < mAdapter.getItemCount()) {
                final long id = mAdapter.getItemId(offsetPosition);
                for (int i = 0; i < changedScrapSize; i++) {
                    final ViewHolder holder = mChangedScrap.get(i);
                    if (!holder.wasReturnedFromScrap() && holder.getItemId() == id) {
                        holder.addFlags(ViewHolder.FLAG_RETURNED_FROM_SCRAP);
                        return holder;
                    }
                }
            }
        }
        return null;
    }

    ViewHolder getScrapOrHiddenOrCachedHolderForPosition(int position, boolean dryRun) {
        final int scrapCount = mAttachedScrap.size();

        // Try first for an exact, non-invalid match from scrap.
        for (int i = 0; i < scrapCount; i++) {
            final ViewHolder holder = mAttachedScrap.get(i);
            if (!holder.wasReturnedFromScrap() && holder.getLayoutPosition() == position
                    && !holder.isInvalid() && (mState.mInPreLayout || !holder.isRemoved())) {
                holder.addFlags(ViewHolder.FLAG_RETURNED_FROM_SCRAP);
                return holder;
            }
        }

        if (!dryRun) {
            View view = mChildHelper.findHiddenNonRemovedView(position);
            if (view != null) {
                // This View is good to be used. We just need to unhide, detach and move to the
                // scrap list.
                final ViewHolder vh = getChildViewHolderInt(view);
                mChildHelper.unhide(view);
                int layoutIndex = mChildHelper.indexOfChild(view);
                if (layoutIndex == RecyclerView.NO_POSITION) {
                    throw new IllegalStateException("layout index should not be -1 after "
                            + "unhiding a view:" + vh + exceptionLabel());
                }
                mChildHelper.detachViewFromParent(layoutIndex);
                scrapView(view);
                vh.addFlags(ViewHolder.FLAG_RETURNED_FROM_SCRAP
                        | ViewHolder.FLAG_BOUNCED_FROM_HIDDEN_LIST);
                return vh;
            }
        }

        // Search in our first-level recycled view cache.
        final int cacheSize = mCachedViews.size();
        for (int i = 0; i < cacheSize; i++) {
            final ViewHolder holder = mCachedViews.get(i);
            // invalid view holders may be in cache if adapter has stable ids as they can be
            // retrieved via getScrapOrCachedViewForId
            if (!holder.isInvalid() && holder.getLayoutPosition() == position
                    && !holder.isAttachedToTransitionOverlay()) {
                if (!dryRun) {
                    mCachedViews.remove(i);
                }
                if (DEBUG) {
                    Log.d(TAG, "getScrapOrHiddenOrCachedHolderForPosition(" + position
                            + ") found match in cache: " + holder);
                }
                return holder;
            }
        }
        return null;
    }

    ViewHolder getScrapOrCachedViewForId(long id, int type, boolean dryRun) {
        // Look in our attached views first
        final int count = mAttachedScrap.size();
        for (int i = count - 1; i >= 0; i--) {
            final ViewHolder holder = mAttachedScrap.get(i);
            if (holder.getItemId() == id && !holder.wasReturnedFromScrap()) {
                if (type == holder.getItemViewType()) {
                    holder.addFlags(ViewHolder.FLAG_RETURNED_FROM_SCRAP);
                    if (holder.isRemoved()) {
                        // this might be valid in two cases:
                        // > item is removed but we are in pre-layout pass
                        // >> do nothing. return as is. make sure we don't rebind
                        // > item is removed then added to another position and we are in
                        // post layout.
                        // >> remove removed and invalid flags, add update flag to rebind
                        // because item was invisible to us and we don't know what happened in
                        // between.
                        if (!mState.isPreLayout()) {
                            holder.setFlags(ViewHolder.FLAG_UPDATE, ViewHolder.FLAG_UPDATE
                                    | ViewHolder.FLAG_INVALID | ViewHolder.FLAG_REMOVED);
                        }
                    }
                    return holder;
                } else if (!dryRun) {
                    // if we are running animations, it is actually better to keep it in scrap
                    // but this would force layout manager to lay it out which would be bad.
                    // Recycle this scrap. Type mismatch.
                    mAttachedScrap.remove(i);
                    removeDetachedView(holder.itemView, false);
                    quickRecycleScrapView(holder.itemView);
                }
            }
        }

        // Search the first-level cache
        final int cacheSize = mCachedViews.size();
        for (int i = cacheSize - 1; i >= 0; i--) {
            final ViewHolder holder = mCachedViews.get(i);
            if (holder.getItemId() == id && !holder.isAttachedToTransitionOverlay()) {
                if (type == holder.getItemViewType()) {
                    if (!dryRun) {
                        mCachedViews.remove(i);
                    }
                    return holder;
                } else if (!dryRun) {
                    recycleCachedViewAt(i);
                    return null;
                }
            }
        }
        return null;
    }
}

```

#### notifyDataSetChanged
调用栈 Adapter.notifyDataSetChanged() -> RecyclerViewDataObserver.onChanged()
```java
class RecyclerView extends ViewGroup implements ScrollingView, NestedScrollingChild2, NestedScrollingChild3 {

    private class RecyclerViewDataObserver extends AdapterDataObserver {

        public void changed() {
            assertNotInLayoutOrScroll(null);
            mState.mStructureChanged = true;

            processDataSetCompletelyChanged(true);
            if (!mAdapterHelper.hasPendingUpdates()) {
                requestLayout();
            }
        }

    }


    void processDataSetCompletelyChanged(boolean dispatchItemsChanged) {
        mDispatchItemsChangedEvent |= dispatchItemsChanged;
        // 代表数据集在下次Layout的时候全部发生了改变
        mDataSetHasChangedAfterLayout = true;
        // 
        markKnownViewsInvalid();
    }

    void markKnownViewsInvalid() {
        // 获取 RecyclerView 当前的 子View
        final int childCount = mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < childCount; i++) {
            // 获取 子View 上的 ViewHolder
            final ViewHolder holder = getChildViewHolderInt(mChildHelper.getUnfilteredChildAt(i));
            if (holder != null && !holder.shouldIgnore()) {
                // ViewHolder.flags 添加 需要更新 和 无效的 标记
                holder.addFlags(ViewHolder.FLAG_UPDATE | ViewHolder.FLAG_INVALID);
            }
        }
        markItemDecorInsetsDirty();
        // 使 Recycler.mCacheView 中已缓存类型  
        mRecycler.markKnownViewsInvalid();
    }
}
```

1、全部刷新 => notifyDataSetChanged[Adapter] -> onChange[RecyclerViewDataObserver]
    mState.mStructureChanged => true
    processDataSetCompletelyChanged(true)
        [1] mDispatchItemsChangedEvent |= true
        [2] mDataSetHasChangedAfterLayout = true
        [3] markKnownViewsInvalid()
            * 取出RecyclerView中的显示和正在隐藏的viewHolder，添加 ViewHolder.FLAG_UPDATE | ViewHolder.FLAG_INVALID 标志, RecyclerView.LayoutParam.mInsetsDirty = true
            * 通知Recycler.CacheView.LayoutParams.mInsetsDirty = true
            * 通知Recycler,CacheView中的viewholder.flag 增加 FLAG_UPDATE | FLAG_INVALID | FLAG_ADAPTER_FULLUPDATE
            * 如果adapter == null 或者 adapter.hasStableIds == false, CacheViews当前的viewHolder全部迁移到 RecyclerViewPool中[如果没超过上限的话],并重置状态
            * 清除预存索引[暂时不知道干什么]
    !mAdapterHelper.hasPendingUpdates()[mPendingUpdates.size == 0 代表没有局部刷新相关的数据] => requestLayout();

2、局部刷新 => onItemRangeChanged[Adapter] -> onItemRangeChanged[RecyclerViewDataObserver](RangeChanged / RangeInserted / RangeRemoved / RangeMoved)
    mAdapterHelper.onItemRangeInserted(positionStart, itemCount)
        [1] mPendingUpdates.add(obtainUpdateOp(UpdateOp.ADD, positionStart, itemCount, null));
        [2] mExistingUpdateTypes |= UpdateOp.ADD;
        [3] return mPendingUpdates.size() == 1;
    // [上述itemCount >= 1 且 当前除了自己没有其他PendingUpdates()时候执行]
    triggerUpdateProcessor();
        // true && false[一般默认不开启] && true
        POST_UPDATES_ON_ANIMATION && mHasFixedSize && mIsAttached => true
            ViewCompat.postOnAnimation(RecyclerView.this, mUpdateChildViewsRunnable);
        else 
            mAdapterUpdateDuringMeasure = true;
            requestLayout();

RecyclerView
1、测量流程
    onMeasure => mLayout.isAutoMeasureEnabled() -> true
        // 测量RecyclerView自身宽高
        [1] mLayout.onMeasure(mRecycler, mState, widthSpec, heightSpec) -> mRecyclerView.defaultOnMeasure(widthSpec, heightSpec);
        [2] 如果RecyclerView是固定宽高的话，则不用测量

2、布局流程(onLayout)
    [1] dispatchLayout()
        * mState.mIsMeasuring = false; [未发现用处]
        if (mState.mLayoutStep == State.STEP_START) ->  // 开始设置数据肯定是在这的
            dispatchLayoutStep1();
            mLayout.setExactMeasureSpecsFrom(this);
            dispatchLayoutStep2();
        else if (mAdapterHelper.hasUpdates() || mLayout.getWidth() != getWidth() || mLayout.getHeight() != getHeight())
            mLayout.setExactMeasureSpecsFrom(this);
            dispatchLayoutStep2();
        else 
            mLayout.setExactMeasureSpecsFrom(this);
        * dispatchLayoutStep3();
    [2] mFirstLayoutComplete = true;

```java
// 重点
dispatchLayoutStep1()
    processAdapterUpdatesAndSetAnimationFlags()
        mDataSetHasChangedAfterLayout(当调用mAdapter.notifyDatasetChanged()的时候为true)
            * mAdapterHelper.reset();   // 回收当前所有的 UpdateOp【mPendingUpdates && mPostponedList】
            * mDispatchItemsChangedEvent[全局更新为true] -> mLayout.onItemsChanged(this); // LinearLayoutManager空实现
        predictiveItemAnimationsEnabled()[LinearLayoutManager为true]
            true[按理走这] -> mAdapterHelper.preProcess();
                * mOpReorderer.reorderOps(mPendingUpdates); // 将move移到修改最后，并修改对应的position
                * 执行所有的 mPendingUpdates => mPostponedList, 并回收mPendingUpdates
            false ->  mAdapterHelper.consumeUpdatesInOnePass();
                * consumePostponedUpdates() // 消耗并挥手延迟动画，但LinearLayoutManager不支持
                * 待处理动画的执行 和 回收
        boolean animationTypeSupported = mItemsAddedOrRemoved || mItemsChanged; // 在上面的判断会决定，此处的值
        // 运行简单动画 - 已执行过布局 && ItemAnimator不为空 && (全量更新 || 支持动画 || 一般情况下为false) && (非全量更新 || 有固定id)
        mState.mRunSimpleAnimations = mFirstLayoutComplete
                                    && mItemAnimator != null
                                    && (mDataSetHasChangedAfterLayout || animationTypeSupported || mLayout.mRequestedSimpleAnimations)
                                    && (!mDataSetHasChangedAfterLayout || mAdapter.hasStableIds())
        // 运行预测动画 - 上述为true && 支持动画 && 不是全量更新 && 支持预处理动画
        mState.mRunPredictiveAnimations = mState.mRunSimpleAnimations
                                        && animationTypeSupported
                                        && !mDataSetHasChangedAfterLayout
                                        && predictiveItemAnimationsEnabled();
    saveFocusInfo() // 存储焦点[mFocusedItemId、mFocusedItemPosition、mFocusedSubChildId]
    // 能够运行简单动画 && 条目发生变更[局部才会，全量更新是没有pendingUpdate的]
    mState.mTrackOldChangeHolders = mState.mRunSimpleAnimations && mItemsChanged;
    // 重置状态
    mItemsAddedOrRemoved = mItemsChanged = false;
    // 是否支持预测动画
    mState.mInPreLayout = mState.mRunPredictiveAnimations;
    mState.mItemCount = mAdapter.getItemCount();
    // 将当前的viewholder最小和最大位置存入mMinMaxLayoutPositions
    findMinMaxChildLayoutPositions(mMinMaxLayoutPositions);
    mState.mRunSimpleAnimations 
        true -> 
            * 存储view[left, top, right, bottom]
            // ViewHolder(key) -> ItemRecord(value)[preInfo = ItemHolderInfo(left, top, right, bottom); flag |= FLAG_PRE;]
            * mViewInfoStore.addToPreLayout(holder, animationInfo);
    mState.mRunPredictiveAnimations
        true ->
            * saveOldPositions(); // holder.mOldPosition = mPosition
            // pre-layout
            * mLayout.onLayoutChildren(mRecycler, mState);
            * // TODO
mLayout.setExactMeasureSpecsFrom(this); // 设置LayoutManager为RecyclerView宽高
dispatchLayoutStep2()
    mAdapterHelper.consumeUpdatesInOnePass();   // 消费mPostponedList事件
    mState.mItemCount = mAdapter.getItemCount();
    mState.mDeletedInvisibleItemCountSincePreviousLayout = 0;
    // Step 2: Run layout【postLayout】
    mState.mInPreLayout = false;
    mLayout.onLayoutChildren(mRecycler, mState);
    mState.mStructureChanged = false;
    mPendingSavedState = null;
    // onLayoutChildren may have caused client code to disable item animations; re-check
    mState.mRunSimpleAnimations = mState.mRunSimpleAnimations && mItemAnimator != null;
    mState.mLayoutStep = State.STEP_ANIMATIONS;
dispatchLayoutStep3()
    mState.mLayoutStep = State.STEP_START;
    mState.mRunSimpleAnimations
        true ->
            [1] 从后往前遍历当前的绘制的View
                // 当前RecyclerView视图内的子view-holder
                * ViewHolder holder = getChildViewHolderInt(mChildHelper.getChildAt(i));
                // 利用holder获取当前key
                * long key = getChangedHolderKey(holder);
                // 记录当前位置[left、top、right、bottom]
                * final ItemHolderInfo animationInfo = mItemAnimator.recordPostLayoutInformation(mState, holder);
                // 根据 key 查找历史holder
                * ViewHolder oldChangeViewHolder = mViewInfoStore.getFromOldChangeHolders(key);
                // 如果老holder不为空
                * oldChangeViewHolder != null -> 
                    final boolean oldDisappearing = mViewInfoStore.isDisappearing(oldChangeViewHolder);
                    final boolean newDisappearing = mViewInfoStore.isDisappearing(holder);
                    // old需要消失，且当前viewHolder等于当前holder的时候
                    oldDisappearing && oldChangeViewHolder == holder -> 
                        // 执行消失动画
                        mViewInfoStore.addToPostLayout(holder, animationInfo);  // [postInfo = info; flags = FLAG_POST;]
                    else -> // oldDisappearing == false || oldChangeViewHolder != holder => 改变的情况
                        // 获取 preLayout [left、top、right、bottom]
                        final ItemHolderInfo preInfo = mViewInfoStore.popFromPreLayout(oldChangeViewHolder);
                        mViewInfoStore.addToPostLayout(holder, animationInfo);
                        preInfo == null -> 
                            handleMissingPreInfoForChangeError(key, holder, oldChangeViewHolder);
                        else -> // 按理来说有 oldChangeViewHolder 都会存在preInfo【从dispatchLayoutStep1可知】
                            animateChange(oldChangeViewHolder, holder, preInfo, postInfo, oldDisappearing, newDisappearing);
                // 不是老holder改变，添加、删除、移动
                else ->
                    * mViewInfoStore.addToPostLayout(holder, animationInfo);
            // Step 4: Process view info lists and trigger animations
            [2] mViewInfoStore.process(mViewInfoProcessCallback);

    // 回收Scrap[mAttachScrap 和 mChangeScrap], 清理掉动画(???)，Scrap剩余的内容移植到RecyclerViewPool
    mLayout.removeAndRecycleScrapInt(mRecycler);
    mState.mPreviousLayoutItemCount = mState.mItemCount;
    mDataSetHasChangedAfterLayout = false;
    mDispatchItemsChangedEvent = false;
    mState.mRunSimpleAnimations = false;
    mState.mRunPredictiveAnimations = false;
    mLayout.mRequestedSimpleAnimations = false;
    // 清空可变内容
    if (mRecycler.mChangedScrap != null) {
        mRecycler.mChangedScrap.clear();
    }
    // 嵌套RecyclerView才执行
    if (mLayout.mPrefetchMaxObservedInInitialPrefetch) {
        // Initial prefetch has expanded cache, so reset until next prefetch.
        // This prevents initial prefetches from expanding the cache permanently.
        mLayout.mPrefetchMaxCountObserved = 0;
        mLayout.mPrefetchMaxObservedInInitialPrefetch = false;
        mRecycler.updateViewCacheSize();
    }
    mLayout.onLayoutCompleted(mState);
    onExitLayoutOrScroll();
    stopInterceptRequestLayout(false);
    mViewInfoStore.clear();
    // 滚动到刷新前的位置
    if (didChildRangeChange(mMinMaxLayoutPositions[0], mMinMaxLayoutPositions[1])) {
        dispatchOnScrolled(0, 0);
    }
    // 恢复焦点
    recoverFocusFromState();
    // 重置焦点变量
    resetFocusInfo();
 ```