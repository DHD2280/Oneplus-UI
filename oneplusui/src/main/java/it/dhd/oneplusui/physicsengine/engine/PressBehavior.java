package it.dhd.oneplusui.physicsengine.engine;

import it.dhd.oneplusui.physicsengine.common.Compat;
import it.dhd.oneplusui.physicsengine.common.Debug;
import it.dhd.oneplusui.physicsengine.common.Vector;
import it.dhd.oneplusui.physicsengine.dynamics.spring.Spring;

public class PressBehavior extends BaseBehavior {

    private static final float DEFAULT_START_SCALE = 1.0f;
    private final Vector mPressForce = new Vector(0.0f, 5000.0f);
    private float mTransformScale = Float.MAX_VALUE;
    private float mMinScaleThreshold = 0.0f;
    private boolean mIsPressing = false;

    public PressBehavior() {
        createSpringDef();
        withProperty(PhysicalAnimator.scaleX(), PhysicalAnimator.scaleY());
    }

    private void calculateDistanceToScale() {
        float physicalSizeToPixels = Compat.physicalSizeToPixels((this.mSpring.getTarget().mY * 2.0f) - this.mPropertyBody.getPosition().mY);
        this.mTransformScale = physicalSizeToPixels;
        float f2 = this.mMinScaleThreshold;
        if (physicalSizeToPixels < f2 / 0.002f) {
            this.mTransformScale = f2 / 0.002f;
        }
        this.mActiveUIItem.setTransformScale(this.mTransformScale);
    }

    private void createSpring() {
        if (createDefaultSpring(this.mSpringDef)) {
            this.mSpring.setTarget(this.mPropertyBody.getPosition());
        }
    }

    private void destroySpring() {
        destroyDefaultSpring();
    }

    @Override
    public void dispatchChanging() {
        if (this.mIsSpringApplied) {
            if (this.mIsPressing) {
                this.mPropertyBody.applyForceToCenter(this.mPressForce);
            }
            calculateDistanceToScale();
        }
    }

    @Override
    public int getType() {
        return BEHAVIOR_TYPE_PRESS;
    }

    @Override
    public void moveToStartValue() {
        Vector vector = new Vector(Compat.pixelsToPhysicalSize(this.mActiveUIItem.mStartScale.mX / this.mValueThreshold), Compat.pixelsToPhysicalSize(this.mActiveUIItem.mStartScale.mY / this.mValueThreshold));
        transformBodyTo(this.mPropertyBody, vector);
        Spring spring = this.mSpring;
        if (spring != null) {
            spring.setTarget(vector);
        }
        if (Debug.isDebugMode()) {
            Debug.logD("PressBehavior : moveToStartValue scaleToPosition =:" + vector);
        }
    }

    public PressBehavior setPressForce(float f2) {
        this.mPressForce.mY = f2;
        return this;
    }

    public void start(boolean z2) {
        this.mIsPressing = true;
        this.mPropertyBody.applyForceToCenter(this.mPressForce);
        startBehavior();
        if (z2) {
            return;
        }
        this.mIsPressing = false;
    }

    @Override
    public void startBehavior() {
        super.startBehavior();
        createSpring();
    }

    public void stop() {
        this.mIsPressing = false;
    }

    @Override
    public boolean stopBehavior() {
        destroySpring();
        return super.stopBehavior();
    }

    @Override
    public void updateStartValue() {
        for (FloatPropertyHolder floatPropertyHolder : this.mPropertyMap.values()) {
            if (floatPropertyHolder != null && !floatPropertyHolder.mIsStartValueSet) {
                floatPropertyHolder.setStartValue(1.0f);
                floatPropertyHolder.verifyStartValue(this.mActiveUIItem);
            }
        }
    }
}