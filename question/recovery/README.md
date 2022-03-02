# 关于系统恢复 ViewPager导致 Fragment 不一致问题

### 问题描述
<div>
    <br/>
    <img width="300px" src='./resources/success.gif'/>
    <img width="300px" src='./resources/error.gif'/>
</div>

从图上可知，右边切换成华为小窗的时候(系统恢复整个节目)，滑动的时候，发现选择按钮消失，而切换前是正常可用

### 问题原因
```java
public abstract class FragmentPagerAdapter extends PagerAdapter {

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        // ...
        
        // 步骤1：当没有重写itemId的时候，默认为当前position
        final long itemId = getItemId(position);
        String name = makeFragmentName(container.getId(), itemId);
        
        // 步骤2：根据当前FragmentName从FragmentManager查找fragment
        Fragment fragment = mFragmentManager.findFragmentByTag(name);
        
        // 步骤3.1：当能找到的时候[系统恢复是可以查找到旧的Fragment]
        if (fragment != null) {
            mCurTransaction.attach(fragment);
        } else {
            // 步骤3.2：当找不到fragment，通过抽象方法创建一个Fragment
            fragment = getItem(position);
            mCurTransaction.add(container.getId(), fragment,
                    makeFragmentName(container.getId(), itemId));
        }
        // ...
        return fragment;
    }
}
```

### 问题解法：
从上述分析过程可知解决方案为 统一Adapter 和 FragmentManager 的 Fragment即可，则根据原代码逻辑有2种方案

1. 重写 Adapter 中的 getItemId，使得每个 fragment对象 有单独itemId，这里可以使用 hasCode()，使 绘制的Fragment 和
   Adapter的Fragment 都是新创建的Fragment
2. 当设置Adapter的Fragment的时候，先从FragmentManager开始查找恢复的Fragment，没找到的时候在进行创建，使 绘制的Fragment 和
   Adapter的Fragment 都是恢复的Fragment
