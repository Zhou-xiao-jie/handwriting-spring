package com.atxiaojie.spring;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName: CustomApplicationContext
 * @Description: 自定义容器
 * @author: zhouxiaojie
 * @date: 2021/12/6 19:45
 * @Version: V1.0.0
 */
public class CustomApplicationContext {

    private Class configClass;

    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();//单例池
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public CustomApplicationContext(Class configClass) {
        this.configClass = configClass;

        //解析配置类
        //ComponentScan注解--->扫描路径--->扫描----->生成beanDefinition---->beanDefinitionMap
        scan(configClass);

        //对于单例的Bean要去创建Bean
        if(beanDefinitionMap != null && beanDefinitionMap.size() > 0){
            for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
                String beanName = entry.getKey();
                BeanDefinition beanDefinition = entry.getValue();
                if(beanDefinition.getScope().equals("singleton")){
                    Object bean = createBean(beanName, beanDefinition);
                    singletonObjects.put(beanName, bean);
                }
            }
        }
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getClazz();
        try {
            //创建对象
            Object instance = clazz.getDeclaredConstructor().newInstance();
            //给属性赋值，依赖注入，首先迭代对象中的属性，判断是否加Autowired注解，加入注解的属性才依赖注入
            Field[] declaredFields = clazz.getDeclaredFields();
            if(declaredFields.length > 0){
                for (Field declaredField : declaredFields) {
                    if (declaredField.isAnnotationPresent(Autowired.class)) {
                        //根据属性名字来找
                        Object bean = getBean(declaredField.getName());
                        if(bean == null){
                            //抛异常
                        }
                        declaredField.setAccessible(true);
                        declaredField.set(instance, bean);
                    }
                }
            }

            //Aware回调，判断当前实例是不是实现了BeanNameAware接口，如果实现了，进行下面操作
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            //初始化前调用BeanPostProcessor的初始化前的方法
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }

            //初始化
            if (instance instanceof InitializingBean) {
                try {
                    ((InitializingBean) instance).afterPropertiesSet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //AOP就发生在初始化后步骤中
            //BeanPostProcessor(Bean的后置处理器)对外的扩展机制
            //初始化后调用BeanPostProcessor的初始化后的方法
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                //如果配置了切面的，执行这个方法后，instance返回的是一个代理对象
                instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }
            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @MethodsName: scan
     * @Description 扫描
     * @Author zhouxiaojie
     * @Date 19:22 2021/12/7
     * @Param [configClass]
     * @return void
     **/
    private void scan(Class configClass) {
        if(configClass.isAnnotationPresent(ComponentScan.class)){
            ComponentScan componentScanAnnotation = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
            String path = componentScanAnnotation.value();
            path = path.replace(".", "/");
            //扫描
            ClassLoader classLoader = CustomApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path);
            if(resource != null){
                File file = new File(resource.getFile());
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    if(files != null){
                        for (File f : files) {
                            String absolutePath = f.getAbsolutePath();
                            if(absolutePath.endsWith(".class")){
                                String className = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.indexOf(".class"));
                                className = className.replace("\\", ".");
                                try {
                                    Class<?> clazz = classLoader.loadClass(className);
                                    boolean annotationPresent = clazz.isAnnotationPresent(Component.class);
                                    if(annotationPresent){
                                        //判断clazz是否实现了BeanPostProcessor的接口
                                        if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                            //调用无参构造方法实例化一个对象，调用里面的两个方法
                                            try {
                                                BeanPostProcessor instance = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                                                //BeanPostProcessor instance = (BeanPostProcessor) getBean("myBeanPostProcessor");
                                                beanPostProcessorList.add(instance);
                                            } catch (InstantiationException e) {
                                                e.printStackTrace();
                                            } catch (IllegalAccessException e) {
                                                e.printStackTrace();
                                            } catch (InvocationTargetException e) {
                                                e.printStackTrace();
                                            } catch (NoSuchMethodException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        //表示当前这个类是一个Bean
                                        //解析类，判断当前的Bean是单例bean，还是prototype（原型）的bean
                                        //解析类，生成BeanDefinition对象
                                        Component componentAnnotation = clazz.getDeclaredAnnotation(Component.class);
                                        String beanName = componentAnnotation.value();
                                        BeanDefinition beanDefinition = new BeanDefinition();
                                        beanDefinition.setClazz(clazz);
                                        if (clazz.isAnnotationPresent(Scope.class)) {
                                            //表示是原型的
                                            Scope scopeAnnotation = clazz.getDeclaredAnnotation(Scope.class);
                                            beanDefinition.setScope(scopeAnnotation.value());
                                        }else{
                                            //表示是单例的
                                            beanDefinition.setScope("singleton");
                                        }
                                        beanDefinitionMap.put(beanName, beanDefinition);
                                    }
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public Object getBean(String beanName){
        if(beanDefinitionMap.containsKey(beanName)){
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if(beanDefinition.getScope().equals("singleton")){
                return singletonObjects.get(beanName);
            }else{
                //原型，创建bean对象
                return createBean(beanName,beanDefinition);
            }
        }else{
            throw new NullPointerException();
        }
    }
}
