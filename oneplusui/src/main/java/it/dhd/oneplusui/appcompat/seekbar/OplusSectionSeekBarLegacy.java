package it.dhd.oneplusui.appcompat.seekbar;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.animation.PathInterpolatorCompat;

import java.math.BigDecimal;
import java.math.RoundingMode;

import it.dhd.oneplusui.R;

public class OplusSectionSeekBarLegacy extends OplusSeekBarLegacy {

    private static final String TAG = "OplusSectionSeekBarLegacy";
    private static final float MARK_RADIUS_SCALE = 2.0f;
    private static final float MOVE_RATIO = 0.4f;
    private final PorterDuffXfermode mPorterDuffXfermode;
    private int mActionMoveDirection;
    private int mCurActiveMarkColor;
    private int mCurInactiveMarkColor;
    private float mCurMarkRadius;
    private float mCurrentOffset;
    private boolean mIsFastMoving;
    private float mMarkRadius;
    private float mMoveAnimationEndThumbX;
    private float mMoveAnimationStartThumbX;
    private float mMoveAnimationValue;
    private ValueAnimator mMoveAnimator;
    private boolean mOnStopTrackingMask;
    private float mOverstep;
    private float mThumbX;
    private int mTouchDownPos;
    private float mTouchDownThumbX;

    public OplusSectionSeekBarLegacy(Context context) {
        this(context, null);
    }

    public OplusSectionSeekBarLegacy(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.oplusSectionSeekBarStyle);
    }

    public OplusSectionSeekBarLegacy(Context context, AttributeSet attributeSet, int defStyleAttr) {
        this(context, attributeSet, defStyleAttr, R.style.Widget_Oplus_SectionSeekBarLegacy);
    }

    public OplusSectionSeekBarLegacy(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);
        this.mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC);
        this.mOnStopTrackingMask = false;
        this.mThumbX = -1.0f;
        this.mIsFastMoving = false;
        this.mTouchDownPos = -1;
        this.mTouchDownThumbX = 0.0f;
        this.mMarkRadius = 0.0f;
        this.mCurMarkRadius = 0.0f;
        float dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.oplus_section_seekbar_tick_mark_radius);
        this.mMarkRadius = dimensionPixelSize;
        this.mCurMarkRadius = dimensionPixelSize;
        this.mCurActiveMarkColor = 0;
        this.mCurInactiveMarkColor = 0;
        PropertyValuesHolder ofInt = PropertyValuesHolder.ofInt("activeAlpha", 0, Color.alpha(ContextCompat.getColor(getContext(), R.color.oplus_seekbar_mark_active_anim_end)));
        PropertyValuesHolder ofInt2 = PropertyValuesHolder.ofInt("inactiveAlpha", 0, Color.alpha(ContextCompat.getColor(getContext(), R.color.oplus_seekbar_mark_inactive_anim_end)));
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setValues(ofInt, ofInt2);
        valueAnimator.addUpdateListener(valueAnimator2 -> {
            int intValue = (Integer) valueAnimator2.getAnimatedValue("activeAlpha");
            int intValue2 = (Integer) valueAnimator2.getAnimatedValue("inactiveAlpha");
            mCurActiveMarkColor = Color.argb(intValue, 0, 0, 0);
            mCurInactiveMarkColor = Color.argb(intValue2, 255, 255, 255);
            invalidate();
        });
        this.mTouchAnimator.play(valueAnimator);
    }

    private void calculateThumbPositionByIndex() {
        int seekBarWidth = getSeekBarWidth();
        this.mThumbX = ((this.mProgress * seekBarWidth) * 1.0f) / this.mMax;
        if (isLayoutRtl()) {
            this.mThumbX = seekBarWidth - this.mThumbX;
        }
    }

    public float getMoveSectionWidth() {
        return (float) getSeekBarMoveWidth() / this.mMax;
    }

    private float getMoveThumbXByIndex(int i2) {
        float stepWidth = getSeekBarMoveWidth() / (float) this.mMax;
        float f2 = (i2 * stepWidth);
        float seekBarMoveWidth = getSeekBarMoveWidth();
        float max = Math.max(0.0f, Math.min(f2, seekBarMoveWidth));
        return isLayoutRtl() ? seekBarMoveWidth - max : max;
    }

    public float getSectionWidth() {
        return (float) getSeekBarNormalWidth() / this.mMax;
    }

    private int getSeekBarMoveWidth() {
        return (int) (((getWidth() - getStart()) - getEnd()) - ((this.mPaddingHorizontal * this.mHorizontalPaddingScale) * 2.0f));
    }

    private int getSeekBarNormalWidth() {
        return (int) (((getWidth() - getStart()) - getEnd()) - (this.mPaddingHorizontal * 2.0f));
    }

    private int getThumbPosByX(float f2) {
        int seekBarWidth = getSeekBarWidth();
        if (isLayoutRtl()) {
            f2 = seekBarWidth - f2;
        }
        return Math.max(0, Math.min(Math.round((f2 * this.mMax) / seekBarWidth), this.mMax));
    }

    private float getThumbXByIndex(int index) {
        float moveWidth = getSeekBarNormalWidth();//getSeekBarMoveWidth();
        float position = (index * moveWidth) / this.mMax;
        float clampedPosition = Math.max(0.0f, Math.min(position, moveWidth));
        return isLayoutRtl() ? moveWidth - clampedPosition : clampedPosition;
    }

    private float getTouchXOfDrawArea(MotionEvent motionEvent) {
        return Math.min(Math.max(0.0f, (motionEvent.getX() - getPaddingLeft()) - this.mCurPaddingHorizontal), getSeekBarWidth());
    }

    public void invalidateProgress(float f2, boolean z2) {
        float thumbXByIndex = getThumbXByIndex(this.mProgress);
        float subtract = subtract(f2, thumbXByIndex);
        float sectionWidth = getSectionWidth();
        int round = this.mIsDragging ? (int) (subtract / sectionWidth) : Math.round(subtract / sectionWidth);
        ValueAnimator valueAnimator = this.mMoveAnimator;
        if (valueAnimator != null && valueAnimator.isRunning() && Float.compare(this.mMoveAnimationEndThumbX, (round * sectionWidth) + thumbXByIndex) == 0) {
            return;
        }
        float f3 = round * sectionWidth;
        this.mCurrentOffset = f3;
        this.mOverstep = thumbXByIndex;
        float f4 = this.mThumbX - thumbXByIndex;
        this.mOnStopTrackingMask = true;
        startMoveAnimation(thumbXByIndex, f3 + thumbXByIndex, f4, z2 ? 100 : 0);
    }


    private void startMoveAnimation(float f2, float f3, float f4, int i2) {
        if (Float.compare(this.mThumbX, f3) == 0 || (this.mMoveAnimator != null && mMoveAnimator.isRunning() && Float.compare(this.mMoveAnimationEndThumbX, f3) == 0)) {
            if (this.mOnStopTrackingMask) {
                onStopTrackingTouch(true);
                this.mOnStopTrackingMask = false;
                return;
            }
            return;
        }
        this.mMoveAnimationEndThumbX = f3;
        this.mMoveAnimationStartThumbX = f2;
        if (this.mMoveAnimator == null) {
            ValueAnimator valueAnimator2 = new ValueAnimator();
            this.mMoveAnimator = valueAnimator2;
            valueAnimator2.setInterpolator(PathInterpolatorCompat.create(0.0f, 0.0f, 0.25f, 1.0f));
            this.mMoveAnimator.addUpdateListener(valueAnimator3 -> {
                mMoveAnimationValue = (Float) valueAnimator3.getAnimatedValue();
                mThumbX = mMoveAnimationStartThumbX + (mMoveAnimationValue * MOVE_RATIO) + (mCurrentOffset * 0.6f);
                mOverstep = mThumbX;
                invalidate();
                int i3 = mProgress;
                boolean z2 = true;
                if (mMoveAnimationEndThumbX - mMoveAnimationStartThumbX > 0.0f) {
                    float f5 = mThumbX;
                    i3 = Math.round(f5 / (mIsDragging ? getMoveSectionWidth() : getSectionWidth()));
                } else if (mMoveAnimationEndThumbX - mMoveAnimationStartThumbX < 0.0f) {
                    float f6 = (int) mThumbX;
                    i3 = (int) Math.ceil(f6 / (mIsDragging ? getMoveSectionWidth() : getSectionWidth()));
                } else {
                    z2 = false;
                }
                if (isLayoutRtl() && z2) {
                    i3 = mMax - i3;
                }
                checkThumbPosChange(i3);
            });
            this.mMoveAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationCancel(@NonNull Animator animator) {
                    if (mOnStopTrackingMask) {
                        onStopTrackingTouch(true);
                        mOnStopTrackingMask = false;
                    }
                }

                @Override
                public void onAnimationEnd(@NonNull Animator animator) {
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
        this.mMoveAnimator.cancel();
        this.mMoveAnimator.setDuration(i2);
        this.mMoveAnimator.setFloatValues(f4, f3 - f2);
        this.mMoveAnimator.start();
    }

    private void trackTouchEvent(float f2) {
        float subtract = subtract(f2, this.mTouchDownThumbX);
        float f3 = subtract < 0.0f ? subtract - 0.1f : subtract + 0.1f;
        float moveSectionWidth = getMoveSectionWidth();
        int floatValue = (int) new BigDecimal(Float.toString(f3)).divide(new BigDecimal(Float.toString(moveSectionWidth)), RoundingMode.FLOOR).floatValue();
        float f4 = floatValue * moveSectionWidth;
        if (isLayoutRtl()) {
            floatValue = -floatValue;
        }
        this.mCurrentOffset = f3;
        if (Math.abs((this.mTouchDownPos + floatValue) - this.mProgress) > 0) {
            float f5 = this.mTouchDownThumbX;
            startMoveAnimation(f5, f4 + f5, this.mMoveAnimationValue, 100);
        } else {
            this.mThumbX = this.mTouchDownThumbX + f4 + ((this.mCurrentOffset - f4) * 0.6f);
            invalidate();
        }
        this.mLastX = f2;
    }


    @Override
    public void drawActiveTrack(Canvas canvas, float f2) {
        float start;
        float f3;
        float width = (getWidth() - getEnd()) - this.mCurPaddingHorizontal;
        int seekBarCenterY = getSeekBarCenterY();
        if (isLayoutRtl()) {
            f3 = getStart() + this.mCurPaddingHorizontal + f2;
            start = getStart() + this.mCurPaddingHorizontal + this.mThumbX;
        } else {
            start = getStart() + this.mCurPaddingHorizontal;
            f3 = this.mThumbX + start;
        }
        if (this.mShowProgress) {
            this.mPaint.setColor(this.mProgressColor);
            RectF rectF = this.mProgressRect;
            float f5 = this.mCurProgressRadius;
            rectF.set(start, (float) seekBarCenterY - f5, f3, (float) seekBarCenterY + f5);
            canvas.drawRect(this.mProgressRect, this.mPaint);
            if (isLayoutRtl()) {
                RectF rectF2 = this.mTempRect;
                float f6 = this.mCurProgressRadius;
                RectF rectF3 = this.mProgressRect;
                rectF2.set(width - f6, rectF3.top, f6 + width, rectF3.bottom);
                canvas.drawArc(this.mTempRect, -90.0f, 180.0f, true, this.mPaint);
            } else {
                RectF rectF4 = this.mTempRect;
                float f7 = this.mCurProgressRadius;
                RectF rectF5 = this.mProgressRect;
                rectF4.set(start - f7, rectF5.top, start + f7, rectF5.bottom);
                canvas.drawArc(this.mTempRect, 90.0f, 180.0f, true, this.mPaint);
            }
        }
        int saveLayer = canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG);
        this.mPaint.setXfermode(this.mPorterDuffXfermode);
        this.mPaint.setColor(this.mShowProgress ? isLayoutRtl() ? this.mCurInactiveMarkColor : this.mCurActiveMarkColor : this.mCurInactiveMarkColor);
        float start2 = getStart() + this.mCurPaddingHorizontal;
        float f8 = width - start2;
        int i2 = 0;
        boolean z2 = false;
        while (true) {
            int i3 = this.mMax;
            if (i2 > i3) {
                break;
            }
            if (this.mShowProgress && !z2 && ((i2 * f8) / i3) + start2 > getStart() + this.mCurPaddingHorizontal + this.mThumbX) {
                this.mPaint.setColor(isLayoutRtl() ? this.mCurActiveMarkColor : this.mCurInactiveMarkColor);
                z2 = true;
            }
            canvas.drawCircle(((i2 * f8) / this.mMax) + start2, seekBarCenterY, this.mCurMarkRadius, this.mPaint);
            i2++;
        }
        this.mPaint.setXfermode(null);
        canvas.restoreToCount(saveLayer);
        this.mLabelX = this.mThumbX;
        if (this.mShowThumb) {
            float start3 = getStart() + this.mCurPaddingHorizontal;
            this.mPaint.setColor(this.mThumbColor);
            canvas.drawCircle(start3 + Math.min(this.mThumbX, getSeekBarWidth()), seekBarCenterY, this.mThumbOutRadius, this.mPaint);
        }
    }

    @Override
    public void drawInactiveTrack(Canvas canvas) {
        if (this.mThumbX == -1.0f) {
            calculateThumbPositionByIndex();
        }
        int seekBarCenterY = getSeekBarCenterY();
        int saveLayer = canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG);
        super.drawInactiveTrack(canvas);
        this.mPaint.setXfermode(this.mPorterDuffXfermode);
        float start = getStart() + this.mCurPaddingHorizontal;
        float width = ((getWidth() - getEnd()) - this.mCurPaddingHorizontal) - start;
        this.mPaint.setColor(this.mShowProgress ? isLayoutRtl() ? this.mBackgroundColor : this.mProgressColor : this.mBackgroundColor);
        int i2 = 0;
        boolean z2 = false;
        while (true) {
            int i3 = this.mMax;
            if (i2 > i3) {
                this.mPaint.setXfermode(null);
                canvas.restoreToCount(saveLayer);
                return;
            }
            if (this.mShowProgress && !z2 && ((i2 * width) / i3) + start > getStart() + this.mThumbX) {
                this.mPaint.setColor(isLayoutRtl() ? this.mProgressColor : this.mBackgroundColor);
                z2 = true;
            }
            canvas.drawCircle(((i2 * width) / this.mMax) + start, seekBarCenterY, this.mMarkRadius, this.mPaint);
            i2++;
        }
    }

    @Override
    public void handleMotionEventDown(MotionEvent motionEvent) {
        float touchXOfDrawArea = getTouchXOfDrawArea(motionEvent);
        this.mTouchDownX = touchXOfDrawArea;
        this.mLastX = touchXOfDrawArea;
    }

    @Override
    public void handleMotionEventMove(MotionEvent motionEvent) {
        float touchXOfDrawArea = getTouchXOfDrawArea(motionEvent);
        if (this.mIsDragging) {
            int r2 = -1;
            float f2 = this.mLastX;
            if (touchXOfDrawArea - f2 > 0.0f) {
                r2 = 1;
            } else if (touchXOfDrawArea - f2 >= 0.0f) {
                r2 = 0;
            }
            if (r2 == (-this.mActionMoveDirection)) {
                this.mActionMoveDirection = r2;
                int i2 = this.mTouchDownPos;
                int i3 = this.mProgress;
                if (i2 != i3) {
                    this.mTouchDownPos = i3;
                    this.mTouchDownThumbX = getMoveThumbXByIndex(i3);
                    this.mMoveAnimationValue = 0.0f;
                }
                if (mMoveAnimator != null) {
                    mMoveAnimator.cancel();
                }
            }
            trackTouchEvent(touchXOfDrawArea);
        } else {
            if (!touchInSeekBar(motionEvent, this)) {
                return;
            }
            if (Math.abs(touchXOfDrawArea - this.mTouchDownX) > this.mTouchSlop) {
                startDrag();
                touchAnim();
                int thumbPosByX = getThumbPosByX(this.mTouchDownX);
                this.mTouchDownPos = thumbPosByX;
                checkThumbPosChange(thumbPosByX);
                float moveThumbXByIndex = getMoveThumbXByIndex(this.mTouchDownPos);
                this.mTouchDownThumbX = moveThumbXByIndex;
                this.mMoveAnimationValue = 0.0f;
                this.mThumbX = moveThumbXByIndex;
                invalidate();
                trackTouchEvent(touchXOfDrawArea);
                this.mActionMoveDirection = touchXOfDrawArea - this.mTouchDownX > 0.0f ? 1 : -1;
            }
        }
        this.mLastX = touchXOfDrawArea;
    }

    @Override
    public void handleMotionEventUp(MotionEvent motionEvent) {
        float touchXOfDrawArea = getTouchXOfDrawArea(motionEvent);
        if (!this.mIsDragging) {
            invalidateProgress(touchXOfDrawArea, false);
            animForClick(touchXOfDrawArea);
            releaseAnim();
            return;
        }
        if (mMoveAnimator != null && mMoveAnimator.isRunning()) {
            this.mIsFastMoving = true;
        }
        if (!this.mIsFastMoving) {
            invalidateProgress(touchXOfDrawArea, true);
        }
        onStopTrackingTouch();
        setPressed(false);
        releaseAnim();
    }

    @Override
    public void onEnlargeAnimationUpdate(ValueAnimator valueAnimator) {
        super.onEnlargeAnimationUpdate(valueAnimator);
        float animatedFraction = valueAnimator.getAnimatedFraction();
        this.mCurMarkRadius = mMarkRadius + (animatedFraction * ((MARK_RADIUS_SCALE * mMarkRadius) - mMarkRadius));
    }

    @Override
    public void onSizeChanged(int i2, int i3, int i4, int i5) {
        super.onSizeChanged(i2, i3, i4, i5);
        this.mThumbX = -1.0f;
    }

    @Override
    public void performFeedback() {
        if (this.mEnableVibrator) {
            if ((this.mHasMotorVibrator && this.mEnableAdaptiveVibrator && performAdaptiveFeedback()) || performHapticFeedback(308)) {
                return;
            }
            performHapticFeedback(302);
        }
    }

    @Override
    public void releaseAnim() {
        super.releaseAnim();
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setValues(PropertyValuesHolder.ofFloat("markRadius", this.mCurMarkRadius, this.mMarkRadius), PropertyValuesHolder.ofInt("activeAlpha", Color.alpha(this.mCurActiveMarkColor), 0), PropertyValuesHolder.ofInt("inactiveAlpha", Color.alpha(this.mCurInactiveMarkColor), 0));
        valueAnimator.setDuration(183L);
        valueAnimator.setInterpolator(PROGRESS_SCALE_INTERPOLATOR);
        valueAnimator.addUpdateListener(valueAnimator2 -> {
            mCurMarkRadius = (Float) valueAnimator2.getAnimatedValue("markRadius");
            int intValue = (Integer) valueAnimator2.getAnimatedValue("activeAlpha");
            int intValue2 = (Integer) valueAnimator2.getAnimatedValue("inactiveAlpha");
            mCurActiveMarkColor = Color.argb(intValue, 0, 0, 0);
            mCurInactiveMarkColor = Color.argb(intValue2, 255, 255, 255);
            invalidate();
        });
        valueAnimator.cancel();
        valueAnimator.start();
    }

    @Override
    public void setMax(int i2) {
        if (i2 < getMin()) {
            i2 = getMin();
        }
        if (i2 != this.mMax) {
            setLocalMax(i2);
            if (this.mProgress > i2) {
                setProgress(i2);
            }
            calculateThumbPositionByIndex();
        }
        invalidate();
    }

    @Override
    public void setProgress(int i2, boolean z2, boolean z3) {
        if (this.mProgress != Math.max(0, Math.min(i2, this.mMax))) {
            if (z2) {
                checkThumbPosChange(i2, false, z3);
                calculateThumbPositionByIndex();
                startTransitionAnim(i2, z3);
                return;
            }
            checkThumbPosChange(i2, false, z3);
            if (getWidth() != 0) {
                calculateThumbPositionByIndex();
                this.mOverstep = mThumbX;
                this.mMoveAnimationEndThumbX = mThumbX;
                invalidate();
            }
        }
    }

    @Override
    public void startTransitionAnim(int i2, boolean z2) {
        if (mClickAnimatorSet == null) {
            this.mClickAnimatorSet = new AnimatorSet();
        } else {
            mClickAnimatorSet.cancel();
        }
        ValueAnimator ofInt = ValueAnimator.ofFloat(this.mLabelX, getThumbXByIndex(i2));
        ofInt.addUpdateListener(valueAnimator -> {
            mThumbX = (float) valueAnimator.getAnimatedValue();
            invalidate();
        });
        ofInt.setInterpolator(THUMB_ANIMATE_INTERPOLATOR);
        long abs = (long) ((Math.abs(mThumbX - mLabelX) / getSeekBarWidth()) * 483.0f);
        if (abs < 150) {
            abs = 150;
        }
        this.mClickAnimatorSet.setDuration(abs);
        this.mClickAnimatorSet.play(ofInt);
        this.mClickAnimatorSet.start();
    }
}
