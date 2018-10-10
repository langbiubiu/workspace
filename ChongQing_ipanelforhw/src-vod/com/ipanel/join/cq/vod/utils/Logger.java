package com.ipanel.join.cq.vod.utils;

import android.util.Log;

public class Logger
{
    private static boolean DEBUG = true;
    private static String Tag = "VOD_HUAWEI";

    public static void d(String TAG,String msg)
    {
        if (DEBUG)
            Log.d(TAG, msg);
    }
    
    public static void d(String msg)
    {
        if (DEBUG)
            Log.d(Tag, msg);
    }

    public static void d(String TAG,String msg, Throwable t)
    {
        if (DEBUG)
            Log.d(TAG, msg, t);
    }

    public static void e(String TAG,String msg)
    {
        if (DEBUG)
            Log.e(TAG, msg);
    }

    public static void e(String TAG,String msg, Throwable t)
    {
        if (DEBUG)
            Log.e(TAG, msg, t);
    }

    public static void printMemory(String TAG,String msg)
    {
        d(TAG,msg);
        d(TAG,"maxMemory: " + Runtime.getRuntime().maxMemory()/1024 + "KB");
        d(TAG,"totalMemory: " + Runtime.getRuntime().totalMemory()/1024 + "KB");
        d(TAG,"freeMemory: " + Runtime.getRuntime().freeMemory()/1024 + "KB");
        d(TAG,"nativeHeapSize: " + android.os.Debug.getNativeHeapSize()/1024 + "KB");
        d(TAG,"nativeHeapAllocatedSize: " + android.os.Debug.getNativeHeapAllocatedSize()/1024 + "KB");
        d(TAG,"nativeHeapFreeSize: " + android.os.Debug.getNativeHeapFreeSize()/1024 + "KB");
    }
}

