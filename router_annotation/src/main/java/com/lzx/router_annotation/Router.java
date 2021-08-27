package com.lzx.router_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Router {

    // 详细路由路径
    String path();

    // 路由组名
    String group() default "";
}
