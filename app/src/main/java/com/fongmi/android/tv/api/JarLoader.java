package com.fongmi.android.tv.api;

import android.content.Context;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.utils.FileUtil;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderNull;

import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dalvik.system.DexClassLoader;

public class JarLoader {

    private final ConcurrentHashMap<String, Spider> spiders;
    private DexClassLoader classLoader;
    private Method proxyFun;

    public JarLoader() {
        this.spiders = new ConcurrentHashMap<>();
    }

    public void load(File file) {
        spiders.clear();
        proxyFun = null;
        classLoader = new DexClassLoader(file.getAbsolutePath(), FileUtil.getCachePath(), null, App.get().getClassLoader());
        try {
            Class<?> classInit = classLoader.loadClass("com.github.catvod.spider.Init");
            Method method = classInit.getMethod("init", Context.class);
            method.invoke(classInit, App.get());
            Class<?> classProxy = classLoader.loadClass("com.github.catvod.spider.Proxy");
            proxyFun = classProxy.getMethod("proxy", Map.class);
        } catch (Exception ignored) {
        }
    }

    public Spider getSpider(String key, String api, String ext) {
        try {
            api = api.replace("csp_", "");
            if (spiders.containsKey(key)) return spiders.get(key);
            if (classLoader == null) return new SpiderNull();
            Spider spider = (Spider) classLoader.loadClass("com.github.catvod.spider." + api).newInstance();
            spider.init(App.get(), ext);
            spiders.put(key, spider);
            return spider;
        } catch (Exception e) {
            e.printStackTrace();
            return new SpiderNull();
        }
    }

    public JSONObject jsonExt(String key, LinkedHashMap<String, String> jxs, String url) {
        try {
            String clsKey = "Json" + key;
            String hotClass = "com.github.catvod.parser." + clsKey;
            Class<?> jsonParserCls = classLoader.loadClass(hotClass);
            Method mth = jsonParserCls.getMethod("parse", LinkedHashMap.class, String.class);
            return (JSONObject) mth.invoke(null, jxs, url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONObject jsonExtMix(String flag, String key, String name, LinkedHashMap<String, HashMap<String, String>> jxs, String url) {
        try {
            String clsKey = "Mix" + key;
            String hotClass = "com.github.catvod.parser." + clsKey;
            Class<?> jsonParserCls = classLoader.loadClass(hotClass);
            Method mth = jsonParserCls.getMethod("parse", LinkedHashMap.class, String.class, String.class, String.class);
            return (JSONObject) mth.invoke(null, jxs, name, flag, url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object[] proxyInvoke(Map params) {
        try {
            if (proxyFun != null) return (Object[]) proxyFun.invoke(null, params);
            else return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
