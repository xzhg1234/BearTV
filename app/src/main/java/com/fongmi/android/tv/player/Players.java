package com.fongmi.android.tv.player;

import androidx.annotation.NonNull;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.Channel;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.event.PlayerEvent;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.ResUtil;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.util.Util;

import java.util.Formatter;
import java.util.Locale;
import java.util.Map;

public class Players implements Player.Listener, AnalyticsListener, ParseTask.Callback {

    private StringBuilder builder;
    private Formatter formatter;
    private ExoPlayer exoPlayer;
    private ParseTask parseTask;
    private int errorCode;
    private int retry;

    public Players init() {
        builder = new StringBuilder();
        formatter = new Formatter(builder, Locale.getDefault());
        setupPlayer();
        return this;
    }

    private void setupPlayer() {
        DefaultTrackSelector selector = new DefaultTrackSelector(App.get());
        selector.setParameters(selector.getParameters().buildUpon().setPreferredTextLanguage("zh").build());
        DefaultRenderersFactory factory = new DefaultRenderersFactory(App.get()).setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON);
        exoPlayer = new ExoPlayer.Builder(App.get()).setLoadControl(new DefaultLoadControl()).setRenderersFactory(factory).setTrackSelector(selector).build();
        exoPlayer.setAudioAttributes(AudioAttributes.DEFAULT, true);
        exoPlayer.addAnalyticsListener(this);
        exoPlayer.setPlayWhenReady(true);
        exoPlayer.addListener(this);
    }

    public ExoPlayer exo() {
        return exoPlayer;
    }

    private void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public int addRetry() {
        ++retry;
        return retry;
    }

    public String getSpeed() {
        return String.format(Locale.getDefault(), "%.2f", exoPlayer.getPlaybackParameters().speed);
    }

    public void addSpeed() {
        float speed = exoPlayer.getPlaybackParameters().speed;
        float addon = speed >= 2 ? 1f : 0.25f;
        speed = speed == 5 ? 0.25f : speed + addon;
        exoPlayer.setPlaybackSpeed(speed);
    }

    public void resetSpeed() {
        exoPlayer.setPlaybackSpeed(1f);
    }

    public String getTime(long time) {
        time = getCurrentPosition() + time;
        if (time > getDuration()) time = getDuration();
        else if (time < 0) time = 0;
        return getStringForTime(time);
    }

    public String getStringForTime(long time) {
        return Util.getStringForTime(builder, formatter, time);
    }

    public long getCurrentPosition() {
        return exoPlayer == null ? 0 : exoPlayer.getCurrentPosition();
    }

    public long getDuration() {
        return exoPlayer == null ? 0 : exoPlayer.getDuration();
    }

    public void seekTo(int time) {
        exoPlayer.seekTo(getCurrentPosition() + time);
    }

    public void seekTo(long time) {
        exoPlayer.seekTo(time);
    }

    public boolean isPlaying() {
        return exoPlayer != null && exoPlayer.isPlaying();
    }

    public boolean isIdle() {
        return exoPlayer != null && exoPlayer.getPlaybackState() == Player.STATE_IDLE;
    }

    public boolean canNext() {
        return getCurrentPosition() >= getDuration();
    }

    public void play() {
        exoPlayer.play();
    }

    public void pause() {
        exoPlayer.pause();
    }

    public void stop() {
        setRetry(0);
        exoPlayer.stop();
        exoPlayer.clearMediaItems();
    }

    public void release() {
        stopParse();
        exoPlayer.stop();
        exoPlayer.clearMediaItems();
        exoPlayer.removeListener(this);
        exoPlayer.release();
        exoPlayer = null;
    }

    public void start(Channel channel) {
        setMediaSource(channel.getHeaders(), channel.getUrl());
    }

    public void start(Result result, boolean useParse) {
        if (result.getUrl().isEmpty()) {
            PlayerEvent.error(R.string.error_play_load);
        } else if (result.getParse(1) == 1 || result.getJx() == 1) {
            stopParse();
            parseTask = ParseTask.create(this).run(result, useParse);
        } else {
            setMediaSource(result);
        }
    }

    private void stopParse() {
        if (parseTask != null) parseTask.cancel();
    }

    private void setMediaSource(Result result) {
        exoPlayer.setMediaSource(ExoUtil.getSource(result, errorCode));
        PlayerEvent.state(0);
        exoPlayer.prepare();
        setErrorCode(0);
    }

    private void setMediaSource(Map<String, String> headers, String url) {
        exoPlayer.setMediaSource(ExoUtil.getSource(headers, url, errorCode));
        PlayerEvent.state(0);
        exoPlayer.prepare();
        setErrorCode(0);
    }

    @Override
    public void onParseSuccess(Map<String, String> headers, String url, String from) {
        if (from.length() > 0) Notify.show(ResUtil.getString(R.string.parse_from, from));
        setMediaSource(headers, url);
    }

    @Override
    public void onParseError() {
        PlayerEvent.error(R.string.error_play_parse);
    }

    @Override
    public void onPlayerError(@NonNull PlaybackException error) {
        setErrorCode(error.errorCode);
        PlayerEvent.error(R.string.error_play_format, true);
    }

    @Override
    public void onPlaybackStateChanged(int state) {
        PlayerEvent.state(state);
    }

    @Override
    public void onAudioSinkError(@NonNull EventTime eventTime, @NonNull Exception audioSinkError) {
        seekTo(500);
    }
}
