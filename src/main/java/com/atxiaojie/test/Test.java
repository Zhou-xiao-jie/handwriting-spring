package com.atxiaojie.test;

import com.atxiaojie.spring.CustomApplicationContext;
import com.atxiaojie.test.service.UserService;
/**
 * @ClassName: Test
 * @Description: TODO
 * @author: zhouxiaojie
 * @date: 2021/12/6 19:45
 * @Version: V1.0.0
 */
public class Test {

    public static void main(String[] args) {
        CustomApplicationContext context = new CustomApplicationContext(AppConfig.class);

        UserService userService = (UserService) context.getBean("userService");
        userService.test();//如果进行了AOP，userService就是一个代理对象，先进行代理逻辑，在进行业务逻辑test方法
    }
}
