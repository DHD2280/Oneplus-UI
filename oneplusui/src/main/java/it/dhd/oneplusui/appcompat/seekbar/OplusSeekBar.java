package it.dhd.oneplusui.appcompat.seekbar;

import static android.os.VibrationEffect.EFFECT_HEAVY_CLICK;
import static android.os.VibrationEffect.EFFECT_TICK;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Interpolator;
import android.widget.AbsSeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.animation.PathInterpolatorCompat;
import androidx.customview.widget.ExploreByTouchHelper;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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

/**
 * A seek bar that supports deformation and physics-based animations.
 */
public class OplusSeekBar extends AbsSeekBar implements AnimationListener, AnimationUpdateListener {

    public static final int MOVE_BY_DEFAULT = 0;
    public static final int MOVE_BY_DISTANCE = 2;
    public static final int MOVE_BY_FINGER = 1;
    public final static double DOUBLE_EPSILON = Double.longBitsToDouble(1);
    protected static final int RELEASE_ANIM_DURATION = 183;
    protected static final Interpolator THUMB_ANIMATE_INTERPOLATOR = new OplusMoveEaseInterpolator();
    protected static final Interpolator PROGRESS_SCALE_INTERPOLATOR = new OplusEaseInterpolator();
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
    private static final String TAG = "OplusSeekBar";
    private static final int TOUCH_ANIMATION_ENLARGE_DURATION = 183;
    private static final int VELOCITY_COMPUTE_TIME = 100;
    private final RectF mBackgroundRect;
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
    protected float mLabelX;
    protected float mLastX;
    protected int mMax;
    protected int mMin;
    protected int mOldProgress;
    protected float mPaddingHorizontal;
    protected Paint mPaint;
    protected int mProgress;
    protected int mProgressColor;
    protected float mProgressEnlargeScale;
    protected float mProgressHeight;
    protected float mProgressRadius;
    protected RectF mProgressRect;
    protected float mProgressRoundCornerWeight;
    protected Interpolator mProgressScaleInterpolator;
    protected float mScale;
    protected boolean mShowProgress;
    protected boolean mShowThumb;
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
    private float mCurBottomDeformationValue;
    private float mCurTopDeformationValue;
    private float mCustomProgressAnimDuration;
    private Interpolator mCustomProgressAnimInterpolator;
    private float mDamping;
    private PatternExploreByTouchHelper mExploreByTouchHelper;
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
    private int mIncrement;
    private final int mInnerShadowRadiusSize;
    private Interpolator mInterpolator;
    private boolean mIsPhysicsEnable;
    private boolean mIsProgressFull;
    private boolean mIsStartFromMiddle;
    private boolean mIsSupportDeformation;
    private float mMaxHeightDeformedValue;
    private int mMaxMovingDistance;
    private final int mMaxWidth;
    private float mMaxWidthDeformedValue;
    private int mMoveType;
    private OnDeformedListener mOnDeformedListener;
    private List<OnSeekBarChangeListener> mOnSeekBarChangeListeners = new ArrayList<>();
    private PhysicalAnimator mPhysicalAnimator;
    private String mProgressContentDescription;
    private int mRealProgress;
    private int mRefreshStyle;
    private final int mSeekbarMinHeight;
    private final int mShadowColor;
    private final int mShadowRadiusSize;
    private boolean mStartDragging;
    private Bitmap mThumbBitmap;
    private int mThumbShadowRadiusSize;
    private VelocityTracker mVelocityTracker;
    private ExecutorService mVibratorExecutor;
    private float mWidthDeformedValue;


    public OplusSeekBar(Context context) {
        this(context, null);
    }

    public OplusSeekBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.oplusSeekBarStyle);
    }

    public OplusSeekBar(Context context, AttributeSet attributeSet, int defStyleAttr) {
        this(context, attributeSet, defStyleAttr, R.style.Widget_Oplus_SeekBar);
    }

    public OplusSeekBar(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);
        mScale = 0.0f;
        mEnableVibrator = true;
        mEnableAdaptiveVibrator = true;
        mHasMotorVibrator = true;
        mVibrator = null;
        mTouchSlop = 0;
        mProgress = 0;
        mOldProgress = 0;
        mMax = 100;
        mMin = 0;
        mIsDragging = false;
        mProgressColorStateList = null;
        mBackgroundColorStateList = null;
        mThumbColorStateList = null;
        mIsProgressFull = false;
        mCustomProgressAnimDuration = -1.0f;
        mCustomProgressAnimInterpolator = null;
        mClipProgressPath = new Path();
        mClipProgressRect = new RectF();
        mProgressRect = new RectF();
        mTempRect = new RectF();
        mTouchAnimator = new AnimatorSet();
        mProgressScaleInterpolator = PathInterpolatorCompat.create(0.33f, 0.0f, 0.67f, 1.0f);
        mThumbAnimateInterpolator = PathInterpolatorCompat.create(0.3f, 0.0f, 0.1f, 1.0f);
        mShowProgress = false;
        mShowThumb = false;
        mIncrement = 1;
        mStartDragging = false;
        mBackgroundRect = new RectF();
        mMoveType = MOVE_BY_FINGER;
        mFastMoveSpringConfig = SpringConfig.fromOrigamiTensionAndFriction(500.0d, 30.0d);
        mIsStartFromMiddle = false;
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
        TypedArray attributes = context.obtainStyledAttributes(attributeSet, R.styleable.OplusSeekBar, defStyleAttr, defStyleRes);
        mEnableVibrator = attributes.getBoolean(R.styleable.OplusSeekBar_oplusSeekBarEnableVibrator, true);
        mEnableAdaptiveVibrator = attributes.getBoolean(R.styleable.OplusSeekBar_oplusSeekBarAdaptiveVibrator, false);
        mIsPhysicsEnable = attributes.getBoolean(R.styleable.OplusSeekBar_oplusSeekBarPhysicsEnable, true);
        mShowProgress = attributes.getBoolean(R.styleable.OplusSeekBar_oplusSeekBarShowProgress, true);
        mShowThumb = attributes.getBoolean(R.styleable.OplusSeekBar_oplusSeekBarShowThumb, true);
        mIsStartFromMiddle = attributes.getBoolean(R.styleable.OplusSeekBar_oplusSeekBarStartMiddle, false);
        mIsProgressFull = attributes.getBoolean(R.styleable.OplusSeekBar_oplusSeekBarProgressFull, false);
        mBackgroundColorStateList = attributes.getColorStateList(R.styleable.OplusSeekBar_oplusSeekBarBackgroundColor);
        mProgressColorStateList = attributes.getColorStateList(R.styleable.OplusSeekBar_oplusSeekBarProgressColor);
        mThumbColorStateList = attributes.getColorStateList(R.styleable.OplusSeekBar_oplusSeekBarThumbColor);
        mBackgroundColor = getColor(this, mBackgroundColorStateList, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_background_color_normal));
        ColorStateList colorStateList = mProgressColorStateList;
        mProgressColor = getColor(this, colorStateList, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_progress_color_normal));
        mThumbColor = getColor(this, mThumbColorStateList, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_progress_color_normal));
        mShadowColor = attributes.getColor(R.styleable.OplusSeekBar_oplusSeekBarShadowColor, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_shadow_color));
        mThumbShadowColor = attributes.getColor(R.styleable.OplusSeekBar_oplusSeekBarThumbShadowColor, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_thumb_shadow_color));
        mBackgroundRadius = attributes.getDimension(R.styleable.OplusSeekBar_oplusSeekBarBackgroundRadius, getResources().getDimension(R.dimen.oplus_seekbar_background_radius));
        mProgressRadius = attributes.getDimension(R.styleable.OplusSeekBar_oplusSeekBarProgressRadius, getResources().getDimension(R.dimen.oplus_seekbar_progress_radius));
        mBackgroundRoundCornerWeight = attributes.getFloat(R.styleable.OplusSeekBar_oplusSeekBarBackgroundRoundCornerWeight, 0.0f);
        mProgressRoundCornerWeight = attributes.getFloat(R.styleable.OplusSeekBar_oplusSeekBarProgressRoundCornerWeight, 0.0f);
        mShadowRadiusSize = attributes.getDimensionPixelSize(R.styleable.OplusSeekBar_oplusSeekBarShadowSize, 0);
        mThumbShadowRadiusSize = attributes.getDimensionPixelSize(R.styleable.OplusSeekBar_oplusSeekBarThumbShadowSize, 0);
        mInnerShadowRadiusSize = attributes.getDimensionPixelSize(R.styleable.OplusSeekBar_oplusSeekBarInnerShadowSize, 0);
        mPaddingHorizontal = attributes.getDimensionPixelOffset(R.styleable.OplusSeekBar_oplusSeekBarProgressPaddingHorizontal, getResources().getDimensionPixelSize(R.dimen.oplus_seekbar_progress_padding_horizontal));
        mBackgroundHeight = attributes.getDimensionPixelSize(R.styleable.OplusSeekBar_oplusSeekBarBackgroundHeight, (int) (mBackgroundRadius * SCALE_DEFORMATION_MAX));
        mProgressHeight = attributes.getDimensionPixelSize(R.styleable.OplusSeekBar_oplusSeekBarProgressHeight, (int) (mProgressRadius * SCALE_DEFORMATION_MAX));
        mSeekbarMinHeight = attributes.getDimensionPixelOffset(R.styleable.OplusSeekBar_oplusSeekBarMinHeight, getResources().getDimensionPixelSize(R.dimen.oplus_seekbar_view_min_height));
        mMaxWidth = attributes.getDimensionPixelSize(R.styleable.OplusSeekBar_oplusSeekBarMaxWidth, 0);
        mBackgroundEnlargeScale = attributes.getFloat(R.styleable.OplusSeekBar_oplusSeekBarBackGroundEnlargeScale, BACKGROUND_RADIUS_SCALE);
        mProgressEnlargeScale = attributes.getFloat(R.styleable.OplusSeekBar_oplusSeekBarProgressEnlargeScale, PROGRESS_RADIUS_SCALE);
        mIsSupportDeformation = attributes.getBoolean(R.styleable.OplusSeekBar_oplusSeekBarDeformation, false);
        attributes.recycle();
        mVibrator = getContext().getSystemService(Vibrator.class);
        mHasMotorVibrator = mVibrator.hasVibrator();
        initView();
        ensureSize();
        initAnimation();
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

    private float calculateDamping(float x) {
        if (mDamping != 0.0f) {
            return mDamping;
        }
        float seekBarWidth = getSeekBarWidth();
        float center = seekBarWidth / 2.0f;
        float interpolation = 1.0f - mInterpolator.getInterpolation(Math.abs(x - center) / center);
        if (x > seekBarWidth - getPaddingRight() || x < getPaddingLeft() || interpolation < MAX_MOVE_DAMPING) {
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
        if (mScale > 1.0f) {
            double d2 = (mScale - 1.0f) / 5.0f;
            mHeightBottomDeformedUpValue = computeValue(d2, mMaxMovingDistance);
            mHeightTopDeformedUpValue = computeValue(d2, mMaxMovingDistance + mMaxHeightDeformedValue);
            mWidthDeformedValue = computeValue(d2, mMaxWidthDeformedValue);
            heightDeformedChanged();
            return;
        }
        if (mScale < 0.0f) {
            double abs = Math.abs(mScale) / 5.0f;
            mHeightTopDeformedDownValue = computeValue(abs, mMaxMovingDistance);
            mHeightBottomDeformedDownValue = computeValue(abs, mMaxMovingDistance + mMaxHeightDeformedValue);
            mWidthDeformedValue = computeValue(abs, mMaxWidthDeformedValue);
            heightDeformedChanged();
        }
    }

    private void clearDeformationValue() {
        if (mProgress <= mMin || mProgress >= mMax) {
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
                    ((float) i2 - (mCurProgressHeight / 2.0f)) - ((float) mInnerShadowRadiusSize / 2),
                    ((float) mInnerShadowRadiusSize / 2) + f3 + mCurProgressRadius,
                    (float) i2 + (mCurProgressHeight / 2.0f) + ((float) mInnerShadowRadiusSize / 2));
            canvas.drawRoundRect(mProgressRect, mCurProgressRadius, mCurProgressRadius, mPaint);
            mPaint.clearShadowLayer();
            mPaint.setStyle(Paint.Style.FILL);
        }
        mPaint.setColor(mProgressColor);
        if (mIsStartFromMiddle && f2 > f3) {
            mProgressRect.set(f3, (float) i2 - (mCurProgressHeight / 2.0f), f2, (float) i2 + (mCurProgressHeight / 2.0f));
        } else if (isLayoutRtl()) {
            float f11 = f2 - mHeightTopDeformedUpValue;
            mProgressRect.set(
                    f11 + mHeightBottomDeformedDownValue,
                    (float) i2 - ((mCurProgressHeight / 2.0f) - mWidthDeformedValue),
                    (f3 - mHeightBottomDeformedUpValue) + mHeightBottomDeformedDownValue,
                    (float) i2 + ((mCurProgressHeight / 2.0f) - mWidthDeformedValue));
        } else {
            float f17 = (f2 - mHeightBottomDeformedDownValue) + mHeightBottomDeformedUpValue;
            mProgressRect.set(
                    f17,
                    (float) i2 - ((mCurProgressHeight / 2.0f) - mWidthDeformedValue),
                    (f3 + mHeightTopDeformedUpValue) - mHeightBottomDeformedDownValue,
                    (float) i2 + ((mCurProgressHeight / 2.0f) - mWidthDeformedValue));
        }
        mClipProgressPath.reset();
        mClipProgressPath.addRoundRect(mClipProgressRect, mCurProgressRadius, mCurProgressRadius, Path.Direction.CCW);
        canvas.save();
        canvas.clipPath(mClipProgressPath);
        if (mShowThumb) {
            float f23 = mProgressRect.left;
            mProgressRect.left = f23 - (mThumbOutHeight / 2.0f);
            mProgressRect.right += mThumbOutHeight / 2.0f;
            canvas.drawRoundRect(mProgressRect, mCurProgressRadius, mCurProgressRadius, mPaint);
        } else {
            canvas.drawRect(mProgressRect, mPaint);
        }
        canvas.restore();
    }


    private void drawThumb(Canvas canvas, int i2, float f2, float f3) {
        Bitmap bitmap;
        if (mThumbShadowRadiusSize > 0 && mCurProgressRadius < mThumbOutRadius) {
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setShadowLayer(mThumbShadowRadiusSize, 0.0f, 8.0f, mShadowColor);
        }
        if (getThumb() == null || (bitmap = mThumbBitmap) == null) {
            mPaint.setColor(mThumbColor);
            float f5 = mThumbOutHeight;
            float f6 = mThumbOutRadius;
            canvas.drawRoundRect(f2, (float) i2 - (f5 / 2.0f), f3, (float) i2 + (f5 / 2.0f), f6, f6, mPaint);
        } else {
            canvas.drawBitmap(bitmap, f2, i2 - (mThumbOutHeight / 2.0f), mPaint);
        }
        mPaint.clearShadowLayer();
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        int max = Math.max(1, drawable.getIntrinsicHeight());
        int max2 = Math.max(1, drawable.getIntrinsicWidth());
        Bitmap createBitmap = Bitmap.createBitmap(max2, max, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        drawable.setBounds(0, 0, max2, max);
        drawable.draw(canvas);
        return createBitmap;
    }

    private void ensureSize() {
        resetProgressSize();
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
            float f2 = mScale;
            if (f2 > 1.0f || f2 < 0.0f) {
                int normalSeekBarWidth = getNormalSeekBarWidth();
                int i2 = mMax - mMin;
                float f3 = i2 > 0 ? normalSeekBarWidth / i2 : 0.0f;
                if (isLayoutRtl()) {
                    mFlingValueHolder.setStartValue((mMax - (getDeformationFlingScale() * i2)) * f3);
                } else {
                    mFlingValueHolder.setStartValue(getDeformationFlingScale() * i2 * f3);
                }
                mFlingBehavior.start();
            }
        }
    }

    private void flingBehaviorAfterEndDrag(float f2) {
        int normalSeekBarWidth = getNormalSeekBarWidth();
        int i2 = mMax - mMin;
        float f3 = i2 > 0 ? (float) normalSeekBarWidth / i2 : 0.0f;
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
        return (int) (((getWidth() - getStart()) - getEnd()) - (mPaddingHorizontal * 2.0f));
    }

    private int getProgressLimit(int i2) {
        int i3 = mMax;
        int i4 = mMin;
        int i5 = i3 - i4;
        return Math.max(i4 - i5, Math.min(i2, i3 + i5));
    }

    private int getRealProgress(int i2) {
        return Math.max(mMin, Math.min(i2, mMax));
    }

    private float getRealScale(float f2) {
        return Math.max(0.0f, Math.min(f2, 1.0f));
    }

    private void heightDeformedChanged() {
        if (mOnDeformedListener != null) {
            boolean z2 = topDeformedChange();
            boolean bottomDeformedChange = bottomDeformedChange();
            if (z2 || bottomDeformedChange) {
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
        PatternExploreByTouchHelper patternExploreByTouchHelper = new PatternExploreByTouchHelper(this);
        mExploreByTouchHelper = patternExploreByTouchHelper;
        ViewCompat.setAccessibilityDelegate(this, patternExploreByTouchHelper);
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        mExploreByTouchHelper.invalidateRoot();
        Paint paint = new Paint();
        mPaint = paint;
        paint.setAntiAlias(true);
        mPaint.setDither(true);
        setThumbBitmap();
    }

    private void invalidateProgress(MotionEvent motionEvent) {
        float x2 = motionEvent.getX();
        float seekBarWidth = getSeekBarWidth();
        float f2 = mCurProgressRadius;
        float f3 = seekBarWidth + (2.0f * f2);
        float f4 = mCurPaddingHorizontal - f2;
        mScale = Math.max(0.0f, Math.min(isLayoutRtl() ? (((getWidth() - x2) - getStart()) - f4) / f3 : ((x2 - getStart()) - f4) / f3, 1.0f));
        int progressLimit = getProgressLimit(Math.round((mScale * (getMax() - getMin())) + getMin()));
        int i2 = mProgress;
        int i3 = mRealProgress;
        setLocalProgress(progressLimit);
        invalidate();
        if (i2 != mProgress) {
            dispatchProgressChanged(mRealProgress, true);
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
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
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

    private void resetProgressSize() {
        if (mIsProgressFull) {
            mProgressRadius = mBackgroundRadius;
            mProgressRoundCornerWeight = mBackgroundRoundCornerWeight;
            mProgressHeight = mBackgroundHeight;
            mProgressEnlargeScale = mBackgroundEnlargeScale;
        }
    }

    private void setDeformationScale(float deformationScale) {
        if (deformationScale > SCALE_MAX) {
            deformationScale = ((deformationScale - SCALE_MAX) * SCALE_DEFORMATION_TIMES) + SCALE_MAX;
        } else if (deformationScale < SCALE_MIN) {
            deformationScale *= SCALE_DEFORMATION_TIMES;
        }
        mScale = Math.max(SCALE_DEFORMATION_MIN, Math.min(deformationScale, SCALE_DEFORMATION_MAX));
    }

    private void setFlingScale(float flingScale) {
        if (!mIsSupportDeformation) {
            mScale = Math.max(SCALE_MIN, Math.min(flingScale, SCALE_MAX));
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

    private void setThumbBitmap() {
        if (getThumb() != null) {
            mThumbBitmap = drawableToBitmap(getThumb());
        }
    }

    private void setTouchScale(float touchScale) {
        if (!mIsSupportDeformation) {
            mScale = Math.max(SCALE_MIN, Math.min(touchScale, SCALE_MAX));
            return;
        }
        mScale = Math.max(SCALE_DEFORMATION_MIN, Math.min(touchScale, SCALE_DEFORMATION_MAX));
        calculateTouchDeformationValue();
        if (mOnDeformedListener != null) {
            DeformedValueBean deformedValueBean = new DeformedValueBean(mHeightBottomDeformedUpValue, mHeightTopDeformedUpValue, mWidthDeformedValue, mHeightBottomDeformedDownValue, mHeightTopDeformedDownValue, mProgress);
            deformedValueBean.setScale(mScale);
            mOnDeformedListener.onScaleChanged(deformedValueBean);
        }
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
                fastMoveSpring.setEndValue(DOUBLE_EPSILON);
                return;
            }
            if (mProgress > MAX_FAST_MOVE_PERCENT * (float) diff || mProgress < (float) diff * MIN_FAST_MOVE_PERCENT) {
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

    public final void trackTouchEvent(MotionEvent motionEvent) {
        float x = motionEvent.getX();
        float f2 = x - this.mLastX;
        int diff = this.mMax - this.mMin;
        if (isLayoutRtl()) {
            f2 = -f2;
        }
        setTouchScale((this.mProgress / (float) diff) + ((f2 * calculateDamping(x)) / getSeekBarWidth()));
        int progressLimit = getProgressLimit(Math.round((this.mScale * (float) diff) + getMin()));
        int oldProgress = this.mProgress;
        int oldRealProgress = this.mRealProgress;
        setLocalProgress(progressLimit);
        invalidate();
        if (oldProgress != this.mProgress) {
            this.mLastX = x;
            dispatchProgressChanged(mRealProgress, true);
            if (oldRealProgress != this.mRealProgress) {
                performFeedback();
            }
        }
        if (mVelocityTracker != null) {
            mVelocityTracker.computeCurrentVelocity(PHYSICAL_VELOCITY_LIMIT);
            startFastMoveAnimation(this.mVelocityTracker.getXVelocity());
        }
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
        scale = Math.max(0f, Math.min(1f, scale));
        int progress = Math.round(scale * (getMax() - getMin())) + getMin();
        progress = Math.max(getMin(), Math.min(progress, getMax()));
        mScale = scale;
        int i2 = this.mProgress;
        int i3 = this.mRealProgress;
        setLocalProgress(progress);
        invalidate();
        if (i2 != this.mProgress) {
            this.mLastX = round;
            dispatchProgressChanged(mRealProgress, true);
            if (i3 != this.mRealProgress) {
                performFeedback();
            }
        }

    }

    private void updateBehavior() {
        if (!mIsPhysicsEnable || mPhysicalAnimator == null || mFlingBehavior == null) {
            return;
        }
        int normalSeekBarWidth = getNormalSeekBarWidth();
        Log.i(TAG, "COUISeekBar updateBehavior : setActiveFrame:" + normalSeekBarWidth);
        mFlingBehavior.setActiveFrame(0.0f, (float) normalSeekBarWidth);
    }

    public void animForClick(float f2) {
        float seekBarWidth = getSeekBarWidth();
        float f3 = mCurProgressRadius;
        float f4 = seekBarWidth + (2.0f * mCurProgressRadius);
        float f5 = mCurPaddingHorizontal - f3;
        startTransitionAnim(getProgressLimit(Math.round(((isLayoutRtl() ? (((getWidth() - f2) - getStart()) - f5) / f4 : ((f2 - getStart()) - f5) / f4) * (getMax() - getMin())) + getMin())), true);
    }

    public void checkThumbPosChange(int i2) {
        checkThumbPosChange(i2, true, true);
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent motionEvent) {
        return super.dispatchHoverEvent(motionEvent);
    }

    public void drawActiveTrack(Canvas canvas, float f2) {
        float f3;
        float f4;
        float f5;
        float f6;
        float f7;
        float f8;
        float f9;
        int seekBarCenterY = getSeekBarCenterY();
        if (mShowThumb) {
            f3 = ((mThumbOutHeight / 2.0f) - mThumbOutRadius) + mCurPaddingHorizontal;
            float f13 = f2 - (mThumbOutHeight - (mThumbOutRadius * 2.0f));
            float f14 = mCurProgressRadius;
            float f15 = mCurPaddingHorizontal - f14;
            f4 = f2 + (f14 * 2.0f);
            f5 = f13;
            f6 = f15;
        } else {
            f5 = f2 + (mCurProgressRadius * 2.0f);
            f6 = mCurPaddingHorizontal - mCurProgressRadius;
            f3 = f6;
            f4 = f5;
        }
        mClipProgressRect.top = ((float) seekBarCenterY - (mCurProgressHeight / 2.0f)) + mWidthDeformedValue;
        mClipProgressRect.bottom = ((float) seekBarCenterY + (mCurProgressHeight / 2.0f)) - mWidthDeformedValue;
        if (mIsStartFromMiddle) {
            if (isLayoutRtl()) {
                f8 = getWidth() / 2.0f;
                f9 = f8 - ((getRealScale(mScale) - 0.5f) * f5);
                float f21 = f4 / 2.0f;
                mClipProgressRect.left = f8 - f21;
                mClipProgressRect.right = f21 + f8;
                f7 = f9;
            } else {
                float width = getWidth() / 2.0f;
                float realScale = width + ((getRealScale(mScale) - 0.5f) * f5);
                RectF rectF3 = mClipProgressRect;
                float f22 = f4 / 2.0f;
                mClipProgressRect.left = width - f22;
                mClipProgressRect.right = f22 + width;
                f7 = realScale;
                f9 = width;
                f8 = f7;
            }
        } else if (isLayoutRtl()) {
            float start = getStart() + f3 + f5;
            f9 = start - (getRealScale(mScale) * f5);
            float start2 = getStart() + f6 + f4;
            float f23 = mHeightBottomDeformedUpValue;
            mClipProgressRect.right = (start2 - f23) + mHeightBottomDeformedDownValue;
            mClipProgressRect.left = (mClipProgressRect.right - f4) - (mHeightTopDeformedUpValue - f23);
            f7 = f9;
            f8 = start;
        } else {
            float start3 = f3 + getStart();
            float realScale2 = start3 + (getRealScale(mScale) * f5);
            float start4 = getStart() + f6;
            float f24 = mHeightBottomDeformedDownValue;
            float f25 = mHeightBottomDeformedUpValue;
            mClipProgressRect.left = (start4 - f24) + f25;
            mClipProgressRect.right = ((((mClipProgressRect.left + f4) + mHeightTopDeformedUpValue) - f25) + f24) - mHeightTopDeformedDownValue;
            f7 = realScale2;
            f8 = f7;
            f9 = start3;
        }
        if (mShowProgress) {
            drawProgress(canvas, seekBarCenterY, f9, f8);
        }
        float f26 = mThumbOutHeight;
        float f27 = f7 - (f26 / 2.0f);
        float f28 = f7 + (f26 / 2.0f);
        mLabelX = ((f28 - f27) / 2.0f) + f27;
        if (mShowThumb) {
            drawThumb(canvas, seekBarCenterY, f27, f28);
        }
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
                    ((float) seekBarCenterY - (mCurBackgroundHeight / 2.0f)) - ((float) mShadowRadiusSize / 2),
                    ((float) mShadowRadiusSize / 2) + width,
                    (float) seekBarCenterY + (mCurBackgroundHeight / 2.0f) + ((float) mShadowRadiusSize / 2));
            canvas.drawRoundRect(mBackgroundRect, mCurBackgroundRadius, mCurBackgroundRadius, mPaint);
            mPaint.clearShadowLayer();
            mPaint.setStyle(Paint.Style.FILL);
        }
        mPaint.setColor(mBackgroundColor);
        if (isLayoutRtl()) {
            float f6 = (start - mHeightTopDeformedUpValue) + mHeightTopDeformedDownValue;
            mBackgroundRect.set(
                    f6,
                    (float) seekBarCenterY - ((mCurBackgroundHeight / 2.0f) - mWidthDeformedValue),
                    (width - mHeightBottomDeformedUpValue) + mHeightBottomDeformedDownValue,
                    (float) seekBarCenterY + ((mCurBackgroundHeight / 2.0f) - mWidthDeformedValue));
        } else {
            float f10 = (start - mHeightBottomDeformedDownValue) + mHeightBottomDeformedUpValue;
            float f12 = mCurBackgroundHeight;
            float f13 = mWidthDeformedValue;
            mBackgroundRect.set(f10, (float) seekBarCenterY - ((f12 / 2.0f) - f13), (width + mHeightTopDeformedUpValue) - mHeightTopDeformedDownValue, (float) seekBarCenterY + ((f12 / 2.0f) - f13));
        }
        float f14 = mCurBackgroundRadius;
        canvas.drawRoundRect(mBackgroundRect, f14, f14, mPaint);
    }

    public int getColor(View view, ColorStateList colorStateList, int i2) {
        return colorStateList == null ? i2 : colorStateList.getColorForState(view.getDrawableState(), i2);
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
        int localMin = Math.max(min, 0);
        if (min > getMax()) {
            localMin = getMax();
            Log.e(TAG, "setMin : the input params is greater than max. (inputMin:" + min + ",mMax:" + mMax + ")");
        }
        if (localMin != mMin) {
            setLocalMin(localMin);
            if (mProgress < localMin) {
                setProgress(localMin);
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

    public void setMoveType(int moveType) {
        mMoveType = moveType;
    }

    public int getProgress() {
        return mRealProgress;
    }

    @Override
    public void setProgress(int progress) {
        setProgress(progress, false);
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
        int diff = mMax - mMin;
        float f2 = (diff > 0 ? (this.mProgress * seekBarWidth) / diff : 0.0f) + mMin;
        if (this.mIsStartFromMiddle && Float.compare(f2, seekBarWidth / 2.0f) == 0 && Math.abs(motionEvent.getX() - this.mLastX) < DAMPING_DISTANCE) {
            return;
        }
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
                return;
            }
            return;
        }
        mIsDragging = false;
        mStartDragging = false;
        Log.i(TAG, "handleMotionEventUp mFlingVelocity = " + mFlingVelocity);
        if (!mIsPhysicsEnable || Math.abs(mFlingVelocity) < VELOCITY_COMPUTE_TIME) {
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
        float f2;
        Object animatedValue = baseBehavior.getAnimatedValue();
        if (animatedValue == null) {
            return;
        }
        float floatValue = ((Float) animatedValue).floatValue();
        int normalSeekBarWidth = getNormalSeekBarWidth();
        if (isLayoutRtl()) {
            float f3 = normalSeekBarWidth;
            f2 = (f3 - floatValue) / f3;
        } else {
            f2 = floatValue / normalSeekBarWidth;
        }
        setFlingScale(f2);
        float f4 = mProgress;
        setLocalProgress(getProgressLimit(Math.round((mMax - mMin) * mScale) + mMin));
        invalidate();
        if (f4 != mProgress) {
            mLastX = floatValue + getStart();
            dispatchProgressChanged(mRealProgress, true);
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopPhysicsMove();
    }

    @Override
    public void onDraw(Canvas canvas) {
        float seekBarWidth = getSeekBarWidth();
        drawInactiveTrack(canvas);
        drawActiveTrack(canvas, seekBarWidth);
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
        int mode = View.MeasureSpec.getMode(heightMeasureSpec);
        int size = View.MeasureSpec.getSize(heightMeasureSpec);
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);

        int seekbarHeight = mSeekbarMinHeight + getPaddingTop() + getPaddingBottom();
        if (mode == View.MeasureSpec.EXACTLY) {
            size = Math.max(size, seekbarHeight);
        } else {
            size = seekbarHeight;
        }
        if (mMaxWidth > 0 && widthSize > mMaxWidth) {
            widthSize = mMaxWidth;
        }

        setMeasuredDimension(widthSize, size);
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
        Log.i(TAG, "COUISeekBar, onTouchEvent: action=" + action);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
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
                clearDeformationValue();
                initVelocityTrackerIfNotExists();
                mVelocityTracker.addMovement(motionEvent);
                handleMotionEventMove(motionEvent);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mVelocityTracker != null) {
                    mVelocityTracker.computeCurrentVelocity(ONE_SECOND_UNITS, MAX_VELOCITY);
                    mFlingVelocity = mVelocityTracker.getXVelocity();
                    Log.i(TAG, "onTouchEvent ACTION_UP mFlingVelocity = " + mFlingVelocity);
                }
                recycleVelocityTracker();
                handleMotionEventUp(motionEvent);
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

    public void refresh() {
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
            mThumbShadowColor = typedArray.getColor(R.styleable.OplusSeekBar_oplusSeekBarThumbShadowColor, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_thumb_shadow_color));
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

    public void setBackgroundEnlargeScale(float enlargeScale) {
        mBackgroundEnlargeScale = enlargeScale;
        ensureSize();
        invalidate();
    }

    public void setBackgroundHeight(float backgroundHeight) {
        mBackgroundHeight = backgroundHeight;
        ensureSize();
        invalidate();
    }

    public void setBackgroundRadius(float f2) {
        mBackgroundRadius = f2;
        ensureSize();
        invalidate();
    }

    public void setBackgroundRoundCornerWeight(float backgroundRoundCornerWeight) {
        mBackgroundRoundCornerWeight = backgroundRoundCornerWeight;
        invalidate();
    }

    public void setCustomProgressAnimDuration(float progressAnimDuration) {
        if (progressAnimDuration <= 0.0f) {
            return;
        }
        mCustomProgressAnimDuration = progressAnimDuration;
    }

    public void setCustomProgressAnimInterpolator(Interpolator interpolator) {
        mCustomProgressAnimInterpolator = interpolator;
    }

    public void setDeformedListener(OnDeformedListener onDeformedListener) {
        mOnDeformedListener = onDeformedListener;
    }

    public void setDeformedParams(DeformedValueBean deformedValueBean) {
        mScale = deformedValueBean.getScale();
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
    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    public void setLocalMax(int max) {
        mMax = max;
        super.setMax(max);
    }

    public void setLocalMin(int min) {
        Log.i(TAG, "setLocalMin: min=" + min);
        mMin = min;
        super.setMin(min);
    }

    public void setLocalProgress(int localProgress) {
        this.mProgress = localProgress;
        this.mRealProgress = getRealProgress(localProgress);
        super.setProgress(localProgress);

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
     * @param onSeekBarChangeListener The {@link OnSeekBarChangeListener} to add.
     */
    public void addOnSeekBarChangeListener(OnSeekBarChangeListener onSeekBarChangeListener) {
        if (onSeekBarChangeListener != null) {
            mOnSeekBarChangeListeners.add(onSeekBarChangeListener);
        }
    }

    /**
     * Remove a listener that was previously added with {@link #addOnSeekBarChangeListener}.
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

    public void setPaddingHorizontal(float f2) {
        mPaddingHorizontal = f2;
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

    public void setProgressContentDescription(String str) {
        mProgressContentDescription = str;
    }

    public void setProgressEnlargeScale(float f2) {
        mProgressEnlargeScale = f2;
        ensureSize();
        invalidate();
    }

    public void setProgressFull() {
        mIsProgressFull = true;
        ensureSize();
    }

    public void setProgressHeight(float f2) {
        mProgressHeight = f2;
        ensureSize();
        invalidate();
    }

    public void setProgressRadius(float f2) {
        mProgressRadius = f2;
        ensureSize();
        invalidate();
    }

    public void setProgressRoundCornerWeight(float f2) {
        mProgressRoundCornerWeight = f2;
        ensureSize();
        invalidate();
    }

    public void setSeekBarBackgroundColor(@NonNull ColorStateList colorStateList) {
        mBackgroundColorStateList = colorStateList;
        mBackgroundColor = getColor(this, mBackgroundColorStateList, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_background_color_normal));
        invalidate();
    }

    public void setStartFromMiddle(boolean z2) {
        mIsStartFromMiddle = z2;
    }

    public void setSupportDeformation(boolean z2) {
        mIsSupportDeformation = z2;
    }

    @Override
    public void setThumb(Drawable drawable) {
        super.setThumb(drawable);
        setThumbBitmap();
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

    public void startTransitionAnim(int targetValue, final boolean z2) {
        Interpolator interpolator;
        if (mClickAnimatorSet == null) {
            mClickAnimatorSet = new AnimatorSet();
        } else {
            mClickAnimatorSet.removeAllListeners();
            mClickAnimatorSet.cancel();
        }
        mClickAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationCancel(@NonNull Animator animator) {
                dispatchProgressChanged(mRealProgress, z2);
                onStopTrackingTouch(z2);
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                dispatchProgressChanged(mRealProgress, z2);
                onStopTrackingTouch(z2);
            }

            @Override
            public void onAnimationStart(@NonNull Animator animator) {
                onStartTrackingTouch(z2);
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {
            }
        });
        int i3 = mProgress;
        final int seekBarWidth = getSeekBarWidth();
        int i4 = mMax - mMin;
        final float f2 = i4 > 0 ? seekBarWidth / i4 : 0.0f;
        if (f2 > 0.0f) {
            ValueAnimator ofFloat = ValueAnimator.ofFloat(i3 * f2, targetValue * f2);
            if (z2 || (interpolator = mCustomProgressAnimInterpolator) == null) {
                ofFloat.setInterpolator(THUMB_ANIMATE_INTERPOLATOR);
            } else {
                ofFloat.setInterpolator(interpolator);
            }
            ofFloat.addUpdateListener(valueAnimator -> {
                float floatValue = (Float) valueAnimator.getAnimatedValue();
                setLocalProgress((int) (floatValue / f2));
                mScale = (floatValue - (mMin * f2)) / seekBarWidth;
                invalidate();
            });
            if (!z2) {
                if (mCustomProgressAnimDuration != -1.0f) {
                    mClickAnimatorSet.setDuration((long) mCustomProgressAnimDuration);
                    mClickAnimatorSet.play(ofFloat);
                    mClickAnimatorSet.start();
                }
            }
            long abs = (long) ((i4 > 0 ? Math.abs(targetValue - i3) / i4 : 0.0f) * DURATION_483);
            if (abs < DURATION_150) {
                abs = DURATION_150;
            }
            mClickAnimatorSet.setDuration(abs);
            mClickAnimatorSet.play(ofFloat);
            mClickAnimatorSet.start();
        }
    }

    public void stopPhysicsMove() {
        FlingBehavior flingBehavior;
        if (!mIsPhysicsEnable || mPhysicalAnimator == null || (flingBehavior = mFlingBehavior) == null) {
            return;
        }
        flingBehavior.stop();
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

    public void checkThumbPosChange(int i2, boolean z2, boolean fromUser) {
        if (mProgress != i2) {
            int i3 = mRealProgress;
            setLocalProgress(i2);
            dispatchProgressChanged(mRealProgress, fromUser);
            if (!z2 || i3 == mRealProgress) {
                return;
            }
            performFeedback();
        }
    }

    public void onStartTrackingTouch(boolean z2) {
        mIsDragging = true;
        mStartDragging = true;
        if (!z2 || mOnSeekBarChangeListeners.isEmpty()) {
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
        dispatchStartTrackingTouch();
    }

    @Override
    public void setProgress(int i2, boolean z2) {
        setProgress(i2, z2, false);
    }

    public void setProgress(int i2, boolean z2, boolean fromUser) {
        mOldProgress = mProgress;
        int max = Math.max(mMin, Math.min(i2, mMax));
        if (mOldProgress != max) {
            if (z2) {
                startTransitionAnim(max, fromUser);
            } else {
                setLocalProgress(max);
                mOldProgress = max;
                int i3 = mMax - mMin;
                mScale = i3 > 0 ? (float) (mProgress) / i3 : 0.0f;
                dispatchProgressChanged(getRealProgress(max), fromUser);
                invalidate();
            }
            resetDeformationValue();
        }
    }

    public interface OnSeekBarChangeListener {
        void onProgressChanged(OplusSeekBar oplusSeekbar, int progress, boolean fromUser);

        void onStartTrackingTouch(OplusSeekBar oplusSeekbar);

        void onStopTrackingTouch(OplusSeekBar oplusSeekbar);
    }

    public interface OnDeformedListener {
        default void onScaleChanged(DeformedValueBean deformedValueBean) {}

        default void onHeightDeformedChanged(float f2, float f3) {}
    }

    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<>() {

            @Override
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

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
        public void writeToParcel(Parcel parcel, int i2) {
            super.writeToParcel(parcel, i2);
            parcel.writeInt(mSaveProgress);
        }
    }

    public final class PatternExploreByTouchHelper extends ExploreByTouchHelper {
        private final Rect mTempRect;

        public PatternExploreByTouchHelper(View view) {
            super(view);
            mTempRect = new Rect();
        }

        private Rect getBoundsForVirtualView(int i2) {
            Rect rect = mTempRect;
            rect.left = 0;
            rect.top = 0;
            rect.right = getWidth();
            rect.bottom = getHeight();
            return rect;
        }

        @Override
        public int getVirtualViewAt(float x, float y) {
            return (x < 0.0f || x > ((float) getWidth()) || y < 0.0f || y > ((float) getHeight())) ? -1 : 0;
        }

        @Override
        public void getVisibleVirtualViews(List<Integer> list) {
            list.add(0);
        }

        @Override
        public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfoCompat);
            accessibilityNodeInfoCompat.addAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SET_PROGRESS);
            accessibilityNodeInfoCompat.setRangeInfo(AccessibilityNodeInfoCompat.RangeInfoCompat.obtain(1, getMin(), getMax(), mProgress));
            if (isEnabled()) {
                int progress = getProgress();
                if (progress > getMin()) {
                    accessibilityNodeInfoCompat.addAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
                }
                if (progress < getMax()) {
                    accessibilityNodeInfoCompat.addAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                }
            }
        }

        @Override
        public boolean onPerformActionForVirtualView(int virtualViewId, int action, @Nullable Bundle arguments) {
            sendEventForVirtualView(virtualViewId, AccessibilityNodeInfo. ACTION_SELECT);
            return false;
        }

        @Override
        public void onPopulateAccessibilityEvent(@NonNull View view, @NonNull AccessibilityEvent accessibilityEvent) {
            super.onPopulateAccessibilityEvent(view, accessibilityEvent);
        }

        @Override
        public void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent accessibilityEvent) {
            accessibilityEvent.getText().add(PatternExploreByTouchHelper.class.getSimpleName());
            accessibilityEvent.setItemCount(getMax() - getMin());
            accessibilityEvent.setCurrentItemIndex(getProgress());
        }

        @Override
        public void onPopulateNodeForVirtualView(int virtualViewId, @NonNull AccessibilityNodeInfoCompat info) {
            info.setContentDescription("");
            info.setClassName(OplusSeekBar.class.getName());
            info.setBoundsInParent(getBoundsForVirtualView(virtualViewId));
        }

        @Override
        public boolean performAccessibilityAction(@NonNull View view, int action, Bundle bundle) {
            if (super.performAccessibilityAction(view, action, bundle)) {
                return true;
            }
            if (!isEnabled()) {
                return false;
            }
            if (action == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) {
                setProgress(getProgress() + mIncrement, false, true);
                announceForAccessibility(mProgressContentDescription);
                return true;
            }
            if (action != AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) {
                return false;
            }
            setProgress(getProgress() - mIncrement, false, true);
            announceForAccessibility(mProgressContentDescription);
            return true;
        }
    }
}
