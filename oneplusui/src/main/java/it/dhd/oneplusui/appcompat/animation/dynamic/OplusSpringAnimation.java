package it.dhd.oneplusui.appcompat.animation.dynamic;


import static it.dhd.oneplusui.appcompat.seekbar.OplusSlider.DOUBLE_EPSILON;

import android.os.Looper;
import android.util.AndroidRuntimeException;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.FloatValueHolder;

public final class OplusSpringAnimation extends OplusDynamicAnimation<OplusSpringAnimation> {
    private static final float UNSET = Float.MAX_VALUE;
    private boolean mEndRequested;
    private float mPendingPosition;
    private OplusSpringForce mSpring;

    public OplusSpringAnimation(FloatValueHolder floatValueHolder) {
        super(floatValueHolder);
        mSpring = null;
        mPendingPosition = Float.MAX_VALUE;
        mEndRequested = false;
    }
    private void sanityCheck() {
        OplusSpringForce cOUISpringForce = mSpring;
        if (cOUISpringForce == null) {
            throw new UnsupportedOperationException("Incomplete SpringAnimation: Either final position or a spring force needs to be set.");
        }
        double finalPosition = cOUISpringForce.getFinalPosition();
        if (finalPosition > mMaxValue) {
            throw new UnsupportedOperationException("Final position of the spring cannot be greater than the max value.");
        }
        if (finalPosition < mMinValue) {
            throw new UnsupportedOperationException("Final position of the spring cannot be less than the min value.");
        }
    }

    public void animateToFinalPosition(float f2) {
        if (isRunning()) {
            mPendingPosition = f2;
            return;
        }
        if (mSpring == null) {
            mSpring = new OplusSpringForce(f2);
        }
        mSpring.setFinalPosition(f2);
        start();
    }

    public boolean canSkipToEnd() {
        return mSpring.mDampingRatio > DOUBLE_EPSILON;
    }

    @Override
    public float getAcceleration(float f2, float f3) {
        return mSpring.getAcceleration(f2, f3);
    }

    public OplusSpringForce getSpring() {
        return mSpring;
    }

    @Override
    public boolean isAtEquilibrium(float f2, float f3) {
        return mSpring.isAtEquilibrium(f2, f3);
    }

    public void reset() {
        if (!canSkipToEnd()) {
            throw new UnsupportedOperationException("Spring animations can only come to an end when there is damping");
        }
        cancel();
        if (mPendingPosition != Float.MAX_VALUE) {
            mSpring.setFinalPosition(mPendingPosition);
            mPendingPosition = Float.MAX_VALUE;
        }
        mValue = mSpring.getFinalPosition();
        mVelocity = 0.0f;
        mEndRequested = false;
    }

    public OplusSpringAnimation setSpring(OplusSpringForce oplusSpringForce) {
        mSpring = oplusSpringForce;
        return this;
    }

    public void skipToEnd() {
        if (!canSkipToEnd()) {
            throw new UnsupportedOperationException("Spring animations can only come to an end when there is damping");
        }
        if (!mEnableNonMainThread && Looper.myLooper() != Looper.getMainLooper()) {
            throw new AndroidRuntimeException("Animations may only be started on the main thread");
        }
        if (mRunning) {
            mEndRequested = true;
        }
    }

    @Override
    public void start() {
        sanityCheck();
        mSpring.setValueThreshold(getValueThreshold());
        super.start();
    }

    @Override
    public boolean updateValueAndVelocity(long j2) {
        if (mEndRequested) {
            float f2 = mPendingPosition;
            if (f2 != Float.MAX_VALUE) {
                mSpring.setFinalPosition(f2);
                mPendingPosition = Float.MAX_VALUE;
            }
            mValue = mSpring.getFinalPosition();
            mVelocity = 0.0f;
            mEndRequested = false;
            return true;
        }
        if (mPendingPosition != Float.MAX_VALUE) {
            mSpring.getFinalPosition();
            long j3 = j2 / 2;
            OplusDynamicAnimation.MassState updateValues = mSpring.updateValues(mValue, mVelocity, j3);
            mSpring.setFinalPosition(mPendingPosition);
            mPendingPosition = Float.MAX_VALUE;
            OplusDynamicAnimation.MassState updateValues2 = mSpring.updateValues(updateValues.mValue, updateValues.mVelocity, j3);
            mValue = updateValues2.mValue;
            mVelocity = updateValues2.mVelocity;
        } else {
            OplusDynamicAnimation.MassState updateValues3 = mSpring.updateValues(mValue, mVelocity, j2);
            mValue = updateValues3.mValue;
            mVelocity = updateValues3.mVelocity;
        }
        float max = Math.max(mValue, mMinValue);
        mValue = max;
        float min = Math.min(max, mMaxValue);
        mValue = min;
        if (!isAtEquilibrium(min, mVelocity)) {
            return false;
        }
        mValue = mSpring.getFinalPosition();
        mVelocity = 0.0f;
        return true;
    }

    public <K> OplusSpringAnimation(K k2, FloatPropertyCompat<K> floatPropertyCompat) {
        super(k2, floatPropertyCompat);
        mSpring = null;
        mPendingPosition = Float.MAX_VALUE;
        mEndRequested = false;
    }

    public <K> OplusSpringAnimation(K k2, FloatPropertyCompat<K> floatPropertyCompat, float f2) {
        super(k2, floatPropertyCompat);
        mSpring = null;
        mPendingPosition = Float.MAX_VALUE;
        mEndRequested = false;
        mSpring = new OplusSpringForce(f2);
    }

    @Override
    public void setValueThreshold(float valueThreshold) {}
}
