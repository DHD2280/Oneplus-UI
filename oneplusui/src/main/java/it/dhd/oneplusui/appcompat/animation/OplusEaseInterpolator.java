package it.dhd.oneplusui.appcompat.animation;

import android.view.animation.PathInterpolator;

public class OplusEaseInterpolator extends PathInterpolator {
    private static final float controlX1 = 0.33f;
    private static final float controlX2 = 0.67f;
    private static final float controlY1 = 0.0f;
    private static final float controlY2 = 1.0f;

    public OplusEaseInterpolator() {
        super(0.33f, 0.0f, 0.67f, 1.0f);
    }
}