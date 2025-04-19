package it.dhd.oneplusui.appcompat.animation;

import android.view.animation.PathInterpolator;

public class OplusMoveEaseInterpolator extends PathInterpolator {

    private static final float controlX1 = 0.3f;
    private static final float controlX2 = 0.1f;
    private static final float controlY1 = 0.0f;
    private static final float controlY2 = 1.0f;

    public OplusMoveEaseInterpolator() {
        super(0.3f, 0.0f, 0.1f, 1.0f);
    }

}
