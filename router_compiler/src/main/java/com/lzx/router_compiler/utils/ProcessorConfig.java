package com.lzx.router_compiler.utils;

public interface ProcessorConfig {

    // @Router注解的包名 + 类名
    String ROUTER_PACKAGE =  "com.lzx.router_annotation.Router";

    // 接收参数的TAG标记
    String OPTIONS = "moduleName"; // 每个module名称
    String APT_PACKAGE = "packageNameForAPT"; // 包名（APT 存放的包名）







    // String全类名
    public static final String STRING_PACKAGE = "java.lang.String";

    // Activity全类名
    public static final String ACTIVITY_PACKAGE = "android.app.Activity";

    // Router api 包名
    String ROUTER_API_PACKAGE = "com.lzx.router_api";

    // Router api RouterGroup 全类名
    String ROUTER_API_GROUP = ROUTER_API_PACKAGE + ".RouterGroup";

    // Router api RouterPath 全类名
    String ROUTER_API_PATH = ROUTER_API_PACKAGE + ".RouterPath";

    // 路由组，中的 Path 里面的 方法名
    String PATH_METHOD_NAME = "getPathMap";

    // 路由组，中的 Group 里面的 方法名
    String GROUP_METHOD_NAME = "getGroupMap";

    // 路由组，中的 Path 里面 的 变量名 1
    String PATH_VAR1 = "pathMap";

    // 路由组，中的 Group 里面 的 变量名 1
    String GROUP_VAR1 = "groupMap";

    // 路由组，PATH 最终要生成的 文件名
    String PATH_FILE_NAME = "Router$$Path$$";

    // 路由组，GROUP 最终要生成的 文件名
    String GROUP_FILE_NAME = "Router$$Group$$";





    // @Parameter注解 的 包名 + 类名
    String PARAMETER_PACKAGE = "com.lzx.router_annotation.Parameter";

    // Router api 的 ParameterGet 高层标准
    String ROUTER_AIP_PARAMETER_GET = ROUTER_API_PACKAGE + ".ParameterGet";

    // Router api 的 ParameterGet 方法参数的名字
    String PARAMETER_NAME = "targetParameter";

    // Router api 的 ParmeterGet 方法的名字
    String PARAMETER_METHOD_NAME = "getParameter";

    // Router aip 的 ParmeterGet 的 生成文件名称 $$Parameter
    String PARAMETER_FILE_NAME = "$$Parameter";

    // String全类名
    public static final String STRING = "java.lang.String";
}
