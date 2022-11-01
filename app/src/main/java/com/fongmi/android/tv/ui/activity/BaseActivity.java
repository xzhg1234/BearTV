package com.fongmi.android.tv.ui.activity;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.utils.Utils;

import me.jessyan.autosize.AutoSizeCompat;

public abstract class BaseActivity extends AppCompatActivity {

    protected abstract ViewBinding getBinding();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getBinding().getRoot());
        Utils.hideSystemUI(this);
        initView();
        initEvent();
    }

    protected Activity getActivity() {
        return this;
    }

    protected void initView() {
    }

    protected void initEvent() {
    }

    protected void hackResources() {
        try {
            AutoSizeCompat.autoConvertDensityOfGlobal(super.getResources());
        } catch (Exception ignored) {
        }
    }

    @Override
    public Resources getResources() {
        hackResources();
        return super.getResources();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Utils.hideSystemUI(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) Utils.hideSystemUI(this);
    }
}
