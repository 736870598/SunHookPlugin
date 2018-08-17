package com.sunxy.plugin.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * -- 反射工具类
 * <p>
 * Created by sunxy on 2018/8/7 0007.
 */
public class ReflexUtils {

    /**
     * 反射获得 指定对像中的成员
     * 找不到的话就去他的 父类 中找
     */
    public static Field findField(Object instance, String name) throws NoSuchFieldException{
        return findField(instance.getClass(), name);
    }

    /**
     * 反射获得 指定对像中的成员
     * 找不到的话就去他的 父类 中找
     */
    public static Field findField(Class<?> clazz, String name) throws NoSuchFieldException{
        while (clazz != null){
            try {
                Field field = clazz.getDeclaredField(name);
                if (!field.isAccessible()){
                    field.setAccessible(true);
                }
                return field;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException("Field " + name + " not found in " + clazz);
    }

    /**
     * 反射获取对象中的指定函数
     *
     */
    public static Method findMethod(Object instance, String name, Class... parameterTypes)
    throws NoSuchMethodException{
        return findMethod( instance.getClass(), name, parameterTypes);
    }

    /**
     * 反射获取对象中的指定函数
     *
     */
    public static Method findMethod(Class<?> clazz, String name, Class... parameterTypes)
            throws NoSuchMethodException{
        while (clazz != null){
            try {
                Method method = clazz.getDeclaredMethod(name, parameterTypes);
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                return method;
            } catch (NoSuchMethodException e) {
                //如果找不到往父类找
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchMethodException("Method " + name + " with parameters " + Arrays.asList
                (parameterTypes) + " not found in " + clazz);
    }

}
