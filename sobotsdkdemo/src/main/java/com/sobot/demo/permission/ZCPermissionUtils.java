package com.sobot.demo.permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;

import androidx.core.content.PermissionChecker;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 一个专门用于动态权限的工具类
 */

final public class ZCPermissionUtils {
    private ZCPermissionUtils() {
    }

    /**
     * 判断系统版本是否大于6.0
     *
     * @return
     */
    public static boolean isOverMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * 从申请的权限中找出未授予的权限
     *
     * @param activity
     * @param permission
     * @return
     */
    @TargetApi(value = Build.VERSION_CODES.M)
    public static List<String> findDeniedPermissions(Activity activity, String... permission) {
        List<String> denyPermissions = new ArrayList<>();
        for (String value : permission) {
            if (!selfPermissionGranted(activity,value)) {
                denyPermissions.add(value);
            }
        }
        return denyPermissions;
    }

    public static boolean selfPermissionGranted(Context context, String permission) {
        return  PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED;
    }
    /**
     * 寻找相应的注解方法
     *
     * @param clazz  寻找的那个类
     * @param clazz1 响应的注解的标记
     * @return
     */
    public static List<Method> findAnnotationMethods(Class clazz, Class<? extends Annotation> clazz1) {
        List<Method> methods = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(clazz1)) {
                methods.add(method);
            }
        }
        return methods;
    }

    public static <A extends Annotation> Method findMethodPermissionFailWithRequestCode(Class clazz,
                                                                                        Class<A> permissionFailClass, int requestCode) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(permissionFailClass)) {
                if (requestCode == method.getAnnotation(ZCPermissionFail.class).requestCode()) {
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * 找到那个相应的注解方法且requestCode与需要的一样
     *
     * @param m
     * @param clazz
     * @param requestCode
     * @return
     */
    public static boolean isEqualRequestCodeFromAnntation(Method m, Class clazz, int requestCode) {
        if (clazz.equals(ZCPermissionFail.class)) {
            return requestCode == m.getAnnotation(ZCPermissionFail.class).requestCode();
        } else if (clazz.equals(ZCPermissionSuccess.class)) {
            return requestCode == m.getAnnotation(ZCPermissionSuccess.class).requestCode();
        } else {
            return false;
        }
    }

    public static <A extends Annotation> Method findMethodWithRequestCode(Class clazz,
                                                                          Class<A> annotation, int requestCode) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                if (isEqualRequestCodeFromAnntation(method, annotation, requestCode)) {
                    return method;
                }
            }
        }
        return null;
    }

    public static <A extends Annotation> Method findMethodPermissionSuccessWithRequestCode(Class clazz,
                                                                                           Class<A> permissionFailClass, int requestCode) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(permissionFailClass)) {
                if (requestCode == method.getAnnotation(ZCPermissionSuccess.class).requestCode()) {
                    return method;
                }
            }
        }
        return null;
    }

    public static Activity getActivity(Object object) {
        if (object instanceof Fragment) {
            return ((Fragment) object).getActivity();
        } else if (object instanceof Activity) {
            return (Activity) object;
        }
        return null;
    }
}
