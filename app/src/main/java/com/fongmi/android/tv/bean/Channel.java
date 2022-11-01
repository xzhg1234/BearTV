package com.fongmi.android.tv.bean;

import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.fongmi.android.tv.App;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.utils.ResUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Channel {

    @SerializedName("urls")
    private List<String> urls;
    @SerializedName("number")
    private String number;
    @SerializedName("logo")
    private String logo;
    @SerializedName("name")
    private String name;
    @SerializedName("ua")
    private String ua;

    private boolean selected;
    private Group group;
    private String url;
    private int line;

    public static Channel objectFrom(JsonElement element) {
        return new Gson().fromJson(element, Channel.class);
    }

    public static Channel create(int number) {
        return new Channel().setNumber(number);
    }

    public static Channel create(String name) {
        return new Channel(name);
    }

    public Channel() {
    }

    public Channel(String name) {
        this.name = name;
    }

    public List<String> getUrls() {
        return urls = urls == null ? new ArrayList<>() : urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public String getNumber() {
        return TextUtils.isEmpty(number) ? "" : number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getLogo() {
        return TextUtils.isEmpty(logo) ? "" : logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getName() {
        return TextUtils.isEmpty(name) ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUa() {
        return TextUtils.isEmpty(ua) ? "" : ua;
    }

    public void setUa(String ua) {
        this.ua = ua;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setSelected(Channel item) {
        this.selected = item.equals(this);
    }

    public int getVisible() {
        return getLogo().isEmpty() ? View.GONE : View.VISIBLE;
    }

    public void loadLogo(ImageView view) {
        if (!getLogo().isEmpty()) Glide.with(App.get()).load(getLogo()).into(view);
    }

    public void addUrls(String... urls) {
        getUrls().addAll(new ArrayList<>(Arrays.asList(urls)));
    }

    public boolean isLastLine() {
        return getLine() == getUrls().size() - 1;
    }

    public String getLineText() {
        return ResUtil.getString(R.string.live_line, getLine() + 1, getUrls().size());
    }

    public Channel setNumber(int number) {
        setNumber(String.format(Locale.getDefault(), "%03d", number));
        return this;
    }

    public Channel nextLine() {
        setLine(getLine() < getUrls().size() - 1 ? getLine() + 1 : 0);
        return this;
    }

    public Channel prevLine() {
        setLine(getLine() > 0 ? getLine() - 1 : getUrls().size() - 1);
        return this;
    }

    public Channel group(Group group) {
        setGroup(group);
        return this;
    }

    public String getScheme() {
        return Uri.parse(getUrls().get(getLine())).getScheme().toLowerCase();
    }

    public boolean isTVBus() {
        return getScheme().equals("tvbus");
    }

    public boolean isForce() {
        return getScheme().startsWith("p") || getScheme().equals("mitv");
    }

    public Map<String, String> getHeaders() {
        HashMap<String, String> map = new HashMap<>();
        if (getUa().isEmpty()) return map;
        map.put("User-Agent", getUa());
        return map;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Channel)) return false;
        Channel it = (Channel) obj;
        return getName().equals(it.getName()) || (!getNumber().isEmpty() && getNumber().equals(it.getNumber()));
    }
}
