package com.sy.comment.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.sy.comment.application.cm.HotLiteratureCM;
import com.sy.comment.application.cm.LiteratureCM;
import com.ulfy.android.adapter.RecyclerAdapter;
import com.ulfy.android.mvvm.IViewModel;
import com.ulfy.android.task_transponder_smart.SmartRefresher;
import com.ulfy.android.ui_injection.Layout;
import com.ulfy.android.ui_injection.ViewById;
import com.sy.comment.R;
import com.sy.comment.application.vm.LiteratureVM;
import com.sy.comment.ui.base.BaseView;
import com.ulfy.android.utils.RecyclerViewUtils;

@Layout(id = R.layout.view_literature)
public class LiteratureView extends BaseView {
    @ViewById(id = R.id.literarySRL) private SmartRefreshLayout literarySRL;
    @ViewById(id = R.id.hotLiteraryRV) private RecyclerView hotLiteraryRV;
    @ViewById(id = R.id.literaryRV) private RecyclerView literaryRV;
    private RecyclerAdapter<LiteratureCM> literatureAdapter = new RecyclerAdapter<>();
    private RecyclerAdapter<HotLiteratureCM> hotLiteratureAdapter = new RecyclerAdapter<>();
    private SmartRefresher literatureRefresher;
    private LiteratureVM vm;

    public LiteratureView(Context context) {
        super(context);
        init(context, null);
    }

    public LiteratureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        RecyclerViewUtils.gridLayout(hotLiteraryRV).vertical(3).dividerDp(Color.alpha(00), 20, 20, 0, 0);
        RecyclerViewUtils.linearLayout(literaryRV).vertical().dividerDp(Color.alpha(00), 20, 0, 0);
        literaryRV.setNestedScrollingEnabled(false);
        hotLiteraryRV.setNestedScrollingEnabled(false);
        literaryRV.setAdapter(literatureAdapter);
        hotLiteraryRV.setAdapter(hotLiteratureAdapter);
        literatureRefresher = new SmartRefresher(literarySRL, new SmartRefresher.OnRefreshSuccessListener() {
            @Override public void onRefreshSuccess(SmartRefresher smartRefresher) {
                bind(vm);
            }
        });
    }

    @Override public void bind(IViewModel model) {
        vm = (LiteratureVM) model;
        literatureRefresher.updateExecuteBody(null);
        literatureAdapter.setData(vm.literatureCMList);
        hotLiteratureAdapter.setData(vm.hotLiteratureCMList);
        literatureAdapter.notifyDataSetChanged();
        hotLiteratureAdapter.notifyDataSetChanged();
    }
}