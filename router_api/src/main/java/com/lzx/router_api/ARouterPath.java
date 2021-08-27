package com.lzx.router_api;


import com.lzx.router_annotation.bean.RouterBean;

import java.util.Map;


public interface ARouterPath {

    /**
     *  key:"/order/MainActivity"
     */
    Map<String, RouterBean> getPathMap();

}
