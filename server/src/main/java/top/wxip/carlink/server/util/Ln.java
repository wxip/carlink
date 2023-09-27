package top.wxip.carlink.server.util;

import android.util.Log;

import cn.hutool.core.exceptions.ExceptionUtil;

public final class Ln {

    private static final String TAG = "top.wxip.carlink.server";

    public static void d(String message) {
        System.out.println("DEBUG:" + message);
        Log.d(TAG, message);
    }

    public static void i(String message) {
        System.out.println("INFO:" + message);
        Log.i(TAG, message);
    }

    public static void e(String message) {
        System.out.println("ERROR:" + message);
        Log.e(TAG, message);
    }

    public static void e(String message, Throwable throwable) {
        System.out.println("ERROR:" + message + " " +
                ExceptionUtil.stacktraceToOneLineString(throwable));
        Log.e(TAG, message, throwable);
    }

    public static void action(String message) {
        System.out.println(message);
    }
}
