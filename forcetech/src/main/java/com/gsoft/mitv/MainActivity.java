package com.gsoft.mitv;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.anymediacloud.iptv.standard.ForceTV;
import com.forcetech.Port;

public class MainActivity extends Service {

    private ForceTV forceTV;
    private IBinder binder;

    static {
        System.loadLibrary("mitv");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            binder = new LocalBinder();
            loadLibrary(1);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        forceTV = new ForceTV();
        forceTV.start(Port.MTV);
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (forceTV != null) forceTV.stop();
        return super.onUnbind(intent);
    }

    private native void loadLibrary(int type);
}
