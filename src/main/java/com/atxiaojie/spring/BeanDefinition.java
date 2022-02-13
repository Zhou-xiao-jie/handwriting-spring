package com.atxiaojie.spring;

/**
 * @ClassName: BeanDefinition
 * @Description: Bean的定义
 * @author: zhouxiaojie
 * @date: 2021/12/7 19:05
 * @Version: V1.0.0
 */
public class BeanDefinition {

    private Class clazz;
    private String scope;

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
