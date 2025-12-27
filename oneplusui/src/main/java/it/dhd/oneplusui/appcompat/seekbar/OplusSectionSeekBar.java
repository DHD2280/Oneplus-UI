package it.dhd.oneplusui.appcompat.seekbar;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.StateSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.animation.PathInterpolatorCompat;
import androidx.dynamicanimation.animation.FloatPropertyCompat;

import java.math.BigDecimal;
import java.math.RoundingMode;

import it.dhd.oneplusui.R;
import it.dhd.oneplusui.appcompat.animation.dynamic.OplusDynamicAnimation;
import it.dhd.oneplusui.appcompat.animation.dynamic.OplusSpringAnimation;
import it.dhd.oneplusui.appcompat.animation.dynamic.OplusSpringForce;


public class OplusSectionSeekBar extends OplusSeekBar {
    private static final float DEFORMATION_RELEASE_SPRING_RESPONSE = 0.35f;
    private static final int DEFORMATION_SCALE_FACTOR = 1000;
    private static final float INTERPOLATOR_CONTROL_X1 = 0.0f;
    private static final float INTERPOLATOR_CONTROL_X2 = 0.25f;
    private static final float INTERPOLATOR_CONTROL_Y1 = 0.0f;
    private static final float INTERPOLATOR_CONTROL_Y2 = 1.0f;
    private static final long MOVE_ANIMATOR_DURATION = 100;
    private static final float MOVE_RATIO = 0.4f;
    private int mActionMoveDirection;
    private final PorterDuffXfermode mPorterDuffXfermode;
    private int mActiveMarkColor;
    private float mCurrentOffset;
    private ColorStateList mActiveMarkColorStateList;
    private OplusSpringAnimation mDeformedReleaseAnim;
    private FloatPropertyCompat<OplusSectionSeekBar> mDeformedReleaseTransition;
    private int mInactiveMarkColor;
    private boolean mIsFastMoving;
    private float mMarkRadius;
    private float mMoveAnimationEndThumbX;
    private float mMoveAnimationStartThumbX;
    private float mMoveAnimationValue;
    private ValueAnimator mMoveAnimator;
    private boolean mOnStopTrackingMask;
    private ColorStateList mInactiveMarkColorStateList;
    private float mThumbX;
    private int mTouchDownPos;
    private float mTouchDownThumbX;

    public OplusSectionSeekBar(Context context) {
        this(context, null);
    }

    public OplusSectionSeekBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.oplusSectionSeekBarStyle);
    }

    public OplusSectionSeekBar(Context context, AttributeSet attributeSet, int i2) {
        this(context, attributeSet, i2, R.style.Widget_Oplus_SectionSeekBar);
    }

    public OplusSectionSeekBar(Context context, AttributeSet attributeSet, int i2, int i3) {
        super(context, attributeSet, i2, i3);
        mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC);
        mOnStopTrackingMask = false;
        mThumbX = -1.0f;
        mIsFastMoving = false;
        mTouchDownPos = -1;
        mTouchDownThumbX = INTERPOLATOR_CONTROL_X1;
        mDeformedReleaseTransition = new FloatPropertyCompat<>("deformedReleaseTransition") {
            @Override
            public float getValue(OplusSectionSeekBar cOUISectionSeekBar) {
                return cOUISectionSeekBar.getScale();
            }

            @Override
            public void setValue(OplusSectionSeekBar cOUISectionSeekBar, float f2) {
                cOUISectionSeekBar.setScale(f2);
            }
        };
        mMarkRadius = getResources().getDimensionPixelSize(R.dimen.oplus_section_seekbar_tick_mark_radius);
        mInactiveMarkColorStateList = createColorStateList(ContextCompat.getColor(getContext(), R.color.oplus_seekbar_inactive_mark_selector), ContextCompat.getColor(getContext(), R.color.oplus_seekbar_inactive_mark_disable_color));
        mActiveMarkColorStateList = createColorStateList(ContextCompat.getColor(getContext(), R.color.oplus_seekbar_active_mark_selector), ContextCompat.getColor(getContext(), R.color.oplus_seekbar_active_mark_disable_color));
        mInactiveMarkColor = getColor(this, mInactiveMarkColorStateList, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_inactive_mark_selector));
        mActiveMarkColor = getColor(this, mActiveMarkColorStateList, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_active_mark_selector));
        initDeformedReleaseAnim();
    }

    public static ColorStateList createColorStateList(int defaultColor, int disabledColor) {
        return new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_enabled},
                        StateSet.WILD_CARD
                },
                new int[]{
                        disabledColor,
                        defaultColor
                }
        );
    }

    public void calculateCurIndex() {
        int iCeil = mProgress;
        float f2 = mMoveAnimationEndThumbX;
        float f3 = mMoveAnimationStartThumbX;
        boolean z2 = true;
        if (f2 - f3 > INTERPOLATOR_CONTROL_X1) {
            iCeil = Math.round(mThumbX / (mIsDragging ? getMoveSectionWidth() : getSectionWidth()));
        } else if (f2 - f3 < INTERPOLATOR_CONTROL_X1) {
            iCeil = (int) Math.ceil(((int) mThumbX) / (mIsDragging ? getMoveSectionWidth() : getSectionWidth()));
        } else {
            z2 = false;
        }
        if (isLayoutRtl() && z2) {
            iCeil = mMax - iCeil;
        }
        checkThumbPosChange(iCeil);
    }

    private void calculateThumbPositionByIndex() {
        int seekBarWidth = getSeekBarWidth();
        mThumbX = ((mProgress * seekBarWidth) * 1.0f) / mMax;
        if (isLayoutRtl()) {
            mThumbX = seekBarWidth - mThumbX;
        }
    }

    private void clearDeformationValue(MotionEvent motionEvent) {
        float x2 = (motionEvent.getX() - getStart()) - mPaddingHorizontal;
        if (x2 <= INTERPOLATOR_CONTROL_X1 || x2 >= getSeekBarWidth()) {
            return;
        }
        resetDeformationValue();
    }

    private void drawMark(Canvas canvas, int i2, float f2) {
        float width = (getWidth() - getEnd()) - mPaddingHorizontal;
        float f3 = mThumbPosition;
        float f4 = mCurThumbRadius;
        float f5 = f3 - f4;
        float f6 = f3 + f4;
        int iSaveLayer = canvas.saveLayer(null, null, 31);
        mPaint.setXfermode(mPorterDuffXfermode);
        int i3 = (!mShowProgress || isLayoutRtl()) ? mInactiveMarkColor : mActiveMarkColor;
        mPaint.setColor(i3);
        float start = getStart() + mPaddingHorizontal;
        float f7 = width - start;
        int i4 = 0;
        boolean z2 = false;
        while (true) {
            int i5 = mMax;
            if (i4 > i5) {
                mPaint.setXfermode(null);
                canvas.restoreToCount(iSaveLayer);
                return;
            }
            if (mShowProgress && !z2 && ((i4 * f7) / i5) + start > getStart() + mPaddingHorizontal + mThumbX) {
                mPaint.setColor(isLayoutRtl() ? mActiveMarkColor : mInactiveMarkColor);
                z2 = true;
            }
            float f8 = ((i4 * f7) / mMax) + start + (isLayoutRtl() ? -f2 : f2);
            float f9 = mMarkRadius;
            float f10 = f8 + f9;
            if (f5 > f8 - f9 || f6 < f10) {
                canvas.drawCircle(f8, i2, f9, mPaint);
            }
            i4++;
        }
    }

    private void drawThumb(Canvas canvas, int i2) {
        if (mShowThumb) {
            if (mThumbShadowRadiusSize > 0 && isEnabled()) {
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setShadowLayer(mThumbShadowRadiusSize, INTERPOLATOR_CONTROL_X1, mThumbShadowOffsetY, mThumbShadowColor);
            }
            mPaint.setColor(mThumbColor);
            canvas.drawCircle(mThumbPosition, i2, mCurThumbRadius, mPaint);
            if (mThumbShadowRadiusSize <= 0 || !isEnabled()) {
                return;
            }
            mPaint.clearShadowLayer();
        }
    }

    private float getMoveSectionWidth() {
        return getSeekBarMoveWidth() / mMax;
    }

    private float getMoveThumbXByIndex(int i2) {
        float stepWidth = getSeekBarMoveWidth() / (float) mMax;
        float seekBarMoveWidth = getSeekBarMoveWidth();
        float fMax = Math.max(INTERPOLATOR_CONTROL_X1, Math.min(stepWidth, seekBarMoveWidth));
        return isLayoutRtl() ? seekBarMoveWidth - fMax : fMax;
    }

    public float getScale() {
        return mScale * DEFORMATION_SCALE_FACTOR;
    }

    public void setScale(float scale) {
        mScale = scale / DEFORMATION_SCALE_FACTOR;
        calculateTouchDeformationValue();
    }

    private float getSectionWidth() {
        return getSeekBarNormalWidth() / mMax;
    }

    private int getSeekBarMoveWidth() {
        return (int) (((getWidth() - getStart()) - getEnd()) - (mPaddingHorizontal * 2.0f));
    }

    private int getSeekBarNormalWidth() {
        return (int) (((getWidth() - getStart()) - getEnd()) - (mPaddingHorizontal * 2.0f));
    }

    private int getThumbPosByX(float f2) {
        int seekBarWidth = getSeekBarWidth();
        if (isLayoutRtl()) {
            f2 = seekBarWidth - f2;
        }
        return Math.max(0, Math.min(Math.round((f2 * mMax) / seekBarWidth), mMax));
    }

    private float getThumbXByIndex(int index) {
        float seekBarWidth = getSeekBarNormalWidth();
        float positionX = ((float) index * seekBarWidth) / (float) mMax;
        float constrainedX = Math.max(INTERPOLATOR_CONTROL_X1, Math.min(positionX, seekBarWidth));
        return (isLayoutRtl()) ?
                seekBarWidth - constrainedX :
                constrainedX;
    }

    private float getTouchXOfDrawArea(MotionEvent motionEvent) {
        return Math.min(Math.max(INTERPOLATOR_CONTROL_X1, (motionEvent.getX() - getStart()) - mPaddingHorizontal), getSeekBarWidth());
    }

    private void initDeformedReleaseAnim() {
        if (mDeformedReleaseAnim != null) {
            return;
        }
        mDeformedReleaseAnim = new OplusSpringAnimation(this, mDeformedReleaseTransition);
        OplusSpringForce oplusSpringForce = new OplusSpringForce();
        oplusSpringForce.setBounce(INTERPOLATOR_CONTROL_X1);
        oplusSpringForce.setResponse(DEFORMATION_RELEASE_SPRING_RESPONSE);
        mDeformedReleaseAnim.setSpring(oplusSpringForce);
    }

    public void invalidateProgress(float f2, boolean z2) {
        float thumbXByIndex = getThumbXByIndex(mProgress);
        float fSubtract = subtract(f2, thumbXByIndex);
        float sectionWidth = getSectionWidth();
        int iRound = mIsDragging ? (int) (fSubtract / sectionWidth) : Math.round(fSubtract / sectionWidth);
        ValueAnimator valueAnimator = mMoveAnimator;
        if (valueAnimator != null && valueAnimator.isRunning() && Float.compare(mMoveAnimationEndThumbX, (iRound * sectionWidth) + thumbXByIndex) == 0) {
            return;
        }
        float f3 = iRound * sectionWidth;
        mCurrentOffset = f3;
        float f4 = mThumbX - thumbXByIndex;
        mOnStopTrackingMask = true;
        startMoveAnimation(thumbXByIndex, f3 + thumbXByIndex, f4, z2);
    }

    private void startMoveAnimation(float f2, float f3, float f4, boolean z2) {
        ValueAnimator valueAnimator;
        if (Float.compare(mThumbX, f3) == 0 || ((valueAnimator = mMoveAnimator) != null && valueAnimator.isRunning() && Float.compare(mMoveAnimationEndThumbX, f3) == 0)) {
            if (mOnStopTrackingMask) {
                onStopTrackingTouch(true);
                mOnStopTrackingMask = false;
                return;
            }
            return;
        }
        mMoveAnimationEndThumbX = f3;
        mMoveAnimationStartThumbX = f2;
        if (!z2) {
            mThumbX = (f3 + f2) - f2;
            calculateCurIndex();
            mOnStopTrackingMask = false;
            return;
        }
        if (mMoveAnimator == null) {
            ValueAnimator valueAnimator2 = new ValueAnimator();
            mMoveAnimator = valueAnimator2;
            valueAnimator2.setInterpolator(PathInterpolatorCompat.create(INTERPOLATOR_CONTROL_X1, INTERPOLATOR_CONTROL_Y1, INTERPOLATOR_CONTROL_X2, 1.0f));
            mMoveAnimator.addUpdateListener(valueAnimator3 -> {
                mMoveAnimationValue = ((Float) valueAnimator3.getAnimatedValue()).floatValue();
                mThumbX = mMoveAnimationStartThumbX + (mMoveAnimationValue * 0.4f) + (mCurrentOffset * 0.6f);
                invalidate();
                calculateCurIndex();
            });
            mMoveAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationCancel(Animator animator) {
                    if (mOnStopTrackingMask) {
                        onStopTrackingTouch(true);
                        mOnStopTrackingMask = false;
                    }
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (mOnStopTrackingMask) {
                        onStopTrackingTouch(true);
                        mOnStopTrackingMask = false;
                    }
                    if (mIsFastMoving) {
                        mIsFastMoving = false;
                        invalidateProgress(mLastX, true);
                    }
                }

                @Override
                public void onAnimationRepeat(@NonNull Animator animator) {
                }

                @Override
                public void onAnimationStart(@NonNull Animator animator) {
                }
            });
        }
        mMoveAnimator.cancel();
        mMoveAnimator.setDuration(100L);
        mMoveAnimator.setFloatValues(f4, f3 - f2);
        mMoveAnimator.start();
    }

    private void trackTouchEvent(MotionEvent motionEvent, float f2) {
        setTouchScale(isLayoutRtl() ? (((getWidth() - motionEvent.getX()) - getEnd()) - mPaddingHorizontal) / getSeekBarWidth() : ((motionEvent.getX() - getStart()) - mPaddingHorizontal) / getSeekBarWidth(), false);
        executeTouchGlitterEffectAnim();
        float fSubtract = subtract(f2, mTouchDownThumbX);
        float f3 = fSubtract < INTERPOLATOR_CONTROL_X1 ? fSubtract - 0.1f : fSubtract + 0.1f;
        float moveSectionWidth = getMoveSectionWidth();
        int iFloatValue = (int) new BigDecimal(Float.toString(f3)).divide(new BigDecimal(Float.toString(moveSectionWidth)), RoundingMode.FLOOR).floatValue();
        float f4 = iFloatValue * moveSectionWidth;
        if (isLayoutRtl()) {
            iFloatValue = -iFloatValue;
        }
        mCurrentOffset = f3;
        if (Math.abs((mTouchDownPos + iFloatValue) - mProgress) > 0) {
            float f5 = mTouchDownThumbX;
            startMoveAnimation(f5, f4 + f5, mMoveAnimationValue, true);
        } else {
            mThumbX = mTouchDownThumbX + f4 + ((mCurrentOffset - f4) * 0.6f);
            invalidate();
        }
        mLastX = f2;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (mThumbX == -1.0f) {
            calculateThumbPositionByIndex();
        }
        super.draw(canvas);
    }

    @Override
    public void drawActiveTrack(Canvas canvas, float f2) {
        int seekBarCenterY = getSeekBarCenterY();
        float f3 = mHeightTopDeformedUpValue - mHeightBottomDeformedDownValue;
        mThumbPosition = getStart() + mPaddingHorizontal + Math.min(mThumbX, getSeekBarWidth()) + (isLayoutRtl() ? -f3 : f3);
        mLabelX = mThumbX;
        drawProgress(canvas);
        drawGlitterEffect(canvas);
        drawMark(canvas, seekBarCenterY, f3);
        drawThumb(canvas, seekBarCenterY);
    }

    @Override
    public void handleMotionEventDown(MotionEvent motionEvent) {
        float touchXOfDrawArea = getTouchXOfDrawArea(motionEvent);
        mTouchDownX = touchXOfDrawArea;
        mLastX = touchXOfDrawArea;
        mIsBumpingEdges = false;
        executeThumbScaleAnim(motionEvent);
    }

    @Override
    public void handleMotionEventMove(MotionEvent event) {
        resetBumpingEdges();
        clearDeformationValue(event);

        float currentTouchX = getTouchXOfDrawArea(event);

        if (mIsDragging) {
            int currentDirection = 0;
            if (currentTouchX > mLastX) {
                currentDirection = 1;
            } else if (currentTouchX < mLastX) {
                currentDirection = -1;
            }

            if (currentDirection == (-mActionMoveDirection)) {
                mActionMoveDirection = currentDirection;

                if (mTouchDownPos != mProgress) {
                    mTouchDownPos = mProgress;
                    mTouchDownThumbX = getMoveThumbXByIndex(mProgress);
                    mMoveAnimationValue = INTERPOLATOR_CONTROL_X1;
                }

                if (mMoveAnimator != null) {
                    mMoveAnimator.cancel();
                }
            }

            trackTouchEvent(event, currentTouchX);

        } else {
            if (!isToucheInSeekBar(event)) {
                return;
            }

            float distanceFromStart = Math.abs(event.getX() - ((mTouchDownX + getStart()) + mPaddingHorizontal));

            if (distanceFromStart > mTouchSlop) {
                mClickAnim.cancel();
                mDeformedReleaseAnim.cancel();

                startDrag();
                touchAnim();

                int initialPos = getThumbPosByX(mTouchDownX);
                mTouchDownPos = initialPos;
                checkThumbPosChange(initialPos);

                float initialThumbX = getMoveThumbXByIndex(mTouchDownPos);
                mTouchDownThumbX = initialThumbX;
                mMoveAnimationValue = 0.0f;
                mThumbX = initialThumbX;

                invalidate();
                trackTouchEvent(event, currentTouchX);

                mActionMoveDirection = (currentTouchX > mTouchDownX) ? 1 : -1;
            }
        }

        mLastX = currentTouchX;
    }

    @Override
    public void handleMotionEventUp(MotionEvent motionEvent) {
        releaseThumbScaleAnim();
        float touchXOfDrawArea = getTouchXOfDrawArea(motionEvent);
        if (!mIsDragging) {
            if (motionEvent.getAction() != 3 && isEnabled() && touchInSeekBar(motionEvent, this)) {
                invalidateProgress(touchXOfDrawArea, false);
                animForClick(touchXOfDrawArea);
                releaseAnim();
                return;
            }
            return;
        }
        ValueAnimator valueAnimator = mMoveAnimator;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            mIsFastMoving = true;
        }
        if (mScale < SCALE_MIN) {
            mDeformedReleaseAnim.setStartValue(mScale * DEFORMATION_SCALE_FACTOR);
            mDeformedReleaseAnim.animateToFinalPosition(SCALE_MIN);
            onStopTrackingTouch(true);
        } else if (mScale > SCALE_MAX) {
            mDeformedReleaseAnim.setStartValue(mScale * DEFORMATION_SCALE_FACTOR);
            mDeformedReleaseAnim.animateToFinalPosition(DEFORMATION_SCALE_FACTOR);
            onStopTrackingTouch(true);
        } else if (!mIsFastMoving) {
            invalidateProgress(touchXOfDrawArea, true);
        }
        onStopTrackingTouch(false);
        setPressed(false);
        releaseAnim();
    }

    @Override
    public void onClickAnimationUpdate(float f2) {
        mThumbX = (int) f2;
        invalidate();
    }

    @Override
    public void onSizeChanged(int i2, int i3, int i4, int i5) {
        super.onSizeChanged(i2, i3, i4, i5);
        mThumbX = -1.0f;
    }

    @Override
    public void performFeedback() {
        if (mEnableVibrator) {
            if ((mHasMotorVibrator && mEnableAdaptiveVibrator && performAdaptiveFeedback()) || performHapticFeedback(308)) {
                return;
            }
            performHapticFeedback(302);
        }
    }

    @Override
    public void setEnabled(boolean z2) {
        super.setEnabled(z2);
        mInactiveMarkColor = getColor(this, mInactiveMarkColorStateList, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_inactive_mark_selector));
        mActiveMarkColor = getColor(this, mActiveMarkColorStateList, ContextCompat.getColor(getContext(), R.color.oplus_seekbar_active_mark_selector));
    }

    @Override
    public void setMax(int max) {
        if (max < getMin()) {
            max = getMin();
        }
        if (max != mMax) {
            setLocalMax(max);
            if (mProgress > max) {
                setProgress(max);
            }
            calculateThumbPositionByIndex();
        }
        invalidate();
    }

    @Override
    public void setProgress(int i2, boolean z2, boolean z3) {
        if (mProgress != Math.max(0, Math.min(i2, mMax))) {
            if (z2) {
                checkThumbPosChange(i2, false, z3);
                calculateThumbPositionByIndex();
                startTransitionAnim(i2, z3);
            } else {
                checkThumbPosChange(i2, false, z3);
                if (getWidth() != 0) {
                    calculateThumbPositionByIndex();
                    mMoveAnimationEndThumbX = mThumbX;
                    invalidate();
                }
            }
        }
    }

    @Override
    public void setProgressRect() {
        float seekBarWidth = getSeekBarWidth();
        int seekBarCenterY = getSeekBarCenterY();
        if (isLayoutRtl()) {
            float start = getStart() + mPaddingHorizontal + seekBarWidth;
            float start2 = getStart() + mPaddingHorizontal + mThumbX;
            float deformation = start2 - mHeightTopDeformedUpValue;
            mProgressRect.set(deformation + mHeightBottomDeformedDownValue, (seekBarCenterY - (mProgressHeight / 2.0f)) + mWidthDeformedValue, (start - mHeightBottomDeformedUpValue) + mHeightBottomDeformedDownValue, (seekBarCenterY + (mProgressHeight / 2.0f)) - mWidthDeformedValue);
        } else {
            float start3 = getStart() + mPaddingHorizontal;
            float f7 = mThumbX + start3;
            float deformation = (start3 - mHeightBottomDeformedDownValue) + mHeightBottomDeformedUpValue;
            mProgressRect.set(deformation, (seekBarCenterY - (mProgressHeight / 2.0f)) + mWidthDeformedValue, (f7 + mHeightTopDeformedUpValue) - mHeightBottomDeformedDownValue, (seekBarCenterY + (mProgressHeight / 2.0f)) - mWidthDeformedValue);
        }
        mProgressRect.left = mProgressRect.left - (mProgressHeight / 2.0f);
        mProgressRect.right += mProgressHeight / 2.0f;
    }

    @Override
    public void startTransitionAnim(int i2, boolean z2) {
        OplusDynamicAnimation.OnAnimationEndListener onAnimationEndListener = (OplusDynamicAnimation, z3, f2, f3) -> onStopTrackingTouch(true);
        int i3 = (int) mLabelX;
        int i4 = (int) mThumbX;
        mClickAnim.cancel();
        OplusDynamicAnimation.OnAnimationEndListener onAnimationEndListener2 = mLastEndClickListener;
        if (onAnimationEndListener2 != null) {
            mClickAnim.removeEndListener(onAnimationEndListener2);
        }
        mClickAnim.addEndListener(onAnimationEndListener);
        mClickAnim.setStartValue(i3);
        onStartTrackingTouch(true);
        mClickAnim.animateToFinalPosition(i4);
        mLastEndClickListener = onAnimationEndListener;
    }

}
