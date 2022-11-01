package com.fongmi.android.tv.ui.custom;

import android.os.Handler;
import android.view.KeyEvent;

import com.fongmi.android.tv.utils.Utils;

public class CustomKeyDownLive {

    private final Listener listener;
    private final StringBuilder text;
    private final Handler handler;
    private boolean press;

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            listener.onFind(text.toString());
            text.setLength(0);
        }
    };

    public static CustomKeyDownLive create(Listener listener) {
        return new CustomKeyDownLive(listener);
    }

    private CustomKeyDownLive(Listener listener) {
        this.listener = listener;
        this.handler = new Handler();
        this.text = new StringBuilder();
    }

    public void onKeyDown(int keyCode) {
        if (text.length() >= 4) return;
        text.append(getNumber(keyCode));
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 1000);
        listener.onShow(text.toString());
    }

    public boolean onKeyDown(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && Utils.isUpKey(event)) {
            listener.onKeyUp();
        } else if (event.getAction() == KeyEvent.ACTION_DOWN && Utils.isDownKey(event)) {
            listener.onKeyDown();
        } else if (event.getAction() == KeyEvent.ACTION_UP && Utils.isLeftKey(event)) {
            listener.onKeyLeft();
        } else if (event.getAction() == KeyEvent.ACTION_UP && Utils.isRightKey(event)) {
            listener.onKeyRight();
        } else if (event.getAction() == KeyEvent.ACTION_UP && Utils.isDigitKey(event)) {
            onKeyDown(event.getKeyCode());
        } else if (Utils.isEnterKey(event)) {
            checkPress(event);
        }
        return true;
    }

    private void checkPress(KeyEvent event) {
        if (event.isLongPress()) {
            press = true;
            listener.onLongPress();
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            if (press) press = false;
            else listener.onKeyCenter();
        }
    }

    public boolean hasEvent(KeyEvent event) {
        return Utils.isEnterKey(event) || Utils.isUpKey(event) || Utils.isDownKey(event) || Utils.isLeftKey(event) || Utils.isRightKey(event) || Utils.isDigitKey(event) || event.isLongPress();
    }

    private int getNumber(int keyCode) {
        return keyCode >= 144 ? keyCode - 144 : keyCode - 7;
    }

    public interface Listener {

        void onShow(String number);

        void onFind(String number);

        void onKeyUp();

        void onKeyDown();

        void onKeyLeft();

        void onKeyRight();

        void onKeyCenter();

        void onLongPress();
    }
}
