package com.lzx.router_api;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import androidx.annotation.RequiresApi;

import com.lzx.router_annotation.bean.RouterBean;

public class RouterManager {

    private String group; // 路由的组名 app，order ...
    private String path;  // 路由的路径  例如：/order/Order_MainActivity

    // 单例模式
    private static RouterManager instance;

    public static RouterManager getInstance() {
        if (instance == null) {
            synchronized (RouterManager.class) {
                if (instance == null) {
                    instance = new RouterManager();
                }
            }
        }
        return instance;
    }

    // 提供性能  LRU缓存
    private LruCache<String, RouterGroup> groupLruCache;
    private LruCache<String, RouterPath> pathLruCache;

    private final static String FILE_GROUP_NAME = "Router$$Group$$";

    private RouterManager() {
        groupLruCache = new LruCache<>(100);
        pathLruCache = new LruCache<>(100);
    }

    /***
     * @param path 例如：/order/Order_MainActivity
     *      * @return
     */
    public BundleManager build(String path) {
        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new IllegalArgumentException("path error: path is empty or not start with /");
        }


        if (path.lastIndexOf("/") == 0) {
            throw new IllegalArgumentException("path error");
        }

        // 截取组名  如：/order/MainActivity  finalGroup=order
        String finalGroup = path.substring(1, path.indexOf("/", 1));

        if (TextUtils.isEmpty(finalGroup)) {
            throw new IllegalArgumentException("path error");
        }

        // ...

        this.path =  path;  //order/MainActivity
        this.group = finalGroup; //order

        return new BundleManager();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public Object navigation(Context context, BundleManager bundleManager) {
        String groupClassName = context.getPackageName() + "." + FILE_GROUP_NAME + group; //Router$$Group$$order
        Log.i("test", "groupClassName=" + groupClassName);


        try {
            //读取路由组Group类文件
            RouterGroup loadGroup = groupLruCache.get(group);
            if (null == loadGroup) {
                Class<?> aClass = Class.forName(groupClassName);
                // 初始化类文件
                loadGroup = (RouterGroup) aClass.newInstance();
                // 保存到缓存
                groupLruCache.put(group, loadGroup);
            }

            if (loadGroup.getGroupMap().isEmpty()) {
                throw new RuntimeException("group map is null");
            }

            //读取路由Path类文件
            RouterPath loadPath = pathLruCache.get(path);
            if (null == loadPath) {
                Class<? extends RouterPath> clazz = loadGroup.getGroupMap().get(group);

                // 3.从map里面获取 Router$$Path$$order.class
                loadPath = clazz.newInstance();

                // 保存到缓存
                pathLruCache.put(path, loadPath);
            }

            if (loadPath != null) {
                if (loadPath.getPathMap().isEmpty()) {
                    throw new RuntimeException("path map is null");
                }

                // 最后跳转
                RouterBean routerBean = loadPath.getPathMap().get(path);

                if (routerBean != null) {
                    switch (routerBean.getTypeEnum()) {
                        case ACTIVITY:
                            Log.i("test", "navigation: "+routerBean.getClazz());
                            Intent intent = new Intent(context, routerBean.getClazz());
                            intent.putExtras(bundleManager.getBundle());
                            context.startActivity(intent, bundleManager.getBundle());
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
