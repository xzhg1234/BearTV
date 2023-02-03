package com.fongmi.android.tv.bean;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.ApiConfig;
import com.fongmi.android.tv.db.AppDatabase;

import java.util.List;

@Entity
public class History {

    @NonNull
    @PrimaryKey
    private String key;
    private String vodPic;
    private String vodName;
    private String vodFlag;
    private String vodRemarks;
    private String episodeUrl;
    private boolean revSort;
    private boolean revPlay;
    private long createTime;
    private long opening;
    private long ending;
    private long position;
    private long duration;
    private float speed;
    private int player;
    private int scale;
    private int cid;

    public History() {
        this.speed = 1;
        this.scale = -1;
        this.player = -1;
    }

    @NonNull
    public String getKey() {
        return key;
    }

    public void setKey(@NonNull String key) {
        this.key = key;
    }

    public String getVodPic() {
        return vodPic;
    }

    public void setVodPic(String vodPic) {
        this.vodPic = vodPic;
    }

    public String getVodName() {
        return vodName;
    }

    public void setVodName(String vodName) {
        this.vodName = vodName;
    }

    public String getVodFlag() {
        return vodFlag;
    }

    public void setVodFlag(String vodFlag) {
        this.vodFlag = vodFlag;
    }

    public String getVodRemarks() {
        return vodRemarks == null ? "" : vodRemarks;
    }

    public void setVodRemarks(String vodRemarks) {
        this.vodRemarks = vodRemarks;
    }

    public String getEpisodeUrl() {
        return episodeUrl == null ? "" : episodeUrl;
    }

    public void setEpisodeUrl(String episodeUrl) {
        this.episodeUrl = episodeUrl;
    }

    public boolean isRevSort() {
        return revSort;
    }

    public void setRevSort(boolean revSort) {
        this.revSort = revSort;
    }

    public boolean isRevPlay() {
        return revPlay;
    }

    public void setRevPlay(boolean revPlay) {
        this.revPlay = revPlay;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getOpening() {
        return opening;
    }

    public void setOpening(long opening) {
        this.opening = opening;
    }

    public long getEnding() {
        return ending;
    }

    public void setEnding(long ending) {
        this.ending = ending;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public int getPlayer() {
        return player;
    }

    public void setPlayer(int player) {
        this.player = player;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getSiteKey() {
        return getKey().substring(0, getKey().lastIndexOf(AppDatabase.SYMBOL));
    }

    public String getVodId() {
        return getKey().substring(getKey().lastIndexOf(AppDatabase.SYMBOL) + AppDatabase.SYMBOL.length());
    }

    public Vod.Flag getFlag() {
        return new Vod.Flag(getVodFlag());
    }

    public Vod.Flag.Episode getEpisode() {
        return new Vod.Flag.Episode(getVodRemarks(), getEpisodeUrl());
    }

    public int getRevPlayText() {
        return isRevPlay() ? R.string.play_backward : R.string.play_forward;
    }

    public int getRevPlayHint() {
        return isRevPlay() ? R.string.play_backward_hint : R.string.play_forward_hint;
    }

    public static List<History> get() {
        return AppDatabase.get().getHistoryDao().find(ApiConfig.getCid());
    }

    public static History find(String key) {
        return AppDatabase.get().getHistoryDao().find(ApiConfig.getCid(), key);
    }

    public static void delete(int cid) {
        AppDatabase.get().getHistoryDao().delete(cid);
    }

    private void checkOpEd(History item) {
        if (getOpening() == 0) setOpening(item.getOpening());
        if (getEnding() == 0) setEnding(item.getEnding());
    }

    private void checkMerge(List<History> items) {
        for (History item : items) {
            if (getKey().equals(item.getKey()) || Math.abs(item.getDuration() - getDuration()) > 10 * 60 * 1000) continue;
            checkOpEd(item);
            item.delete();
        }
    }

    public void update(long position, long duration) {
        setPosition(position);
        setDuration(duration);
        checkMerge(AppDatabase.get().getHistoryDao().findByName(ApiConfig.getCid(), getVodName()));
        AppDatabase.get().getHistoryDao().insertOrUpdate(this);
    }

    public History delete() {
        AppDatabase.get().getHistoryDao().delete(ApiConfig.getCid(), getKey());
        return this;
    }

    public void findEpisode(List<Vod.Flag> flags) {
        setVodFlag(flags.get(0).getFlag());
        setVodRemarks(flags.get(0).getEpisodes().get(0).getName());
        for (History item : AppDatabase.get().getHistoryDao().findByName(ApiConfig.getCid(), getVodName())) {
            if (getPosition() > 0) break;
            for (Vod.Flag flag : flags) {
                Vod.Flag.Episode episode = flag.find(item.getVodRemarks());
                if (episode == null) continue;
                setVodFlag(flag.getFlag());
                setPosition(item.getPosition());
                setVodRemarks(episode.getName());
                checkOpEd(item);
                break;
            }
        }
    }
}
