package it.dhd.oneplusui.appcompat.animation.dynamic;

import android.os.Looper;
import android.util.AndroidRuntimeException;
import android.view.View;
import androidx.annotation.FloatRange;
import androidx.annotation.RestrictTo;
import androidx.core.view.ViewCompat;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.FloatValueHolder;
import java.util.ArrayList;

public abstract class OplusDynamicAnimation<T extends OplusDynamicAnimation<T>> implements OplusAnimationHandler.AnimationFrameCallback {
    
    public static final float MIN_VISIBLE_CHANGE_ALPHA = 0.00390625f;
    public static final float MIN_VISIBLE_CHANGE_PIXELS = 1.0f;
    public static final float MIN_VISIBLE_CHANGE_ROTATION_DEGREES = 0.1f;
    public static final float MIN_VISIBLE_CHANGE_SCALE = 0.002f;
    private static final float THRESHOLD_MULTIPLIER = 0.75f;
    private static final float UNSET = Float.MAX_VALUE;
    boolean mEnableNonMainThread;
    private final ArrayList<OnAnimationEndListener> mEndListeners;
    private long mLastFrameTime;
    float mMaxValue;
    float mMinValue;
    private float mMinVisibleChange;
    final FloatPropertyCompat mProperty;
    boolean mRunning;
    boolean mStartValueIsSet;
    final Object mTarget;
    private final ArrayList<OnAnimationUpdateListener> mUpdateListeners;
    float mValue;
    float mVelocity;
    public static final ViewProperty TRANSLATION_X = new ViewProperty("translationX") {
        @Override
        public float getValue(View view) {
            return view.getTranslationX();
        }

        @Override
        public void setValue(View view, float f2) {
            view.setTranslationX(f2);
        }
    };
    public static final ViewProperty TRANSLATION_Y = new ViewProperty("translationY") {
        @Override
        public float getValue(View view) {
            return view.getTranslationY();
        }

        @Override
        public void setValue(View view, float f2) {
            view.setTranslationY(f2);
        }
    };
    public static final ViewProperty TRANSLATION_Z = new ViewProperty("translationZ") {
        @Override
        public float getValue(View view) {
            return ViewCompat.getTranslationZ(view);
        }

        @Override
        public void setValue(View view, float f2) {
            ViewCompat.setTranslationZ(view, f2);
        }
    };
    public static final ViewProperty SCALE_X = new ViewProperty("scaleX") {
        @Override
        public float getValue(View view) {
            return view.getScaleX();
        }

        @Override
        public void setValue(View view, float f2) {
            view.setScaleX(f2);
        }
    };
    public static final ViewProperty SCALE_Y = new ViewProperty("scaleY") {
        @Override
        public float getValue(View view) {
            return view.getScaleY();
        }

        @Override
        public void setValue(View view, float f2) {
            view.setScaleY(f2);
        }
    };
    public static final ViewProperty ROTATION = new ViewProperty("rotation") {
        @Override
        public float getValue(View view) {
            return view.getRotation();
        }

        @Override
        public void setValue(View view, float f2) {
            view.setRotation(f2);
        }
    };
    public static final ViewProperty ROTATION_X = new ViewProperty("rotationX") {
        @Override
        public float getValue(View view) {
            return view.getRotationX();
        }

        @Override
        public void setValue(View view, float f2) {
            view.setRotationX(f2);
        }
    };
    public static final ViewProperty ROTATION_Y = new ViewProperty("rotationY") {
        @Override
        public float getValue(View view) {
            return view.getRotationY();
        }

        @Override
        public void setValue(View view, float f2) {
            view.setRotationY(f2);
        }
    };

    public static final ViewProperty X = new ViewProperty("x") {
        @Override
        public float getValue(View view) {
            return view.getX();
        }

        @Override
        public void setValue(View view, float f2) {
            view.setX(f2);
        }
    };

    public static final ViewProperty Y = new ViewProperty("y") {
        @Override
        public float getValue(View view) {
            return view.getY();
        }

        @Override
        public void setValue(View view, float f2) {
            view.setY(f2);
        }
    };

    public static final ViewProperty Z = new ViewProperty("z") {
        @Override
        public float getValue(View view) {
            return ViewCompat.getZ(view);
        }

        @Override
        public void setValue(View view, float f2) {
            ViewCompat.setZ(view, f2);
        }
    };
    public static final ViewProperty ALPHA = new ViewProperty("alpha") {
        @Override
        public float getValue(View view) {
            return view.getAlpha();
        }

        @Override
        public void setValue(View view, float f2) {
            view.setAlpha(f2);
        }
    };
    public static final ViewProperty SCROLL_X = new ViewProperty("scrollX") {
        @Override
        public float getValue(View view) {
            return view.getScrollX();
        }

        @Override
        public void setValue(View view, float f2) {
            view.setScrollX((int) f2);
        }
    };
    public static final ViewProperty SCROLL_Y = new ViewProperty("scrollY") {
        @Override
        public float getValue(View view) {
            return view.getScrollY();
        }

        @Override
        public void setValue(View view, float f2) {
            view.setScrollY((int) f2);
        }
    };

    public static class MassState {
        public float mValue;
        public float mVelocity;
    }

    public interface OnAnimationEndListener {
        void onAnimationEnd(OplusDynamicAnimation<?> oplusDynamicAnimation, boolean z2, float f2, float f3);
    }

    public interface OnAnimationUpdateListener {
        void onAnimationUpdate(OplusDynamicAnimation oplusDynamicAnimation, float f2, float f3);
    }

    public static abstract class ViewProperty extends FloatPropertyCompat<View> {
        private ViewProperty(String str) {
            super(str);
        }
    }

    public OplusDynamicAnimation(final FloatValueHolder floatValueHolder) {
        mVelocity = 0.0f;
        mValue = Float.MAX_VALUE;
        mStartValueIsSet = false;
        mEnableNonMainThread = false;
        mRunning = false;
        mMaxValue = Float.MAX_VALUE;
        mMinValue = -Float.MAX_VALUE;
        mLastFrameTime = 0L;
        mEndListeners = new ArrayList<>();
        mUpdateListeners = new ArrayList<>();
        mTarget = null;
        mProperty = new FloatPropertyCompat("FloatValueHolder") {
            @Override
            public float getValue(Object obj) {
                return floatValueHolder.getValue();
            }

            @Override
            public void setValue(Object obj, float f2) {
                floatValueHolder.setValue(f2);
            }
        };
        mMinVisibleChange = 1.0f;
    }

    private void endAnimationInternal(boolean z2) {
        mRunning = false;
        OplusAnimationHandler.getInstance().removeCallback(this);
        mLastFrameTime = 0L;
        mStartValueIsSet = false;
        for (int i2 = 0; i2 < mEndListeners.size(); i2++) {
            if (mEndListeners.get(i2) != null) {
                mEndListeners.get(i2).onAnimationEnd(this, z2, mValue, mVelocity);
            }
        }
        removeNullEntries(mEndListeners);
    }

    private float getPropertyValue() {
        return mProperty.getValue(mTarget);
    }

    private static <T> void removeEntry(ArrayList<T> arrayList, T t2) {
        int indexOf = arrayList.indexOf(t2);
        if (indexOf >= 0) {
            arrayList.set(indexOf, null);
        }
    }

    private static <T> void removeNullEntries(ArrayList<T> arrayList) {
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            if (arrayList.get(size) == null) {
                arrayList.remove(size);
            }
        }
    }

    private void startAnimationInternal() {
        if (mRunning) {
            return;
        }
        mRunning = true;
        if (!mStartValueIsSet) {
            mValue = getPropertyValue();
        }
        if (mValue > mMaxValue || mValue < mMinValue) {
            throw new IllegalArgumentException("Starting value need to be in between min value and max value");
        }
        OplusAnimationHandler.getInstance().addAnimationFrameCallback(this, 0L);
    }

    public T addEndListener(OnAnimationEndListener onAnimationEndListener) {
        if (!mEndListeners.contains(onAnimationEndListener)) {
            mEndListeners.add(onAnimationEndListener);
        }
        return (T) this;
    }

    public T addUpdateListener(OnAnimationUpdateListener onAnimationUpdateListener) {
        if (isRunning()) {
            throw new UnsupportedOperationException("Error: Update listeners must be added beforethe animation.");
        }
        if (!mUpdateListeners.contains(onAnimationUpdateListener)) {
            mUpdateListeners.add(onAnimationUpdateListener);
        }
        return (T) this;
    }

    public void cancel() {
        if (!mEnableNonMainThread && Looper.myLooper() != Looper.getMainLooper()) {
            throw new AndroidRuntimeException("Animations may only be canceled on the main thread");
        }
        if (mRunning) {
            endAnimationInternal(true);
        }
    }

    @Override
    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public boolean doAnimationFrame(long lastFrameTime) {
        if (mLastFrameTime == 0) {
            mLastFrameTime = lastFrameTime;
            setPropertyValue(mValue);
            return false;
        }
        mLastFrameTime = lastFrameTime;
        boolean updateValueAndVelocity = updateValueAndVelocity(lastFrameTime - mLastFrameTime);
        float min = Math.min(mValue, mMaxValue);
        mValue = min;
        float max = Math.max(min, mMinValue);
        mValue = max;
        setPropertyValue(max);
        if (updateValueAndVelocity) {
            endAnimationInternal(false);
        }
        return updateValueAndVelocity;
    }

    public abstract float getAcceleration(float f2, float f3);

    public float getMinimumVisibleChange() {
        return mMinVisibleChange;
    }

    public float getValueThreshold() {
        return mMinVisibleChange * 0.75f;
    }

    public abstract boolean isAtEquilibrium(float f2, float f3);

    public boolean isRunning() {
        return mRunning;
    }

    public void removeEndListener(OnAnimationEndListener onAnimationEndListener) {
        removeEntry(mEndListeners, onAnimationEndListener);
    }

    public void removeUpdateListener(OnAnimationUpdateListener onAnimationUpdateListener) {
        removeEntry(mUpdateListeners, onAnimationUpdateListener);
    }

    public T setEnableNonMainThread(boolean z2) {
        mEnableNonMainThread = z2;
        return (T) this;
    }

    public T setMaxValue(float f2) {
        mMaxValue = f2;
        return (T) this;
    }

    public T setMinValue(float f2) {
        mMinValue = f2;
        return (T) this;
    }

    public T setMinimumVisibleChange(@FloatRange(from = 0.0d, fromInclusive = false) float f2) {
        if (f2 <= 0.0f) {
            throw new IllegalArgumentException("Minimum visible change must be positive.");
        }
        mMinVisibleChange = f2;
        setValueThreshold(f2 * 0.75f);
        return (T) this;
    }

    public void setPropertyValue(float f2) {
        mProperty.setValue(mTarget, f2);
        for (int i2 = 0; i2 < mUpdateListeners.size(); i2++) {
            if (mUpdateListeners.get(i2) != null) {
                mUpdateListeners.get(i2).onAnimationUpdate(this, mValue, mVelocity);
            }
        }
        removeNullEntries(mUpdateListeners);
    }

    public T setStartValue(float f2) {
        mValue = f2;
        mStartValueIsSet = true;
        return (T) this;
    }

    public T setStartVelocity(float f2) {
        mVelocity = f2;
        return (T) this;
    }

    public abstract void setValueThreshold(float f2);

    public void start() {
        if (!mEnableNonMainThread && Looper.myLooper() != Looper.getMainLooper()) {
            throw new AndroidRuntimeException("Animations may only be started on the main thread");
        }
        if (mRunning) {
            return;
        }
        startAnimationInternal();
    }

    public abstract boolean updateValueAndVelocity(long j2);

    public <K> OplusDynamicAnimation(K k2, FloatPropertyCompat<K> floatPropertyCompat) {
        mVelocity = 0.0f;
        mValue = Float.MAX_VALUE;
        mStartValueIsSet = false;
        mEnableNonMainThread = false;
        mRunning = false;
        mMaxValue = Float.MAX_VALUE;
        mMinValue = -Float.MAX_VALUE;
        mLastFrameTime = 0L;
        mEndListeners = new ArrayList<>();
        mUpdateListeners = new ArrayList<>();
        mTarget = k2;
        mProperty = floatPropertyCompat;
        if (floatPropertyCompat != ROTATION && floatPropertyCompat != ROTATION_X && floatPropertyCompat != ROTATION_Y) {
            if (floatPropertyCompat == ALPHA) {
                mMinVisibleChange = 0.00390625f;
                return;
            } else if (floatPropertyCompat != SCALE_X && floatPropertyCompat != SCALE_Y) {
                mMinVisibleChange = 1.0f;
                return;
            } else {
                mMinVisibleChange = 0.00390625f;
                return;
            }
        }
        mMinVisibleChange = 0.1f;
    }
}
