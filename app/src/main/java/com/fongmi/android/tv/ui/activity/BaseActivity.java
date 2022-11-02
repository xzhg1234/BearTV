package com.fongmi.android.tv.ui.activity;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.utils.FileUtil;
import com.fongmi.android.tv.utils.Prefers;
import com.fongmi.android.tv.utils.ResUtil;
import com.fongmi.android.tv.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import me.jessyan.autosize.AutoSizeCompat;

public abstract class BaseActivity extends AppCompatActivity {

    protected abstract ViewBinding getBinding();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getBinding().getRoot());
        EventBus.getDefault().register(this);
        Utils.hideSystemUI(this);
        setWall();
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

    private void setWall() {
        File file = FileUtil.getWall(Prefers.getWall());
        if (file.exists() && file.length() > 0) getWindow().setBackgroundDrawable(Drawable.createFromPath(file.getPath()));
        else getWindow().setBackgroundDrawableResource(ResUtil.getDrawable(file.getName()));
    }

    private void hackResources() {
        try {
            AutoSizeCompat.autoConvertDensityOfGlobal(super.getResources());
        } catch (Exception ignored) {
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {
        if (event.getType() == RefreshEvent.Type.WALL) setWall();
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
