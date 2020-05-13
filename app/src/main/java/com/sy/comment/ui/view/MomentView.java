package com.sy.comment.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.sy.comment.R;
import com.sy.comment.application.cm.MomentCM;
import com.sy.comment.application.vm.FollowVM;
import com.sy.comment.ui.base.BaseView;
import com.ulfy.android.adapter.RecyclerAdapter;
import com.ulfy.android.mvvm.IViewModel;
import com.ulfy.android.task_transponder.RecyclerViewPageLoader;
import com.ulfy.android.task_transponder_smart.SmartRefresher;
import com.ulfy.android.ui_injection.Layout;
import com.ulfy.android.ui_injection.ViewById;
import com.ulfy.android.utils.RecyclerViewUtils;

@Layout(id = R.layout.view_follow)
public class MomentView extends BaseView {
    @ViewById(id = R.id.followSRL) private SmartRefreshLayout followSRL;
    @ViewById(id = R.id.followRV) private RecyclerView followRV;
    private RecyclerAdapter<MomentCM> followAdapter = new RecyclerAdapter<>();
    private SmartRefresher followRefresher;
    private RecyclerViewPageLoader followLoader;
    private FollowVM vm;

    public MomentView(Context context) {
        super(context);
        init(context, null);
    }

    public MomentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        RecyclerViewUtils.linearLayout(followRV).vertical().dividerDp(Color.parseColor("#ffeff0f7"), 10, 0, 1);
        followRV.setAdapter(followAdapter);
        followRefresher = new SmartRefresher(followSRL, new SmartRefresher.OnRefreshSuccessListener() {
            @Override public void onRefreshSuccess(SmartRefresher smartRefresher) {
                bind(vm);
            }
        });
        followLoader = new RecyclerViewPageLoader(followRV, followAdapter, null);
    }

    @Override public void bind(IViewModel model) {
        vm = (FollowVM) model;
        followRefresher.updateExecuteBody(vm.followTaskInfo, vm.loadContentDataPerPageOnExe());
        followLoader.updateExecuteBody(vm.followTaskInfo, vm.loadContentDataPerPageOnExe());
        followAdapter.setData(vm.followCMList);
        followAdapter.notifyDataSetChanged();
        followLoader.notifyDataSetChanged();
    }
}