package it.dhd.oneplusui.appcompat.seekbar;

import static android.os.VibrationEffect.EFFECT_HEAVY_CLICK;
import static android.os.VibrationEffect.EFFECT_TICK;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.AbsSeekBar;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.dynamicanimation.animation.FloatPropertyCompat;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.dhd.oneplusui.R;
import it.dhd.oneplusui.appcompat.animation.OplusMoveEaseInterpolator;
import it.dhd.oneplusui.appcompat.animation.dynamic.OplusDynamicAnimation;
import it.dhd.oneplusui.appcompat.animation.dynamic.OplusSpringAnimation;
import it.dhd.oneplusui.appcompat.animation.dynamic.OplusSpringForce;
import it.dhd.oneplusui.physicsengine.common.Compat;
import it.dhd.oneplusui.physicsengine.engine.AnimationListener;
import it.dhd.oneplusui.physicsengine.engine.AnimationUpdateListener;
import it.dhd.oneplusui.physicsengine.engine.BaseBehavior;
import it.dhd.oneplusui.physicsengine.engine.FlingBehavior;
import it.dhd.oneplusui.physicsengine.engine.FloatValueHolder;
import it.dhd.oneplusui.physicsengine.engine.PhysicalAnimator;

/**
 * A seek bar that supports deformation and physics-based animations.
 * With a new glittered effect.
 */
public class OplusSeekBar extends AbsSeekBar implements AnimationListener, AnimationUpdateListener {
    public static final int MOVE_BY_DEFAULT = 0;
    public static final int MOVE_BY_DISTANCE = 2;
    public static final int MOVE_BY_FINGER = 1;
    protected static final int DIRECTION_180 = 180;
    protected static final int DIRECTION_90 = 90;
    protected static final int RELEASE_ANIM_DURATION = 183;
    protected static final float SCALE_MAX = 1.0f;
    protected static final float SCALE_MIN = 0.0f;
    protected static final float SPRING_BOUNCE = 0.0f;
    protected static final Interpolator THUMB_ANIMATE_INTERPOLATOR = new OplusMoveEaseInterpolator();
    protected static final Interpolator PROGRESS_SCALE_INTERPOLATOR = new OplusMoveEaseInterpolator();
    private static final float BACKGROUND_RADIUS_SCALE = 1.4f;
    private static final float CLICK_SPRING_RESPONSE = 0.3f;
    private static final int DAMPING_DISTANCE = 20;
    private static final int DEFORMATION_SCALE_FACTOR = 100000;
    private static final float DEFORMATION_SPRING_RESPONSE = 0.1f;
    private static final int DURATION_150 = 150;
    private static final int DURATION_483 = 483;
    private static final int FAST_MOVE_VELOCITY = 95;
    private static final int FLEXIBLE_FOLLOW_HAND_SCALE_FACTOR = 1000;
    private static final float FLEXIBLE_FOLLOW_HAND_SPRING_RESPONSE = 0.1f;
    private static final float GLITTER_EFFECT_SPRING_RESPONSE = 0.6f;
    private static final float MAX_FAST_MOVE_PERCENT = 0.95f;
    private static final float MAX_MOVE_DAMPING = 1.0f;
    private static final int MAX_VELOCITY = 8000;
    private static final float MIN_FAST_MOVE_PERCENT = 0.05f;
    private static final int ONE_SECOND_UNITS = 1000;
    private static final int PHYSICAL_VELOCITY_LIMIT = 100;
    private static final float SCALE_DEFORMATION_MAX = 2.0f;
    private static final float SCALE_DEFORMATION_MIN = -1.0f;
    private static final int SCALE_DEFORMATION_TIMES = 5;
    private static final String TAG = "OplusSeekBar";
    private static final float THUMB_SCALE_SPRING_RESPONSE = 0.2f;
    private static final int TOUCH_ANIMATION_ENLARGE_DURATION = 183;
    private static final int VELOCITY_COMPUTE_TIME = 100;
    private final Path mBackgroundPath;
    private final List<OnSeekBarChangeListener> mOnSeekBarChangeListeners = new ArrayList<>();
    private final String mSeekBarRoleDescription;
    protected int mBackgroundColor;
    private final float mBackgroundEnlargeScale;
    private final float mBackgroundHeight;
    private final RectF mBackgroundRect;
    protected float mBackgroundRoundCornerWeight;
    protected OplusSpringAnimation mClickAnim;
    protected float mCurBackgroundHeight;
    protected float mCurBottomDeformationValue;
    protected float mCurPaddingHorizontal;
    protected float mCurProgressRadius;
    protected float mCurThumbRadius;
    protected float mCurTopDeformationValue;
    protected float mDrawProgressScale;
    protected boolean mEnableAdaptiveVibrator;
    protected boolean mEnableVibrator;
    protected Vibrator mVibrator;
    protected boolean mHasMotorVibrator;
    protected float mHeightBottomDeformedDownValue;
    protected float mHeightBottomDeformedUpValue;
    protected float mHeightTopDeformedDownValue;
    protected float mHeightTopDeformedUpValue;
    protected boolean mIsBumpingEdges;
    protected boolean mIsDragging;
    protected float mLabelX;
    protected OplusDynamicAnimation.OnAnimationEndListener mLastEndClickListener;
    protected float mLastX;
    protected int mMax;
    protected int mMin;
    protected int mOldProgress;
    private final float mPaddingHorizontal;
    protected Paint mPaint;
    protected int mProgress;
    private int mProgressColor;
    private final float mProgressHeight;
    private final Path mProgressPath;
    private final RectF mProgressRect;
    protected float mProgressRoundCornerWeight;
    protected float mScale;
    protected boolean mShowProgress;
    protected boolean mShowThumb;
    protected int mThumbColor;
    private final float mThumbMaxRadius;
    protected float mThumbPosition;
    private final float mThumbRadius;
    protected int mThumbShadowColor;
    protected float mThumbShadowOffsetY;
    private final int mThumbShadowRadiusSize;
    protected float mTouchDownX;
    protected ValueAnimator mTouchEnlargeAnimator;
    protected ValueAnimator mTouchReleaseAnimator;
    protected int mTouchSlop;
    protected float mWidthDeformedValue;
    ColorStateList mBackgroundColorStateList;
    ColorStateList mProgressColorStateList;
    ColorStateList mThumbColorStateList;
    private Locale mCachedLocale;
    private int mCurGlitterEffectAlpha;
    private float mCurGlitterEffectValue;
    private float mDamping;
    private OplusSpringAnimation mDeformationAnim;
    private float mFastMoveScaleOffsetX;
    private Spring mFastMoveSpring;
    private final SpringConfig mFastMoveSpringConfig;
    private OplusSpringAnimation mFlexibleFollowHandAnim;
    private FlingBehavior mFlingBehavior;
    private float mFlingDampingRatio;
    private float mFlingFrequency;
    private float mFlingLinearDamping;
    private FloatValueHolder mFlingValueHolder;
    private float mFlingVelocity;
    private OplusSpringAnimation mGlitterEffectAnim;
    private final int mGlitterEffectMaxColor;
    private final int mGlitterEffectMinColor;
    private Paint mGlitterEffectPaint;
    private final FloatPropertyCompat<OplusSeekBar> mGlitterEffectTransition;
    private int mIncrement;
    private boolean mIsPhysicsEnable;
    private boolean mIsStartFromMiddle;
    private boolean mIsSupportDeformation;
    private final float mMaxBackgroundHeight;
    private float mMaxHeightDeformedValue;
    private int mMaxMovingDistance;
    private LinearGradient mMaxToMinLinearGradient;
    private final int mMaxWidth;
    private float mMaxWidthDeformedValue;
    private LinearGradient mMinToMaxLinearGradient;
    private int mMoveType;
    private OnDeformedListener mOnDeformedListener;
    private NumberFormat mPercentFormat;
    private PhysicalAnimator mPhysicalAnimator;
    private float mPixPerProgress;
    private int mRealProgress;
    private int mRefreshStyle;
    private final int mSeekbarMinHeight;
    private final boolean mShowGlitterEffect;
    private boolean mStartDragging;
    private OplusSpringAnimation mThumbScaleAnim;
    private final FloatPropertyCompat<OplusSeekBar> mThumbScaleTransition;
    private VelocityTracker mVelocityTracker;
    private ExecutorService mVibratorExecutor;

    public OplusSeekBar(Context context) {
        this(context, null);
    }

    public OplusSeekBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.oplusSeekBarStyle);
    }

    public OplusSeekBar(Context context, AttributeSet attributeSet, int i2) {
        this(context, attributeSet, i2, R.style.Widget_Oplus_SeekBar);
    }

    public OplusSeekBar(Context context, AttributeSet attributeSet, int i2, int i3) {
        super(context, attributeSet, i2, i3);
        mSeekBarRoleDescription = "Slider";
        mDrawProgressScale = SCALE_MIN;
        mScale = SCALE_MIN;
        mEnableVibrator = true;
        mEnableAdaptiveVibrator = true;
        mHasMotorVibrator = true;
        mTouchSlop = 0;
        mProgress = 0;
        mOldProgress = 0;
        mMax = 100;
        mMin = 0;
        mIsDragging = false;
        mProgressColorStateList = null;
        mBackgroundColorStateList = null;
        mThumbColorStateList = null;
        mBackgroundRect = new RectF();
        mProgressRect = new RectF();
        mShowProgress = false;
        mShowThumb = false;
        mBackgroundPath = new Path();
        mProgressPath = new Path();
        mIncrement = 1;
        mStartDragging = false;
        mMoveType = 1;
        mFastMoveSpringConfig = SpringConfig.fromOrigamiTensionAndFriction(500.0d, 30.0d);
        mIsStartFromMiddle = false;
        mDamping = 0.0f;
        mIsPhysicsEnable = false;
        mFlingVelocity = 0.0f;
        mFlingFrequency = 2.8f;
        mFlingDampingRatio = 1.0f;
        mFlingLinearDamping = 15.0f;
        mMaxMovingDistance = 30;
        mMaxHeightDeformedValue = 28.5f;
        mMaxWidthDeformedValue = 4.7f;
        mThumbScaleTransition = new FloatPropertyCompat<>("thumbScaleTransition") {

            @Override
            public float getValue(OplusSeekBar OplusSeekBar) {
                return OplusSeekBar.getCurThumbRadius();
            }

            @Override
            public void setValue(OplusSeekBar OplusSeekBar, float f2) {
                OplusSeekBar.setCurThumbRadius(f2);
            }
        };
        mGlitterEffectTransition = new FloatPropertyCompat<>("glitterEffectTransition") {
            @Override
            public float getValue(OplusSeekBar OplusSeekBar) {
                return OplusSeekBar.getCurGlitterEffectValue();
            }

            @Override
            public void setValue(OplusSeekBar OplusSeekBar, float f2) {
                OplusSeekBar.setCurGlitterEffectValue(f2);
            }
        };
        if (attributeSet != null) {
            mRefreshStyle = attributeSet.getStyleAttribute();
        }
        if (mRefreshStyle == 0) {
            mRefreshStyle = i2;
        }
        setForceDarkAllowed(false);
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.OplusSeekBar, i2, i3);
        mEnableVibrator = typedArrayObtainStyledAttributes.getBoolean(R.styleable.OplusSeekBar_oplusSeekBarEnableVibrator, true);
        mEnableAdaptiveVibrator = typedArrayObtainStyledAttributes.getBoolean(R.styleable.OplusSeekBar_oplusSeekBarAdaptiveVibrator, false);
        mIsPhysicsEnable = typedArrayObtainStyledAttributes.getBoolean(R.styleable.OplusSeekBar_oplusSeekBarPhysicsEnable, true);
        mShowProgress = typedArrayObtainStyledAttributes.getBoolean(R.styleable.OplusSeekBar_oplusSeekBarShowProgress, true);
        mShowThumb = typedArrayObtainStyledAttributes.getBoolean(R.styleable.OplusSeekBar_oplusSeekBarShowThumb, true);
        mShowGlitterEffect = typedArrayObtainStyledAttributes.getBoolean(R.styleable.OplusSeekBar_oplusSeekBarShowGlitterEffect, true);
        mIsStartFromMiddle = typedArrayObtainStyledAttributes.getBoolean(R.styleable.OplusSeekBar_oplusSeekBarStartMiddle, false);
        mBackgroundColorStateList = typedArrayObtainStyledAttributes.getColorStateList(R.styleable.OplusSeekBar_oplusSeekBarBackgroundColor);
        mProgressColorStateList = typedArrayObtainStyledAttributes.getColorStateList(R.styleable.OplusSeekBar_oplusSeekBarProgressColor);
        mThumbColorStateList = typedArrayObtainStyledAttributes.getColorStateList(R.styleable.OplusSeekBar_oplusSeekBarThumbColor);
        mBackgroundColor = getColor(this, mBackgroundColorStateList, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_background_selector));
        mProgressColor = getColor(this, mProgressColorStateList, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_progress_selector));
        mThumbColor = getColor(this, mThumbColorStateList, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_thumb_selector));
        mThumbShadowColor = typedArrayObtainStyledAttributes.getColor(R.styleable.OplusSeekBar_oplusSeekBarThumbShadowColor, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_thumb_shadow_color));
        mThumbShadowOffsetY = getResources().getDimension(R.dimen.oplus_seekbar_shadow_offset_y);
        mThumbRadius = getResources().getDimension(R.dimen.oplus_seekbar_thumb_radius);
        mThumbMaxRadius = getResources().getDimension(R.dimen.oplus_seekbar_thumb_max_radius);
        mGlitterEffectMinColor = getResources().getColor(R.color.oplus_seekbar_glitter_effect_min_color);
        mGlitterEffectMaxColor = getResources().getColor(R.color.oplus_seekbar_glitter_effect_max_color);
        mBackgroundRoundCornerWeight = typedArrayObtainStyledAttributes.getFloat(R.styleable.OplusSeekBar_oplusSeekBarBackgroundRoundCornerWeight, 0.0f);
        mProgressRoundCornerWeight = typedArrayObtainStyledAttributes.getFloat(R.styleable.OplusSeekBar_oplusSeekBarProgressRoundCornerWeight, 0.0f);
        mThumbShadowRadiusSize = typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.OplusSeekBar_oplusSeekBarThumbShadowSize, 0);
        mBackgroundHeight = typedArrayObtainStyledAttributes.getDimension(R.styleable.OplusSeekBar_oplusSeekBarBackgroundHeight, getResources().getDimension(R.dimen.oplus_seekbar_background_height));
        mProgressHeight = typedArrayObtainStyledAttributes.getDimension(R.styleable.OplusSeekBar_oplusSeekBarProgressHeight, getResources().getDimension(R.dimen.oplus_seekbar_progress_height));
        mSeekbarMinHeight = typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.OplusSeekBar_oplusSeekBarMinHeight, getResources().getDimensionPixelSize(R.dimen.oplus_seekbar_view_min_height));
        mMaxWidth = typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.OplusSeekBar_oplusSeekBarMaxWidth, 0);
        mBackgroundEnlargeScale = typedArrayObtainStyledAttributes.getFloat(R.styleable.OplusSeekBar_oplusSeekBarBackGroundEnlargeScale, BACKGROUND_RADIUS_SCALE);
        mIsSupportDeformation = typedArrayObtainStyledAttributes.getBoolean(R.styleable.OplusSeekBar_oplusSeekBarDeformation, true);
        typedArrayObtainStyledAttributes.recycle();
        mVibrator = getContext().getSystemService(Vibrator.class);
        mHasMotorVibrator = mVibrator.hasVibrator();
        float f2 = mBackgroundHeight * mBackgroundEnlargeScale;
        mMaxBackgroundHeight = f2;
        mPaddingHorizontal = f2 / 2.0f;
        initView();
        ensureSize();
        initAnim();
    }

    private void attemptClaimDrag() {
        if (getParent() instanceof ViewGroup) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    private boolean bottomDeformedChange() {
        if (!mIsSupportDeformation) {
            return false;
        }
        float heightBottomDeformedValue = getHeightBottomDeformedValue();
        if (mCurBottomDeformationValue == heightBottomDeformedValue) {
            return false;
        }
        mCurBottomDeformationValue = heightBottomDeformedValue;
        return true;
    }

    private float calculateDamping() {
        float f2 = mDamping;
        if (mDamping != 0.0f) {
            return f2;
        }
        return MAX_MOVE_DAMPING;
    }

    private void calculateFlingDeformationValue(float f2) {
        if (f2 > MAX_MOVE_DAMPING) {
            mDeformationAnim.animateToFinalPosition((f2 - MAX_MOVE_DAMPING) * DEFORMATION_SCALE_FACTOR);
        } else if (f2 >= 0.0f) {
            resetDeformationValue();
        } else {
            mDeformationAnim.animateToFinalPosition(Math.abs(f2) * DEFORMATION_SCALE_FACTOR);
        }
    }

    private void clearDeformationValue() {
        if (mScale <= SCALE_MIN || mScale >= MAX_MOVE_DAMPING) {
            return;
        }
        resetDeformationValue();
    }

    private int computeGlitterEffectAlpha(float f2) {
        return (int) Math.round((1.0d - Math.exp((-(Math.log(85.0d) / 360.0d)) * f2)) * 255.0d);
    }

    public float computeValue(double d2, float f2) {
        return (float) (f2 * (1.0d - Math.exp(d2 * (-11.5d))));
    }

    protected void drawProgress(Canvas canvas) {
        if (mShowProgress) {
            mPaint.setColor(mProgressColor);
            if (mProgressRoundCornerWeight == 0.0f) {
                canvas.drawRoundRect(mProgressRect, mProgressHeight / 2.0f, mProgressHeight / 2.0f, mPaint);
            } else {
                drawUniversalSmoothRect(canvas, mProgressRect, mProgressHeight / 2.0f, mPaint);
            }
            mProgressPath.reset();
            canvas.save();
            canvas.clipPath(mProgressPath);
            canvas.drawColor(mProgressColor);
            canvas.restore();
        }
    }

    private void drawThumb(Canvas canvas) {
        if (mShowThumb) {
            int seekBarCenterY = getSeekBarCenterY();
            float thumbDiff = mThumbPosition - mCurThumbRadius;
            float thumbSum = mThumbPosition + mCurThumbRadius;
            if (mThumbShadowRadiusSize > 0 && isEnabled()) {
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setShadowLayer(mThumbShadowRadiusSize, 0.0f, mThumbShadowOffsetY, mThumbShadowColor);
            }
            mPaint.setColor(mThumbColor);
            canvas.drawRoundRect(thumbDiff, seekBarCenterY - mCurThumbRadius, thumbSum, seekBarCenterY + mCurThumbRadius, mCurThumbRadius, mCurThumbRadius, mPaint);
            if (mThumbShadowRadiusSize <= 0 || !isEnabled()) {
                return;
            }
            mPaint.clearShadowLayer();
        }
    }

    private void ensureSize() {
        mCurBackgroundHeight = mBackgroundHeight;
        mCurThumbRadius = mThumbRadius;
        mCurProgressRadius = mProgressHeight / 2.0f;
        mCurPaddingHorizontal = mPaddingHorizontal;
        Log.i(TAG, "OplusSeekBar ensureSize : mPaddingHorizontal:" + mPaddingHorizontal + ",mBackgroundHeight:" + mBackgroundHeight + ",mBackgroundEnlargeScale" + mBackgroundEnlargeScale + ",mProgressHeight:" + mProgressHeight + ",mThumbRadius" + mThumbRadius);
        updateBehavior();
    }

    private void executeFlingGlitterEffectAnim(BaseBehavior baseBehavior, float f2) {
        float fMin = Math.min(Compat.physicalSizeToPixels(baseBehavior.getPropertyBodyVelocity().mX), MAX_VELOCITY);
        if (!mIsStartFromMiddle) {
            if (mScale < SCALE_MAX || mIsBumpingEdges || f2 >= SCALE_MAX) {
                return;
            }
            startGlitterEffectAnim(fMin);
            return;
        }
        if (mScale >= SCALE_MAX && !mIsBumpingEdges && mScale < SCALE_MAX) {
            startGlitterEffectAnim(fMin);
        } else {
            if (mScale > SCALE_MIN || mIsBumpingEdges || mScale <= SCALE_MIN) {
                return;
            }
            startGlitterEffectAnim(fMin);
        }
    }

    private void flingBehaviorAfterDeformationDrag() {
        if (mFlingValueHolder == null || mFlingBehavior == null || !mIsSupportDeformation) {
            return;
        }
        if (mScale > SCALE_MAX || mScale < SCALE_MIN) {
            int seekBarWidth = getSeekBarWidth();
            int i2 = mMax - mMin;
            float f3 = i2 > 0 ? seekBarWidth / i2 : 0.0f;
            if (isLayoutRtl()) {
                mFlingValueHolder.setStartValue((mMax - (getDeformationFlingScale() * i2)) * f3);
            } else {
                mFlingValueHolder.setStartValue(getDeformationFlingScale() * i2 * f3);
            }
            mFlingBehavior.start();
        }
    }

    private void flingBehaviorAfterEndDrag(float f2) {
        if (mFlingValueHolder == null || mFlingBehavior == null) {
            return;
        }
        int seekBarWidth = getSeekBarWidth();
        int i2 = mMax - mMin;
        float f3 = i2 > 0 ? seekBarWidth / i2 : 0.0f;
        if (isLayoutRtl()) {
            if (mIsSupportDeformation) {
                mFlingValueHolder.setStartValue((mMax - (getDeformationFlingScale() * i2)) * f3);
            } else {
                mFlingValueHolder.setStartValue(((mMax - mProgress) + mMin) * f3);
            }
        } else if (mIsSupportDeformation) {
            mFlingValueHolder.setStartValue(getDeformationFlingScale() * i2 * f3);
        } else {
            mFlingValueHolder.setStartValue((mProgress - mMin) * f3);
        }
        mFlingBehavior.start(f2);
    }

    public String formatStateDescription(int i2) {
        Locale locale = getResources().getConfiguration().getLocales().get(0);
        if (locale != null && !locale.equals(mCachedLocale)) {
            mCachedLocale = locale;
            mPercentFormat = NumberFormat.getPercentInstance(locale);
        }
        NumberFormat numberFormat = mPercentFormat;
        return numberFormat != null ? numberFormat.format(getPercent(i2)) : Integer.toString(i2);
    }

    public float getCurGlitterEffectValue() {
        return mCurGlitterEffectValue;
    }

    public void setCurGlitterEffectValue(float f2) {
        mCurGlitterEffectValue = f2;
        mCurGlitterEffectAlpha = computeGlitterEffectAlpha(f2);
        invalidate();
    }

    public float getCurThumbRadius() {
        return mCurThumbRadius;
    }

    public void setCurThumbRadius(float f2) {
        mCurThumbRadius = f2;
        invalidate();
    }

    private float getDeformationFlingScale() {
        return mScale > SCALE_MAX ? ((mScale - SCALE_MAX) / SCALE_DEFORMATION_TIMES) + SCALE_MAX : mScale < SCALE_MIN ? mScale / SCALE_DEFORMATION_TIMES : mScale;
    }

    private ValueAnimator getEnlargeAnimator(long duration, Interpolator interpolator) {
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setDuration(duration);
        valueAnimator.setInterpolator(interpolator);
        valueAnimator.addUpdateListener(this::getEnlargeAnimator);
        return valueAnimator;
    }

    @NonNull
    private Spring getFastMoveSpring() {
        if (mFastMoveSpring == null) {
            initFastMoveAnimation();
        }
        return mFastMoveSpring;
    }

    private float getHeightBottomDeformedValue() {
        float f2;
        float f3;
        if (isLayoutRtl()) {
            f2 = mHeightBottomDeformedDownValue;
            f3 = mHeightBottomDeformedUpValue;
        } else {
            f2 = mHeightBottomDeformedUpValue;
            f3 = mHeightBottomDeformedDownValue;
        }
        return f2 - f3;
    }

    private float getHeightTopDeformedValue() {
        return
                isLayoutRtl() ?
                        mHeightTopDeformedDownValue - mHeightTopDeformedUpValue :
                        mHeightTopDeformedUpValue - mHeightTopDeformedDownValue;
    }

    private float getPercent(int i2) {
        float max = getMax();
        float min = getMin();
        float diff = max - min;
        if (diff <= 0.0f) {
            return 0.0f;
        }
        return Math.max(0.0f, Math.min(1.0f, ((float) i2 - min) / diff));
    }

    private int getProgressLimit(int i2) {
        int diff = mMax - mMin;
        return Math.max(mMin - diff, Math.min(i2, mMax + diff));
    }

    private int getRealProgress(int i2) {
        return Math.max(mMin, Math.min(i2, mMax));
    }

    private float getRealScale(float f2) {
        return Math.max(0.0f, Math.min(f2, 1.0f));
    }

    private ValueAnimator getReleaseAnimator(long duration, Interpolator interpolator) {
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setDuration(duration);
        valueAnimator.setInterpolator(interpolator);
        valueAnimator.addUpdateListener(this::getEnlargeAnimator);
        return valueAnimator;
    }

    public void heightDeformedChanged() {
        if (mOnDeformedListener != null) {
            boolean z2 = topDeformedChange();
            boolean zBottomDeformedChange = bottomDeformedChange();
            if (z2 || zBottomDeformedChange) {
                mOnDeformedListener.onHeightDeformedChanged(mCurTopDeformationValue, mCurBottomDeformationValue);
            }
        }
    }

    private void initAnim() {
        initEnlargeAnim();
        initThumbScaleAnim();
        initGlitterEffectAnim();
        initFlexibleFollowHandAnim();
        initClickAnim();
        initDeformationAnim();
    }

    private void initClickAnim() {
        if (mClickAnim != null) {
            return;
        }
        androidx.dynamicanimation.animation.FloatValueHolder floatValueHolder = new androidx.dynamicanimation.animation.FloatValueHolder(0.0f);
        OplusSpringForce springForce = new OplusSpringForce();
        springForce.setBounce(0.0f);
        springForce.setResponse(CLICK_SPRING_RESPONSE);
        OplusSpringAnimation spring = new OplusSpringAnimation(floatValueHolder).setSpring(springForce);
        mClickAnim = spring;
        spring.addUpdateListener((oplusDynamicAnimation, f2, f3) -> onClickAnimationUpdate(f2));
    }

    private void initDeformationAnim() {
        if (mDeformationAnim != null) {
            return;
        }
        androidx.dynamicanimation.animation.FloatValueHolder floatValueHolder = new androidx.dynamicanimation.animation.FloatValueHolder(0.0f);
        OplusSpringForce oplusSpringForce = new OplusSpringForce();
        oplusSpringForce.setBounce(0.0f);
        oplusSpringForce.setResponse(DEFORMATION_SPRING_RESPONSE);
        OplusSpringAnimation spring = new OplusSpringAnimation(floatValueHolder).setSpring(oplusSpringForce);
        mDeformationAnim = spring;
        spring.addUpdateListener((OplusDynamicAnimation, f2, f3) -> {
            float f4 = f2 / DEFORMATION_SCALE_FACTOR;
            if (mScale > SCALE_MAX) {
                mHeightBottomDeformedUpValue = computeValue(f4, mMaxMovingDistance);
                mHeightTopDeformedUpValue = computeValue(f4, mMaxMovingDistance + mMaxHeightDeformedValue);
                mWidthDeformedValue = computeValue(f4, mMaxWidthDeformedValue);
                heightDeformedChanged();
                invalidate();
                return;
            }
            if (mScale < SCALE_MIN) {
                mHeightTopDeformedDownValue = computeValue(f4, mMaxMovingDistance);
                mHeightBottomDeformedDownValue = computeValue(f4, mMaxMovingDistance + mMaxHeightDeformedValue);
                mWidthDeformedValue = computeValue(f4, mMaxWidthDeformedValue);
                heightDeformedChanged();
                invalidate();
            }
        });
    }

    private void initEnlargeAnim() {
        if (mTouchEnlargeAnimator == null) {
            mTouchEnlargeAnimator = getEnlargeAnimator(TOUCH_ANIMATION_ENLARGE_DURATION, PROGRESS_SCALE_INTERPOLATOR);
        } else {
            cancelAnim(mTouchEnlargeAnimator);
        }
        setEnlargeAnimatorValues(mTouchEnlargeAnimator);
    }

    private void initFastMoveAnimation() {
        if (mFastMoveSpring != null) {
            return;
        }
        Spring springCreateSpring = SpringSystem.create().createSpring();
        mFastMoveSpring = springCreateSpring;
        springCreateSpring.setSpringConfig(mFastMoveSpringConfig);
        mFastMoveSpring.addListener(new SpringListener() {
            @Override
            public void onSpringActivate(Spring spring) {
            }

            @Override
            public void onSpringAtRest(Spring spring) {
            }

            @Override
            public void onSpringEndStateChange(Spring spring) {
            }

            @Override
            public void onSpringUpdate(Spring spring) {
                if (mFastMoveScaleOffsetX != spring.getEndValue()) {
                    if (isEnabled()) {
                        mFastMoveScaleOffsetX = (float) spring.getCurrentValue();
                    } else {
                        mFastMoveScaleOffsetX = 0.0f;
                    }
                    invalidate();
                }
            }
        });
    }

    private void initFlexibleFollowHandAnim() {
        if (mFlexibleFollowHandAnim != null) {
            return;
        }
        androidx.dynamicanimation.animation.FloatValueHolder floatValueHolder = new androidx.dynamicanimation.animation.FloatValueHolder(0.0f);
        OplusSpringForce oplusSpringForce = new OplusSpringForce();
        oplusSpringForce.setBounce(0.0f);
        oplusSpringForce.setResponse(DEFORMATION_SPRING_RESPONSE);
        OplusSpringAnimation spring = new OplusSpringAnimation(floatValueHolder).setSpring(oplusSpringForce);
        mFlexibleFollowHandAnim = spring;
        spring.addUpdateListener((OplusDynamicAnimation, f2, f3) -> {
            mDrawProgressScale = f2 / FLEXIBLE_FOLLOW_HAND_SCALE_FACTOR;
            invalidate();
        });
    }

    private void initGlitterEffectAnim() {
        if (mGlitterEffectAnim != null) {
            return;
        }
        mGlitterEffectAnim = new OplusSpringAnimation(this, mGlitterEffectTransition);
        OplusSpringForce oplusSpringForce = new OplusSpringForce();
        oplusSpringForce.setBounce(0.0f);
        oplusSpringForce.setResponse(GLITTER_EFFECT_SPRING_RESPONSE);
        mGlitterEffectAnim.setSpring(oplusSpringForce);
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void initPhysicsAnimator(Context context) {
        mPhysicalAnimator = PhysicalAnimator.create(context);
        mFlingValueHolder = new FloatValueHolder(0.0f);
        int seekBarWidth = getSeekBarWidth();
        Log.i(TAG, "OplusSeekBar initPhysicsAnimator : setActiveFrame:" + seekBarWidth);
        FlingBehavior flingBehavior = new FlingBehavior(4, 0.0f, (float) seekBarWidth).withProperty(mFlingValueHolder).setSpringProperty(mFlingFrequency, mFlingDampingRatio).applyTo(null);
        mFlingBehavior = flingBehavior;
        flingBehavior.setLinearDamping(mFlingLinearDamping);
        mPhysicalAnimator.addBehavior(mFlingBehavior);
        mPhysicalAnimator.addAnimationListener(mFlingBehavior, this);
        mPhysicalAnimator.addAnimationUpdateListener(mFlingBehavior, this);
    }

    private void initThumbScaleAnim() {
        if (mThumbScaleAnim != null) {
            return;
        }
        mThumbScaleAnim = new OplusSpringAnimation(this, mThumbScaleTransition);
        OplusSpringForce oplusSpringForce = new OplusSpringForce();
        oplusSpringForce.setBounce(0.0f);
        oplusSpringForce.setResponse(0.2f);
        mThumbScaleAnim.setSpring(oplusSpringForce);
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void initView() {
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        ViewCompat.setAccessibilityDelegate(this, new AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(@NonNull View view, @NonNull AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
                super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfoCompat);
                accessibilityNodeInfoCompat.addAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SET_PROGRESS);
                accessibilityNodeInfoCompat.setRangeInfo(AccessibilityNodeInfoCompat.RangeInfoCompat.obtain(1, getMin(), getMax(), getProgress()));
                accessibilityNodeInfoCompat.setRoleDescription(mSeekBarRoleDescription);
                OplusSeekBar OplusSeekBar = OplusSeekBar.this;
                accessibilityNodeInfoCompat.setStateDescription(OplusSeekBar.formatStateDescription(OplusSeekBar.getProgress()));
                if (OplusSeekBar.isEnabled()) {
                    int progress = OplusSeekBar.getProgress();
                    if (progress > OplusSeekBar.getMin()) {
                        accessibilityNodeInfoCompat.addAction(8192);
                    }
                    if (progress < OplusSeekBar.getMax()) {
                        accessibilityNodeInfoCompat.addAction(4096);
                    }
                }
            }

            @Override
            public boolean performAccessibilityAction(@NonNull View view, int i2, Bundle bundle) {
                if (!isEnabled()) {
                    return false;
                }
                if (i2 == 4096) {
                    setProgress(getProgress() + mIncrement, false, true);
                    announceForAccessibility(formatStateDescription(getProgress()));
                    return true;
                }
                if (i2 != 8192) {
                    return super.performAccessibilityAction(view, i2, bundle);
                }
                setProgress(getProgress() - mIncrement, false, true);
                announceForAccessibility(formatStateDescription(getProgress()));
                return true;
            }
        });
        Paint paint = new Paint(1);
        mPaint = paint;
        paint.setDither(true);
        TextPaint textPaint = new TextPaint(1);
        mGlitterEffectPaint = textPaint;
        textPaint.setColor(ViewCompat.MEASURED_STATE_MASK);
    }

    private void invalidateProgress(MotionEvent motionEvent) {
        float x2 = motionEvent.getX();
        setTouchScale(isLayoutRtl() ? (((getWidth() - x2) - getEnd()) - mPaddingHorizontal) / getSeekBarWidth() : ((x2 - getStart()) - mPaddingHorizontal) / getSeekBarWidth(), true);
        mFlexibleFollowHandAnim.animateToFinalPosition(mScale * FLEXIBLE_FOLLOW_HAND_SCALE_FACTOR);
        int progressLimit = getProgressLimit(Math.round((mScale * (getMax() - getMin())) + getMin()));
        int progress = mProgress;
        int realProgress = mRealProgress;
        setLocalProgress(progressLimit);
        if (progress != mProgress) {
            dispatchProgressChanged(mRealProgress, true);
            if (realProgress != mRealProgress) {
                performFeedback();
            }
        }
    }

    private boolean isDeformationFling() {
        if (mIsSupportDeformation) {
            return (mScale > SCALE_MAX || mScale < SCALE_MIN) && (mPhysicalAnimator != null) && mPhysicalAnimator.isAnimatorRunning();
        }
        return false;
    }

    private boolean isMoveFollowHand() {
        return mMoveType != 2;
    }

    private boolean isWithinThumbBounds(float x, float y) {
        int seekBarCenterY = getSeekBarCenterY();
        if (x >= mThumbPosition - mPaddingHorizontal && x <= mThumbPosition + mPaddingHorizontal) {
            return y >= (float) seekBarCenterY - mPaddingHorizontal && y <= (float) seekBarCenterY + mPaddingHorizontal;
        }
        return false;
    }

    public void getEnlargeAnimator(ValueAnimator valueAnimator) {
        getCurAnimatorValues(valueAnimator);
        invalidate();
    }

    private void recycleVelocityTracker() {
        VelocityTracker velocityTracker = mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void setDeformationScale(float f2) {
        if (f2 > SCALE_MAX) {
            f2 = ((f2 - SCALE_MAX) * SCALE_DEFORMATION_TIMES) + SCALE_MAX;
        } else if (f2 < SCALE_MIN) {
            f2 *= SCALE_DEFORMATION_TIMES;
        }
        float fMax = Math.max(SCALE_DEFORMATION_MIN, Math.min(f2, SCALE_DEFORMATION_MAX));
        mScale = fMax;
        mDrawProgressScale = fMax;
    }

    private void setFlingScale(float f2) {
        if (!mIsSupportDeformation) {
            float fMax = Math.max(SCALE_MIN, Math.min(f2, SCALE_MAX));
            mScale = fMax;
            mDrawProgressScale = fMax;
            return;
        }
        calculateFlingDeformationValue(f2);
        setDeformationScale(f2);
        if (mOnDeformedListener != null) {
            DeformedValueBean deformedValueBean = new DeformedValueBean(mHeightBottomDeformedUpValue, mHeightTopDeformedUpValue, mWidthDeformedValue, mHeightBottomDeformedDownValue, mHeightTopDeformedDownValue, mProgress);
            deformedValueBean.setScale(mScale);
            deformedValueBean.setDrawProgressScale(mDrawProgressScale);
            mOnDeformedListener.onScaleChanged(deformedValueBean);
        }
    }

    private void setMaxToMinLinearGradient() {
        if (mMaxToMinLinearGradient == null) {
            mMaxToMinLinearGradient = new LinearGradient(mProgressRect.left, 0.0f, mProgressRect.right, 0.0f, mGlitterEffectMaxColor, mGlitterEffectMinColor, Shader.TileMode.CLAMP);
        }
        mGlitterEffectPaint.setShader(mMaxToMinLinearGradient);
    }

    private void setMinToMaxLinearGradient() {
        if (mMinToMaxLinearGradient == null) {
            mMinToMaxLinearGradient = new LinearGradient(mProgressRect.left, 0.0f, mProgressRect.right, 0.0f, mGlitterEffectMinColor, mGlitterEffectMaxColor, Shader.TileMode.CLAMP);
        }
        mGlitterEffectPaint.setShader(mMinToMaxLinearGradient);
    }

    private void startFastMoveAnimation(float velocity) {
        Spring fastMoveSpring = getFastMoveSpring();
        if (fastMoveSpring.getCurrentValue() == fastMoveSpring.getEndValue()) {
            int diff = mMax - mMin;
            if (velocity >= FAST_MOVE_VELOCITY) {
                if (mProgress > MAX_FAST_MOVE_PERCENT * (float) diff || mProgress < (float) diff * MIN_FAST_MOVE_PERCENT) {
                    return;
                }
                fastMoveSpring.setEndValue(1.0d);
                return;
            }
            if (velocity > -FAST_MOVE_VELOCITY) {
                fastMoveSpring.setEndValue(0.0d);
                return;
            }
            if (mProgress > MAX_FAST_MOVE_PERCENT * (float) diff || mProgress < (float) diff * MIN_FAST_MOVE_PERCENT) {
                return;
            }
            fastMoveSpring.setEndValue(-1.0d);
        }
    }

    private void startGlitterEffectAnim(float f2) {
        mIsBumpingEdges = true;
        mGlitterEffectAnim.setStartValue(mCurGlitterEffectValue);
        mGlitterEffectAnim.animateToFinalPosition(0.0f);
        mGlitterEffectAnim.setStartVelocity(Math.abs(f2));
    }

    private void stopDeformationFling() {
        if (isDeformationFling()) {
            stopPhysicsMove();
        }
    }

    private boolean topDeformedChange() {
        if (!mIsSupportDeformation) {
            return false;
        }
        float heightTopDeformedValue = getHeightTopDeformedValue();
        if (mCurTopDeformationValue == heightTopDeformedValue) {
            return false;
        }
        mCurTopDeformationValue = heightTopDeformedValue;
        return true;
    }

    private void trackTouchEvent(MotionEvent motionEvent) {
        float x2 = motionEvent.getX();
        float deltax = x2 - mLastX;
        int diff = mMax - mMin;
        if (isLayoutRtl()) {
            deltax = -deltax;
        }
        setTouchScale((mProgress / (float) diff) + ((deltax * calculateDamping()) / getSeekBarWidth()), false);
        executeTouchGlitterEffectAnim();
        mFlexibleFollowHandAnim.animateToFinalPosition(mScale * FLEXIBLE_FOLLOW_HAND_SCALE_FACTOR);
        int progressLimit = getProgressLimit(Math.round((mScale * (float) diff) + getMin()));
        int progress = mProgress;
        int realProgress = mRealProgress;
        setLocalProgress(progressLimit);
        if (progress != mProgress) {
            mLastX = x2;
            dispatchProgressChanged(mRealProgress, true);
            if (realProgress != mRealProgress) {
                performFeedback();
            }
        }
        if (mVelocityTracker != null) {
            mVelocityTracker.computeCurrentVelocity(VELOCITY_COMPUTE_TIME);
            startFastMoveAnimation(mVelocityTracker.getXVelocity());
        }
    }

    private void trackTouchEventByFinger(MotionEvent motionEvent) {
        float start;
        int seekBarWidth;
        int iRound = Math.round(((motionEvent.getX() - mLastX) * calculateDamping()) + mLastX);
        if (isLayoutRtl()) {
            start = ((getWidth() - iRound) - getEnd()) - mPaddingHorizontal;
        } else {
            start = (iRound - getStart()) - mPaddingHorizontal;
        }
        seekBarWidth = getSeekBarWidth();
        setTouchScale(start / seekBarWidth, false);
        executeTouchGlitterEffectAnim();
        mFlexibleFollowHandAnim.animateToFinalPosition(mScale * FLEXIBLE_FOLLOW_HAND_SCALE_FACTOR);
        int progressLimit = getProgressLimit(Math.round((mScale * (getMax() - getMin())) + getMin()));
        int progress = mProgress;
        int realProgress = mRealProgress;
        setLocalProgress(progressLimit);
        if (progress != mProgress) {
            mLastX = iRound;
            dispatchProgressChanged(mRealProgress, true);
            if (realProgress != mRealProgress) {
                performFeedback();
            }
        }
    }

    private void updateBehavior() {
        if (!mIsPhysicsEnable || mPhysicalAnimator == null || mFlingBehavior == null) {
            return;
        }
        int seekBarWidth = getSeekBarWidth();
        Log.i(TAG, "OplusSeekBar updateBehavior : setActiveFrame:" + seekBarWidth);
        mFlingBehavior.setActiveFrame(0.0f, (float) seekBarWidth);
    }

    private void updatePixPerProgress() {
        int seekBarWidth = getSeekBarWidth();
        int i2 = mMax - mMin;
        mPixPerProgress = i2 > 0 ? seekBarWidth / i2 : 0.0f;
    }

    private void updateScale() {
        // Calculate the total range between max and min
        int range = mMax - mMin;

        // Calculate progress as a normalized float (0.0 to 1.0)
        // Formula: (currentProgress - minValue) / (maxValue - minValue)
        float progressRatio = (range > 0) ? ((float) (mProgress - mMin) / (float) range) : SCALE_MIN;

        mScale = progressRatio;
        mDrawProgressScale = progressRatio;
    }

    public void animForClick(float f2) {
        float seekBarWidth = getSeekBarWidth();
        float f4 = seekBarWidth + ((mProgressHeight / SCALE_DEFORMATION_MAX) * SCALE_DEFORMATION_MAX);
        float f5 = mPaddingHorizontal - (mProgressHeight / SCALE_DEFORMATION_MAX);
        float width = isLayoutRtl() ? (((getWidth() - f2) - getStart()) - f5) / f4 : ((f2 - getStart()) - f5) / f4;
        clearDeformationValue();
        startTransitionAnim(getProgressLimit(Math.round((width * (getMax() - getMin())) + getMin())), true);
    }

    public void calculateTouchDeformationValue() {
        if (mScale > SCALE_MAX) {
            mDeformationAnim.animateToFinalPosition(((mScale - SCALE_MAX) / SCALE_DEFORMATION_TIMES) * DEFORMATION_SCALE_FACTOR);
        } else if (mScale < SCALE_MIN) {
            mDeformationAnim.animateToFinalPosition((Math.abs(mScale) / SCALE_DEFORMATION_TIMES) * DEFORMATION_SCALE_FACTOR);
        }
    }

    public void cancelAnim(ValueAnimator valueAnimator) {
        if (valueAnimator == null || !valueAnimator.isRunning()) {
            return;
        }
        valueAnimator.cancel();
    }

    public void checkThumbPosChange(int progress) {
        checkThumbPosChange(progress, true, true);
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent motionEvent) {
        return super.dispatchHoverEvent(motionEvent);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        setBackgroundRect();
        setProgressRect();
        super.draw(canvas);
    }

    public void drawActiveTrack(Canvas canvas, float f2) {
        drawProgress(canvas);
        drawGlitterEffect(canvas);
        drawThumb(canvas);
    }

    public void drawGlitterEffect(Canvas canvas) {
        if (mShowGlitterEffect) {
            if (mIsStartFromMiddle) {
                if (isLayoutRtl()) {
                    if (mScale >= SCALE_MAX) {
                        setMaxToMinLinearGradient();
                    } else if (mScale <= SCALE_MIN) {
                        setMinToMaxLinearGradient();
                    }
                } else {
                    if (mScale >= SCALE_MAX) {
                        setMinToMaxLinearGradient();
                    } else if (mScale <= SCALE_MIN) {
                        setMaxToMinLinearGradient();
                    }
                }
            } else if (isLayoutRtl()) {
                if (mScale >= SCALE_MAX) {
                    setMaxToMinLinearGradient();
                }
            } else if (mScale >= SCALE_MAX) {
                setMinToMaxLinearGradient();
            }
            mGlitterEffectPaint.setAlpha(mCurGlitterEffectAlpha);
            canvas.drawRoundRect(mProgressRect, mProgressHeight / SCALE_DEFORMATION_MAX, mProgressHeight / SCALE_DEFORMATION_MAX, mGlitterEffectPaint);
        }
    }

    public void drawInactiveTrack(Canvas canvas) {
        mPaint.setColor(mBackgroundColor);
        if (mBackgroundRoundCornerWeight == 0.0f) {
            canvas.drawRoundRect(mBackgroundRect, mCurBackgroundHeight / SCALE_DEFORMATION_MAX, mCurBackgroundHeight / SCALE_DEFORMATION_MAX, mPaint);
        } else {
            drawUniversalSmoothRect(canvas, mBackgroundRect, mCurBackgroundHeight / SCALE_DEFORMATION_MAX, mPaint);
        }
        mBackgroundPath.reset();
        canvas.save();
        canvas.clipPath(mBackgroundPath);
        canvas.drawColor(mBackgroundColor);
        canvas.restore();
    }

    protected void drawUniversalSmoothRect(Canvas canvas, RectF rect, float radius, Paint paint) {
        Path path = new Path();
        float weight = 1.52f;

        float left = rect.left;
        float top = rect.top;
        float right = rect.right;
        float bottom = rect.bottom;

        path.reset();
        path.moveTo(left + radius, top);
        path.lineTo(right - radius, top);
        path.cubicTo(right - radius / weight, top, right, top + radius / weight, right, top + radius);
        path.lineTo(right, bottom - radius);
        path.cubicTo(right, bottom - radius / weight, right - radius / weight, bottom, right - radius, bottom);
        path.lineTo(left + radius, bottom);
        path.cubicTo(left + radius / weight, bottom, left, bottom - radius / weight, left, bottom - radius);
        path.lineTo(left, top + radius);
        path.cubicTo(left, top + radius / weight, left + radius / weight, top, left + radius, top);
        path.close();

        canvas.drawPath(path, paint);
    }

    public void executeThumbScaleAnim(MotionEvent motionEvent) {
        if (mShowThumb && isWithinThumbBounds(motionEvent.getX(), motionEvent.getY())) {
            mThumbScaleAnim.setStartValue(mCurThumbRadius);
            mThumbScaleAnim.animateToFinalPosition(mThumbMaxRadius);
        }
    }

    public void executeTouchGlitterEffectAnim() {
        mVelocityTracker.computeCurrentVelocity(ONE_SECOND_UNITS, MAX_VELOCITY);
        float xVelocity = mVelocityTracker.getXVelocity();
        if (!mIsStartFromMiddle) {
            if (isLayoutRtl()) {
                if (mScale < SCALE_MAX || mIsBumpingEdges || xVelocity >= 0.0f) {
                    return;
                }
                startGlitterEffectAnim(xVelocity);
                return;
            }
            if (mScale < SCALE_MAX || mIsBumpingEdges || xVelocity <= 0.0f) {
                return;
            }
            startGlitterEffectAnim(xVelocity);
            return;
        }
        if (isLayoutRtl()) {
            if (mScale <= SCALE_MIN && !mIsBumpingEdges && xVelocity > 0.0f) {
                startGlitterEffectAnim(xVelocity);
                return;
            } else {
                if (mScale < SCALE_MAX || mIsBumpingEdges || xVelocity >= 0.0f) {
                    return;
                }
                startGlitterEffectAnim(xVelocity);
                return;
            }
        }
        if (mScale <= SCALE_MIN && !mIsBumpingEdges && xVelocity < 0.0f) {
            startGlitterEffectAnim(xVelocity);
        } else {
            if (mScale < SCALE_MAX || mIsBumpingEdges || xVelocity <= 0.0f) {
                return;
            }
            startGlitterEffectAnim(xVelocity);
        }
    }

    public int getColor(View view, ColorStateList colorStateList, int i2) {
        return colorStateList == null ? i2 : colorStateList.getColorForState(view.getDrawableState(), i2);
    }

    public void getCurAnimatorValues(ValueAnimator valueAnimator) {
        mCurBackgroundHeight = (Float) valueAnimator.getAnimatedValue("backgroundHeight");
    }

    public int getEnd() {
        return getPaddingEnd();
    }

    @Override
    public int getMax() {
        return mMax;
    }

    @Override
    public void setMax(int max) {
        if (max < getMin()) {
            int min = getMin();
            Log.e(TAG, "setMax : the input params is lower than min. (inputMax:" + max + ",mMin:" + mMin + ")");
            max = min;
        }
        if (max != mMax) {
            setLocalMax(max);
            if (mProgress > max) {
                setProgress(max);
            }
        }
        invalidate();
    }

    @Override
    public int getMin() {
        return mMin;
    }

    @Override
    public void setMin(int min) {
        int max = Math.max(min, 0);
        if (min > getMax()) {
            max = getMax();
            Log.e(TAG, "setMin : the input params is greater than max. (inputMin:" + min + ",mMax:" + mMax + ")");
        }
        if (max != mMin) {
            setLocalMin(max);
            if (mProgress < max) {
                setProgress(max);
            }
        }
        invalidate();
    }

    public float getMoveDamping() {
        return mDamping;
    }

    public void setMoveDamping(float f2) {
        mDamping = f2;
    }

    public int getMoveType() {
        return mMoveType;
    }

    public void setMoveType(int i2) {
        mMoveType = i2;
    }

    @Deprecated
    public int getNormalSeekBarWidth() {
        return getSeekBarWidth();
    }

    @Override
    public int getProgress() {
        return mRealProgress;
    }

    @Override
    public void setProgress(int i2) {
        setProgress(i2, false);
    }

    public int getSeekBarCenterY() {
        return getPaddingTop() + (((getHeight() - getPaddingBottom()) - getPaddingTop()) >> 1);
    }

    public int getSeekBarWidth() {
        return (int) (((getWidth() - getStart()) - getEnd()) - (mPaddingHorizontal * 2.0f));
    }

    public int getStart() {
        return getPaddingStart();
    }

    public void handleMotionEventDown(MotionEvent motionEvent) {
        mTouchDownX = motionEvent.getX();
        mLastX = motionEvent.getX();
        mIsBumpingEdges = false;
        executeThumbScaleAnim(motionEvent);
    }

    public void handleMotionEventMove(MotionEvent motionEvent) {
        float seekBarWidth = getSeekBarWidth();
        int i2 = mMax;
        int i3 = mMin;
        int i4 = i2 - i3;
        float f2 = (i4 > 0 ? (mProgress * seekBarWidth) / i4 : 0.0f) + i3;
        if (mIsStartFromMiddle && Float.compare(f2, seekBarWidth / 2.0f) == 0 && Math.abs(motionEvent.getX() - mLastX) < DAMPING_DISTANCE) {
            return;
        }
        if (mIsDragging && mStartDragging) {
            if (mMoveType != MOVE_BY_DEFAULT) {
                if (mMoveType == MOVE_BY_FINGER) {
                    trackTouchEventByFinger(motionEvent);
                    return;
                } else if (mMoveType != MOVE_BY_DISTANCE) {
                    return;
                }
            }
            trackTouchEvent(motionEvent);
            return;
        }
        if (isToucheInSeekBar(motionEvent)) {
            float x2 = motionEvent.getX();
            if (Math.abs(x2 - mTouchDownX) > mTouchSlop) {
                Log.i(TAG, "start drag mScale = " + mScale);
                mClickAnim.cancel();
                stopDeformationFling();
                startDrag();
                touchAnim();
                mLastX = x2;
                mFlexibleFollowHandAnim.setStartValue(mScale * FLEXIBLE_FOLLOW_HAND_SCALE_FACTOR);
                if (isMoveFollowHand()) {
                    invalidateProgress(motionEvent);
                }
            }
        }
    }

    public void handleMotionEventUp(MotionEvent motionEvent) {
        releaseThumbScaleAnim();
        getFastMoveSpring().setEndValue(0.0d);
        if (!mIsDragging) {
            if (isEnabled() && touchInSeekBar(motionEvent, this) && isMoveFollowHand()) {
                stopDeformationFling();
                animForClick(motionEvent.getX());
                return;
            }
            return;
        }
        mIsDragging = false;
        mStartDragging = false;
        Log.i(TAG, "handleMotionEventUp mFlingVelocity = " + mFlingVelocity);
        if (!mIsPhysicsEnable || Math.abs(mFlingVelocity) < PHYSICAL_VELOCITY_LIMIT) {
            if (mScale >= SCALE_MIN && mScale <= SCALE_MAX) {
                dispatchStopTrackingTouch();
            }
            flingBehaviorAfterDeformationDrag();
        } else {
            flingBehaviorAfterEndDrag(mFlingVelocity);
        }
        setPressed(false);
        releaseAnim();
    }

    public boolean isLayoutRtl() {
        return getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    public boolean isToucheInSeekBar(MotionEvent motionEvent) {
        return touchInSeekBar(motionEvent, this);
    }

    @Override
    public void onAnimationCancel(BaseBehavior baseBehavior) {
        onStopTrackingTouch(true);
    }

    @Override
    public void onAnimationEnd(BaseBehavior baseBehavior) {
        dispatchStopTrackingTouch();
    }

    @Override
    public void onAnimationUpdate(BaseBehavior baseBehavior) {
        float flingScale;
        Object animatedValue = baseBehavior.getAnimatedValue();
        if (animatedValue == null) {
            return;
        }
        float fFloatValue = (Float) animatedValue;
        int seekBarWidth = getSeekBarWidth();
        if (isLayoutRtl()) {
            flingScale = (seekBarWidth - fFloatValue) / seekBarWidth;
        } else {
            flingScale = fFloatValue / seekBarWidth;
        }
        setFlingScale(flingScale);
        executeFlingGlitterEffectAnim(baseBehavior, mScale);
        float progress = mProgress;
        setLocalProgress(getProgressLimit(Math.round((mMax - mMin) * mScale) + mMin));
        invalidate();
        if (progress != mProgress) {
            mLastX = fFloatValue + getStart();
            dispatchProgressChanged(mRealProgress, true);
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public void onClickAnimationUpdate(float f2) {
        float f3 = mPixPerProgress;
        if (f3 > 0.0f) {
            setLocalProgress((int) (f2 / f3));
            float seekBarWidth = getSeekBarWidth() > 0 ? (f2 - (mMin * mPixPerProgress)) / getSeekBarWidth() : 0.0f;
            mScale = seekBarWidth;
            mDrawProgressScale = seekBarWidth;
            invalidate();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopPhysicsMove();
    }

    @Override
    public void onDraw(Canvas canvas) {
        drawInactiveTrack(canvas);
        drawActiveTrack(canvas, getSeekBarWidth());
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mode = View.MeasureSpec.getMode(heightMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int paddingTop = mSeekbarMinHeight + getPaddingTop() + getPaddingBottom();
        if (mode != View.MeasureSpec.EXACTLY || height < paddingTop) {
            height = paddingTop;
        }
        if (mMaxWidth > 0 && width > mMaxWidth) {
            width = mMaxWidth;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    public void onRestoreInstanceState(Parcelable parcelable) {
        if (parcelable instanceof SavedState savedState) {
            super.onRestoreInstanceState(savedState.getSuperState());
            setProgress(savedState.mSaveProgress);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.mSaveProgress = mProgress;
        return savedState;
    }

    @Override
    public void onSizeChanged(int i2, int i3, int i4, int i5) {
        super.onSizeChanged(i2, i3, i4, i5);
        mMaxToMinLinearGradient = null;
        mMinToMaxLinearGradient = null;
        mStartDragging = false;
        stopPhysicsMove();
        updateBehavior();
        updatePixPerProgress();
    }

    public void onStartTrackingTouch() {
        onStartTrackingTouch(true);
    }

    public void onStopTrackingTouch() {
        onStopTrackingTouch(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If the widget is disabled, only handle UP/CANCEL actions to ensure
        // internal state is reset, then consume the event or return false.
        if (!isEnabled()) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                handleMotionEventUp(event);
                return true;
            }
            return false;
        }

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // Stop any ongoing physics-based movement unless it's a deformation fling
                if (!isDeformationFling()) {
                    stopPhysicsMove();
                }

                // Initialize the physics engine animator if enabled and not yet created
                if (mIsPhysicsEnable && mPhysicalAnimator == null) {
                    initPhysicsAnimator(getContext());
                }

                // Initialize or reset the velocity tracker to start monitoring this touch gesture
                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(event);

                // Reset dragging state flags
                mIsDragging = false;
                mStartDragging = false;

                handleMotionEventDown(event);
                break;

            case MotionEvent.ACTION_MOVE:
                // Reset visual effects like edge bumping and deformation before processing move
                resetBumpingEdges();
                clearDeformationValue();

                initVelocityTrackerIfNotExists();
                mVelocityTracker.addMovement(event);

                handleMotionEventMove(event);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Cancel any active spring animations (click feedback or hand-following)
                mClickAnim.cancel();
                mFlexibleFollowHandAnim.cancel();

                // Compute the final velocity to determine if a fling gesture occurred
                if (mVelocityTracker != null) {
                    // 1000ms units, with a max velocity of 8000 pixels per second
                    mVelocityTracker.computeCurrentVelocity(ONE_SECOND_UNITS, MAX_VELOCITY);
                    mFlingVelocity = mVelocityTracker.getXVelocity();

                    Log.i("OplusSeekBar", "onTouchEvent ACTION_UP mFlingVelocity = " + mFlingVelocity);
                }

                // Clean up the velocity tracker and handle the final touch-up logic
                recycleVelocityTracker();
                handleMotionEventUp(event);
                break;

            default:
                break;
        }

        return true;
    }

    public boolean performAdaptiveFeedback() {
        if (mVibrator == null) {
            VibratorManager vibratorManager = (VibratorManager) getContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            mVibrator = vibratorManager != null ? vibratorManager.getDefaultVibrator() : null;
            mHasMotorVibrator = mVibrator != null && mVibrator.hasVibrator();
        }

        if (mVibrator == null || !mHasMotorVibrator) {
            return false;
        }

        if (mRealProgress == getMax() || mRealProgress == getMin()) {
            mVibrator.vibrate(VibrationEffect.createPredefined(EFFECT_HEAVY_CLICK));
        } else {
            if (mVibratorExecutor == null) {
                mVibratorExecutor = Executors.newSingleThreadExecutor();
            }
            mVibratorExecutor.execute(() -> {
                if (mIsDragging) {
                    mVibrator.vibrate(VibrationEffect.createPredefined(EFFECT_TICK));
                }
            });
        }
        return true;
    }

    public void performFeedback() {
        if (!mEnableVibrator) return;

        if (mHasMotorVibrator && mEnableAdaptiveVibrator && performAdaptiveFeedback()) {
            return;
        }

        if (mRealProgress == getMax() || mRealProgress == getMin()) {
            performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            return;
        }

        if (mIsDragging) {
            performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
        }
    }

    public void refresh() throws Resources.NotFoundException {
        String resourceTypeName = getResources().getResourceTypeName(mRefreshStyle);
        TypedArray typedArray = null;
        if (TextUtils.equals(resourceTypeName, "attr")) {
            typedArray = getContext().getTheme().obtainStyledAttributes(null, R.styleable.OplusSeekBar, mRefreshStyle, 0);
        } else if (TextUtils.equals(resourceTypeName, "style")) {
            typedArray = getContext().getTheme().obtainStyledAttributes(null, R.styleable.OplusSeekBar, 0, mRefreshStyle);
        }
        if (typedArray != null) {
            mProgressColor = getColor(this, typedArray.getColorStateList(R.styleable.OplusSeekBar_oplusSeekBarProgressColor), ContextCompat.getColor(getContext(), R.color.oplus_seekbar_progress_color_normal));
            mBackgroundColor = getColor(this, typedArray.getColorStateList(R.styleable.OplusSeekBar_oplusSeekBarBackgroundColor), ContextCompat.getColor(getContext(), R.color.oplus_seekbar_background_color_normal));
            mThumbColor = getColor(this, typedArray.getColorStateList(R.styleable.OplusSeekBar_oplusSeekBarThumbColor), ContextCompat.getColor(getContext(), R.color.oplus_seekbar_thumb_selector));
            mThumbShadowColor = typedArray.getColor(R.styleable.OplusSeekBar_oplusSeekBarThumbShadowColor, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_thumb_shadow_color));
            invalidate();
            typedArray.recycle();
        }
    }

    public void releaseAnim() {
        cancelAnim(mTouchEnlargeAnimator);
        if (mTouchReleaseAnimator == null) {
            mTouchReleaseAnimator = getReleaseAnimator(TOUCH_ANIMATION_ENLARGE_DURATION, PROGRESS_SCALE_INTERPOLATOR);
        } else {
            cancelAnim(mTouchReleaseAnimator);
        }
        setReleaseAnimatorValues(mTouchReleaseAnimator);
        mTouchReleaseAnimator.start();
    }

    public void releaseThumbScaleAnim() {
        if (mShowThumb) {
            float f2 = mCurThumbRadius;
            if (f2 != mThumbRadius) {
                mThumbScaleAnim.setStartValue(f2);
                mThumbScaleAnim.animateToFinalPosition(mThumbRadius);
            }
        }
    }

    public void resetBumpingEdges() {
        if (!mIsStartFromMiddle) {
            if (mScale < SCALE_MAX) {
                mIsBumpingEdges = false;
            }
        } else {
            if (mScale >= SCALE_MAX || mScale <= SCALE_MIN) {
                return;
            }
            mIsBumpingEdges = false;
        }
    }

    public void resetDeformationValue() {
        if (mIsSupportDeformation) {
            mHeightTopDeformedUpValue = 0.0f;
            mHeightBottomDeformedUpValue = 0.0f;
            mWidthDeformedValue = 0.0f;
            mHeightTopDeformedDownValue = 0.0f;
            mHeightBottomDeformedDownValue = 0.0f;
            heightDeformedChanged();
        }
    }

    @Deprecated
    public void setBackgroundEnlargeScale(float f2) {
    }

    @Deprecated
    public void setBackgroundHeight(float f2) {
    }

    @Deprecated
    public void setBackgroundRadius(float f2) {
    }

    public void setBackgroundRect() {
        int seekBarCenterY = getSeekBarCenterY();
        float start = (getStart() + mPaddingHorizontal) - (mCurBackgroundHeight / 2.0f);
        float width = ((getWidth() - getEnd()) - mPaddingHorizontal) + (mCurBackgroundHeight / 2.0f);
        if (isLayoutRtl()) {
            float f2 = (start - mHeightTopDeformedUpValue) + mHeightTopDeformedDownValue;
            mBackgroundRect.set(f2, (float) seekBarCenterY - ((mCurBackgroundHeight / 2.0f) - mWidthDeformedValue), (width - mHeightBottomDeformedUpValue) + mHeightBottomDeformedDownValue, (float) seekBarCenterY + ((mCurBackgroundHeight / 2.0f) - mWidthDeformedValue));
            return;
        }
        float f6 = (start - mHeightBottomDeformedDownValue) + mHeightBottomDeformedUpValue;
        mBackgroundRect.set(f6, seekBarCenterY - ((mCurBackgroundHeight / 2.0f) - mWidthDeformedValue), (width + mHeightTopDeformedUpValue) - mHeightTopDeformedDownValue, seekBarCenterY + ((mCurBackgroundHeight / 2.0f) - mWidthDeformedValue));
    }

    public void setBackgroundRoundCornerWeight(float f2) {
        mBackgroundRoundCornerWeight = f2;
        invalidate();
    }

    @Deprecated
    public void setCustomProgressAnimDuration(float f2) {
    }

    @Deprecated
    public void setCustomProgressAnimInterpolator(Interpolator interpolator) {
    }

    public void setDeformedListener(OnDeformedListener onDeformedListener) {
        mOnDeformedListener = onDeformedListener;
    }

    public void setDeformedParams(DeformedValueBean deformedValueBean) {
        mScale = deformedValueBean.getScale();
        mDrawProgressScale = deformedValueBean.getDrawProgressScale();
        mProgress = (int) deformedValueBean.getProgress();
        mHeightBottomDeformedUpValue = deformedValueBean.getHeightBottomDeformedUpValue();
        mHeightTopDeformedUpValue = deformedValueBean.getHeightTopDeformedUpValue();
        mWidthDeformedValue = deformedValueBean.getWidthDeformedValue();
        mHeightBottomDeformedDownValue = deformedValueBean.getHeightBottomDeformedDownValue();
        mHeightTopDeformedDownValue = deformedValueBean.getHeightTopDeformedDownValue();
        invalidate();
    }

    public void setEnableAdaptiveVibrator(boolean z2) {
        mEnableAdaptiveVibrator = z2;
    }

    public void setEnableVibrator(boolean z2) {
        mEnableVibrator = z2;
    }

    @Override
    public void setEnabled(boolean z2) {
        super.setEnabled(z2);
        mProgressColor = getColor(this, mProgressColorStateList, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_progress_selector));
        mBackgroundColor = getColor(this, mBackgroundColorStateList, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_background_selector));
        mThumbColor = getColor(this, mThumbColorStateList, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_thumb_selector));
    }

    public void setEnlargeAnimatorValues(ValueAnimator valueAnimator) {
        valueAnimator.setValues(PropertyValuesHolder.ofFloat("backgroundHeight", mBackgroundHeight, mMaxBackgroundHeight));
    }

    public void setFlingLinearDamping(float f2) {
        FlingBehavior flingBehavior;
        if (mIsPhysicsEnable) {
            mFlingLinearDamping = f2;
            if (mPhysicalAnimator == null || (flingBehavior = mFlingBehavior) == null) {
                return;
            }
            flingBehavior.setLinearDamping(f2);
        }
    }

    public void setFlingProperty(float f2, float f3) {
        FlingBehavior flingBehavior;
        if (mIsPhysicsEnable) {
            mFlingFrequency = f2;
            mFlingDampingRatio = f3;
            if (mPhysicalAnimator == null || (flingBehavior = mFlingBehavior) == null) {
                return;
            }
            flingBehavior.setSpringProperty(f2, f3);
        }
    }

    public void setIncrement(int i2) {
        mIncrement = Math.abs(i2);
    }

    @Override
    @Deprecated
    public void setInterpolator(Interpolator interpolator) {
    }

    public void setLocalMax(int i2) {
        mMax = i2;
        updatePixPerProgress();
        updateScale();
        super.setMax(i2);
    }

    public void setLocalMin(int i2) {
        mMin = i2;
        updatePixPerProgress();
        updateScale();
        super.setMin(i2);
    }

    public void setLocalProgress(int i2) {
        mProgress = i2;
        mRealProgress = getRealProgress(i2);
        super.setProgress(i2);
    }

    public void setMaxHeightDeformed(float f2) {
        mMaxHeightDeformedValue = f2;
    }

    public void setMaxMovingDistance(int i2) {
        mMaxMovingDistance = i2;
    }

    public void setMaxWidthDeformed(float f2) {
        mMaxWidthDeformedValue = f2;
    }

    /**
     * Add a listener to be notified of changes to the seek bar's progress.
     *
     * @param onSeekBarChangeListener The {@link OnSeekBarChangeListener} to add.
     */
    public void addOnSeekBarChangeListener(OnSeekBarChangeListener onSeekBarChangeListener) {
        if (onSeekBarChangeListener != null) {
            mOnSeekBarChangeListeners.add(onSeekBarChangeListener);
        }
    }

    /**
     * Remove a listener that was previously added with {@link #addOnSeekBarChangeListener}.
     *
     * @param onSeekBarChangeListener The {@link OnSeekBarChangeListener} to remove.
     */
    public void removeOnSeekBarChangeListener(OnSeekBarChangeListener onSeekBarChangeListener) {
        mOnSeekBarChangeListeners.remove(onSeekBarChangeListener);
    }

    /**
     * Clear all listeners that were added with {@link #addOnSeekBarChangeListener}.
     */
    public void clearListeners() {
        mOnSeekBarChangeListeners.clear();
    }

    private void dispatchProgressChanged(int realProgress, boolean fromUser) {
        for (OnSeekBarChangeListener onSeekBarChangeListener : mOnSeekBarChangeListeners) {
            onSeekBarChangeListener.onProgressChanged(this, realProgress, fromUser);
        }
    }

    private void dispatchStopTrackingTouch() {
        for (OnSeekBarChangeListener onSeekBarChangeListener : mOnSeekBarChangeListeners) {
            onSeekBarChangeListener.onStopTrackingTouch(this);
        }
    }

    private void dispatchStartTrackingTouch() {
        for (OnSeekBarChangeListener onSeekBarChangeListener : mOnSeekBarChangeListeners) {
            onSeekBarChangeListener.onStartTrackingTouch(this);
        }
    }

    @Deprecated
    public void setPaddingHorizontal(float f2) {
    }

    public void setPhysicalEnabled(boolean z2) {
        if (z2 == mIsPhysicsEnable) {
            return;
        }
        if (z2) {
            mIsPhysicsEnable = true;
            updateBehavior();
        } else {
            stopPhysicsMove();
            mIsPhysicsEnable = false;
        }
    }

    public void setProgressColor(@NonNull ColorStateList colorStateList) {
        mProgressColorStateList = colorStateList;
        mProgressColor = getColor(this, colorStateList, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_progress_selector));
        invalidate();
    }

    @Deprecated
    public void setProgressContentDescription(String str) {
    }

    @Deprecated
    public void setProgressEnlargeScale(float f2) {
    }

    @Deprecated
    public void setProgressFull() {
    }

    @Deprecated
    public void setProgressHeight(float f2) {
    }

    @Deprecated
    public void setProgressRadius(float f2) {
    }

    public void setProgressRect() {
        float start;
        float realScale;
        float start2;
        float realScale2;
        float f2;
        float f3;
        float seekBarWidth = getSeekBarWidth();
        int seekBarCenterY = getSeekBarCenterY();
        if (mIsStartFromMiddle) {
            if (isLayoutRtl()) {
                start2 = getWidth() / SCALE_DEFORMATION_MAX;
                realScale2 = start2 - ((getRealScale(mDrawProgressScale) - 0.5f) * seekBarWidth);
                f2 = start2;
                f3 = realScale2;
            } else {
                start = getWidth() / SCALE_DEFORMATION_MAX;
                realScale = start + ((getRealScale(mDrawProgressScale) - 0.5f) * seekBarWidth);
                f2 = realScale;
                realScale2 = start;
                f3 = f2;
            }
        } else if (isLayoutRtl()) {
            start2 = getStart() + mPaddingHorizontal + seekBarWidth;
            realScale2 = start2 - (getRealScale(mDrawProgressScale) * seekBarWidth);
            f2 = start2;
            f3 = realScale2;
        } else {
            start = getStart() + mPaddingHorizontal;
            realScale = start + (getRealScale(mDrawProgressScale) * seekBarWidth);
            f2 = realScale;
            realScale2 = start;
            f3 = f2;
        }
        if (!mIsStartFromMiddle || realScale2 <= f2) {
            if (isLayoutRtl()) {
                float f4 = realScale2 - mHeightTopDeformedUpValue;
                mProgressRect.set(f4 + mHeightBottomDeformedDownValue, seekBarCenterY - ((mProgressHeight / SCALE_DEFORMATION_MAX) - mWidthDeformedValue), (f2 - mHeightBottomDeformedUpValue) + mHeightBottomDeformedDownValue, seekBarCenterY + ((mProgressHeight / SCALE_DEFORMATION_MAX) - mWidthDeformedValue));
            } else {
                float f10 = (realScale2 - mHeightBottomDeformedDownValue) + mHeightBottomDeformedUpValue;
                mProgressRect.set(f10, seekBarCenterY - ((mProgressHeight / 2.0f) - mWidthDeformedValue), (f2 + mHeightTopDeformedUpValue) - mHeightBottomDeformedDownValue, seekBarCenterY + ((mProgressHeight / SCALE_DEFORMATION_MAX) - mWidthDeformedValue));
            }
        } else if (isLayoutRtl()) {
            float f14 = f2 - mHeightTopDeformedUpValue;
            mProgressRect.set(f14 + mHeightBottomDeformedDownValue, seekBarCenterY - ((mProgressHeight / SCALE_DEFORMATION_MAX) - mWidthDeformedValue), (realScale2 - mHeightBottomDeformedUpValue) + mHeightBottomDeformedDownValue, seekBarCenterY + ((mProgressHeight / SCALE_DEFORMATION_MAX) - mWidthDeformedValue));
        } else {
            float f20 = (f2 - mHeightBottomDeformedDownValue) + mHeightBottomDeformedUpValue;
            mProgressRect.set(f20, seekBarCenterY - ((mProgressHeight / SCALE_DEFORMATION_MAX) - mWidthDeformedValue), (realScale2 + mHeightTopDeformedUpValue) - mHeightBottomDeformedDownValue, seekBarCenterY + ((mProgressHeight / SCALE_DEFORMATION_MAX) - mWidthDeformedValue));
        }
        mProgressRect.left = mProgressRect.left - (mProgressHeight / SCALE_DEFORMATION_MAX);
        mProgressRect.right += mProgressHeight / SCALE_DEFORMATION_MAX;
        float f26 = mHeightTopDeformedUpValue - mHeightBottomDeformedDownValue;
        if (isLayoutRtl()) {
            f26 = -f26;
        }
        mThumbPosition = f3 + f26;
    }

    public void setProgressRoundCornerWeight(float f2) {
        mProgressRoundCornerWeight = f2;
        ensureSize();
        invalidate();
    }

    public void setReleaseAnimatorValues(ValueAnimator valueAnimator) {
        valueAnimator.setValues(PropertyValuesHolder.ofFloat("backgroundHeight", mCurBackgroundHeight, mBackgroundHeight));
    }

    public void setSeekBarBackgroundColor(@NonNull ColorStateList colorStateList) {
        mBackgroundColorStateList = colorStateList;
        mBackgroundColor = getColor(this, colorStateList, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_background_selector));
        invalidate();
    }

    public void setStartFromMiddle(boolean z2) {
        mIsStartFromMiddle = z2;
    }

    public void setSupportDeformation(boolean z2) {
        mIsSupportDeformation = z2;
    }

    @Deprecated
    public void setText(String str) {
    }

    public void setThumbColor(@NonNull ColorStateList colorStateList) {
        mThumbColorStateList = colorStateList;
        mThumbColor = getColor(this, colorStateList, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_thumb_selector));
        invalidate();
    }

    public void setTouchScale(float f2, boolean z2) {
        if (!mIsSupportDeformation) {
            if (!z2) {
                mScale = Math.max(SCALE_MIN, Math.min(f2, SCALE_MAX));
                return;
            }
            float fMax = Math.max(SCALE_MIN, Math.min(f2, SCALE_MAX));
            mScale = fMax;
            mDrawProgressScale = fMax;
            return;
        }
        if (z2) {
            float fMax2 = Math.max(SCALE_DEFORMATION_MIN, Math.min(f2, SCALE_DEFORMATION_MAX));
            mScale = fMax2;
            mDrawProgressScale = fMax2;
        } else {
            mScale = Math.max(SCALE_DEFORMATION_MIN, Math.min(f2, SCALE_DEFORMATION_MAX));
        }
        calculateTouchDeformationValue();
        if (mOnDeformedListener != null) {
            DeformedValueBean deformedValueBean = new DeformedValueBean(mHeightBottomDeformedUpValue, mHeightTopDeformedUpValue, mWidthDeformedValue, mHeightBottomDeformedDownValue, mHeightTopDeformedDownValue, mProgress);
            deformedValueBean.setScale(mScale);
            deformedValueBean.setDrawProgressScale(mDrawProgressScale);
            mOnDeformedListener.onScaleChanged(deformedValueBean);
        }
    }

    public void startDrag() {
        setPressed(true);
        onStartTrackingTouch(true);
        attemptClaimDrag();
    }

    public void startTransitionAnim(int i2, final boolean z2) {
        OplusDynamicAnimation.OnAnimationEndListener onAnimationEndListener = (OplusDynamicAnimation, z3, f2, f3) -> {
            dispatchProgressChanged(mRealProgress, z2);
            onStopTrackingTouch(z2);
        };
        float max = (mScale * (getMax() - getMin())) + getMin();
        mClickAnim.cancel();
        if (mLastEndClickListener != null) {
            mClickAnim.removeEndListener(mLastEndClickListener);
        }
        mClickAnim.addEndListener(onAnimationEndListener);
        mClickAnim.setStartValue(max * mPixPerProgress);
        onStartTrackingTouch(z2);
        mClickAnim.animateToFinalPosition(i2 * mPixPerProgress);
        mLastEndClickListener = onAnimationEndListener;
    }

    public void stopPhysicsMove() {
        if (!mIsPhysicsEnable || mPhysicalAnimator == null || mFlingBehavior == null) {
            return;
        }
        mFlingBehavior.stop();
    }

    public float subtract(float f2, float f3) {
        return new BigDecimal(Float.toString(f2)).subtract(new BigDecimal(Float.toString(f3))).floatValue();
    }

    public void touchAnim() {
        cancelAnim(mTouchEnlargeAnimator);
        mTouchEnlargeAnimator.start();
    }

    public boolean touchInSeekBar(MotionEvent motionEvent, View view) {
        float y2 = motionEvent.getY();
        return mTouchDownX >= ((float) view.getPaddingStart()) && mTouchDownX <= ((float) (view.getWidth() - view.getPaddingEnd())) && y2 >= 0.0f && y2 <= ((float) view.getHeight());
    }

    public void checkThumbPosChange(int i2, boolean z2, boolean fromUser) {
        if (mProgress != i2) {
            int realProgress = mRealProgress;
            setLocalProgress(i2);
            dispatchProgressChanged(mRealProgress, fromUser);
            if (!z2 || realProgress == mRealProgress) {
                return;
            }
            performFeedback();
        }
    }

    public void onStartTrackingTouch(boolean z2) {
        mIsDragging = true;
        mStartDragging = true;
        if (!z2) {
            return;
        }
        dispatchStartTrackingTouch();
    }

    public void onStopTrackingTouch(boolean z2) {
        mIsDragging = false;
        mStartDragging = false;
        if (!z2 || mOnSeekBarChangeListeners.isEmpty()) {
            return;
        }
        dispatchStopTrackingTouch();
    }

    @Override
    public void setProgress(int i2, boolean z2) {
        setProgress(i2, z2, false);
    }

    public void setProgress(int i2, boolean z2, boolean z3) {
        if (mFlexibleFollowHandAnim != null) {
            mFlexibleFollowHandAnim.cancel();
        }
        mOldProgress = mProgress;
        int iMax = Math.max(mMin, Math.min(i2, mMax));
        if (mOldProgress != iMax) {
            if (z2) {
                startTransitionAnim(iMax, z3);
            } else {
                setLocalProgress(iMax);
                mOldProgress = iMax;
                updateScale();
                dispatchProgressChanged(getRealProgress(iMax), z3);
                invalidate();
            }
            resetDeformationValue();
        }
    }

    public interface OnDeformedListener {
        default void onHeightDeformedChanged(float f2, float f3) {
        }

        default void onScaleChanged(DeformedValueBean deformedValueBean) {
        }
    }

    public interface OnSeekBarChangeListener {
        void onProgressChanged(OplusSeekBar OplusSeekBar, int i2, boolean z2);

        void onStartTrackingTouch(OplusSeekBar OplusSeekBar);

        void onStopTrackingTouch(OplusSeekBar OplusSeekBar);
    }

    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<>() {

            @Override
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            @Override
            public SavedState[] newArray(int i2) {
                return new SavedState[i2];
            }
        };
        int mSaveProgress;

        public SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        private SavedState(Parcel parcel) {
            super(parcel);
            mSaveProgress = parcel.readInt();
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            super.writeToParcel(parcel, flags);
            parcel.writeInt(mSaveProgress);
        }
    }
}
