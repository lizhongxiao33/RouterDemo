package com.lzx.router_annotation.bean;

import javax.lang.model.element.Element;


public class RouterBean {

    public enum TypeEnum {
        ACTIVITY
    }

    private TypeEnum typeEnum;
    private Element element; // JavaPoet
    private Class<?> clazz; // 被注解的 Class
    private String path; // 路由地址  例如：/app/MainActivity
    private String group; // 路由组  例如：app  order

    public TypeEnum getTypeEnum() {
        return typeEnum;
    }

    public void setTypeEnum(TypeEnum typeEnum) {
        this.typeEnum = typeEnum;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    private RouterBean(TypeEnum typeEnum, /*Element element,*/ Class<?> clazz, String path, String group) {
        this.typeEnum = typeEnum;
        // this.element = element;
        this.clazz = clazz;
        this.path = path;
        this.group = group;
    }

    public static RouterBean create(TypeEnum type, Class<?> clazz, String path, String group) {
        return new RouterBean(type, clazz, path, group);
    }

    private RouterBean(Builder builder) {
        this.typeEnum = builder.type;
        this.element = builder.element;
        this.clazz = builder.clazz;
        this.path = builder.path;
        this.group = builder.group;
    }

    /**
     * 构建者模式
     */
    public static class Builder {

        // 枚举类型：Activity
        private TypeEnum type;
        // 类节点
        private Element element;
        // 注解使用的类对象
        private Class<?> clazz;
        // 路由地址
        private String path;
        // 路由组
        private String group;

        public Builder addType(TypeEnum type) {
            this.type = type;
            return this;
        }

        public Builder addElement(Element element) {
            this.element = element;
            return this;
        }

        public Builder addClazz(Class<?> clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder addPath(String path) {
            this.path = path;
            return this;
        }

        public Builder addGroup(String group) {
            this.group = group;
            return this;
        }

        // 最后的build或者create，往往是做参数的校验或者初始化赋值工作
        public RouterBean build() {
            if (path == null || path.length() == 0) {
                throw new IllegalArgumentException("path is null，simple like：/app/MainActivity");
            }
            return new RouterBean(this);
        }
    }

    @Override
    public String toString() {
        return "RouterBean{" +
                "path='" + path + '\'' +
                ", group='" + group + '\'' +
                '}';
    }
}
