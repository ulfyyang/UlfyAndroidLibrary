package com.ulfy.android.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.ViewPager;

import com.ulfy.android.mvvm.IView;
import com.ulfy.android.mvvm.IViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ViewPager 适配器：该适配器用于 model 列表，可以动态的托管数据模型与视图的创建
 */
public class PagerAdapter<M extends IViewModel> extends androidx.viewpager.widget.PagerAdapter {
    private List<M> modelList;
    private List<View> viewList = new ArrayList<>();        // 用于缓存游离于页面之外的视图

    public PagerAdapter() { }

    public PagerAdapter(List<M> modelList) {
        setData(modelList);
    }

    public PagerAdapter<M> setData(List<M> modelList) {
        Objects.requireNonNull(modelList, "model list can not be null");
        this.modelList = modelList;
        return this;
    }

    @Override public Object instantiateItem(ViewGroup container, int position) {
        IViewModel model = modelList.get(position);
        View convertView = findAvaliableModelView(model);
        convertView = UiUtils.createView(container.getContext(), convertView, model);
        UiUtils.clearParent(convertView);
        container.addView(convertView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        convertView.setTag(position);
        return convertView;
    }

    /**
     * 从 view 缓存池中找到可以与 model 匹配上的 view
     * @return 如果找到了，则会从缓存池中移除，如果没找到则返回 null
     */
    private View findAvaliableModelView(IViewModel model) {
        for (View view : viewList) {
            if (UiUtils.isViewCanReuse(view, model)) {
                viewList.remove(view);
                return view;
            }
        }
        return null;
    }

    /**
     * 更新当前屏幕中已经存在的项目
     * 1）在没有数据数量变化的情况下单纯调用notifyDataSetChanged不会触发页面更新
     * 2）但是正常的手动切换页面会触发页面更新
     * 3）当数据内容（非数量）变化后可通过该方法将已经存在的项重新刷新
     * @param viewPager 和该Adapter关联的ViewPager
     */
    public void updateScreenItems(ViewPager viewPager) {
        for (int i = 0; i < viewPager.getChildCount(); i++) {
            View view = viewPager.getChildAt(i);
            ((IView)view).bind(modelList.get((Integer) view.getTag()));
        }
    }

    @Override public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        viewList.add((View) object);
    }

    @Override public int getCount() {
        return modelList == null ? 0 : modelList.size();
    }

    @Override public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
