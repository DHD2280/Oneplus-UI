package it.dhd.oneplusui.physicsengine.common;

import android.util.Log;

public class Debug {

    public static final String COMMON_LOG_TAG = "PhysicsWorld";
    public static final String FRAME_LOG_TAG = "PhysicsWorld-Frame";
    public static boolean sDebug = false;
    public static boolean sDebugFrame = false;

    public static boolean debugFrame() {
        return sDebugFrame;
    }

    public static boolean isDebugMode() {
        return sDebug;
    }

    public static void logBackTrace(String str) {
        Log.d(COMMON_LOG_TAG, str, new Throwable());
    }

    public static void logD(String str) {
        logD(COMMON_LOG_TAG, str);
    }

    public static void logE(String str) {
        logD(COMMON_LOG_TAG, str);
    }

    public static void logThrowable(Throwable th) {
        Log.d(COMMON_LOG_TAG, th.getMessage());
    }

    public static void setDebugMode(boolean debugMode) {
        sDebug = debugMode;
        sDebugFrame = debugMode;
    }

    public static void logD(String tag, String message) {
        Log.d(tag, message);
    }

    public static void logE(String tag, String message) {
        Log.e(tag, message);
    }
}