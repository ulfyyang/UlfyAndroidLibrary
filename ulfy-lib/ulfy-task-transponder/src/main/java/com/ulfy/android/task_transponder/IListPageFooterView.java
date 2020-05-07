package com.ulfy.android.task_transponder;

public interface IListPageFooterView extends IReloadView {
    public void gone();
    public void showLoading();
    public void showNoData();
    public void showError(Object message);
}
