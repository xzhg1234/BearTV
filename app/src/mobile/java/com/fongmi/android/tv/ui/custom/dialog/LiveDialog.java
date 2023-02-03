package com.fongmi.android.tv.ui.custom.dialog;

import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.fongmi.android.tv.api.LiveConfig;
import com.fongmi.android.tv.bean.Live;
import com.fongmi.android.tv.databinding.DialogLiveBinding;
import com.fongmi.android.tv.impl.LiveCallback;
import com.fongmi.android.tv.ui.adapter.LiveAdapter;
import com.fongmi.android.tv.ui.custom.SpaceItemDecoration;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class LiveDialog implements LiveAdapter.OnClickListener {

    private final DialogLiveBinding binding;
    private final LiveCallback callback;
    private final AlertDialog dialog;
    private final LiveAdapter adapter;

    public static LiveDialog create(Fragment fragment) {
        return new LiveDialog(fragment);
    }

    public LiveDialog(Fragment fragment) {
        this.callback = (LiveCallback) fragment;
        this.binding = DialogLiveBinding.inflate(LayoutInflater.from(fragment.getContext()));
        this.dialog = new MaterialAlertDialogBuilder(fragment.getActivity()).setView(binding.getRoot()).create();
        this.adapter = new LiveAdapter(this);
    }

    public void show() {
        setRecyclerView();
        setDialog();
    }

    private void setRecyclerView() {
        binding.recycler.setAdapter(adapter);
        binding.recycler.setHasFixedSize(true);
        binding.recycler.addItemDecoration(new SpaceItemDecoration(1, 16));
        binding.recycler.scrollToPosition(LiveConfig.getHomeIndex());
    }

    private void setDialog() {
        if (adapter.getItemCount() == 0) return;
        dialog.getWindow().setDimAmount(0);
        dialog.show();
    }

    @Override
    public void onItemClick(Live item) {
        callback.setLive(item);
        dialog.dismiss();
    }
}
