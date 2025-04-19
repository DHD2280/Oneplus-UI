package it.dhd.oneplusui.physicsengine.common;

public class Compat {
    public static final float EPSILON = 1.1920929E-7f;
    public static final float FORCE_RATIO = 1.0f;
    public static final float LINEAR_DAMPING_INTERCEPT = 2.2141f;
    public static final float LINEAR_DAMPING_SLOPE = 0.052f;
    public static final int PHYSICAL_SIZE_TO_DP_RATIO = 55;
    public static final float POSITION_VALUE_THRESHOLD = 1.0f;
    public static final float ROTATION_VALUE_THRESHOLD = 0.1f;
    public static final float SCALE_VALUE_THRESHOLD = 0.002f;
    public static final float UNSET = 0.0f;
    public static final float UNSET_FREQUENCY = 50.0f;
    public static final int VELOCITY_ITERATIONS = 4;
    public static float sPhysicalSizeToPixelsRatio = 160.0f;
    public static float sRefreshRate = 0.008333334f;
    public static float sSteadyAccuracy = 0.1f;

    public static float calculateLinearDampingByMass(float f2) {
        return (MathUtils.sqrt(f2) * 2.8600001f) + LINEAR_DAMPING_INTERCEPT;
    }

    public static float dpToPhysicalSize(float f2) {
        return f2 / PHYSICAL_SIZE_TO_DP_RATIO;
    }

    public static boolean lessThanSteadyAccuracy(float f2) {
        return f2 < sSteadyAccuracy;
    }

    public static float physicalSizeToDp(float f2) {
        return f2 * PHYSICAL_SIZE_TO_DP_RATIO;
    }

    public static float physicalSizeToPixels(float f2) {
        return f2 * sPhysicalSizeToPixelsRatio;
    }

    public static float pixelsToPhysicalSize(float f2) {
        return f2 / sPhysicalSizeToPixelsRatio;
    }

    public static void updatePhysicalSizeToPixelsRatio(float f2) {
        sPhysicalSizeToPixelsRatio = (f2 * PHYSICAL_SIZE_TO_DP_RATIO) + 0.5f;
        sSteadyAccuracy = pixelsToPhysicalSize(0.1f);
    }

    public static void updateRefreshRate(float refreshRate) {
        sRefreshRate = refreshRate;
    }
}