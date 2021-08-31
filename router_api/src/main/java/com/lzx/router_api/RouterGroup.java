package com.lzx.router_api;

import java.util.Map;


public interface RouterGroup {


    Map<String, Class<? extends RouterPath>> getGroupMap();

}
