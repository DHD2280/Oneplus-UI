package it.dhd.oneplusui.physicsengine.common;

public class MathUtils {

    @SuppressWarnings("unused")
    public static final float PI = 3.1415927f;

    public static float abs(float f2) {
        return f2 > 0.0f ? f2 : -f2;
    }

    public static boolean floatEquals(float f2, float f3) {
        return ((double) Math.abs(f2 - f3)) < 1.0E-7d;
    }

    public static float max(float f2, float f3) {
        return Math.max(f2, f3);
    }

    public static float min(float f2, float f3) {
        return Math.min(f2, f3);
    }

    public static float sqrt(float f2) {
        return (float) StrictMath.sqrt(f2);
    }
}