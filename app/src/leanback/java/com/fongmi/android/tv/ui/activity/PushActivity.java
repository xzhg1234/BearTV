package com.fongmi.android.tv.ui.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.view.View;

import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.ActivityPushBinding;
import com.fongmi.android.tv.server.Server;
import com.fongmi.android.tv.utils.QRCode;
import com.fongmi.android.tv.utils.ResUtil;

public class PushActivity extends BaseActivity {

    private ActivityPushBinding mBinding;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, PushActivity.class));
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityPushBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        String address = Server.get().getAddress(false);
        mBinding.code.setImageBitmap(QRCode.getBitmap(address, 250, 1));
        mBinding.info.setText(ResUtil.getString(R.string.push_info, address));
        mBinding.clip.setOnClickListener(this::onClip);
    }

    private void onClip(View view) {
        ClipData data = ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).getPrimaryClip();
        if (data.getItemCount() > 0) DetailActivity.start(PushActivity.this, "push_agent", data.getItemAt(0).getText().toString());
    }
}
