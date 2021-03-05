package com.ulfy.android.dialog;

public interface IProgressView {
    void setTitle(String title);
    void updatePrograss(int total, int current);
}
