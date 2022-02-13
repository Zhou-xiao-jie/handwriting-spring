package com.atxiaojie.test.service;

import com.atxiaojie.spring.*;

/**
 * @ClassName: UserService
 * @Description: TODO
 * @author: zhouxiaojie
 * @date: 2021/12/6 19:55
 * @Version: V1.0.0
 */
@Component("userService")
//@Scope("prototype")
public class UserServiceImpl implements UserService, BeanNameAware, InitializingBean {

    @Autowired
    private OrderService orderService;

    private String name;

    private String beanName;

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("初始化");
    }

    @Override
    public void test(){
        System.out.println(orderService);
        System.out.println(beanName);
        System.out.println(name);
    }
}
