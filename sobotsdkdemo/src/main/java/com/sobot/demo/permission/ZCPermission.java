package com.sobot.demo.permission;

import static com.sobot.demo.permission.ZCPermissionUtils.getActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Build;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Yang on 2017/9/20.
 * desc: 一个专门用于动态权限的工具类
 */

public class ZCPermission {
    private String[] mPermissions;
    private int mRequestCode;
    private Object object;
    private static ZCPermissionCallback zcPermissionCallback;

    private ZCPermission(Object object) {
        this.object = object;
    }

    public static ZCPermission with(Activity activity) {
        return new ZCPermission(activity);
    }

    public static ZCPermission with(Fragment fragment) {
        return new ZCPermission(fragment);
    }

    public ZCPermission permissions(String... permissions) {
        this.mPermissions = permissions;
        return this;
    }

    public ZCPermission addRequestCode(int requestCode) {
        this.mRequestCode = requestCode;
        return this;
    }

    @TargetApi(value = Build.VERSION_CODES.M)
    public void request() {
        zcPermissionCallback = null;
        requestPermissions(object, mRequestCode, mPermissions);
    }

    @TargetApi(value = Build.VERSION_CODES.M)
    public void request(ZCPermissionCallback callback) {
        if (callback != null) {
            zcPermissionCallback = callback;
        }
        requestPermissions(object, mRequestCode, mPermissions);
    }

    public static void needPermission(Activity activity, int requestCode, String[] permissions) {
        zcPermissionCallback = null;
        requestPermissions(activity, requestCode, permissions);
    }

    public static void needPermission(Fragment fragment, int requestCode, String[] permissions) {
        zcPermissionCallback = null;
        requestPermissions(fragment, requestCode, permissions);
    }

    public static void needPermission(Activity activity, int requestCode, String[] permissions
            , ZCPermissionCallback callback) {
        if (callback != null) {
            zcPermissionCallback = callback;
        }
        requestPermissions(activity, requestCode, permissions);
    }

    public static void needPermission(Fragment fragment, int requestCode, String[] permissions
            , ZCPermissionCallback callback) {
        if (callback != null) {
            zcPermissionCallback = callback;
        }
        requestPermissions(fragment, requestCode, permissions);
    }

    public static void needPermission(Activity activity, int requestCode, String permission) {
        zcPermissionCallback = null;
        needPermission(activity, requestCode, new String[]{permission});
    }

    public static void needPermission(Fragment fragment, int requestCode, String permission) {
        zcPermissionCallback = null;
        needPermission(fragment, requestCode, new String[]{permission});
    }

    public static void needPermission(Activity activity, int requestCode, String permission, ZCPermissionCallback callback) {
        if (callback != null) {
            zcPermissionCallback = callback;
        }
        needPermission(activity, requestCode, new String[]{permission});
    }

    public static void needPermission(Fragment fragment, int requestCode, String permission, ZCPermissionCallback callback) {
        if (callback != null) {
            zcPermissionCallback = callback;
        }
        needPermission(fragment, requestCode, new String[]{permission});
    }


    /**
     * 检查权限是否已授权
     *
     * @param object
     * @param permissions
     */
    @TargetApi(value = Build.VERSION_CODES.M)
    public static boolean checkPermissions(Object object, String[] permissions) {
        List<String> deniedPermissions = ZCPermissionUtils.findDeniedPermissions(getActivity(object), permissions);
        /**
         * 先检查是否有没有授予的权限，有的话返回false，没有的话就直接返回true
         */
        if (deniedPermissions.size() > 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 请求权限
     *
     * @param object
     * @param requestCode
     * @param permissions
     */
    @TargetApi(value = Build.VERSION_CODES.M)
    private static void requestPermissions(Object object, int requestCode, String[] permissions) {
        if (!ZCPermissionUtils.isOverMarshmallow()) {
            if (zcPermissionCallback != null) {
                zcPermissionCallback.permissionSuccess(requestCode);
            } else {
                doExecuteSuccess(object, requestCode);
            }
            return;
        }
        List<String> deniedPermissions = ZCPermissionUtils.findDeniedPermissions(getActivity(object), permissions);

        /**
         * 先检查是否有没有授予的权限，有的话请求，没有的话就直接执行权限授予成功的接口/注解方法
         */
        if (deniedPermissions.size() > 0) {
            if (object instanceof Activity) {
                ((Activity) object).requestPermissions(deniedPermissions.toArray(new String[deniedPermissions.size()]), requestCode);
            } else if (object instanceof Fragment) {
                ((Fragment) object).requestPermissions(deniedPermissions.toArray(new String[deniedPermissions.size()]), requestCode);
            } else {
                throw new IllegalArgumentException(object.getClass().getName() + " is not supported");
            }

        } else {
            if (zcPermissionCallback != null) {
                zcPermissionCallback.permissionSuccess(requestCode);
            } else {
                doExecuteSuccess(object, requestCode);
            }
        }
    }

    private static void doExecuteSuccess(Object activity, int requestCode) {
        Method executeMethod = ZCPermissionUtils.findMethodWithRequestCode(activity.getClass(),
                ZCPermissionSuccess.class, requestCode);

        executeMethod(activity, executeMethod);
    }

    private static void doExecuteFail(Object activity, int requestCode) {
        Method executeMethod = ZCPermissionUtils.findMethodWithRequestCode(activity.getClass(),
                ZCPermissionFail.class, requestCode);

        executeMethod(activity, executeMethod);
    }

    private static void executeMethod(Object activity, Method executeMethod) {
        if (executeMethod != null) {
            try {
                if (!executeMethod.isAccessible()) executeMethod.setAccessible(true);
                executeMethod.invoke(activity, new Object[]{});
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public static void onRequestPermissionsResult(Activity activity, int requestCode, String[] permissions,
                                                  int[] grantResults) {
        requestResult(activity, requestCode, permissions, grantResults);
    }

    public static void onRequestPermissionsResult(Fragment fragment, int requestCode, String[] permissions,
                                                  int[] grantResults) {
        requestResult(fragment, requestCode, permissions, grantResults);
    }

    /**
     * 有回调接口的话(即回调接口不为空的话)先执行回调接口的方法，若为空，则寻找响应的注解方法。
     *
     * @param obj
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    private static void requestResult(Object obj, int requestCode, String[] permissions,
                                      int[] grantResults) {
        List<String> deniedPermissions = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permissions[i]);
            }
        }

        if (deniedPermissions.size() > 0) {
            if (zcPermissionCallback != null) {
                zcPermissionCallback.permissionFail(requestCode);
            } else {
                doExecuteFail(obj, requestCode);
            }
        } else {
            if (zcPermissionCallback != null) {
                zcPermissionCallback.permissionSuccess(requestCode);
            } else {
                doExecuteSuccess(obj, requestCode);
            }
        }
    }

    public interface ZCPermissionCallback {
        //请求权限成功
        void permissionSuccess(int requsetCode);

        //请求权限失败
        void permissionFail(int requestCode);
    }
}
