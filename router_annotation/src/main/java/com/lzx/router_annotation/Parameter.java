package com.lzx.router_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <strong>Activity使用的布局文件注解</strong>
 * <ul>
 * <li>@Target(ElementType.TYPE)   // 接口、类、枚举、注解</li>
 * <li>@Target(ElementType.FIELD) // 属性、枚举的常量</li>
 * <li>@Target(ElementType.METHOD) // 方法</li>
 * <li>@Target(ElementType.PARAMETER) // 方法参数</li>
 * <li>@Target(ElementType.CONSTRUCTOR)  // 构造函数</li>
 * <li>@Target(ElementType.LOCAL_VARIABLE)// 局部变量</li>
 * <li>@Target(ElementType.ANNOTATION_TYPE)// 该注解使用在另一个注解上</li>
 * <li>@Target(ElementType.PACKAGE) // 包</li>
 * <li>@Retention(RetentionPolicy.RUNTIME) <br>注解会在class字节码文件中存在，jvm加载时可以通过反射获取到该注解的内容</li>
 * </ul>
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface Parameter {

    String name() default "";
}
