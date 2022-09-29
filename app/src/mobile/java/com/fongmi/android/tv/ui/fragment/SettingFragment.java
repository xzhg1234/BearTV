package com.fongmi.android.tv.ui.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.BuildConfig;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.ApiConfig;
import com.fongmi.android.tv.bean.Config;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.databinding.FragmentSettingBinding;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.impl.ConfigCallback;
import com.fongmi.android.tv.impl.SiteCallback;
import com.fongmi.android.tv.net.Callback;
import com.fongmi.android.tv.ui.activity.BaseFragment;
import com.fongmi.android.tv.ui.custom.ConfigDialog;
import com.fongmi.android.tv.ui.custom.SiteDialog;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.Prefers;
import com.fongmi.android.tv.utils.ResUtil;
import com.fongmi.android.tv.utils.Updater;

public class SettingFragment extends BaseFragment implements ConfigCallback, SiteCallback {

    private final ActivityResultLauncher<String> launcherString = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> loadConfig());
    private final ActivityResultLauncher<Intent> launcherIntent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> loadConfig());

    private FragmentSettingBinding mBinding;

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return mBinding = FragmentSettingBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        mBinding.url.setText(Prefers.getUrl());
        mBinding.home.setText(ApiConfig.getHomeName());
        mBinding.scaleText.setText(ResUtil.getStringArray(R.array.select_scale)[Prefers.getScale()]);
        mBinding.renderText.setText(ResUtil.getStringArray(R.array.select_render)[Prefers.getRender()]);
        mBinding.qualityText.setText(ResUtil.getStringArray(R.array.select_quality)[Prefers.getQuality()]);
        mBinding.versionText.setText(BuildConfig.VERSION_NAME);
    }

    @Override
    protected void initEvent() {
        mBinding.site.setOnClickListener(view -> SiteDialog.show(this));
        mBinding.config.setOnClickListener(view -> ConfigDialog.show(this));
        //mBinding.history.setOnClickListener(view -> HistoryDialog.show(this));
        mBinding.version.setOnClickListener(view -> Updater.create(getActivity()).force().start());
        mBinding.quality.setOnClickListener(this::setQuality);
        mBinding.render.setOnClickListener(this::setRender);
        mBinding.scale.setOnClickListener(this::setScale);
    }

    @Override
    public void setSite(Site item) {
        mBinding.home.setText(item.getName());
        ApiConfig.get().setHome(item);
        RefreshEvent.video();
    }

    @Override
    public void setConfig(String url) {
        mBinding.url.setText(url);
        Notify.progress(getActivity());
        Prefers.putUrl(url);
        checkUrl(url);
    }

    private void checkUrl(String url) {
        if (url.startsWith("file://") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            openSetting();
        } else if (url.startsWith("file://") && Build.VERSION.SDK_INT < Build.VERSION_CODES.R && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            launcherString.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            loadConfig();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void openSetting() {
        try {
            launcherIntent.launch(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + BuildConfig.APPLICATION_ID)));
        } catch (Exception e) {
            launcherIntent.launch(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
        }
    }

    private void loadConfig() {
        ApiConfig.get().clear().loadConfig(new Callback() {
            @Override
            public void success() {
                Config.save();
                setSite(0);
            }

            @Override
            public void error(int resId) {
                setSite(resId);
            }
        });
    }

    private void setSite(int resId) {
        mBinding.home.setText(ApiConfig.getHomeName());
        RefreshEvent.history();
        RefreshEvent.video();
        Notify.show(resId);
        Notify.dismiss();
    }

    private void setQuality(View view) {
        CharSequence[] array = ResUtil.getStringArray(R.array.select_quality);
        int index = Prefers.getQuality();
        Prefers.putQuality(index = index == array.length - 1 ? 0 : ++index);
        mBinding.qualityText.setText(array[index]);
        RefreshEvent.image();
    }

    private void setRender(View view) {
        CharSequence[] array = ResUtil.getStringArray(R.array.select_render);
        int index = Prefers.getRender();
        Prefers.putRender(index = index == array.length - 1 ? 0 : ++index);
        mBinding.renderText.setText(array[index]);
    }

    private void setScale(View view) {
        CharSequence[] array = ResUtil.getStringArray(R.array.select_scale);
        int index = Prefers.getScale();
        Prefers.putScale(index = index == array.length - 1 ? 0 : ++index);
        mBinding.scaleText.setText(array[index]);
    }
}
