package com.sy.comment.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.sy.comment.R;
import com.sy.comment.application.cm.MomentCM;
import com.sy.comment.application.vm.RecommendVM;
import com.sy.comment.ui.base.BaseView;
import com.ulfy.android.adapter.RecyclerAdapter;
import com.ulfy.android.mvvm.IViewModel;
import com.ulfy.android.task_transponder.RecyclerViewPageLoader;
import com.ulfy.android.task_transponder_smart.SmartRefresher;
import com.ulfy.android.ui_injection.Layout;
import com.ulfy.android.ui_injection.ViewById;
import com.ulfy.android.utils.RecyclerViewUtils;

@Layout(id = R.layout.view_recommend)
public class RecommendView extends BaseView {
    @ViewById(id = R.id.recommendSRL) private SmartRefreshLayout recommendSRL;
    @ViewById(id = R.id.recommendRV) private RecyclerView recommendRV;
    private RecyclerAdapter<MomentCM> recommendAdapter = new RecyclerAdapter<>();
    private SmartRefresher recommendRefresher;
    private RecyclerViewPageLoader recommendLoader;
    private RecommendVM vm;

    public RecommendView(Context context) {
        super(context);
        init(context, null);
    }

    public RecommendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        RecyclerViewUtils.linearLayout(recommendRV).vertical().dividerDp(Color.parseColor("#ffeff0f7"), 10, 0, 1);;
        recommendRV.setAdapter(recommendAdapter);
        recommendRefresher = new SmartRefresher(recommendSRL, new SmartRefresher.OnRefreshSuccessListener() {
            @Override public void onRefreshSuccess(SmartRefresher smartRefresher) {
                bind(vm);
            }
        });
        recommendLoader = new RecyclerViewPageLoader(recommendRV, recommendAdapter, null);
    }

    @Override public void bind(IViewModel model) {
        vm = (RecommendVM) model;
        recommendRefresher.updateExecuteBody(vm.recommendTaskInfo, vm.loadContentDataPerPageOnExe());
        recommendLoader.updateExecuteBody(vm.recommendTaskInfo, vm.loadContentDataPerPageOnExe());
        recommendAdapter.setData(vm.recommendCMList);
        recommendAdapter.notifyDataSetChanged();
        recommendLoader.notifyDataSetChanged();
    }
}