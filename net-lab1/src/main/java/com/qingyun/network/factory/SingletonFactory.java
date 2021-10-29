package com.qingyun.network.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description： 单例工厂
 * @author: 張青云
 * @create: 2021-10-27 19:17
 **/
public class SingletonFactory {
    /**
     * 用来存储由当前工厂所创建单例对象实例
     */
    private final Map<Class<?>, Object> objects = new ConcurrentHashMap<>();

    private static SingletonFactory instance;

    private SingletonFactory() {

    }

    /**
     * 获取当前工厂的实例
     */
    public static synchronized SingletonFactory getInstance() {
        if (instance == null) {
            instance = new SingletonFactory();
        }
        return instance;
    }

    /**
     * 获取或创建单例对象
     * @param clazz 对象的Class类型
     * @param <T> 对象的类型
     * @return 单例对象
     */
    public synchronized <T> T getObject(Class<T> clazz) {
        Object object = objects.get(clazz);
        if (object == null) {
            //  通过反射来创建对象
            try {
                object = clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            //  加入到缓存中
            objects.put(clazz, object);
        }
        return (T)object;
    }

    /**
     * 向单例工厂中注册一个单例对象，将该对象交给工厂来管理
     */
    public synchronized void addSingleton(Object object) throws Exception {
        Class<?> clazz = object.getClass();
        if (objects.containsKey(clazz)) {
            throw new Exception(clazz + "类型的对象已经在工厂中存在了！");
        }
        objects.put(clazz, object);
    }
}
