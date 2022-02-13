package com.atxiaojie.test.service;

import com.atxiaojie.spring.BeanPostProcessor;
import com.atxiaojie.spring.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @ClassName: MyBeanPostProcessor
 * @Description: TODO
 * @author: zhouxiaojie
 * @date: 2021/12/8 18:46
 * @Version: V1.0.0
 */
@Component("myBeanPostProcessor")
public class MyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        /*if("userService".equals(beanName)){
            ((UserServiceImpl) bean).setName("你好啊！");
        }*/
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("初始化后");
        //这里可以进行AOP逻辑，返回一个代理对象
        if("userService".equals(beanName)){
            Object proxyInstance = Proxy.newProxyInstance(MyBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("执行代理逻辑");//找切点
                    return method.invoke(bean, args);
                }
            });
            return proxyInstance;
        }
        return bean;
    }
}
