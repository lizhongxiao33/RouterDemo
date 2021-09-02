package com.lzx.router_api;

import android.app.Activity;
import android.util.LruCache;

//仿butterknife
public class ParameterManager {

    private static ParameterManager instance;
    private LruCache<String, ParameterGet> cache;

    public static ParameterManager getInstance() {
        if (instance == null) {
            synchronized (ParameterManager.class) {
                if (instance == null) {
                    instance = new ParameterManager();
                }
            }
        }
        return instance;
    }

    private ParameterManager() {
        cache = new LruCache<>(100);
    }

    static final String FILE_SUFFIX_NAME = "$$Parameter";

    public void loadParameter(Activity activity) {
        String className = activity.getClass().getName();
        ParameterGet parameterLoad = cache.get(className); // 先从缓存里面拿
        if (null == parameterLoad) {
            try {
                // MainActivity + $$Parameter
                Class<?> aClass = Class.forName(className + FILE_SUFFIX_NAME);
                parameterLoad = (ParameterGet) aClass.newInstance();
                cache.put(className, parameterLoad); // 保存到缓存
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        parameterLoad.getParameter(activity); // 赋值
    }
}
