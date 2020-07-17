package com.ulfy.android.adapter;

import android.view.View;
import android.view.ViewGroup;

import com.ulfy.android.mvvm.IViewModel;

import java.util.AbstractList;
import java.util.List;

/**
 * 分组适配器，目前只支持一级分组
 */
public class RecyclerGroupAdapter<G extends IViewModel, C extends IViewModel> extends RecyclerAdapter<IViewModel> {
    private List<G> groupModelList;
    private OnGroupItemClickListener<G, C> groupItemClickListener;

    public RecyclerGroupAdapter() {
        super();
    }

    public RecyclerGroupAdapter(List<G> groupModelList) {
        super();
        setData(groupModelList);
    }

    @Override public RecyclerGroupAdapter setData(List groupModelList) {
        if (groupModelList == null) {
            throw new NullPointerException("group mode list cannot be null");
        }
        this.groupModelList = groupModelList;
        super.setData(new ListWrapper());
        return this;
    }

    public RecyclerGroupAdapter setOnGroupItemClickListener(OnGroupItemClickListener<G, C> onGroupItemClickListener) {
        this.groupItemClickListener = onGroupItemClickListener;
        registerItemClickListenerIfNeed();
        return this;
    }

    @Override protected boolean shouldRegisterItemClickListener() {
        return groupItemClickListener != null || super.shouldRegisterItemClickListener();
    }

    @Override protected void dispatchOnItemClick(ViewGroup parent, View view, int position, IViewModel model) {
        if (groupItemClickListener == null || groupModelList == null) {
            return;
        }
        for (int i = 0; i < groupModelList.size(); i++) {
            G groupModel = groupModelList.get(i);
            if (model == groupModel) {              // 表示点击的是分组
                groupItemClickListener.onGroupItemClick(parent, view, position, i, (G) model);
                break;
            } else {
                List<IViewModel> childModelList = groupModel.getChildViewModelList();
                if (childModelList == null) {
                    continue;
                }
                int index = childModelList.indexOf(model);
                if (index >= 0) {                   // 表示点击的是具体的子项
                    groupItemClickListener.onChildItemClick(parent, view, position, i, index, (C) model);
                    break;
                }
            }
        }
    }

    @Override protected boolean shouldFullSpanForGridLayout(int position) {
        return isGroupForPosition(getHeaderView() == null ? position : position - 1) || super.shouldFullSpanForGridLayout(position);
    }

    // 并未对 StaggeredGridLayoutManager 做单行处理支持

    /**
     * 指定的位置是否是一个分组的位置
     */
    public boolean isGroupForPosition(int position) {
        if (groupModelList == null) {
            return false;
        } else {
            int groupStartIndex = 0;
            for (G groupModel : groupModelList) {
                if (position == groupStartIndex) {
                    return true;
                } else {
                    List<IViewModel> childModelList = groupModel.getChildViewModelList();
                    groupStartIndex += 1 + (childModelList == null ? 0 : childModelList.size());
                }
            }
        }
        return false;
    }

    /**
     * 根据指定的位置和每行的数量来判断当前位置是否是所在组的最后一行
     */
    public boolean isLastRowInItsGroup(int position, int spanCount) {
        if (groupModelList == null) {
            return false;
        }
        int groupStartIndex = 0, groupSize = 0;
        for (G groupModel : groupModelList) {
            if (position == groupStartIndex) {
                List<IViewModel> childModelList = groupModel.getChildViewModelList();
                return childModelList == null || childModelList.size() == 0;
            } else {
                List<IViewModel> childModelList = groupModel.getChildViewModelList();
                groupSize = 1 + (childModelList == null ? 0 : childModelList.size());
                if (position < groupStartIndex + groupSize) {
                    int childCount = childModelList.size();
                    int totalRow = childCount / spanCount; if (childCount % spanCount > 0) totalRow ++;
                    int childPosition = position - groupStartIndex - 1 + 1;     // +1 是为了算出从 1 开始计数的位置
                    int positionRow = childPosition / spanCount; if (childPosition % spanCount > 0) positionRow ++;
                    return positionRow == totalRow;
                }
                groupStartIndex += groupSize;
            }
        }
        return false;
    }

    /**
     * 根据指定的位置和每行的数量来判断当前位置是否是所有组的最后一行
     */
    public boolean isLastRowInAllGroup(int position, int spanCount) {
        if (groupModelList == null) {
            return false;
        }
        int groupStartIndex = 0, groupSize = 0;
        for (int i = 0; i < groupModelList.size(); i++) {
            G groupModel = groupModelList.get(i);
            if (position == groupStartIndex) {
                return i == groupModelList.size() - 1;
            } else {
                List<IViewModel> childModelList = groupModel.getChildViewModelList();
                groupSize = 1 + (childModelList == null ? 0 : childModelList.size());
                if (position < groupStartIndex + groupSize) {
                    int childCount = childModelList.size();
                    int totalRow = childCount / spanCount; if (childCount % spanCount > 0) totalRow ++;
                    int childPosition = position - groupStartIndex - 1 + 1;     // +1 是为了算出从 1 开始计数的位置
                    int positionRow = childPosition / spanCount; if (childPosition % spanCount > 0) positionRow ++;
                    if (positionRow == totalRow) {
                        return i == groupModelList.size() - 1;
                    }
                }
                groupStartIndex += groupSize;
            }
        }
        return false;
    }

    /**
     * 根据指定的每行数量来计算出在逻辑上最前一组的最前一行有几个子元素
     */
    public int getFirstRowCountInAllGroup(int spanCount) {
        if (groupModelList == null || groupModelList.size() == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * 根据指定的每行数量来计算出在逻辑上最后一组的最后一行有几个子元素
     */
    public int getLastRowCountInAllGroup(int spanCount) {
        if (groupModelList == null || groupModelList.size() == 0) {
            return 0;
        }
        G groupModel = groupModelList.get(groupModelList.size() - 1);
        List<IViewModel> childModelList = groupModel.getChildViewModelList();
        if (childModelList == null || childModelList.size() == 0) {
            return 1;
        } else {
            int rowCount = childModelList.size() % spanCount;
            if (rowCount == 0) {
                rowCount = spanCount;
            }
            return rowCount;
        }
    }

    /**
     * 根据指定的位置和每行的数量来判断当前位置所在行的位置，从 1 开始计数
     */
    public int findColumnIndexForPosition(int position, int spanCount) {
        if (groupModelList == null) {
            return 0;
        }
        int groupStartIndex = 0, groupSize = 0;
        for (G groupModel : groupModelList) {
            if (position == groupStartIndex) {
                return 1;
            } else {
                List<IViewModel> childModelList = groupModel.getChildViewModelList();
                groupSize = 1 + (childModelList == null ? 0 : childModelList.size());
                if (position < groupStartIndex + groupSize) {
                    int column = (position - groupStartIndex - 1 + 1) % spanCount;    // -1 是要排除掉组占的位置，+1 是为了从 1 开始计数
                    if (column == 0) {          // 如果是最后一项，则这时候取余会等于零，这种情况应该矫正为最后一项
                        column = spanCount;
                    }
                    return column;
                }
                groupStartIndex += groupSize;
            }
        }
        return 0;
    }

    /**
     * 设置单击事件
     */
    public interface OnGroupItemClickListener<G, C> {
        void onGroupItemClick(ViewGroup parent, View view, int position, int groupPosition, G model);
        void onChildItemClick(ViewGroup parent, View view, int position, int groupPosition, int childPosition, C model);
    }

    /**
     * 将分组的数据代理成具有线程排列不分组的特性，目前适配器中只用到了 get、size 方法，因此只实现这个即可。
     */
    private class ListWrapper extends AbstractList<IViewModel> {

        @Override public IViewModel get(int index) {
            if (groupModelList == null) {
                return null;
            }
            int groupStartIndex = 0, groupSize = 0;
            for (G groupModel : groupModelList) {
                if (index == groupStartIndex) {     // 如果寻找的位置正好是分组的位置，则直接返回分组
                    return groupModel;
                } else {                            // 否则找到 index 所处的分组，然后在定位到其中的元素
                    List<IViewModel> childModelList = groupModel.getChildViewModelList();
                    groupSize = 1 + (childModelList == null ? 0 : childModelList.size());
                    if (index < groupStartIndex + groupSize) {
                        return childModelList.get(index - groupStartIndex - 1);
                    }
                    groupStartIndex += groupSize;
                }
            }
            return null;
        }

        @Override public int size() {
            if (groupModelList == null) {
                return 0;
            }
            int count = 0;
            for (G groupModel : groupModelList) {
                List<IViewModel> childModelList = groupModel.getChildViewModelList();
                count ++; count += childModelList == null ? 0 : childModelList.size();
            }
            return count;
        }
    }
}
