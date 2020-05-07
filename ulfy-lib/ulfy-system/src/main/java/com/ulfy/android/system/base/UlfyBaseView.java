package com.ulfy.android.system.base;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.ulfy.android.bus.BusUtils;
import com.ulfy.android.bus.Subscribe;
import com.ulfy.android.mvvm.IView;
import com.ulfy.android.mvvm.IViewModel;
import com.ulfy.android.system.event.OnNetworkStateChangedEvent;
import com.ulfy.android.ui_injection.InjectUtils;

public abstract class UlfyBaseView extends FrameLayout implements IView {

    /*
	注册一个Activity级别的事件总线和一个全局的事件总线。需要注意的是全局的事件总线尽量不要发送
	局部的事件，因为全局的总线处理的范围是包括局部的处理范围的
	凡是有Activity内部产生的事件都由内部事件总线处理
	 */

    /*
    不应该在该父类中指定一个所有子类都应该实现的初始化模板方法，因为构造方法决定了类成员的初始化
    若指定一个模板方法并在构造方法中调用，则在子类中是通过super调用的。此时子类中的成员并未完成初始化，即使指定了new操作
    子类成员的初始化会在父类super执行完毕之后执行的，因此这时子类覆写的初始化方法调用这些成员将会出现控制真方法
    尽量遵循约定大于语法的方式设计代码
     */

    public UlfyBaseView(Context context) {
        super(context);
        init();
    }

    public UlfyBaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        InjectUtils.processLayoutFile(this);
        InjectUtils.processViewById(this);
        InjectUtils.processViewClick(this);
        // 在初始化时也注册一次，在ViewGroup中的View如果刚开始是不可见的就不会触发
        BusUtils.register(getContext(), this);
        BusUtils.register(this);
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        BusUtils.register(getContext(), this);
        BusUtils.register(this);
    }

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        BusUtils.unregister(getContext(), this);
        BusUtils.unregister(this);
    }

    @Override public void bind(IViewModel model) { }

    /**
     * 当网络重新链接后调用onNetworkConnected回调
     */
    @Subscribe private void OnNetworkStateChangedEvent(OnNetworkStateChangedEvent event) {
        if (event.connected) {
            onNetworkReconnected();
        }
    }

    protected void onNetworkReconnected() {}

}
