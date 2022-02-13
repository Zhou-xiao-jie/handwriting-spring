package com.atxiaojie.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @MethodsName: Scope
 * @Description 加了这个注解就是原型Bean
 * @Author zhouxiaojie
 * @Date 18:55 2021/12/7
 * @Param
 * @return
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Scope {
    String value();
}
