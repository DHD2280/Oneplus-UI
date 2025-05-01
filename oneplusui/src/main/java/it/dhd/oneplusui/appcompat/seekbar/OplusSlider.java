package it.dhd.oneplusui.appcompat.seekbar;

import static android.os.VibrationEffect.EFFECT_HEAVY_CLICK;
import static android.os.VibrationEffect.EFFECT_TICK;
import static android.view.accessibility.AccessibilityManager.FLAG_CONTENT_CONTROLS;
import static android.view.accessibility.AccessibilityManager.FLAG_CONTENT_TEXT;
import static android.view.accessibility.AccessibilityNodeInfo.RangeInfo.RANGE_TYPE_FLOAT;
import static androidx.core.math.MathUtils.clamp;
import static java.math.MathContext.DECIMAL64;
import static it.dhd.oneplusui.appcompat.seekbar.LabelFormatter.LABEL_FLOATING;
import static it.dhd.oneplusui.appcompat.seekbar.LabelFormatter.LABEL_GONE;
import static it.dhd.oneplusui.appcompat.seekbar.LabelFormatter.LABEL_VISIBLE;
import static it.dhd.oneplusui.appcompat.seekbar.LabelFormatter.LABEL_WITHIN_BOUNDS;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.SeekBar;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.animation.PathInterpolatorCompat;
import androidx.customview.widget.ExploreByTouchHelper;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.google.android.material.internal.DescendantOffsetUtils;
import com.google.android.material.internal.ViewOverlayImpl;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.motion.MotionUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.dhd.oneplusui.R;
import it.dhd.oneplusui.appcompat.animation.OplusEaseInterpolator;
import it.dhd.oneplusui.appcompat.animation.OplusMoveEaseInterpolator;
import it.dhd.oneplusui.physicsengine.engine.AnimationListener;
import it.dhd.oneplusui.physicsengine.engine.AnimationUpdateListener;
import it.dhd.oneplusui.physicsengine.engine.BaseBehavior;
import it.dhd.oneplusui.physicsengine.engine.ConstraintBehavior;
import it.dhd.oneplusui.physicsengine.engine.FlingBehavior;
import it.dhd.oneplusui.physicsengine.engine.FloatValueHolder;
import it.dhd.oneplusui.physicsengine.engine.PhysicalAnimator;

public class OplusSlider extends View implements AnimationListener, AnimationUpdateListener {

    private static final String TAG = "OplusSlider";
    // Move Type
    public static final int MOVE_BY_DEFAULT = 0;
    public static final int MOVE_BY_DISTANCE = 2;
    public static final int MOVE_BY_FINGER = 1;

    public final static double DOUBLE_EPSILON = Double.longBitsToDouble(1);
    protected static final int RELEASE_ANIM_DURATION = 183;

    protected static final Interpolator THUMB_ANIMATE_INTERPOLATOR = new OplusMoveEaseInterpolator();
    protected static final Interpolator PROGRESS_SCALE_INTERPOLATOR = new OplusEaseInterpolator();

    static final int UNIT_VALUE = 1;
    static final int UNIT_PX = 0;

    private static final float BACKGROUND_RADIUS_SCALE = 6.0f;
    private static final int DAMPING_DISTANCE = 20;
    private static final int DURATION_150 = 150;
    private static final int DURATION_483 = 483;
    private static final int FAST_MOVE_VELOCITY = 95;
    private static final float MAX_FAST_MOVE_PERCENT = 0.95f;
    private static final float MAX_MOVE_DAMPING = 0.4f;
    private static final int MAX_VELOCITY = 8000;
    private static final float MIN_FAST_MOVE_PERCENT = 0.05f;
    private static final int ONE_SECOND_UNITS = 1000;
    private static final int PHYSICAL_VELOCITY_LIMIT = 100;
    private static final float PROGRESS_RADIUS_SCALE = 4.0f;
    private static final float SCALE_DEFORMATION_MAX = 2.0f;
    private static final float SCALE_DEFORMATION_MIN = -1.0f;
    private static final int SCALE_DEFORMATION_TIMES = 5;
    private static final float SCALE_MAX = 1.0f;
    private static final float SCALE_MIN = 0.0f;
    private static final float TEXT_SHADOW_DX = 0.0f;
    private static final float TEXT_SHADOW_DY = 8.0f;
    @SuppressWarnings("unused")
    private static final int THUMB_SHADOW_OFFSET = 8;
    private static final int TOUCH_ANIMATION_ENLARGE_DURATION = 183;
    @SuppressWarnings("unused")
    private static final int VELOCITY_COMPUTE_TIME = 100;
    private static final String EXCEPTION_ILLEGAL_VALUE =
            "Slider value(%s) must be greater or equal to valueFrom(%s), and lower or equal to"
                    + " valueTo(%s)";
    private static final String EXCEPTION_ILLEGAL_DISCRETE_VALUE =
            "Value(%s) must be equal to valueFrom(%s) plus a multiple of stepSize(%s) when using"
                    + " stepSize(%s)";
    private static final String EXCEPTION_ILLEGAL_VALUE_FROM =
            "valueFrom(%s) must be smaller than valueTo(%s)";
    private static final String EXCEPTION_ILLEGAL_STEP_SIZE =
            "The stepSize(%s) must be 0, or a factor of the valueFrom(%s)-valueTo(%s) range";
    private static final String EXCEPTION_ILLEGAL_MIN_SEPARATION =
            "minSeparation(%s) must be greater or equal to 0";
    private static final String EXCEPTION_ILLEGAL_MIN_SEPARATION_STEP_SIZE_UNIT =
            "minSeparation(%s) cannot be set as a dimension when using stepSize(%s)";
    private static final String EXCEPTION_ILLEGAL_MIN_SEPARATION_STEP_SIZE =
            "minSeparation(%s) must be greater or equal and a multiple of stepSize(%s) when using"
                    + " stepSize(%s)";
    private static final String WARNING_FLOATING_POINT_ERROR =
            "Floating point value used for %s(%s). Using floats can have rounding errors which may"
                    + " result in incorrect values. Instead, consider using integers with a custom"
                    + " LabelFormatter to display the value correctly.";
    private static final String WARNING_PARSE_ERROR =
            "Error parsing value(%s), valueFrom(%s), and valueTo(%s) into a float.";
    private static final int DEFAULT_LABEL_ANIMATION_ENTER_DURATION = 83;
    private static final int DEFAULT_LABEL_ANIMATION_EXIT_DURATION = 117;
    private static final int LABEL_ANIMATION_ENTER_DURATION_ATTR = com.google.android.material.R.attr.motionDurationMedium4;
    private static final int LABEL_ANIMATION_EXIT_DURATION_ATTR = com.google.android.material.R.attr.motionDurationShort3;
    private static final int LABEL_ANIMATION_ENTER_EASING_ATTR =
            com.google.android.material.R.attr.motionEasingEmphasizedInterpolator;
    private static final int LABEL_ANIMATION_EXIT_EASING_ATTR =
            com.google.android.material.R.attr.motionEasingEmphasizedAccelerateInterpolator;
    private static final float TOP_LABEL_PIVOT_X = 0.5f;
    private static final float TOP_LABEL_PIVOT_Y = 1.2f;
    private static final float RIGHT_LABEL_PIVOT_X = -0.2f;
    private static final float RIGHT_LABEL_PIVOT_Y = 0.5f;
    // The index of the currently focused thumb.
    private static final int MIN_TIMEOUT_TOOLTIP_WITH_ACCESSIBILITY = 10000;
    private static final int TIMEOUT_SEND_ACCESSIBILITY_EVENT = 200;
    private static final double THRESHOLD = .0001;
    private final RectF mBackgroundRect;
    private final AccessibilityManager accessibilityManager;
    private final int tooltipTimeoutMillis;
    @NonNull
    private final Rect labelRect = new Rect();
    @NonNull
    private final List<TooltipDrawable> labels = new ArrayList<>();
    protected int mBackgroundColor;
    protected float mBackgroundEnlargeScale;
    protected float mBackgroundHeight;
    protected float mBackgroundRadius;
    protected float mBackgroundRoundCornerWeight;
    protected AnimatorSet mClickAnimatorSet;
    protected Path mClipProgressPath;
    protected RectF mClipProgressRect;
    protected float mCurBackgroundHeight;
    protected float mCurBackgroundRadius;
    protected float mCurPaddingHorizontal;
    protected float mCurProgressHeight;
    protected float mCurProgressRadius;
    protected boolean mEnableAdaptiveVibrator;
    protected boolean mEnableVibrator;
    protected Vibrator mVibrator;
    protected boolean mHasMotorVibrator;
    protected float mHorizontalPaddingScale;
    protected boolean mIsDragging;
    protected float mLastX;
    protected float mOldProgress;
    protected float mPaddingHorizontal;
    protected Paint mPaint;
    protected float mProgress;
    protected int mProgressColor;
    protected float mProgressEnlargeScale;
    protected float mProgressHeight;
    protected float mProgressRadius;
    protected RectF mProgressRect;
    protected float mProgressRoundCornerWeight;
    protected Interpolator mProgressScaleInterpolator;
    protected float mScale;
    protected RectF mTempRect;
    protected Interpolator mThumbAnimateInterpolator;
    protected int mThumbColor;
    protected float mThumbOutHeight;
    protected float mThumbOutRadius;
    protected float mThumbOutRoundCornerWeight;
    protected int mThumbShadowColor;
    protected AnimatorSet mTouchAnimator;
    protected float mTouchDownX;
    protected int mTouchSlop;
    ColorStateList mBackgroundColorStateList;
    ColorStateList mProgressColorStateList;
    ColorStateList mThumbColorStateList;
    // Whether the labels are showing or in the process of animating in.
    private boolean labelsAreAnimatedIn = false;
    private ValueAnimator labelsInAnimator;
    private ValueAnimator labelsOutAnimator;
    private float mCurBottomDeformationValue;
    private float mCurTopDeformationValue;
    private Interpolator mCustomProgressAnimInterpolator;
    private float mDamping;
    private AccessibilityHelper mAccessibilityHelper;
    private AccessibilityEventSender accessibilityEventSender;
    private float mFastMoveScaleOffsetX;
    private Spring mFastMoveSpring;
    private final SpringConfig mFastMoveSpringConfig;
    private FlingBehavior mFlingBehavior;
    private float mFlingDampingRatio;
    private float mFlingFrequency;
    private float mFlingLinearDamping;
    private FloatValueHolder mFlingValueHolder;
    private float mFlingVelocity;
    private float mHeightBottomDeformedDownValue;
    private float mHeightBottomDeformedUpValue;
    private float mHeightTopDeformedDownValue;
    private float mHeightTopDeformedUpValue;
    private final int mInnerShadowRadiusSize;
    private Interpolator mInterpolator;
    private boolean mIsPhysicsEnable;
    private boolean mIsSupportDeformation;
    private int labelStyle;
    private float mMaxHeightDeformedValue;
    private int mMaxMovingDistance;
    private float mMaxWidthDeformedValue;
    private int mMoveType;
    private OnDeformedListener mOnDeformedListener;
    private List<OnSliderChangeListener> mOnSliderChangeListeners = new ArrayList<>();
    private PhysicalAnimator mPhysicalAnimator;
    private float mRealProgress;
    private int mRefreshStyle;
    private final int mSeekbarMinHeight;
    private final int mShadowColor;
    private final int mShadowRadiusSize;
    private boolean mStartDragging;
    private int mThumbShadowRadiusSize;
    private VelocityTracker mVelocityTracker;
    private ExecutorService mVibratorExecutor;
    private float mWidthDeformedValue;
    private float valueFrom;
    private float valueTo;
    private LabelFormatter formatter;
    // Holds the values set to this mOplusSlider. We keep this array sorted in order to check if the value
    // has been changed when a new value is set and to find the minimum and maximum values.
    private ArrayList<Float> values = new ArrayList<>();
    // The index of the currently touched thumb.
    private int activeThumbIdx = -1;
    @NonNull
    private final Runnable resetActiveThumbIndex =
            () -> {
                setActiveThumbIndex(-1);
                invalidate();
            };
    private int focusedThumbIdx = -1;
    private float stepSize = 0.0f;
    private boolean isLongPress = false;
    private boolean dirtyConfig;
    private boolean thumbIsPressed = false;
    private int labelBehavior = LABEL_VISIBLE;
    @NonNull
    private final ViewTreeObserver.OnScrollChangedListener onScrollChangedListener =
            this::updateLabels;
    @NonNull
    private final ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = this::updateLabels;
    @SeparationUnit
    private final int separationUnit = UNIT_PX;

    public OplusSlider(Context context) {
        this(context, null);
    }

    public OplusSlider(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.oplusSliderStyle);
    }

    public OplusSlider(Context context, AttributeSet attributeSet, int defStyleAttr) {
        this(context, attributeSet, defStyleAttr, R.style.Widget_Oplus_Slider);
    }

    public OplusSlider(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);
        mScale = 0.0f;
        mEnableVibrator = true;
        mEnableAdaptiveVibrator = true;
        mHasMotorVibrator = true;
        mVibrator = null;
        mTouchSlop = 0;
        mProgress = 0;
        mOldProgress = 0;
        mIsDragging = false;
        mProgressColorStateList = null;
        mBackgroundColorStateList = null;
        mThumbColorStateList = null;
        mCustomProgressAnimInterpolator = null;
        mClipProgressPath = new Path();
        mClipProgressRect = new RectF();
        mProgressRect = new RectF();
        mTempRect = new RectF();
        mTouchAnimator = new AnimatorSet();
        mProgressScaleInterpolator = PathInterpolatorCompat.create(0.33f, 0.0f, 0.67f, 1.0f);
        mThumbAnimateInterpolator = PathInterpolatorCompat.create(0.3f, 0.0f, 0.1f, 1.0f);
        mStartDragging = false;
        mBackgroundRect = new RectF();
        mMoveType = MOVE_BY_FINGER;
        mFastMoveSpringConfig = SpringConfig.fromOrigamiTensionAndFriction(500.0d, 30.0d);
        mDamping = 0.0f;
        mInterpolator = PathInterpolatorCompat.create(0.3f, 0.0f, 0.1f, 1.0f);
        mIsPhysicsEnable = false;
        mFlingVelocity = 0.0f;
        mFlingFrequency = 2.8f;
        mFlingDampingRatio = 1.0f;
        mFlingLinearDamping = 15.0f;
        mMaxMovingDistance = 30;
        mMaxHeightDeformedValue = 28.5f;
        mMaxWidthDeformedValue = 4.7f;
        if (attributeSet != null) {
            mRefreshStyle = attributeSet.getStyleAttribute();
        }
        if (mRefreshStyle == 0) {
            mRefreshStyle = defStyleAttr;
        }
        setForceDarkAllowed(false);
        TypedArray attributes = context.obtainStyledAttributes(attributeSet, R.styleable.OplusSlider, defStyleAttr, defStyleRes);
        mEnableVibrator = attributes.getBoolean(R.styleable.OplusSlider_oplusSeekBarEnableVibrator, true);
        mEnableAdaptiveVibrator = attributes.getBoolean(R.styleable.OplusSlider_oplusSeekBarAdaptiveVibrator, false);
        mIsPhysicsEnable = attributes.getBoolean(R.styleable.OplusSlider_oplusSeekBarPhysicsEnable, true);
        mBackgroundColorStateList = attributes.getColorStateList(R.styleable.OplusSlider_oplusSeekBarBackgroundColor);
        mProgressColorStateList = attributes.getColorStateList(R.styleable.OplusSlider_oplusSeekBarProgressColor);
        mThumbColorStateList = attributes.getColorStateList(R.styleable.OplusSlider_oplusSeekBarThumbColor);
        mBackgroundColor = getColor(this, mBackgroundColorStateList, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_background_color_normal));
        ColorStateList colorStateList = mProgressColorStateList;
        Context context2 = getContext();
        int i4 = R.color.oplus_seekbar_progress_color_normal;
        mProgressColor = getColor(this, colorStateList, ContextCompat.getColor(context2, i4));
        mThumbColor = getColor(this, mThumbColorStateList, ContextCompat.getColor(getContext(), i4));
        mShadowColor = attributes.getColor(R.styleable.OplusSlider_oplusSeekBarShadowColor, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_shadow_color));
        mThumbShadowColor = attributes.getColor(R.styleable.OplusSlider_oplusSeekBarThumbShadowColor, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_thumb_shadow_color));
        mBackgroundRadius = attributes.getDimension(R.styleable.OplusSlider_oplusSeekBarBackgroundRadius, getResources().getDimension(R.dimen.oplus_seekbar_background_radius));
        mProgressRadius = attributes.getDimension(R.styleable.OplusSlider_oplusSeekBarProgressRadius, getResources().getDimension(R.dimen.oplus_seekbar_progress_radius));
        mBackgroundRoundCornerWeight = attributes.getFloat(R.styleable.OplusSlider_oplusSeekBarBackgroundRoundCornerWeight, 0.0f);
        mProgressRoundCornerWeight = attributes.getFloat(R.styleable.OplusSlider_oplusSeekBarProgressRoundCornerWeight, 0.0f);
        mShadowRadiusSize = attributes.getDimensionPixelSize(R.styleable.OplusSlider_oplusSeekBarShadowSize, 0);
        mThumbShadowRadiusSize = attributes.getDimensionPixelSize(R.styleable.OplusSlider_oplusSeekBarThumbShadowSize, 0);
        mInnerShadowRadiusSize = attributes.getDimensionPixelSize(R.styleable.OplusSlider_oplusSeekBarInnerShadowSize, 0);
        mPaddingHorizontal = attributes.getDimensionPixelOffset(R.styleable.OplusSlider_oplusSeekBarProgressPaddingHorizontal, getResources().getDimensionPixelSize(R.dimen.oplus_seekbar_progress_padding_horizontal));
        mBackgroundHeight = attributes.getDimensionPixelSize(R.styleable.OplusSlider_oplusSeekBarBackgroundHeight, (int) (mBackgroundRadius * SCALE_DEFORMATION_MAX));
        mProgressHeight = attributes.getDimensionPixelSize(R.styleable.OplusSlider_oplusSeekBarProgressHeight, (int) (mProgressRadius * SCALE_DEFORMATION_MAX));
        mSeekbarMinHeight = attributes.getDimensionPixelOffset(R.styleable.OplusSlider_oplusSeekBarMinHeight, getResources().getDimensionPixelSize(R.dimen.oplus_seekbar_view_min_height));
        mBackgroundEnlargeScale = attributes.getFloat(R.styleable.OplusSlider_oplusSeekBarBackGroundEnlargeScale, BACKGROUND_RADIUS_SCALE);
        mProgressEnlargeScale = attributes.getFloat(R.styleable.OplusSlider_oplusSeekBarProgressEnlargeScale, PROGRESS_RADIUS_SCALE);
        mIsSupportDeformation = attributes.getBoolean(R.styleable.OplusSlider_oplusSeekBarDeformation, false);
        labelStyle = attributes.getResourceId(R.styleable.OplusSlider_oplusLabelStyle, R.style.Widget_Oplus_Tooltip);
        attributes.recycle();
        mVibrator = getContext().getSystemService(Vibrator.class);
        mHasMotorVibrator = mVibrator.hasVibrator();
        accessibilityManager =
                (AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        tooltipTimeoutMillis =
                accessibilityManager.getRecommendedTimeoutMillis(
                        MIN_TIMEOUT_TOOLTIP_WITH_ACCESSIBILITY, FLAG_CONTENT_CONTROLS | FLAG_CONTENT_TEXT);
        initView();
        ensureSize();
        initAnimation();
    }

    /**
     * A helper method to get the current animated value of a {@link ValueAnimator}. If the target
     * animator is null or not running, return the default value provided.
     */
    private static float getAnimatorCurrentValueOrDefault(
            ValueAnimator animator, float defaultValue) {
        // If the in animation is interrupting the out animation, attempt to smoothly interrupt by
        // getting the current value of the out animator.
        if (animator != null && animator.isRunning()) {
            float value = (float) animator.getAnimatedValue();
            animator.cancel();
            return value;
        }

        return defaultValue;
    }

    public void scheduleTooltipTimeout() {
        removeCallbacks(resetActiveThumbIndex);
        postDelayed(resetActiveThumbIndex, tooltipTimeoutMillis);
    }

    protected void setActiveThumbIndex(int index) {
        if (values.size() == 1) {
            activeThumbIdx = 0;
            return;
        }
        activeThumbIdx = index;
    }

    /**
     * Schedule a command for sending an accessibility event. </br> Note: A command is used to ensure
     * that accessibility events are sent at most one in a given time frame to save system resources
     * while the value changes quickly.
     */
    private void scheduleAccessibilityEventSender(int idx) {
        if (accessibilityEventSender == null) {
            accessibilityEventSender = new AccessibilityEventSender();
        } else {
            removeCallbacks(accessibilityEventSender);
        }
        accessibilityEventSender.setVirtualViewId(idx);
        postDelayed(accessibilityEventSender, TIMEOUT_SEND_ACCESSIBILITY_EVENT);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SliderState sliderState = new SliderState(superState);
        sliderState.valueFrom = valueFrom;
        sliderState.valueTo = valueTo;
        sliderState.values = new ArrayList<>(values);
        sliderState.stepSize = stepSize;
        sliderState.hasFocus = hasFocus();
        return sliderState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SliderState sliderState = (SliderState) state;
        super.onRestoreInstanceState(sliderState.getSuperState());

        valueFrom = sliderState.valueFrom;
        valueTo = sliderState.valueTo;
        setValuesInternal(sliderState.values);
        stepSize = sliderState.stepSize;
        if (sliderState.hasFocus) {
            requestFocus();
        }
    }

    void updateBoundsForVirtualViewId(int virtualViewId, Rect virtualViewBounds) {
        int x = (int) (mCurPaddingHorizontal + (int) (normalizeValue(getValues().get(virtualViewId)) * mThumbOutRadius));
        int y = getSeekBarCenterY();
        int touchTargetOffsetX = (int) Math.max(mThumbOutRadius / 2, 48 / 2);
        int touchTargetOffsetY = (int) Math.max(mThumbOutRadius / 2, 48 / 2);
        RectF rect =
                new RectF(
                        x - touchTargetOffsetX,
                        y - touchTargetOffsetY,
                        x + touchTargetOffsetX,
                        y + touchTargetOffsetY);
        virtualViewBounds.set((int) rect.left, (int) rect.top, (int) rect.right, (int) rect.bottom);
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

    private float calculateDamping(float f2) {
        if (mDamping != 0.0f) {
            return mDamping;
        }
        float seekBarWidth = getSeekBarWidth();
        float f4 = seekBarWidth / SCALE_DEFORMATION_MAX;
        float interpolation = 1.0f - mInterpolator.getInterpolation(Math.abs(f2 - f4) / f4);
        if (f2 > seekBarWidth - getPaddingRight() || f2 < getPaddingLeft() || interpolation < MAX_MOVE_DAMPING) {
            return MAX_MOVE_DAMPING;
        }
        return interpolation;
    }

    private void calculateFlingDeformationValue(float flingScale) {
        if (flingScale > 1.0f) {
            double d2 = flingScale - 1.0f;
            mHeightBottomDeformedUpValue = computeValue(d2, mMaxMovingDistance);
            mHeightTopDeformedUpValue = computeValue(d2, mMaxMovingDistance + mMaxHeightDeformedValue);
            mWidthDeformedValue = computeValue(d2, mMaxWidthDeformedValue);
            heightDeformedChanged();
            return;
        }
        if (flingScale >= 0.0f) {
            resetDeformationValue();
            return;
        }
        double abs = Math.abs(flingScale);
        mHeightTopDeformedDownValue = computeValue(abs, mMaxMovingDistance);
        mHeightBottomDeformedDownValue = computeValue(abs, mMaxMovingDistance + mMaxHeightDeformedValue);
        mWidthDeformedValue = computeValue(abs, mMaxWidthDeformedValue);
        heightDeformedChanged();
    }

    private void calculateTouchDeformationValue() {
        if (mScale > SCALE_MAX) {
            double d2 = (mScale - SCALE_MAX) / SCALE_DEFORMATION_TIMES;
            mHeightBottomDeformedUpValue = computeValue(d2, mMaxMovingDistance);
            mHeightTopDeformedUpValue = computeValue(d2, mMaxMovingDistance + mMaxHeightDeformedValue);
            mWidthDeformedValue = computeValue(d2, mMaxWidthDeformedValue);
            heightDeformedChanged();
            return;
        }
        if (mScale < SCALE_MIN) {
            double abs = Math.abs(mScale) / SCALE_DEFORMATION_TIMES;
            mHeightTopDeformedDownValue = computeValue(abs, mMaxMovingDistance);
            mHeightBottomDeformedDownValue = computeValue(abs, mMaxMovingDistance + mMaxHeightDeformedValue);
            mWidthDeformedValue = computeValue(abs, mMaxWidthDeformedValue);
            heightDeformedChanged();
        }
    }

    private void clearDeformationValue() {
        float i2 = mProgress;
        if (i2 <= valueFrom || i2 >= valueTo) {
            return;
        }
        resetDeformationValue();
    }

    private float computeValue(double d2, float f2) {
        return (float) (f2 * (1.0d - Math.exp(d2 * (-11.5d))));
    }

    private void drawProgress(Canvas canvas, int i2, float f2, float f3) {
        if (mInnerShadowRadiusSize > 0 && mCurProgressRadius > mProgressRadius) {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(0.0f);
            mPaint.setColor(0);
            mPaint.setShadowLayer(mInnerShadowRadiusSize, 0.0f, 0.0f, mShadowColor);
            mProgressRect.set(
                    (f2 - ((float) mInnerShadowRadiusSize / 2)) - mCurProgressRadius,
                    ((float) i2 - (mCurProgressHeight / SCALE_DEFORMATION_MAX)) - ((float) mInnerShadowRadiusSize / 2),
                    ((float) mInnerShadowRadiusSize / 2) + f3 + mCurProgressRadius,
                    (float) i2 + (mCurProgressHeight / SCALE_DEFORMATION_MAX) + ((float) mInnerShadowRadiusSize / 2));
            canvas.drawRoundRect(mProgressRect, mCurProgressRadius, mCurProgressRadius, mPaint);
            mPaint.clearShadowLayer();
            mPaint.setStyle(Paint.Style.FILL);
        }
        mPaint.setColor(mProgressColor);
        if (isLayoutRtl()) {
            float f11 = f2 - mHeightTopDeformedUpValue;
            mProgressRect.set(
                    f11 + mHeightBottomDeformedDownValue,
                    (float) i2 - ((mCurProgressHeight / SCALE_DEFORMATION_MAX) - mWidthDeformedValue),
                    (f3 - mHeightBottomDeformedUpValue) + mHeightBottomDeformedDownValue,
                    (float) i2 + ((mCurProgressHeight / SCALE_DEFORMATION_MAX) - mWidthDeformedValue));
        } else {
            float f17 = (f2 - mHeightBottomDeformedDownValue) + mHeightBottomDeformedUpValue;
            mProgressRect.set(
                    f17,
                    (float) i2 - ((mCurProgressHeight / SCALE_DEFORMATION_MAX) - mWidthDeformedValue),
                    (f3 + mHeightTopDeformedUpValue) - mHeightBottomDeformedDownValue,
                    (float) i2 + ((mCurProgressHeight / SCALE_DEFORMATION_MAX) - mWidthDeformedValue));
        }
        mClipProgressPath.reset();
        mClipProgressPath.addRoundRect(mClipProgressRect, mCurProgressRadius, mCurProgressRadius, Path.Direction.CCW);
        canvas.save();
        canvas.clipPath(mClipProgressPath);
        float left = mProgressRect.left;
        mProgressRect.left = left - (mThumbOutHeight / SCALE_DEFORMATION_MAX);
        mProgressRect.right += mThumbOutHeight / SCALE_DEFORMATION_MAX;
        canvas.drawRoundRect(mProgressRect, mCurProgressRadius, mCurProgressRadius, mPaint);
        canvas.restore();
    }

    private void drawThumb(Canvas canvas, int center, float left, float right) {
        if (mThumbShadowRadiusSize > 0 && mCurProgressRadius < mThumbOutRadius) {
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setShadowLayer(mThumbShadowRadiusSize, TEXT_SHADOW_DX, TEXT_SHADOW_DY, mShadowColor);
        }
        mPaint.setColor(mThumbColor);
        canvas.drawRoundRect(
                left,
                (float) center - (mThumbOutHeight / SCALE_DEFORMATION_MAX),
                right,
                (float) center + (mThumbOutHeight / SCALE_DEFORMATION_MAX), mThumbOutRadius, mThumbOutRadius, mPaint);
        mPaint.clearShadowLayer();
    }

    private void ensureSize() {
        mHorizontalPaddingScale = mBackgroundEnlargeScale != 1.0f ? (getResources().getDimensionPixelSize(R.dimen.oplus_seekbar_progress_pressed_padding_horizontal) + (mBackgroundRadius * mBackgroundEnlargeScale)) / mPaddingHorizontal : 1.0f;
        mCurProgressRadius = mProgressRadius;
        mCurBackgroundRadius = mBackgroundRadius;
        mThumbOutRadius = mProgressRadius * mProgressEnlargeScale;
        mThumbOutRoundCornerWeight = mProgressRoundCornerWeight;
        mCurProgressHeight = mProgressHeight;
        mCurBackgroundHeight = mBackgroundHeight;
        mThumbOutHeight = mProgressHeight * mProgressEnlargeScale;
        mCurPaddingHorizontal = mPaddingHorizontal;
        updateBehavior();
    }

    private void flingBehaviorAfterDeformationDrag() {
        if (mIsSupportDeformation) {
            if (mScale > 1.0f || mScale < 0.0f) {
                int normalSeekBarWidth = getNormalSeekBarWidth();
                float diff = valueTo - valueFrom;
                float f3 = diff > 0 ? normalSeekBarWidth / diff : 0.0f;
                if (isLayoutRtl()) {
                    mFlingValueHolder.setStartValue((valueTo - (getDeformationFlingScale() * diff)) * f3);
                } else {
                    mFlingValueHolder.setStartValue(getDeformationFlingScale() * diff * f3);
                }
                mFlingBehavior.start();
            }
        }
    }

    private void flingBehaviorAfterEndDrag(float f2) {
        int normalSeekBarWidth = getNormalSeekBarWidth();
        float i2 = valueTo - valueFrom;
        float f3 = i2 > 0 ? (float) normalSeekBarWidth / i2 : 0.0f;
        if (isLayoutRtl()) {
            if (mIsSupportDeformation) {
                mFlingValueHolder.setStartValue((valueTo - (getDeformationFlingScale() * i2)) * f3);
            } else {
                float prog =
                        activeThumbIdx == -1 ?
                                mProgress :
                                values.get(activeThumbIdx);
                mFlingValueHolder.setStartValue(((valueTo - prog) + valueFrom) * f3);
            }
        } else if (mIsSupportDeformation) {
            mFlingValueHolder.setStartValue(getDeformationFlingScale() * i2 * f3);
        } else {
            mFlingValueHolder.setStartValue((mProgress - valueFrom) * f3);
        }
        mFlingBehavior.start(f2);
        invalidate();
    }

    private float getDeformationFlingScale() {
        return
                mScale > SCALE_MAX ?
                        ((mScale - SCALE_MAX) / SCALE_DEFORMATION_TIMES) + SCALE_MAX :
                        mScale < SCALE_MIN ?
                                mScale / SCALE_DEFORMATION_TIMES :
                                mScale;
    }

    @NonNull
    private Spring getFastMoveSpring() {
        if (mFastMoveSpring == null) {
            initFastMoveAnimation();
        }
        return mFastMoveSpring;
    }

    private float getHeightBottomDeformedValue() {
        return
                isLayoutRtl() ?
                    mHeightBottomDeformedDownValue - mHeightBottomDeformedUpValue :
                    mHeightBottomDeformedUpValue - mHeightBottomDeformedDownValue;
    }

    private float getHeightTopDeformedValue() {
        return
                isLayoutRtl() ?
                        mHeightTopDeformedDownValue - mHeightTopDeformedUpValue :
                        mHeightTopDeformedUpValue - mHeightTopDeformedDownValue;
    }

    private int getNormalSeekBarWidth() {
        return (int) (((getWidth() - getStart()) - getEnd()) - (mPaddingHorizontal * SCALE_DEFORMATION_MAX));
    }

    public float getProgressLimit(float value) {
        float totalProgress = valueTo - valueFrom;
        float limitedValue = Math.max(valueFrom - totalProgress, Math.min(value, valueTo + totalProgress));

        if (stepSize > 0) {
            limitedValue = valueFrom +
                    (Math.round((limitedValue - valueFrom) / stepSize) * stepSize);
        }
        return limitedValue;
    }

    private float getRealProgress(float currentValue) {
        return Math.max(valueFrom, Math.min(currentValue, valueTo));
    }

    private void heightDeformedChanged() {
        if (mOnDeformedListener != null) {
            if (topDeformedChange() || bottomDeformedChange()) {
                mOnDeformedListener.onHeightDeformedChanged(mCurTopDeformationValue, mCurBottomDeformationValue);
            }
        }
    }

    private void initAnimation() {
        mTouchAnimator.setInterpolator(PROGRESS_SCALE_INTERPOLATOR);
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setDuration(TOUCH_ANIMATION_ENLARGE_DURATION);
        ofFloat.addUpdateListener(valueAnimator -> {
            onEnlargeAnimationUpdate(valueAnimator);
            invalidate();
        });
        mTouchAnimator.play(ofFloat);
    }

    private void initFastMoveAnimation() {
        if (mFastMoveSpring != null) {
            return;
        }
        Spring createSpring = SpringSystem.create().createSpring();
        mFastMoveSpring = createSpring;
        createSpring.setSpringConfig(mFastMoveSpringConfig);
        mFastMoveSpring.addListener(new SpringListener() {
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

            @Override
            public void onSpringActivate(Spring spring) {}

            @Override
            public void onSpringAtRest(Spring spring) {}

            @Override
            public void onSpringEndStateChange(Spring spring) {}
        });
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
        int normalSeekBarWidth = getNormalSeekBarWidth();
        mFlingBehavior = new FlingBehavior(ConstraintBehavior.COLLISION_MODE_SIMPLE_LIMIT, 0.0f, (float) normalSeekBarWidth).withProperty(mFlingValueHolder).setSpringProperty(mFlingFrequency, mFlingDampingRatio).applyTo(null);
        mFlingBehavior.setLinearDamping(mFlingLinearDamping);
        mPhysicalAnimator.addBehavior(mFlingBehavior);
        mPhysicalAnimator.addAnimationListener(mFlingBehavior, this);
        mPhysicalAnimator.addAnimationUpdateListener(mFlingBehavior, this);
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void initView() {
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        AccessibilityHelper patternExploreByTouchHelper = new AccessibilityHelper(this);
        mAccessibilityHelper = patternExploreByTouchHelper;
        ViewCompat.setAccessibilityDelegate(this, patternExploreByTouchHelper);
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        mAccessibilityHelper.invalidateRoot();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    private void invalidateProgress(MotionEvent motionEvent) {
        float x2 = motionEvent.getX();
        float seekBarWidth = getSeekBarWidth();
        float f3 = seekBarWidth + (SCALE_DEFORMATION_MAX * mCurProgressRadius);
        float f4 = mCurPaddingHorizontal - mCurProgressRadius;
        mScale = Math.max(0.0f, Math.min(isLayoutRtl() ? (((getWidth() - x2) - getStart()) - f4) / f3 : ((x2 - getStart()) - f4) / f3, 1.0f));
        float progressLimit = getProgressLimit(Math.round((mScale * (valueTo - valueFrom)) + valueFrom));
        float i2 = values.get(activeThumbIdx);
        float i3 = mRealProgress;
        setLocalProgress(progressLimit);
        invalidate();
        if (i2 != mProgress) {
            dispatchChangeListener(true);
            if (i3 != mRealProgress) {
                performFeedback();
            }
        }
    }

    private boolean isDeformationFling() {
        PhysicalAnimator physicalAnimator;
        if (mIsSupportDeformation) {
            float f2 = mScale;
            return (f2 > 1.0f || f2 < 0.0f) && (physicalAnimator = mPhysicalAnimator) != null && physicalAnimator.isAnimatorRunning();
        }
        return false;
    }

    private boolean isMoveFollowHand() {
        return mMoveType != MOVE_BY_DISTANCE;
    }

    private boolean isTouchInSeekBar(MotionEvent motionEvent) {
        if (mIsSupportDeformation) {
            float f2 = mScale;
            if (f2 > 1.0f || f2 < 0.0f) {
                return touchInSeekBarWhenDeformation(motionEvent, this);
            }
        }
        return touchInSeekBar(motionEvent, this);
    }

    private void recycleVelocityTracker() {
        VelocityTracker velocityTracker = mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void resetDeformationValue() {
        if (mIsSupportDeformation) {
            mHeightTopDeformedUpValue = 0.0f;
            mHeightBottomDeformedUpValue = 0.0f;
            mWidthDeformedValue = 0.0f;
            mHeightTopDeformedDownValue = 0.0f;
            mHeightBottomDeformedDownValue = 0.0f;
            heightDeformedChanged();
        }
    }

    private void setDeformationScale(float flingScale) {
        if (flingScale > 1.0f) {
            flingScale = ((flingScale - 1.0f) * 5.0f) + 1.0f;
        } else if (flingScale < 0.0f) {
            flingScale *= 5.0f;
        }
        mScale = Math.max(SCALE_DEFORMATION_MIN, Math.min(flingScale, SCALE_DEFORMATION_MAX));
    }

    private void setFlingScale(float flingScale) {
        if (!mIsSupportDeformation) {
            mScale = Math.max(0.0f, Math.min(flingScale, 1.0f));
            return;
        }
        calculateFlingDeformationValue(flingScale);
        setDeformationScale(flingScale);
        if (mOnDeformedListener != null) {
            DeformedValueBean deformedValueBean = new DeformedValueBean(mHeightBottomDeformedUpValue, mHeightTopDeformedUpValue, mWidthDeformedValue, mHeightBottomDeformedDownValue, mHeightTopDeformedDownValue, mProgress);
            deformedValueBean.setScale(mScale);
            mOnDeformedListener.onScaleChanged(deformedValueBean);
        }
    }

    private void setTouchScale(float f2) {
        if (!mIsSupportDeformation) {
            mScale = Math.max(0.0f, Math.min(f2, 1.0f));
            return;
        }
        mScale = Math.max(SCALE_DEFORMATION_MIN, Math.min(f2, SCALE_DEFORMATION_MAX));
        calculateTouchDeformationValue();
        if (mOnDeformedListener != null) {
            DeformedValueBean deformedValueBean = new DeformedValueBean(mHeightBottomDeformedUpValue, mHeightTopDeformedUpValue, mWidthDeformedValue, mHeightBottomDeformedDownValue, mHeightTopDeformedDownValue, mProgress);
            deformedValueBean.setScale(mScale);
            mOnDeformedListener.onScaleChanged(deformedValueBean);
        }
    }

    private String formatValue(float value) {
        if (hasLabelFormatter()) {
            return formatter.getFormattedValue(value);
        }

        return String.format((int) value == value ? "%.0f" : "%.2f", value);
    }

    /**
     * Returns {@code true} if the mOplusSlider has a {@link LabelFormatter} attached, {@code false}
     * otherwise.
     */
    public boolean hasLabelFormatter() {
        return formatter != null;
    }

    /**
     * Registers a {@link LabelFormatter} to be used to format the value displayed in the bubble shown
     * when the mOplusSlider operates in discrete mode.
     *
     * @param formatter The {@link LabelFormatter} to use to format the bubble's text
     */
    public void setLabelFormatter(@Nullable LabelFormatter formatter) {
        this.formatter = formatter;
    }

    /**
     * Returns the {@link LabelBehavior} used.
     *
     * @see #setLabelBehavior(int)
     * @attr ref com.google.android.material.R.styleable#Slider_labelBehavior
     */
    @LabelBehavior
    public int getLabelBehavior() {
        return labelBehavior;
    }

    /**
     * Determines the {@link LabelBehavior} used.
     *
     * @see LabelBehavior
     * @see #getLabelBehavior()
     * @attr ref com.google.android.material.R.styleable#Slider_labelBehavior
     */
    public void setLabelBehavior(@LabelBehavior int labelBehavior) {
        if (this.labelBehavior != labelBehavior) {
            this.labelBehavior = labelBehavior;
            requestLayout();
        }
    }

    @SuppressLint("RestrictedApi")
    private void positionLabel(TooltipDrawable label, float value) {
        // Calculate the difference between the bounds of this view and the bounds of the root view to
        // correctly position this view in the overlay layer.
        calculateLabelBounds(label, value);
        DescendantOffsetUtils.offsetDescendantRect(ViewUtils.getContentView(this), this, labelRect);
        label.setBounds(labelRect);
    }

    private void calculateLabelBounds(TooltipDrawable label, float value) {
        int left;
        int right;
        int bottom;
        int top;
        left =
                (int) (mCurPaddingHorizontal
                        + (int) (normalizeValue(value) * getSeekBarWidth())
                        + (mThumbOutRadius / 2)
                        - label.getIntrinsicWidth() / 2);
        right = left + label.getIntrinsicWidth();
        bottom = (int) (labels.get(0).getIntrinsicHeight() - (mThumbOutRadius));
        top = bottom - label.getIntrinsicHeight();
        labelRect.set(left, top, right, bottom);
    }

    @SuppressLint({"RestrictedApi"})
    private void setValueForLabel(TooltipDrawable label, float value) {
        label.setText(formatValue(value));
        label.invalidateSelf();
        positionLabel(label, value);
        ViewUtils.getContentViewOverlay(this).add(label);
    }

    private void startFastMoveAnimation(float f2) {
        Spring fastMoveSpring = getFastMoveSpring();
        if (fastMoveSpring.getCurrentValue() == fastMoveSpring.getEndValue()) {
            float i2 = valueTo - valueFrom;
            if (f2 >= FAST_MOVE_VELOCITY) {
                float i3 = mProgress;
                if (i3 > MAX_FAST_MOVE_PERCENT * i2 || i3 < i2 * MIN_FAST_MOVE_PERCENT) {
                    return;
                }
                fastMoveSpring.setEndValue(1.0d);
                return;
            }
            if (f2 > -FAST_MOVE_VELOCITY) {
                fastMoveSpring.setEndValue(DOUBLE_EPSILON);
                return;
            }
            if (mProgress > MAX_FAST_MOVE_PERCENT * i2 || mProgress < i2 * MIN_FAST_MOVE_PERCENT) {
                return;
            }
            fastMoveSpring.setEndValue(-1.0d);
        }
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

    private boolean touchInSeekBarWhenDeformation(MotionEvent motionEvent, View view) {
        float y2 = motionEvent.getY();
        return y2 >= 0.0f && y2 <= ((float) view.getHeight());
    }

    private void trackTouchEvent(MotionEvent motionEvent) {
        float x2 = motionEvent.getX();
        float f2 = x2 - this.mLastX;
        float diff = this.valueTo - this.valueFrom;
        if (isLayoutRtl()) {
            f2 = -f2;
        }
        setTouchScale((this.mProgress / diff) + ((f2 * calculateDamping(x2)) / getSeekBarWidth()));
        float progressLimit = getProgressLimit(Math.round((this.mScale * diff) + valueFrom));
        float i3 = this.mProgress;
        float i4 = this.mRealProgress;
        setLocalProgress(progressLimit);
        invalidate();
        if (i3 != this.mProgress) {
            this.mLastX = x2;
            dispatchChangeListener(true);
            if (i4 != this.mRealProgress) {
                performFeedback();
            }
        }
        if (mVelocityTracker != null) {
            mVelocityTracker.computeCurrentVelocity(PHYSICAL_VELOCITY_LIMIT);
            startFastMoveAnimation(this.mVelocityTracker.getXVelocity());
        }
    }

    private void validateConfigurationIfDirty() {
        if (dirtyConfig) {
            validateValues();
            validateStepSize();
            validateMinSeparation();
            warnAboutFloatingPointError();
            dirtyConfig = false;
        }
    }

    private void warnAboutFloatingPointError() {
        if (stepSize == 0) {
            // Only warn if mOplusSlider uses a step value.
            return;
        }

        if ((int) stepSize != stepSize) {
            Log.w(TAG, String.format(WARNING_FLOATING_POINT_ERROR, "stepSize", stepSize));
        }

        if ((int) valueFrom != valueFrom) {
            Log.w(TAG, String.format(WARNING_FLOATING_POINT_ERROR, "valueFrom", valueFrom));
        }

        if ((int) valueTo != valueTo) {
            Log.w(TAG, String.format(WARNING_FLOATING_POINT_ERROR, "valueTo", valueTo));
        }
    }

    private void validateStepSize() {
        if (stepSize > 0.0f && !valueLandsOnTick(valueTo)) {
            throw new IllegalStateException(
                    String.format(EXCEPTION_ILLEGAL_STEP_SIZE, stepSize, valueFrom, valueTo));
        }
    }

    protected float getMinSeparation() {
        return 0;
    }

    private void validateMinSeparation() {
        final float minSeparation = getMinSeparation();
        if (minSeparation < 0) {
            throw new IllegalStateException(
                    String.format(EXCEPTION_ILLEGAL_MIN_SEPARATION, minSeparation));
        }
        if (stepSize > 0 && minSeparation > 0) {
            if (separationUnit != UNIT_VALUE) {
                throw new IllegalStateException(
                        String.format(
                                EXCEPTION_ILLEGAL_MIN_SEPARATION_STEP_SIZE_UNIT, minSeparation, stepSize));
            }
            if (minSeparation < stepSize || !isMultipleOfStepSize(minSeparation)) {
                throw new IllegalStateException(
                        String.format(
                                EXCEPTION_ILLEGAL_MIN_SEPARATION_STEP_SIZE, minSeparation, stepSize, stepSize));
            }
        }
    }

    private void validateValues() {
        if (valueFrom >= valueTo) {
            throw new IllegalStateException(
                    String.format(EXCEPTION_ILLEGAL_VALUE_FROM, valueFrom, valueTo));
        }

        for (Float value : values) {
            if (value < valueFrom || value > valueTo) {
                throw new IllegalStateException(
                        String.format(EXCEPTION_ILLEGAL_VALUE, value, valueFrom, valueTo));
            }
            if (stepSize > 0.0f && !valueLandsOnTick(value)) {
                throw new IllegalStateException(
                        String.format(EXCEPTION_ILLEGAL_DISCRETE_VALUE, value, valueFrom, stepSize, stepSize));
            }
        }
    }

    private boolean valueLandsOnTick(float value) {
        // Check that the value is a multiple of stepSize given the offset of valueFrom.
        double result =
                new BigDecimal(Float.toString(value))
                        .subtract(new BigDecimal(Float.toString(valueFrom)), DECIMAL64)
                        .doubleValue();
        return isMultipleOfStepSize(result);
    }

    private boolean isMultipleOfStepSize(double value) {
        // We're using BigDecimal here to avoid floating point rounding errors.
        double result =
                new BigDecimal(Double.toString(value))
                        .divide(new BigDecimal(Float.toString(stepSize)), DECIMAL64)
                        .doubleValue();

        // If the result is a whole number, it means the value is a multiple of stepSize.
        return Math.abs(Math.round(result) - result) < THRESHOLD;
    }

    private void trackTouchEventByFinger(MotionEvent motionEvent) {
        float touchX = motionEvent.getX();
        int seekBarStart = getStart() + (int) mCurPaddingHorizontal;
        int seekBarEnd = getWidth() - getEnd() - (int) mCurPaddingHorizontal;
        int seekBarWidth = seekBarEnd - seekBarStart;

        float relativeX = touchX - seekBarStart;
        relativeX = Math.max(0, Math.min(relativeX, seekBarWidth));
        int round = Math.round(((motionEvent.getX() - this.mLastX) * calculateDamping(motionEvent.getX())) + this.mLastX);

        if (isLayoutRtl()) {
            relativeX = seekBarWidth - relativeX;
        }

        float scale = (seekBarWidth > 0) ? relativeX / seekBarWidth : 0f;
        scale = Math.max(SCALE_MIN, Math.min(SCALE_MAX, scale));

        float progress = (scale * (valueTo - valueFrom)) + valueFrom;
        if (stepSize > 0) {
            progress = valueFrom + (Math.round((progress - valueFrom) / stepSize) * stepSize);
        }
        progress = Math.max(valueFrom, Math.min(progress, valueTo));
        float snappedProgress = getProgressLimit(progress);

        mScale = scale;
        float i2 = values.get(activeThumbIdx);
        float realProgress = this.mRealProgress;
        setLocalProgress(snappedProgress);
        snapActiveThumbToValue(snappedProgress);
        invalidate();
        if (i2 != values.get(activeThumbIdx)) {
            this.mLastX = round;
            mProgress = progress;
            dispatchChangeListener(true);
            if (realProgress != this.mRealProgress) {
                performFeedback();
            }
        }
    }

    private void updateBehavior() {
        if (!mIsPhysicsEnable || mPhysicalAnimator == null || mFlingBehavior == null) {
            return;
        }
        int normalSeekBarWidth = getNormalSeekBarWidth();
        mFlingBehavior.setActiveFrame(0.0f, (float) normalSeekBarWidth);
    }

    public void animForClick(float f2) {
        float seekBarWidth = getSeekBarWidth();
        float f4 = seekBarWidth + (SCALE_DEFORMATION_MAX * mCurProgressRadius);
        float f5 = mCurPaddingHorizontal - mCurProgressRadius;
        startTransitionAnim(getProgressLimit((((isLayoutRtl() ? (((getWidth() - f2) - getStart()) - f5) / f4 : ((f2 - getStart()) - f5) / f4) * (valueTo - valueFrom)) + valueFrom)), true);
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent motionEvent) {
        return super.dispatchHoverEvent(motionEvent);
    }

    public void drawActiveTrackSingle(Canvas canvas, float seekWidth) {
        float progressStartX;
        int seekBarCenterY = getSeekBarCenterY();
        progressStartX = mCurPaddingHorizontal - mCurProgressRadius;

        mClipProgressRect.top = seekBarCenterY - (mCurProgressHeight / 2f);
        mClipProgressRect.bottom = seekBarCenterY + (mCurProgressHeight / 2f);

        mClipProgressRect.left = progressStartX;
        mClipProgressRect.right = mCurPaddingHorizontal + mThumbOutRadius + (mScale * seekWidth);

        drawProgress(canvas, seekBarCenterY, mClipProgressRect.left, mClipProgressRect.right);

        float thumbCenterX = mCurPaddingHorizontal + (mScale * seekWidth);
        drawThumb(canvas, seekBarCenterY,
                thumbCenterX - mThumbOutRadius,
                thumbCenterX + mThumbOutRadius);
    }

    public void drawActiveTrackRange(Canvas canvas, float seekWidth) {
        // normalize our values
        float[] activeRange = getActiveRange();
        float normalizedLeft = activeRange[0];
        float normalizedRight = activeRange[1];

        int seekBarCenterY = getSeekBarCenterY();
        float trackStart = mCurPaddingHorizontal;
        float trackEnd = seekWidth - mCurPaddingHorizontal;
        float leftX = mCurPaddingHorizontal + (normalizedLeft * seekWidth);
        float rightX = trackStart + (normalizedRight * seekWidth);
        float thumbRadius = mThumbOutRadius;

        if (isLayoutRtl()) {
            float temp = leftX;
            leftX = trackEnd - rightX;
            rightX = trackEnd - temp;
        }

        RectF activeTrackRect = mClipProgressRect;
        activeTrackRect.left = leftX;
        activeTrackRect.right = rightX;
        activeTrackRect.top = seekBarCenterY - (mCurProgressHeight / 2f) + mWidthDeformedValue;
        activeTrackRect.bottom = seekBarCenterY + (mCurProgressHeight / 2f) - mWidthDeformedValue;

        mPaint.setColor(mProgressColor);
        canvas.drawRoundRect(activeTrackRect, 0, 0, mPaint);

        // draw thumbs
        drawThumb(canvas, seekBarCenterY, leftX - thumbRadius, leftX + thumbRadius); // Thumb sinistro
        drawThumb(canvas, seekBarCenterY, rightX - thumbRadius, rightX + thumbRadius); // Thumb destro
    }

    private float valueToX(float value) {
        return normalizeValue(value) * getSeekBarWidth() + mCurPaddingHorizontal;
    }

    public void drawInactiveTrack(Canvas canvas) {
        float start = (getStart() + mCurPaddingHorizontal) - mCurBackgroundRadius;
        float width = ((getWidth() - getEnd()) - mCurPaddingHorizontal) + mCurBackgroundRadius;
        int seekBarCenterY = getSeekBarCenterY();
        if (mShadowRadiusSize > 0) {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(0.0f);
            mPaint.setColor(0);
            mPaint.setShadowLayer(mShadowRadiusSize, 0.0f, 0.0f, mShadowColor);
            mBackgroundRect.set(
                    start - ((float) mShadowRadiusSize / 2),
                    ((float) seekBarCenterY - (mCurBackgroundHeight / SCALE_DEFORMATION_MAX)) - ((float) mShadowRadiusSize / 2),
                    ((float) mShadowRadiusSize / 2) + width,
                    (float) seekBarCenterY + (mCurBackgroundHeight / SCALE_DEFORMATION_MAX) + ((float) mShadowRadiusSize / 2));
            canvas.drawRoundRect(mBackgroundRect, mCurBackgroundRadius, mCurBackgroundRadius, mPaint);
            mPaint.clearShadowLayer();
            mPaint.setStyle(Paint.Style.FILL);
        }
        mPaint.setColor(mBackgroundColor);
        if (isLayoutRtl()) {
            float deformedValue = (start - mHeightTopDeformedUpValue) + mHeightTopDeformedDownValue;
            mBackgroundRect.set(deformedValue, (float) seekBarCenterY - ((mCurBackgroundHeight / SCALE_DEFORMATION_MAX) - mWidthDeformedValue), (width - mHeightBottomDeformedUpValue) + mHeightBottomDeformedDownValue, (float) seekBarCenterY + ((mCurBackgroundHeight / SCALE_DEFORMATION_MAX) - mWidthDeformedValue));
        } else {
            float deformedValue = (start - mHeightBottomDeformedDownValue) + mHeightBottomDeformedUpValue;
            mBackgroundRect.set(deformedValue, (float) seekBarCenterY - ((mCurBackgroundHeight / SCALE_DEFORMATION_MAX) - mWidthDeformedValue), (width + mHeightTopDeformedUpValue) - mHeightTopDeformedDownValue, (float) seekBarCenterY + ((mCurBackgroundHeight / SCALE_DEFORMATION_MAX) - mWidthDeformedValue));
        }
        canvas.drawRoundRect(mBackgroundRect, mCurBackgroundRadius, mCurBackgroundRadius, mPaint);
    }

    @SuppressLint({"RestrictedApi"})
    public void ensureLabelsRemoved() {
        // If the labels are animated in or in the process of animating in, create and start a new
        // animator to animate out the labels and remove them once the animation ends.
        if (labelsAreAnimatedIn) {
            labelsAreAnimatedIn = false;
            labelsOutAnimator = createLabelAnimator(false);
            labelsInAnimator = null;
            labelsOutAnimator.addListener(
                    new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            ViewOverlayImpl contentViewOverlay = ViewUtils.getContentViewOverlay(OplusSlider.this);
                            for (TooltipDrawable label : labels) {
                                contentViewOverlay.remove(label);
                            }
                        }
                    });
            labelsOutAnimator.start();
        }
    }

    public int getColor(View view, ColorStateList colorStateList, int i2) {
        return colorStateList == null ? i2 : colorStateList.getColorForState(view.getDrawableState(), i2);
    }

    public int getEnd() {
        return getPaddingEnd();
    }

    /**
     * Returns a float array where {@code float[0]} is the normalized left position and {@code
     * float[1]} is the normalized right position of the range.
     */
    private float[] getActiveRange() {
        float min = values.get(0);
        float max = values.get(values.size() - 1);
        float left = normalizeValue(values.size() == 1 ? valueFrom : min);
        float right = normalizeValue(max);

        // In RTL we draw things in reverse, so swap the left and right range values
        return isLayoutRtl() ? new float[]{right, left} : new float[]{left, right};
    }

    /**
     * Returns a number between 0 and 1 indicating where on the track this value should sit with 0
     * being on the far left, and 1 on the far right.
     */
    private float normalizeValue(float value) {
        float normalized = (value - valueFrom) / (valueTo - valueFrom);
        if (isLayoutRtl()) {
            return 1 - normalized;
        }
        return normalized;
    }

    private float getValueFromNormalized(float normalized) {
        if (isLayoutRtl()) {
            normalized = 1 - normalized;
        }
        return valueFrom + normalized * (valueTo - valueFrom);
    }

    public float getMoveDamping() {
        return mDamping;
    }

    public void setMoveDamping(float moveDamping) {
        mDamping = moveDamping;
    }

    public int getMoveType() {
        return mMoveType;
    }

    /**
     * Set the move type of the slider.
     * @param moveType The move type of the slider.
     *                 - {@link #MOVE_BY_DEFAULT} - Default move type.
     *                 - {@link #MOVE_BY_FINGER} - Move by finger.
     *                 - {@link #MOVE_BY_DISTANCE} - Move by distance.
     */
    public void setMoveType(int moveType) {
        mMoveType = moveType;
    }

    public int getSeekBarCenterY() {
        return getPaddingTop() + (((getHeight() - getPaddingBottom()) - getPaddingTop()) >> 1);
    }

    public int getSeekBarWidth() {
        int width = (getWidth() - getStart() - getEnd()) - (int) (2 * mCurPaddingHorizontal);
        return Math.max(0, width);
    }

    public int getStart() {
        return getPaddingStart();
    }

    public void handleMotionEventDown(MotionEvent motionEvent) {
        mTouchDownX = motionEvent.getX();
        mLastX = motionEvent.getX();
    }

    public void handleMotionEventMove(MotionEvent motionEvent) {

        float seekBarWidth = getSeekBarWidth();
        float diff = valueTo - valueFrom;
        float f2 = (diff > 0 ? (values.get(activeThumbIdx) * seekBarWidth) / diff : 0.0f) + valueFrom;
        if (this.mIsDragging && this.mStartDragging) {
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
        if (isTouchInSeekBar(motionEvent)) {
            float x2 = motionEvent.getX();
            if (Math.abs(x2 - this.mTouchDownX) > this.mTouchSlop) {
                stopDeformationFling();
                startDrag();
                touchAnim();
                this.mLastX = x2;
                if (isMoveFollowHand()) {
                    invalidateProgress(motionEvent);
                }
            }
        }

    }

    public void handleMotionEventUp(MotionEvent motionEvent) {
        getFastMoveSpring().setEndValue(DOUBLE_EPSILON);

        if (!mIsDragging) {
            if (isEnabled() && touchInSeekBar(motionEvent, this) && isMoveFollowHand()) {
                animForClick(motionEvent.getX());
                invalidate();
                return;
            }
            return;
        }
        mIsDragging = false;
        mStartDragging = false;
        if (!mIsPhysicsEnable || Math.abs(mFlingVelocity) < PHYSICAL_VELOCITY_LIMIT) {
            onStopTrackingTouch();
            flingBehaviorAfterDeformationDrag();
        } else {
            flingBehaviorAfterEndDrag(mFlingVelocity);
        }
        startTransitionAnim(values.get(activeThumbIdx), false);
        setPressed(false);
        releaseAnim();
        removeCallbacks(resetActiveThumbIndex);
        setActiveThumbIndex(-1);
    }

    public boolean isLayoutRtl() {
        return getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    @Override
    public void onAnimationCancel(BaseBehavior baseBehavior) {
        onStopTrackingTouch(true);
    }

    @Override
    public void onAnimationEnd(BaseBehavior baseBehavior) {
        onStopTrackingTouch(true);
        invalidate();
    }

    @Override
    public void onAnimationUpdate(BaseBehavior baseBehavior) {
        float scaledValue;
        Object animatedValue = baseBehavior.getAnimatedValue();
        if (animatedValue == null) {
            return;
        }
        float floatValue = (Float) animatedValue;
        int normalSeekBarWidth = getNormalSeekBarWidth();
        if (isLayoutRtl()) {
            scaledValue = ((float) normalSeekBarWidth - floatValue) / (float) normalSeekBarWidth;
        } else {
            scaledValue = floatValue / normalSeekBarWidth;
        }
        setFlingScale(scaledValue);
        float oldProgress = mProgress;
        float snappedProgress = getProgressLimit(getValueFromNormalized(mScale));
        snapThumbToValue(0, snappedProgress);
        setLocalProgress(snappedProgress);
        invalidate();
        if (oldProgress != mProgress) {
            mLastX = floatValue + getStart();
            dispatchChangeListener(true);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Update factoring in the visibility of all ancestors.
        getViewTreeObserver().addOnScrollChangedListener(onScrollChangedListener);
        getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        // The label is attached on the Overlay relative to the content.
        for (TooltipDrawable label : labels) {
            attachLabelToContentView(label);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopPhysicsMove();
    }

    @SuppressLint("RestrictedApi")
    private boolean isSliderVisibleOnScreen() {
        final Rect contentViewBounds = new Rect();
        ViewUtils.getContentView(this).getHitRect(contentViewBounds);
        return getLocalVisibleRect(contentViewBounds);
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        validateConfigurationIfDirty();
        float seekBarWidth = getSeekBarWidth();
        updateLabels();
        drawInactiveTrack(canvas);

        if (values.size() == 2) {
            drawActiveTrackRange(canvas, seekBarWidth);
        } else {
            drawActiveTrackSingle(canvas, seekBarWidth);
        }
    }

    public void onEnlargeAnimationUpdate(ValueAnimator valueAnimator) {
        float animatedFraction = valueAnimator.getAnimatedFraction();
        mCurBackgroundRadius = mBackgroundRadius + (((mBackgroundRadius * mBackgroundEnlargeScale) - mBackgroundRadius) * animatedFraction);
        mCurProgressRadius = mProgressRadius + (((mProgressRadius * mProgressEnlargeScale) - mProgressRadius) * animatedFraction);
        mCurBackgroundHeight = mBackgroundHeight + (((mBackgroundEnlargeScale * mBackgroundHeight) - mBackgroundHeight) * animatedFraction);
        mCurProgressHeight = mProgressHeight + (((mProgressEnlargeScale * mProgressHeight) - mProgressHeight) * animatedFraction);
        mCurPaddingHorizontal = mPaddingHorizontal + (animatedFraction * ((mHorizontalPaddingScale * mPaddingHorizontal) - mPaddingHorizontal));
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int labelSize = 0;
        if ((labelBehavior == LABEL_WITHIN_BOUNDS || shouldAlwaysShowLabel()) && !labels.isEmpty()) {
            labelSize = labels.get(0).getIntrinsicHeight();
        }
        int seekbarHeight = mSeekbarMinHeight + getPaddingTop() + getPaddingBottom();
        int spec = MeasureSpec.makeMeasureSpec(seekbarHeight + labelSize, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, spec);
    }

    /**
     * Returns whether the labels should be always shown based on the {@link LabelBehavior}.
     *
     * @attr ref com.google.android.material.R.styleable#Slider_labelBehavior
     * @see LabelBehavior
     */
    private boolean shouldAlwaysShowLabel() {
        return this.labelBehavior == LABEL_VISIBLE;
    }

    @Override
    public void onSizeChanged(int i2, int i3, int i4, int i5) {
        super.onSizeChanged(i2, i3, i4, i5);
        mStartDragging = false;
        stopPhysicsMove();
        updateBehavior();
    }

    public void onStartTrackingTouch() {
        onStartTrackingTouch(true);
    }

    public void onStopTrackingTouch() {
        onStopTrackingTouch(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!isEnabled()) {
            if (motionEvent.getAction() != MotionEvent.ACTION_UP && motionEvent.getAction() != MotionEvent.ACTION_CANCEL) {
                return false;
            }
            handleMotionEventUp(motionEvent);
            return true;
        }

        int action = motionEvent.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:

                if (!determineActiveThumb(motionEvent)) {
                    Log.d(TAG, "Couldn't determine the active thumb yet");
                    // Couldn't determine the active thumb yet.
                    break;
                }
                requestFocus();
                thumbIsPressed = true;
                if (mClickAnimatorSet != null) {
                    mClickAnimatorSet.removeAllListeners();
                    mClickAnimatorSet.cancel();
                }
                if (!isDeformationFling()) {
                    stopPhysicsMove();
                }
                if (mIsPhysicsEnable && mPhysicalAnimator == null) {
                    initPhysicsAnimator(getContext());
                }

                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(motionEvent);
                mIsDragging = false;
                mStartDragging = false;
                handleMotionEventDown(motionEvent);
                break;

            case MotionEvent.ACTION_MOVE:
                if (!thumbIsPressed) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    if (!determineActiveThumb(motionEvent)) {
                        // Couldn't determine the active thumb yet.
                        break;
                    }
                    thumbIsPressed = true;
                }

                clearDeformationValue();
                initVelocityTrackerIfNotExists();
                mVelocityTracker.addMovement(motionEvent);
                handleMotionEventMove(motionEvent);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                thumbIsPressed = false;
                if (mVelocityTracker != null) {
                    mVelocityTracker.computeCurrentVelocity(ONE_SECOND_UNITS, MAX_VELOCITY);
                    mFlingVelocity = mVelocityTracker.getXVelocity();
                }
                recycleVelocityTracker();
                handleMotionEventUp(motionEvent);
                break;
        }
        return true;
    }

    private boolean snapActiveThumbToValue(float value) {
        return snapThumbToValue(activeThumbIdx, value);
    }

    public boolean snapThumbToValue(int idx, float value) {
        focusedThumbIdx = idx;

        // Check if the new value equals a value that was already set.
        if (Math.abs(value - values.get(idx)) < THRESHOLD) {
            return false;
        }

        float newValue = getClampedValue(idx, value);
        // Replace the old value with the new value of the touch position.
        values.set(idx, newValue);

        dispatchOnChangedFromUser(idx);
        return true;
    }

    @SuppressWarnings("unchecked")
    private void dispatchOnChangedFromUser(int idx) {
        dispatchChangeListener(true);
        if (accessibilityManager != null && accessibilityManager.isEnabled()) {
            scheduleAccessibilityEventSender(idx);
        }
    }

    private float dimenToValue(float dimen) {
        if (dimen == 0) {
            return 0;
        }
        return ((dimen - mHorizontalPaddingScale) / getSeekBarWidth()) * (valueFrom - valueTo) + valueFrom;
    }

    /**
     * Thumbs cannot cross each other, clamp the value to a bound or the value next to it.
     */
    private float getClampedValue(int idx, float value) {
        float minSeparation = 0;
        minSeparation = separationUnit == UNIT_PX ? dimenToValue(minSeparation) : minSeparation;
        if (isLayoutRtl()) {
            minSeparation = -minSeparation;
        }

        float upperBound = idx + 1 >= values.size() ? valueTo : values.get(idx + 1) - minSeparation;
        float lowerBound = idx - 1 < 0 ? valueFrom : values.get(idx - 1) + minSeparation;
        return clamp(value, lowerBound, upperBound);
    }

    private boolean determineActiveThumb(MotionEvent event) {
        float x = event.getX();
        activeThumbIdx = -1;
        float minDistance = Float.MAX_VALUE;

        for (int i = 0; i < values.size(); i++) {
            float value = values.get(i);
            float thumbX = valueToX(value);
            float distance = Math.abs(x - thumbX);

            if (distance < minDistance) {
                minDistance = distance;
                activeThumbIdx = i;
            } else if (distance == minDistance) {
                // If equidistant, choose the thumb in the direction of the touch movement
                activeThumbIdx = (x > thumbX) ? i : activeThumbIdx;
            }
        }

        return activeThumbIdx != -1;
    }

    /**
     * Create an animator that shows or hides all mOplusSlider labels.
     *
     * @param enter True if this animator should show (reveal) labels. False if this animator should
     *              hide labels.
     * @return A value animator that, when run, will animate all labels in or out using {@link
     * TooltipDrawable#setRevealFraction(float)}.
     */
    private ValueAnimator createLabelAnimator(boolean enter) {
        float startFraction = enter ? 0F : 1F;
        // Update the start fraction to the current animated value of the label, if any.
        startFraction =
                getAnimatorCurrentValueOrDefault(
                        enter ? labelsOutAnimator : labelsInAnimator, startFraction);
        float endFraction = enter ? 1F : 0F;
        ValueAnimator animator = ValueAnimator.ofFloat(startFraction, endFraction);
        int duration;
        TimeInterpolator interpolator;
        if (enter) {
            duration =
                    MotionUtils.resolveThemeDuration(
                            getContext(),
                            LABEL_ANIMATION_ENTER_DURATION_ATTR,
                            DEFAULT_LABEL_ANIMATION_ENTER_DURATION);
            interpolator =
                    MotionUtils.resolveThemeInterpolator(
                            getContext(),
                            LABEL_ANIMATION_ENTER_EASING_ATTR,
                            new DecelerateInterpolator());
        } else {
            duration =
                    MotionUtils.resolveThemeDuration(
                            getContext(),
                            LABEL_ANIMATION_EXIT_DURATION_ATTR,
                            DEFAULT_LABEL_ANIMATION_EXIT_DURATION);
            interpolator =
                    MotionUtils.resolveThemeInterpolator(
                            getContext(),
                            LABEL_ANIMATION_EXIT_EASING_ATTR,
                            new FastOutLinearInInterpolator());
        }
        animator.setDuration(duration);
        animator.setInterpolator(interpolator);
        animator.addUpdateListener(
                animation -> {
                    float fraction = (float) animation.getAnimatedValue();
                    for (TooltipDrawable label : labels) {
                        label.setRevealFraction(fraction);
                    }
                    // Ensure the labels are redrawn even if the mOplusSlider has stopped moving
                    postInvalidateOnAnimation();
                });
        return animator;
    }

    private float screenXToValue(float x) {
        float normalized = (x - mCurPaddingHorizontal) / (getWidth() - 2 * mCurPaddingHorizontal);
        if (isLayoutRtl()) normalized = 1 - normalized;
        return valueFrom + normalized * (valueTo - valueFrom);
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

        if (mRealProgress == valueTo || mRealProgress == valueFrom) {
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

        if (mRealProgress == valueTo || mRealProgress == valueFrom) {
            performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            return;
        }

        if (mIsDragging) {
            performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
        }
    }

    public void refresh() {
        String resourceTypeName = getResources().getResourceTypeName(mRefreshStyle);
        TypedArray typedArray = null;
        if (TextUtils.equals(resourceTypeName, "attr")) {
            typedArray = getContext().getTheme().obtainStyledAttributes(null, R.styleable.OplusSlider, mRefreshStyle, 0);
        } else if (TextUtils.equals(resourceTypeName, "style")) {
            typedArray = getContext().getTheme().obtainStyledAttributes(null, R.styleable.OplusSlider, 0, mRefreshStyle);
        }
        if (typedArray != null) {
            mProgressColor = getColor(this, typedArray.getColorStateList(R.styleable.OplusSlider_oplusSeekBarProgressColor), ContextCompat.getColor(getContext(), R.color.oplus_seekbar_progress_color_normal));
            mBackgroundColor = getColor(this, typedArray.getColorStateList(R.styleable.OplusSlider_oplusSeekBarBackgroundColor), ContextCompat.getColor(getContext(), R.color.oplus_seekbar_background_color_normal));
            mThumbShadowColor = typedArray.getColor(R.styleable.OplusSlider_oplusSeekBarThumbShadowColor, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_thumb_shadow_color));
            invalidate();
            typedArray.recycle();
        }
    }

    public void releaseAnim() {
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setValues(PropertyValuesHolder.ofFloat("progressRadius", mCurProgressRadius, mProgressRadius), PropertyValuesHolder.ofFloat("backgroundRadius", mCurBackgroundRadius, mBackgroundRadius), PropertyValuesHolder.ofFloat("progressHeight", mCurProgressHeight, mProgressHeight), PropertyValuesHolder.ofFloat("backgroundHeight", mCurBackgroundHeight, mBackgroundHeight), PropertyValuesHolder.ofFloat("animatePadding", mCurPaddingHorizontal, mPaddingHorizontal));
        valueAnimator.setDuration(RELEASE_ANIM_DURATION);
        valueAnimator.setInterpolator(PROGRESS_SCALE_INTERPOLATOR);
        valueAnimator.addUpdateListener(valueAnimator2 -> {
            mCurProgressRadius = (Float) valueAnimator2.getAnimatedValue("progressRadius");
            mCurBackgroundRadius = (Float) valueAnimator2.getAnimatedValue("backgroundRadius");
            mCurProgressHeight = (Float) valueAnimator2.getAnimatedValue("progressHeight");
            mCurBackgroundHeight = (Float) valueAnimator2.getAnimatedValue("backgroundHeight");
            mCurPaddingHorizontal = (Float) valueAnimator2.getAnimatedValue("animatePadding");
            invalidate();
        });
        mTouchAnimator.cancel();
        valueAnimator.start();
    }

    public void setBackgroundEnlargeScale(float backgroundEnlargeScale) {
        mBackgroundEnlargeScale = backgroundEnlargeScale;
        ensureSize();
        invalidate();
    }

    public void setBackgroundHeight(float backgroundHeight) {
        mBackgroundHeight = backgroundHeight;
        ensureSize();
        invalidate();
    }

    public void setBackgroundRadius(float backgroundRadius) {
        mBackgroundRadius = backgroundRadius;
        ensureSize();
        invalidate();
    }

    public void setBackgroundRoundCornerWeight(float backgroundRoundCornerWeight) {
        mBackgroundRoundCornerWeight = backgroundRoundCornerWeight;
        invalidate();
    }

    public void setCustomProgressAnimInterpolator(Interpolator interpolator) {
        mCustomProgressAnimInterpolator = interpolator;
    }

    public void setDeformedListener(OnDeformedListener onDeformedListener) {
        mOnDeformedListener = onDeformedListener;
    }

    public void setDeformedParams(DeformedValueBean deformedValueBean) {
        mScale = deformedValueBean.getScale();
        mProgress = deformedValueBean.getProgress();
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
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mProgressColor = getColor(this, mProgressColorStateList, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_progress_color_normal));
        mBackgroundColor = getColor(this, mBackgroundColorStateList, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_background_color_normal));
        mThumbColor = getColor(this, mThumbColorStateList, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_progress_color_normal));
        if (enabled) {
            mThumbShadowRadiusSize = getContext().getResources().getDimensionPixelSize(R.dimen.oplus_seekbar_thumb_shadow_size);
        } else {
            mThumbShadowRadiusSize = 0;
        }
    }

    public void setFlingLinearDamping(float flingLinearDamping) {
        FlingBehavior flingBehavior;
        if (mIsPhysicsEnable) {
            mFlingLinearDamping = flingLinearDamping;
            if (mPhysicalAnimator == null || (flingBehavior = mFlingBehavior) == null) {
                return;
            }
            flingBehavior.setLinearDamping(flingLinearDamping);
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

    /**
     * Returns the mOplusSlider's {@code valueFrom} value.
     *
     * @attr ref com.google.android.material.R.styleable#Slider_android_valueFrom
     * @see #setValueFrom(float)
     */
    public float getValueFrom() {
        return valueFrom;
    }

    /**
     * Sets the mOplusSlider's {@code valueFrom} value.
     *
     * <p>The {@code valueFrom} value must be strictly lower than the {@code valueTo} value. If that
     * is not the case, an {@link IllegalStateException} will be thrown when the view is laid out.
     *
     * @param valueFrom The minimum value for the mOplusSlider's range of values
     * @attr ref com.google.android.material.R.styleable#Slider_android_valueFrom
     * @see #getValueFrom()
     */
    public void setValueFrom(float valueFrom) {
        this.valueFrom = valueFrom;
        dirtyConfig = true;
        postInvalidate();
    }

    /**
     * Returns the mOplusSlider's {@code valueTo} value.
     *
     * @attr ref com.google.android.material.R.styleable#Slider_android_valueTo
     * @see #setValueTo(float)
     */
    public float getValueTo() {
        return valueTo;
    }

    /**
     * Sets the mOplusSlider's {@code valueTo} value.
     *
     * <p>The {@code valueTo} value must be strictly greater than the {@code valueFrom} value. If that
     * is not the case, an {@link IllegalStateException} will be thrown when the view is laid out.
     *
     * @param valueTo The maximum value for the mOplusSlider's range of values
     * @attr ref com.google.android.material.R.styleable#Slider_android_valueTo
     * @see #getValueTo()
     */
    public void setValueTo(float valueTo) {
        this.valueTo = valueTo;
        dirtyConfig = true;
        postInvalidate();
    }

    /**
     * Returns the step size used to mark the ticks.
     *
     * <p>A step size of 0 means that the mOplusSlider is operating in continuous mode. A step size greater
     * than 0 means that the mOplusSlider is operating in discrete mode.
     *
     * @attr ref com.google.android.material.R.styleable#Slider_android_stepSize
     * @see #setStepSize(float)
     */
    public float getStepSize() {
        return stepSize;
    }

    public void setStepSize(float stepSize) {
        if (stepSize < 0.0f) {
            throw new IllegalArgumentException(
                    String.format(EXCEPTION_ILLEGAL_STEP_SIZE, stepSize, valueFrom, valueTo));
        }
        if (this.stepSize != stepSize) {
            this.stepSize = stepSize;
            dirtyConfig = true;
            postInvalidate();
        }
    }

    @NonNull
    public List<Float> getValues() {
        return new ArrayList<>(values);
    }

    /**
     * Sets multiple values for the mOplusSlider. Each value will represent a different thumb.
     *
     * <p>Each value must be greater or equal to {@code valueFrom}, and lesser or equal to {@code
     * valueTo}. If that is not the case, an {@link IllegalStateException} will be thrown when the
     * view is laid out.
     *
     * <p>If the mOplusSlider is in discrete mode (i.e. the tick increment value is greater than 0), the
     * values must be set to a value falls on a tick (i.e.: {@code value == valueFrom + x * stepSize},
     * where {@code x} is an integer equal to or greater than 0). If that is not the case, an {@link
     * IllegalStateException} will be thrown when the view is laid out.
     *
     * @param values An array of values to set.
     * @see #getValues()
     */
    public void setValues(@NonNull Float... values) {
        ArrayList<Float> list = new ArrayList<>();
        Collections.addAll(list, values);
        setValuesInternal(list);
    }

    /**
     * Sets multiple values for the mOplusSlider. Each value will represent a different thumb.
     *
     * <p>Each value must be greater or equal to {@code valueFrom}, and lesser or equal to {@code
     * valueTo}. If that is not the case, an {@link IllegalStateException} will be thrown when the
     * view is laid out.
     *
     * <p>If the mOplusSlider is in discrete mode (i.e. the tick increment value is greater than 0), the
     * values must be set to a value falls on a tick (i.e.: {@code value == valueFrom + x * stepSize},
     * where {@code x} is an integer equal to or greater than 0). If that is not the case, an {@link
     * IllegalStateException} will be thrown when the view is laid out.
     *
     * @param values An array of values to set.
     * @throws IllegalArgumentException If {@code values} is empty.
     */
    public void setValues(@NonNull List<Float> values) {
        if (values.size() > 1) {
            mIsPhysicsEnable = false;
        }
        setValuesInternal(new ArrayList<>(values));
        postInvalidate();
    }

    /**
     * This method assumes the list passed in is a copy. It is split out so we can call it from {@link
     * #setValues(Float...)} and {@link #setValues(List)}
     */
    private void setValuesInternal(@NonNull ArrayList<Float> values) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("At least one value must be set");
        }

        Collections.sort(values);
        if (values.size() > 1) {
            mIsPhysicsEnable = false;
        }
        if (this.values.size() == values.size()) {
            if (this.values.equals(values)) {
                Log.i(TAG, "setValuesInternal: values are the same, no need to update");
                return;
            }
        }

        this.values = values;
        dirtyConfig = true;
        // Only update the focused thumb index. The active thumb index will be updated on touch.
        focusedThumbIdx = 0;
        createLabelPool();
//        dispatchOnChangedProgrammatically();
        mScale = normalizeValue(values.get(0));
        setProgress(values.get(0), false);
        postInvalidate();
    }

    /**
     * Sets the index of the currently focused thumb
     */
    public void setFocusedThumbIndex(int index) {
        if (index < 0 || index >= values.size()) {
            throw new IllegalArgumentException("index out of range");
        }
        focusedThumbIdx = index;
        mAccessibilityHelper.requestKeyboardFocusForVirtualView(focusedThumbIdx);
        postInvalidate();
    }

    private void createLabelPool() {
        // If there are too many labels, remove the extra ones from the end.
        if (labels.size() > values.size()) {
            List<TooltipDrawable> tooltipDrawables = labels.subList(values.size(), labels.size());
            for (TooltipDrawable label : tooltipDrawables) {
                if (isAttachedToWindow()) {
                    detachLabelFromContentView(label);
                }
            }
            tooltipDrawables.clear();
        }

        // If there's not enough labels, add more.
        while (labels.size() < values.size()) {
            // Because there's currently no way to copy the TooltipDrawable we use this to make more
            // if more thumbs are added.
            TooltipDrawable tooltipDrawable =
                    TooltipDrawable.createFromAttributes(getContext(), null, 0, labelStyle);
            labels.add(tooltipDrawable);
            if (isAttachedToWindow()) {
                attachLabelToContentView(tooltipDrawable);
            }
        }

        // Add a stroke if there is more than one label for when they overlap.
        int strokeWidth = labels.size() == 1 ? 0 : 1;
        for (TooltipDrawable label : labels) {
            label.setStrokeWidth(strokeWidth);
        }
    }

    @SuppressLint({"RestrictedApi"})
    private void attachLabelToContentView(TooltipDrawable label) {
        label.setRelativeToView(ViewUtils.getContentView(this));
    }

    @SuppressLint({"RestrictedApi"})
    private void detachLabelFromContentView(TooltipDrawable label) {
        ViewOverlayImpl contentViewOverlay = ViewUtils.getContentViewOverlay(this);
        if (contentViewOverlay != null) {
            contentViewOverlay.remove(label);
            label.detachView(ViewUtils.getContentView(this));
        }
    }

    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    private void setLocalProgress(float localProgress) {
        this.mProgress = localProgress;
        this.mRealProgress = getRealProgress(localProgress);
    }

    public void setMaxHeightDeformed(float maxHeightDeformedValue) {
        mMaxHeightDeformedValue = maxHeightDeformedValue;
    }

    public void setMaxMovingDistance(int maxMovingDistance) {
        mMaxMovingDistance = maxMovingDistance;
    }

    public void setMaxWidthDeformed(float maxWidthDeformed) {
        mMaxWidthDeformedValue = maxWidthDeformed;
    }

    /**
     * Adds a callback {@link OnSliderChangeListener}
     * @param onSliderChangeListener The callback to run when the slider changes
     */
    public void addOnSliderChangeListener(OnSliderChangeListener onSliderChangeListener) {
        mOnSliderChangeListeners.add(onSliderChangeListener);
    }

    public void removeOnSliderChangeListener(OnSliderChangeListener onSliderChangeListener) {
        mOnSliderChangeListeners.remove(onSliderChangeListener);
    }

    public void clearSliderChangeListeners() {
        mOnSliderChangeListeners.clear();
    }

    private void dispatchChangeListener(boolean fromUser) {
        for (OnSliderChangeListener listener : mOnSliderChangeListeners) {
            listener.onProgressChanged(this, fromUser);
        }
    }

    private void dispatchOnStartTrackingTouch() {
        for (OnSliderChangeListener listener : mOnSliderChangeListeners) {
            listener.onStartTrackingTouch(this);
        }
    }

    private void dispatchOnStopTrackingTouch() {
        for (OnSliderChangeListener listener : mOnSliderChangeListeners) {
            listener.onStopTrackingTouch(this);
        }
    }

    public void setPaddingHorizontal(float paddingHorizontal) {
        mPaddingHorizontal = paddingHorizontal;
        ensureSize();
        invalidate();
    }

    public void setPhysicalEnabled(boolean enabled) {
        if (mIsPhysicsEnable == enabled) {
            return;
        }
        mIsPhysicsEnable = enabled;
        if (enabled) {
            updateBehavior();
        } else {
            stopPhysicsMove();
        }
    }

    public void setProgressColor(@NonNull ColorStateList colorStateList) {
        mProgressColorStateList = colorStateList;
        mProgressColor = getColor(this, mProgressColorStateList, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_progress_color_normal));
        invalidate();
    }

    public void setProgressEnlargeScale(float progressEnlargeScale) {
        mProgressEnlargeScale = progressEnlargeScale;
        ensureSize();
        invalidate();
    }

    public void setProgressHeight(float progressHeight) {
        mProgressHeight = progressHeight;
        ensureSize();
        invalidate();
    }

    public void setProgressRadius(float progressRadius) {
        mProgressRadius = progressRadius;
        ensureSize();
        invalidate();
    }

    public void setProgressRoundCornerWeight(float progressRoundCornerWeight) {
        mProgressRoundCornerWeight = progressRoundCornerWeight;
        ensureSize();
        invalidate();
    }

    public void setSeekBarBackgroundColor(@NonNull ColorStateList colorStateList) {
        mBackgroundColorStateList = colorStateList;
        mBackgroundColor = getColor(this, mBackgroundColorStateList, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_background_color_normal));
        invalidate();
    }

    public void setSupportDeformation(boolean z2) {
        mIsSupportDeformation = z2;
    }

    public void setThumbColor(@NonNull ColorStateList colorStateList) {
        mThumbColorStateList = colorStateList;
        mThumbColor = getColor(this, mThumbColorStateList, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_progress_color_normal));
        invalidate();
    }

    public void startDrag() {
        setPressed(true);
        onStartTrackingTouch(true);
        attemptClaimDrag();
    }

    public void startTransitionAnim(float targetValue, final boolean fromUser) {
        if (mClickAnimatorSet == null) {
            mClickAnimatorSet = new AnimatorSet();
        } else {
            mClickAnimatorSet.removeAllListeners();
            mClickAnimatorSet.cancel();
        }

        mClickAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationCancel(@NonNull Animator animator) {
                dispatchChangeListener(fromUser);
                onStopTrackingTouch(fromUser);
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                setLocalProgress(targetValue);
                dispatchChangeListener(fromUser);
                onStopTrackingTouch(fromUser);
            }

            @Override
            public void onAnimationStart(@NonNull Animator animator) {
                onStartTrackingTouch(fromUser);
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {
            }
        });

        float currentValue = values.get(activeThumbIdx);
        final int seekBarWidth = getSeekBarWidth();
        float totalRange = valueTo - valueFrom;
        final float scaleFactor = totalRange > 0 ? (seekBarWidth / totalRange) : SCALE_MIN;

        if (scaleFactor > SCALE_MIN) {
            float startPx = currentValue * scaleFactor;
            float targetPx = targetValue * scaleFactor;

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(startPx, targetPx);
            Interpolator interpolator = (fromUser || mCustomProgressAnimInterpolator == null)
                    ? THUMB_ANIMATE_INTERPOLATOR : mCustomProgressAnimInterpolator;
            valueAnimator.setInterpolator(interpolator);

            valueAnimator.addUpdateListener(animation -> {
                float animatedPx = (Float) animation.getAnimatedValue();
                float newProgress = (animatedPx / scaleFactor);
                snapThumbToValue(activeThumbIdx, getProgressLimit(newProgress));
                mScale = (animatedPx - (valueFrom * scaleFactor)) / seekBarWidth;
                updateLabels();
                invalidate();
            });
            long duration = (long) ((totalRange > 0 ? Math.abs(targetValue - currentValue) / totalRange : 0.0f) * DURATION_483);
            if (duration < DURATION_150) {
                duration = DURATION_150;
            }
            mClickAnimatorSet.setDuration(duration);
            mClickAnimatorSet.play(valueAnimator);
            mClickAnimatorSet.start();
            updateLabels();
        }
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
        if (mTouchAnimator.isRunning()) {
            mTouchAnimator.cancel();
        }
        mTouchAnimator.start();
    }

    public boolean touchInSeekBar(MotionEvent motionEvent, View view) {
        float x2 = motionEvent.getX();
        float y2 = motionEvent.getY();
        return x2 >= ((float) view.getPaddingLeft()) && x2 <= ((float) (view.getWidth() - view.getPaddingRight())) && y2 >= 0.0f && y2 <= ((float) view.getHeight());
    }

    private void updateLabels() {
        updateLabelPivots();

        switch (labelBehavior) {
            case LABEL_GONE:
                ensureLabelsRemoved();
                break;
            case LABEL_VISIBLE:
                if (isEnabled() && isSliderVisibleOnScreen() && mIsDragging) {
                    ensureLabelsAdded();
                } else {
                    ensureLabelsRemoved();
                }
                break;
            case LABEL_FLOATING:
            case LABEL_WITHIN_BOUNDS:
                if (activeThumbIdx != -1 && isEnabled()) {
                    ensureLabelsAdded();
                } else {
                    ensureLabelsRemoved();
                }
                break;
            default:
                throw new IllegalArgumentException("Unexpected labelBehavior: " + labelBehavior);
        }
    }

    private void updateLabelPivots() {
        // Set the pivot point so that the label pops up in the direction from the thumb.
        final float labelPivotX;
        final float labelPivotY;

        final boolean isRtl = isLayoutRtl();
        if (isRtl) {
            labelPivotX = RIGHT_LABEL_PIVOT_X;
            labelPivotY = RIGHT_LABEL_PIVOT_Y;
        } else {
            labelPivotX = TOP_LABEL_PIVOT_X;
            labelPivotY = TOP_LABEL_PIVOT_Y;
        }

        for (TooltipDrawable label : labels) {
            label.setPivots(labelPivotX, labelPivotY);
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        for (TooltipDrawable label : labels) {
            if (label.isStateful()) {
                label.setState(getDrawableState());
            }
        }
    }

    private void ensureLabelsAdded() {
        // If the labels are not animating in, start an animator to show them. ensureLabelsAdded will
        // be called multiple times by BaseSlider's draw method, making this check necessary to avoid
        // creating and starting an animator for each draw call.
        if (!labelsAreAnimatedIn) {
            labelsAreAnimatedIn = true;
            labelsInAnimator = createLabelAnimator(true);
            labelsOutAnimator = null;
            labelsInAnimator.start();
        }

        Iterator<TooltipDrawable> labelItr = labels.iterator();

        for (int i = 0; i < values.size() && labelItr.hasNext(); i++) {
            if (i == focusedThumbIdx) {
                // We position the focused thumb last so it's displayed on top, so skip it for now.
                continue;
            }

            setValueForLabel(labelItr.next(), values.get(i));
        }

        if (!labelItr.hasNext()) {
            throw new IllegalStateException(
                    String.format(
                            "Not enough labels(%d) to display all the values(%d)", labels.size(), values.size()));
        }

        // Now set the label for the focused thumb so it's on top.
        setValueForLabel(labelItr.next(), values.get(focusedThumbIdx));
    }

    @Override
    protected void onFocusChanged(
            boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (!gainFocus) {
            activeThumbIdx = -1;
            mAccessibilityHelper.clearKeyboardFocusForVirtualView(focusedThumbIdx);
        } else {
            focusThumbOnFocusGained(direction);
            mAccessibilityHelper.requestKeyboardFocusForVirtualView(focusedThumbIdx);
        }
    }

    private void focusThumbOnFocusGained(int direction) {
        switch (direction) {
            case FOCUS_BACKWARD:
                moveFocus(Integer.MAX_VALUE);
                break;
            case FOCUS_LEFT:
                moveFocusInAbsoluteDirection(Integer.MAX_VALUE);
                break;
            case FOCUS_FORWARD:
                moveFocus(Integer.MIN_VALUE);
                break;
            case FOCUS_RIGHT:
                moveFocusInAbsoluteDirection(Integer.MIN_VALUE);
                break;
            case FOCUS_UP:
            case FOCUS_DOWN:
            default:
                // Don't make assumptions about where exactly focus came from. Use previously focused thumb.
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (!isEnabled()) {
            return super.onKeyDown(keyCode, event);
        }

        // If there's only one thumb, we can select it right away.
        if (values.size() == 1) {
            activeThumbIdx = 0;
        }

        // If there is no active thumb, key events will be used to pick the thumb to change.
        if (activeThumbIdx == -1) {
            Boolean handled = onKeyDownNoActiveThumb(keyCode, event);
            return handled != null ? handled : super.onKeyDown(keyCode, event);
        }

        isLongPress |= event.isLongPress();
        Float increment = calculateIncrementForKey(keyCode);
        if (increment != null) {
            if (snapActiveThumbToValue(values.get(activeThumbIdx) + increment)) {
                postInvalidate();
            }
            return true;
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_TAB:
                if (event.hasNoModifiers()) {
                    return moveFocus(1);
                }

                if (event.isShiftPressed()) {
                    return moveFocus(-1);
                }
                return false;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                activeThumbIdx = -1;
                postInvalidate();
                return true;
            default:
                // Nothing to do in this case.
        }

        return super.onKeyDown(keyCode, event);
    }

    @Nullable
    private Float calculateIncrementForKey(int keyCode) {
        // If this is a long press, increase the increment so it will only take around 20 steps.
        // Otherwise choose the smallest valid increment.
        float increment = isLongPress ? calculateStepIncrement(20) : calculateStepIncrement();
        return switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT -> isLayoutRtl() ? increment : -increment;
            case KeyEvent.KEYCODE_DPAD_RIGHT -> isLayoutRtl() ? -increment : increment;
            case KeyEvent.KEYCODE_MINUS -> -increment;
            // Numpad Plus == Shift + Equals, at least in AVD, so fall through.
            case KeyEvent.KEYCODE_EQUALS, KeyEvent.KEYCODE_PLUS -> increment;
            default -> null;
        };
    }

    /**
     * Returns a small valid step increment to use when adding an offset to an existing value
     */
    private float calculateStepIncrement() {
        return stepSize == 0 ? 1 : stepSize;
    }

    /**
     * Returns a valid increment based on the {@code stepSize} (if it's set) that will allow
     * approximately {@code stepFactor} steps to cover the whole range.
     */
    private float calculateStepIncrement(int stepFactor) {
        float increment = calculateStepIncrement();
        float numSteps = (valueTo - valueFrom) / increment;
        if (numSteps <= stepFactor) {
            return increment;
        }

        return Math.round((numSteps / stepFactor)) * increment;
    }

    @Nullable
    private Boolean onKeyDownNoActiveThumb(int keyCode, @NonNull KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_TAB:
                if (event.hasNoModifiers()) {
                    return moveFocus(1);
                }

                if (event.isShiftPressed()) {
                    return moveFocus(-1);
                }
                return false;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                moveFocusInAbsoluteDirection(-1);
                return true;
            case KeyEvent.KEYCODE_MINUS:
                moveFocus(-1);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                moveFocusInAbsoluteDirection(1);
                return true;
            case KeyEvent.KEYCODE_EQUALS:
                // Numpad Plus == Shift + Equals, at least in AVD, so fall through.
            case KeyEvent.KEYCODE_PLUS:
                moveFocus(1);
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                activeThumbIdx = focusedThumbIdx;
                postInvalidate();
                return true;
            default:
                // Nothing to do in this case.
        }

        return null;
    }

    /**
     * Attempts to move focus to the <i>left or right</i> of currently focused thumb and returns
     * whether the focused thumb changed. If focused thumb didn't change, we're at the view boundary
     * for specified {@code direction} and focus may be moved to next or previous view instead.
     *
     * @see #moveFocus(int)
     */
    private boolean moveFocusInAbsoluteDirection(int direction) {
        if (isLayoutRtl()) {
            // Prevent integer overflow.
            direction = direction == Integer.MIN_VALUE ? Integer.MAX_VALUE : -direction;
        }
        return moveFocus(direction);
    }

    /**
     * Attempts to move focus to next or previous thumb <i>independent of layout direction</i> and
     * returns whether the focused thumb changed. If focused thumb didn't change, we're at the view
     * boundary for specified {@code direction} and focus may be moved to next or previous view
     * instead.
     *
     * @see #moveFocusInAbsoluteDirection(int)
     */
    private boolean moveFocus(int direction) {
        int oldFocusedThumbIdx = focusedThumbIdx;
        // Prevent integer overflow.
        final long newFocusedThumbIdx = (long) oldFocusedThumbIdx + direction;
        focusedThumbIdx = (int) clamp(newFocusedThumbIdx, 0, values.size() - 1);
        if (focusedThumbIdx == oldFocusedThumbIdx) {
            // Move focus to next or previous view.
            return false;
        }
        if (activeThumbIdx != -1) {
            activeThumbIdx = focusedThumbIdx;
        }
        postInvalidate();
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        isLongPress = false;
        return super.onKeyUp(keyCode, event);
    }

    public void onStartTrackingTouch(boolean fromUser) {
        mIsDragging = true;
        mStartDragging = true;
        if (!fromUser || mOnSliderChangeListeners.isEmpty()) {
            return;
        }
        dispatchOnStartTrackingTouch();
    }

    public void onStopTrackingTouch(boolean fromUser) {
        mIsDragging = false;
        mStartDragging = false;
        invalidate();
        if (!fromUser || mOnSliderChangeListeners.isEmpty()) {
            return;
        }
        dispatchOnStopTrackingTouch();
    }

    public void setProgress(float i2, boolean animate) {
        setProgress(i2, animate, false);
    }

    public void setProgress(float progress, boolean animate, boolean fromUser) {
        if (stepSize > 0) {
            progress = valueFrom + (Math.round((progress - valueFrom) / stepSize) * stepSize);
        }
        mOldProgress = mProgress;

        float clampedProgress = Math.max(valueFrom, Math.min(progress, valueTo));
        if (mOldProgress != clampedProgress) {
            if (animate) {
                startTransitionAnim(clampedProgress, fromUser);
            } else {
                setLocalProgress(clampedProgress);
                mOldProgress = clampedProgress;

                float diff = valueTo - valueFrom;
                mScale = diff > 0 ? (mProgress - valueFrom) / diff : 0.0f;
                dispatchChangeListener(fromUser);
                invalidate();
            }

            resetDeformationValue();
        }
    }

    /**
     * Determines the behavior of the label which can be any of the following.
     *
     * <ul>
     *   <li>{@code LABEL_FLOATING}: The label will only be visible on interaction. It will float
     *       above the mOplusSlider and may cover views above this one. This is the default and recommended
     *       behavior.
     *   <li>{@code LABEL_WITHIN_BOUNDS}: The label will only be visible on interaction. The label
     *       will always be drawn within the bounds of this view. This means extra space will be
     *       visible above the mOplusSlider when the label is not visible.
     *   <li>{@code LABEL_GONE}: The label will never be drawn.
     *   <li>{@code LABEL_VISIBLE}: The label will never be hidden.
     * </ul>
     */
    @IntDef({LABEL_FLOATING, LABEL_WITHIN_BOUNDS, LABEL_GONE, LABEL_VISIBLE})
    @Retention(RetentionPolicy.SOURCE)
    @interface LabelBehavior {
    }

    @IntDef({UNIT_PX, UNIT_VALUE})
    @Retention(RetentionPolicy.SOURCE)
    @interface SeparationUnit {
    }

    /**
     * Interface definition for a callback to be invoked when the slider's value changes.
     */
    public interface OnSliderChangeListener {
        void onProgressChanged(OplusSlider oplusSlider, boolean fromUser);

        void onStartTrackingTouch(OplusSlider oplusSlider);

        void onStopTrackingTouch(OplusSlider oplusSlider);
    }

    /**
     * Interface definition for a callback invoked when a slider's value is changed.
     */
    public interface OnChangeListener {

        /** Called when the value of the slider changes. */
        void onValueChange(@NonNull OplusSlider oplusSlider, float value, boolean fromUser);
    }

    public interface OnDeformedListener {
        default void onScaleChanged(DeformedValueBean deformedValueBean) {}

        default void onHeightDeformedChanged(float topDeformation, float bottomDeformation) {}
    }

    public static class AccessibilityHelper extends ExploreByTouchHelper {

        final Rect virtualViewBounds = new Rect();
        private final OplusSlider slider;

        AccessibilityHelper(OplusSlider slider) {
            super(slider);
            this.slider = slider;
        }

        @Override
        protected int getVirtualViewAt(float x, float y) {
            for (int i = 0; i < slider.getValues().size(); i++) {
                slider.updateBoundsForVirtualViewId(i, virtualViewBounds);
                if (virtualViewBounds.contains((int) x, (int) y)) {
                    return i;
                }
            }

            return ExploreByTouchHelper.HOST_ID;
        }

        @Override
        protected void getVisibleVirtualViews(@NonNull List<Integer> virtualViewIds) {
            for (int i = 0; i < slider.getValues().size(); i++) {
                virtualViewIds.add(i);
            }
        }

        @Override
        protected void onPopulateNodeForVirtualView(
                int virtualViewId, @NonNull AccessibilityNodeInfoCompat info) {

            info.addAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SET_PROGRESS);

            List<Float> values = slider.getValues();
            float value = values.get(virtualViewId);
            float valueFrom = slider.getValueFrom();
            float valueTo = slider.getValueTo();

            if (slider.isEnabled()) {
                if (value > valueFrom) {
                    info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD);
                }
                if (value < valueTo) {
                    info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD);
                }
            }

            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMaximumFractionDigits(2);
            try {
                valueFrom = nf.parse(nf.format(valueFrom)).floatValue();
                valueTo = nf.parse(nf.format(valueTo)).floatValue();
                value = nf.parse(nf.format(value)).floatValue();
            } catch (ParseException e) {
                Log.w(TAG, String.format(WARNING_PARSE_ERROR, value, valueFrom, valueTo));
            }

            info.setRangeInfo(AccessibilityNodeInfoCompat.RangeInfoCompat.obtain(RANGE_TYPE_FLOAT, valueFrom, valueTo, value));
            info.setClassName(SeekBar.class.getName());
            StringBuilder contentDescription = new StringBuilder();
            // Add the content description of the mOplusSlider.
            if (slider.getContentDescription() != null) {
                contentDescription.append(slider.getContentDescription()).append(",");
            }
            // Add the range/value to the content description.
            String verbalValue = slider.formatValue(value);
            String verbalValueType = ("R.string.material_slider_value");
            if (values.size() > 1) {
                verbalValueType = startOrEndDescription(virtualViewId);
            }
            CharSequence stateDescription = ViewCompat.getStateDescription(slider);
            if (!TextUtils.isEmpty(stateDescription)) {
                info.setStateDescription(stateDescription);
            } else {
                contentDescription.append(
                        String.format(Locale.getDefault(), "%s, %s", verbalValueType, verbalValue));
            }
            info.setContentDescription(contentDescription.toString());

            slider.updateBoundsForVirtualViewId(virtualViewId, virtualViewBounds);
            info.setBoundsInParent(virtualViewBounds);
        }

        @SuppressLint("PrivateResource")
        @NonNull
        private String startOrEndDescription(int virtualViewId) {
            List<Float> values = slider.getValues();
            if (virtualViewId == values.size() - 1) {
                return slider.getContext().getString(com.google.android.material.R.string.material_slider_range_end);
            }

            if (virtualViewId == 0) {
                return slider.getContext().getString(com.google.android.material.R.string.material_slider_range_start);
            }

            return "";
        }

        @Override
        protected boolean onPerformActionForVirtualView(
                int virtualViewId, int action, @Nullable Bundle arguments) {
            if (!slider.isEnabled()) {
                return false;
            }

            switch (action) {
                case android.R.id.accessibilityActionSetProgress: {
                    if (arguments == null
                            || !arguments.containsKey(
                            AccessibilityNodeInfoCompat.ACTION_ARGUMENT_PROGRESS_VALUE)) {
                        return false;
                    }
                    float value =
                            arguments.getFloat(AccessibilityNodeInfoCompat.ACTION_ARGUMENT_PROGRESS_VALUE);
                    if (slider.snapThumbToValue(virtualViewId, value)) {
                        slider.postInvalidate();
                        invalidateVirtualView(virtualViewId);
                        return true;
                    }
                    return false;
                }
                case AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD:
                case AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD: {
                    float increment = slider.calculateStepIncrement(20);
                    if (action == AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD) {
                        increment = -increment;
                    }

                    // Swap the increment if we're in RTL.
                    if (slider.isLayoutRtl()) {
                        increment = -increment;
                    }

                    List<Float> values = slider.getValues();
                    float clamped =
                            clamp(
                                    values.get(virtualViewId) + increment,
                                    slider.getValueFrom(),
                                    slider.getValueTo());
                    if (slider.snapThumbToValue(virtualViewId, clamped)) {
                        slider.setActiveThumbIndex(virtualViewId);
                        slider.scheduleTooltipTimeout();
                        slider.postInvalidate();
                        invalidateVirtualView(virtualViewId);
                        return true;
                    }
                    return false;
                }
                default:
                    return false;
            }
        }
    }

    static class SliderState extends BaseSavedState {

        public static final Creator<SliderState> CREATOR =
                new Creator<>() {

                    @NonNull
                    @Override
                    public SliderState createFromParcel(@NonNull Parcel source) {
                        return new SliderState(source);
                    }

                    @NonNull
                    @Override
                    public SliderState[] newArray(int size) {
                        return new SliderState[size];
                    }
                };
        float valueFrom;
        float valueTo;
        ArrayList<Float> values;
        float stepSize;
        boolean hasFocus;

        SliderState(Parcelable superState) {
            super(superState);
        }

        private SliderState(@NonNull Parcel source) {
            super(source);
            valueFrom = source.readFloat();
            valueTo = source.readFloat();
            values = new ArrayList<>();
            source.readList(values, Float.class.getClassLoader());
            stepSize = source.readFloat();
            hasFocus = source.createBooleanArray()[0];
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeFloat(valueFrom);
            dest.writeFloat(valueTo);
            dest.writeList(values);
            dest.writeFloat(stepSize);
            boolean[] booleans = new boolean[1];
            booleans[0] = hasFocus;
            dest.writeBooleanArray(booleans);
        }
    }

    /**
     * Command for sending an accessibility event.
     */
    private class AccessibilityEventSender implements Runnable {
        int virtualViewId = -1;

        void setVirtualViewId(int virtualViewId) {
            this.virtualViewId = virtualViewId;
        }

        @Override
        public void run() {
            mAccessibilityHelper.sendEventForVirtualView(
                    virtualViewId, AccessibilityEvent.TYPE_VIEW_SELECTED);
        }
    }
}
