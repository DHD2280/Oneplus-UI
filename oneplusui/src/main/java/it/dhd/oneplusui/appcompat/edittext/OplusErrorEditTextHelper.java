package it.dhd.oneplusui.appcompat.edittext;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.TextWatcher;
import android.text.method.TransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;

import java.util.ArrayList;

import it.dhd.oneplusui.R;
import it.dhd.oneplusui.appcompat.animation.OplusEaseInterpolator;

class OplusErrorEditTextHelper {

    private static final int DELAY_MASK_ANIMATOR = 80;
    private static final int DURATION_HINT_ANIMATOR = 217;
    private static final int DURATION_MASK_ANIMATOR = 133;
    private static final int MAX_COLOR_VALUE = 255;
    private static final float SELECTION_MASK_ALPHA_MAX = 0.3f;
    private static final Rect tmpRect = new Rect();
    private final OplusCutoutDrawable.OplusCollapseTextHelper mOplusCollapseTextHelper;
    private final EditText mEditText;
    private boolean mAnimating;
    private OplusCutoutDrawable mBoxBackground;
    private ColorStateList mCollapsedTextColor;
    private int mErrorColor;
    private Paint mErrorPaint;
    private boolean mErrorState;
    private AnimatorSet mErrorTrueAnimatorSet;
    private ColorStateList mExpandedTextColor;
    private float mHintColorChangeProgress;
    private boolean mIsFocusedAtAnimateBeginning;
    private ArrayList<OplusEditText.OnErrorStateChangedListener> mOnErrorStateChangedListeners;
    private int mOriginalHighlightColor;
    private ColorStateList mOriginalTextColors;
    private float mSelectionMaskAlpha;
    private Paint mSelectionMaskPaint;
    private float mSingleCOUIEditTextHeight;
    private int mStrokeWidth;
    private float mTextShakeOffset;
    private float mTextWidth;

    @SuppressWarnings("unused")
    public OplusErrorEditTextHelper(@NonNull EditText editText) {
        this(editText, 1);
    }

    public OplusErrorEditTextHelper(@NonNull EditText editText, int hintLines) {
        mEditText = editText;
        mOplusCollapseTextHelper = new OplusCutoutDrawable.OplusCollapseTextHelper(editText);
        mOplusCollapseTextHelper.setHintLines(hintLines);
        mOplusCollapseTextHelper.setTextSizeInterpolator(new LinearInterpolator());
        mOplusCollapseTextHelper.setPositionInterpolator(new LinearInterpolator());
        mOplusCollapseTextHelper.setCollapsedTextGravity(Gravity.START | Gravity.TOP);
    }

    private void cancelAnimation() {
        if (mErrorTrueAnimatorSet.isStarted()) {
            mErrorTrueAnimatorSet.cancel();
        }
    }

    @SuppressLint("SwitchIntDef")
    private Layout.Alignment getAlignment() {
        return switch (mEditText.getTextAlignment()) {
            case View.TEXT_ALIGNMENT_GRAVITY -> getAlignmentFromGravity();
            case View.TEXT_ALIGNMENT_TEXT_END, View.TEXT_ALIGNMENT_VIEW_END ->
                    Layout.Alignment.ALIGN_OPPOSITE;
            case View.TEXT_ALIGNMENT_CENTER -> Layout.Alignment.ALIGN_CENTER;
            default -> Layout.Alignment.ALIGN_NORMAL;
        };
    }

    private Layout.Alignment getAlignmentFromGravity() {
        int gravity = mEditText.getGravity() & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK;

        return switch (gravity) {
            case Gravity.CENTER_HORIZONTAL -> Layout.Alignment.ALIGN_CENTER;
            case Gravity.RIGHT ->
                    isRtlMode() ? Layout.Alignment.ALIGN_NORMAL : Layout.Alignment.ALIGN_OPPOSITE;
            case Gravity.LEFT ->
                    isRtlMode() ? Layout.Alignment.ALIGN_OPPOSITE : Layout.Alignment.ALIGN_NORMAL;
            case Gravity.START -> Layout.Alignment.ALIGN_NORMAL;
            case Gravity.END -> Layout.Alignment.ALIGN_OPPOSITE;
            default -> Layout.Alignment.ALIGN_NORMAL;
        };
    }

    private CharSequence getFullText() {
        return !isPassword() ? mEditText.getText() : getMaskChars();
    }

    private int getGradientColor(int color, int errorColor, float hintProgress) {
        if (hintProgress <= 0.0f) {
            return color;
        }
        if (hintProgress >= 1.0f) {
            return errorColor;
        }
        float f3 = 1.0f - hintProgress;
        int alpha = (int) ((Color.alpha(color) * f3) + (Color.alpha(errorColor) * hintProgress));
        int red = (int) ((Color.red(color) * f3) + (Color.red(errorColor) * hintProgress));
        int green = (int) ((Color.green(color) * f3) + (Color.green(errorColor) * hintProgress));
        int blue = (int) ((Color.blue(color) * f3) + (Color.blue(errorColor) * hintProgress));
        if (alpha > MAX_COLOR_VALUE) {
            alpha = MAX_COLOR_VALUE;
        }
        if (red > MAX_COLOR_VALUE) {
            red = MAX_COLOR_VALUE;
        }
        if (green > MAX_COLOR_VALUE) {
            green = MAX_COLOR_VALUE;
        }
        if (blue > MAX_COLOR_VALUE) {
            blue = MAX_COLOR_VALUE;
        }
        return Color.argb(alpha, red, green, blue);
    }

    private CharSequence getMaskChars() {
        TransformationMethod transformationMethod = mEditText.getTransformationMethod();
        return transformationMethod != null ? transformationMethod.getTransformation(mEditText.getText(), mEditText) : mEditText.getText();
    }

    private int getSelectionMaskColor(float f2) {
        return Color.argb((int) (f2 * 255.0f), Color.red(mErrorColor), Color.green(mErrorColor), Color.blue(mErrorColor));
    }

    private void initAnimator() {
        float dimension = mEditText.getResources().getDimension(R.dimen.oplus_edit_text_shake_amplitude);
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setInterpolator(new OplusEaseInterpolator());
        ofFloat.setDuration(DURATION_HINT_ANIMATOR);
        ofFloat.addUpdateListener(valueAnimator -> mHintColorChangeProgress = (Float) valueAnimator.getAnimatedValue());
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(0.0f, dimension);
        ofFloat2.setInterpolator(new ShakeInterpolator());
        ofFloat2.setDuration(450L);
        ofFloat2.addUpdateListener(valueAnimator -> {
            if (mIsFocusedAtAnimateBeginning) {
                mTextShakeOffset = (Float) valueAnimator.getAnimatedValue();
            }
            mEditText.invalidate();
        });
        ValueAnimator ofFloat3 = ValueAnimator.ofFloat(0.0f, SELECTION_MASK_ALPHA_MAX);
        ofFloat3.setInterpolator(new OplusEaseInterpolator());
        ofFloat3.setDuration(DURATION_MASK_ANIMATOR);
        ofFloat3.setStartDelay(DELAY_MASK_ANIMATOR);
        ofFloat3.addUpdateListener(valueAnimator -> {
            if (mIsFocusedAtAnimateBeginning) {
                mSelectionMaskAlpha = (Float) valueAnimator.getAnimatedValue();
            }
        });
        mErrorTrueAnimatorSet = new AnimatorSet();
        mErrorTrueAnimatorSet.playTogether(ofFloat, ofFloat2, ofFloat3);
        mErrorTrueAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                setErrorStateEnd(true, true, true);
                performOnErrorStateChangeAnimationEnd(true);
            }

            @Override
            public void onAnimationStart(@NonNull Animator animator) {
                mEditText.setSelection(mEditText.length());
                if (mSingleCOUIEditTextHeight <= 0.0f) {
                    mEditText.post(() -> mSingleCOUIEditTextHeight = mEditText.getHeight());
                }
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {}

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {}
        });
    }

    private boolean isPassword() {
        return (mEditText.getInputType() & InputType.TYPE_TEXT_VARIATION_PASSWORD) == InputType.TYPE_TEXT_VARIATION_PASSWORD || (mEditText.getInputType() & InputType.TYPE_NUMBER_VARIATION_PASSWORD) == InputType.TYPE_NUMBER_VARIATION_PASSWORD;
    }

    private boolean isRtlMode() {
        return mEditText.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    public void performOnErrorStateChangeAnimationEnd(boolean z2) {
        if (mOnErrorStateChangedListeners != null) {
            for (int i = 0; i < mOnErrorStateChangedListeners.size(); i++) {
                mOnErrorStateChangedListeners.get(i).onErrorStateChangeAnimationEnd(z2);
            }
        }
    }

    private void performOnErrorStateChanged(boolean z2) {
        if (mOnErrorStateChangedListeners != null) {
            for (int i = 0; i < mOnErrorStateChangedListeners.size(); i++) {
                mOnErrorStateChangedListeners.get(i).onErrorStateChanged(z2);
            }
        }
    }

    public void setErrorStateEnd(boolean z2, boolean z3, boolean z4) {
        mAnimating = false;
        if (!z2) {
            mEditText.setTextColor(mOriginalTextColors);
            mEditText.setHighlightColor(mOriginalHighlightColor);
            return;
        }
        if (z3) {
            mEditText.setTextColor(mOriginalTextColors);
        }
        mEditText.setHighlightColor(getSelectionMaskColor(SELECTION_MASK_ALPHA_MAX));
        if (z4) {
            EditText editText = mEditText;
            editText.setSelection(0, editText.getText().length());
        }
    }

    private void setErrorStateWithAnimation(boolean z2, boolean z3) {
        if (!z2) {
            cancelAnimation();
            setErrorStateEnd(false, false, z3);
            return;
        }
        cancelAnimation();
        mEditText.setTextColor(0);
        mEditText.setHighlightColor(0);
        mHintColorChangeProgress = 0.0f;
        mTextShakeOffset = 0.0f;
        mSelectionMaskAlpha = 0.0f;
        mAnimating = true;
        mIsFocusedAtAnimateBeginning = mEditText.isFocused();
        mErrorTrueAnimatorSet.start();
    }

    private void setErrorStateWithoutAnimation(boolean z2, boolean z3) {
        if (!z2) {
            setErrorStateEnd(false, false, z3);
            return;
        }
        mHintColorChangeProgress = 1.0f;
        mTextShakeOffset = 0.0f;
        mSelectionMaskAlpha = 0.0f;
        setErrorStateEnd(true, false, z3);
    }

    public void addOnErrorStateChangedListener(OplusEditText.OnErrorStateChangedListener onErrorStateChangedListener) {
        if (mOnErrorStateChangedListeners == null) {
            mOnErrorStateChangedListeners = new ArrayList<>();
        }
        if (mOnErrorStateChangedListeners.contains(onErrorStateChangedListener)) {
            return;
        }
        mOnErrorStateChangedListeners.add(onErrorStateChangedListener);
    }

    public void drawCollapseText(Canvas canvas, OplusCutoutDrawable.OplusCollapseTextHelper cOUICollapseTextHelper) {
        mOplusCollapseTextHelper.setCollapsedTextColor(ColorStateList.valueOf(getGradientColor(mCollapsedTextColor.getDefaultColor(), mErrorColor, mHintColorChangeProgress)));
        mOplusCollapseTextHelper.setExpandedTextColor(ColorStateList.valueOf(getGradientColor(mExpandedTextColor.getDefaultColor(), mErrorColor, mHintColorChangeProgress)));
        mOplusCollapseTextHelper.setExpansionFraction(cOUICollapseTextHelper.getExpandedFraction());
        mOplusCollapseTextHelper.draw(canvas);
    }

    public void drawModeBackgroundLine(Canvas canvas, int i2, int i3, int i4, Paint paint, Paint paint2) {
        mErrorPaint.setColor(getGradientColor(paint.getColor(), mErrorColor, mHintColorChangeProgress));
        canvas.drawRect(0.0f, i2 - mStrokeWidth, i3, (float) i2, mErrorPaint);
        mErrorPaint.setColor(getGradientColor(paint2.getColor(), mErrorColor, mHintColorChangeProgress));
        canvas.drawRect(0.0f, i2 - mStrokeWidth, i4, (float) i2, mErrorPaint);
    }

    public void drawModeBackgroundRect(Canvas canvas, GradientDrawable gradientDrawable, int strokeColor) {
        mBoxBackground.setBounds(gradientDrawable.getBounds());
        if (gradientDrawable instanceof OplusCutoutDrawable) {
            mBoxBackground.setCutout(((OplusCutoutDrawable) gradientDrawable).getCutout());
        }
        mBoxBackground.setStroke(mStrokeWidth, getGradientColor(strokeColor, mErrorColor, mHintColorChangeProgress));
        mBoxBackground.draw(canvas);
    }

    public void drawableStateChanged(int[] iArr) {
        mOplusCollapseTextHelper.setState(iArr);
    }

    public void init(int i2, int i3, int i4, float[] fArr, OplusCutoutDrawable.OplusCollapseTextHelper cOUICollapseTextHelper) {
        mOriginalTextColors = mEditText.getTextColors();
        mOriginalHighlightColor = mEditText.getHighlightColor();
        mErrorColor = i2;
        mStrokeWidth = i3;
        if (i4 == 2) {
            mOplusCollapseTextHelper.setTypefaces(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        }
        mOplusCollapseTextHelper.setExpandedTextSize(cOUICollapseTextHelper.getExpandedTextSize());
        mOplusCollapseTextHelper.setCollapsedTextGravity(cOUICollapseTextHelper.getCollapsedTextGravity());
        mOplusCollapseTextHelper.setExpandedTextGravity(cOUICollapseTextHelper.getExpandedTextGravity());
        OplusCutoutDrawable OplusCutoutDrawable = new OplusCutoutDrawable();
        mBoxBackground = OplusCutoutDrawable;
        OplusCutoutDrawable.setCornerRadii(fArr);
        mErrorPaint = new Paint();
        mSelectionMaskPaint = new Paint();
        initAnimator();
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                setErrorState(false, false, false);
                Editable text = mEditText.getText();
                int length = text.length();
                OplusErrorEditTextHelper cOUIErrorEditTextHelper = OplusErrorEditTextHelper.this;
                cOUIErrorEditTextHelper.mTextWidth = cOUIErrorEditTextHelper.mEditText.getPaint().measureText(text, 0, length);
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i5, int i6, int i7) {
                if (mSingleCOUIEditTextHeight <= 0.0f) {
                    mSingleCOUIEditTextHeight = mEditText.getHeight();
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i5, int i6, int i7) {
            }
        });
        setHintInternal(cOUICollapseTextHelper);
        updateLabelState(cOUICollapseTextHelper);
    }

    public boolean isErrorState() {
        return mErrorState;
    }

    public void setErrorState(boolean z2) {
        setErrorState(z2, true);
    }

    public void onDraw(Canvas canvas) {
        float f2;
        float f3;
        if (mAnimating && mErrorState) {
            int save = canvas.save();
            if (isRtlMode()) {
                canvas.translate(-mTextShakeOffset, 0.0f);
            } else {
                canvas.translate(mTextShakeOffset, 0.0f);
            }
            int compoundPaddingStart = mEditText.getCompoundPaddingStart();
            int compoundPaddingEnd = mEditText.getCompoundPaddingEnd();
            int width = mEditText.getWidth() - compoundPaddingEnd;
            int i2 = width - compoundPaddingStart;
            float x2 = width + mEditText.getX() + mEditText.getScrollX();
            float f4 = i2;
            float scrollX = (mTextWidth - mEditText.getScrollX()) - f4;
            mEditText.getLineBounds(0, tmpRect);
            int save2 = canvas.save();
            if (isRtlMode()) {
                canvas.translate(compoundPaddingEnd, tmpRect.top);
            } else {
                canvas.translate(compoundPaddingStart, tmpRect.top);
            }
            int save3 = canvas.save();
            if (mEditText.getBottom() - mEditText.getTop() == mSingleCOUIEditTextHeight && mTextWidth > f4) {
                if (isRtlMode()) {
                    canvas.clipRect(mEditText.getScrollX() + i2, 0.0f, mEditText.getScrollX(), mSingleCOUIEditTextHeight);
                } else {
                    canvas.translate(-scrollX, 0.0f);
                    canvas.clipRect(mEditText.getScrollX(), 0.0f, x2, mSingleCOUIEditTextHeight);
                }
            }
            Layout layout = mEditText.getLayout();
            layout.getPaint().setColor(mOriginalTextColors.getDefaultColor());
            layout.draw(canvas);
            canvas.restoreToCount(save3);
            canvas.restoreToCount(save2);
            Layout.Alignment alignment = getAlignment();
            mSelectionMaskPaint.setColor(getSelectionMaskColor(mSelectionMaskAlpha));
            if ((alignment != Layout.Alignment.ALIGN_NORMAL || isRtlMode()) && (!(alignment == Layout.Alignment.ALIGN_OPPOSITE && isRtlMode()) && (!(alignment == Layout.Alignment.ALIGN_NORMAL && isRtlMode()) && (alignment != Layout.Alignment.ALIGN_OPPOSITE || isRtlMode())))) {
                float f5 = ((compoundPaddingStart + mTextWidth) - compoundPaddingEnd) / 2.0f;
                float f6 = mTextWidth;
                float f7 = f5 - (f6 / 2.0f);
                f2 = f7;
                f3 = f7 + f6;
            } else {
                f2 = compoundPaddingStart;
                f3 = f2;
            }
            canvas.drawRect(f2, tmpRect.top, f3, tmpRect.bottom, mSelectionMaskPaint);
            canvas.restoreToCount(save);
        }
    }

    public void onLayout(OplusCutoutDrawable.OplusCollapseTextHelper cOUICollapseTextHelper) {
        Rect expandedBounds = cOUICollapseTextHelper.getExpandedBounds();
        Rect collapsedBounds = cOUICollapseTextHelper.getCollapsedBounds();
        mOplusCollapseTextHelper.setExpandedBounds(expandedBounds.left, expandedBounds.top, expandedBounds.right, expandedBounds.bottom);
        mOplusCollapseTextHelper.setCollapsedBounds(collapsedBounds.left, collapsedBounds.top, collapsedBounds.right, collapsedBounds.bottom);
        mOplusCollapseTextHelper.recalculate();
    }

    public void removeOnErrorStateChangedListener(@Nullable OplusEditText.OnErrorStateChangedListener onErrorStateChangedListener) {
        ArrayList<OplusEditText.OnErrorStateChangedListener> arrayList = mOnErrorStateChangedListeners;
        if (arrayList == null) {
            return;
        }
        arrayList.remove(onErrorStateChangedListener);
    }

    public void setCollapsedTextAppearance(int i2, ColorStateList colorStateList) {
        mOplusCollapseTextHelper.setCollapsedTextAppearance(i2, colorStateList);
    }

    public void setErrorColor(int i2) {
        mErrorColor = i2;
    }

    public void setHintInternal(OplusCutoutDrawable.OplusCollapseTextHelper cOUICollapseTextHelper) {
        mOplusCollapseTextHelper.setText(cOUICollapseTextHelper.getText());
    }

    public void setOriginalTextColors(ColorStateList colorStateList) {
        mOriginalTextColors = colorStateList;
    }

    public void updateLabelState(OplusCutoutDrawable.OplusCollapseTextHelper cOUICollapseTextHelper) {
        mCollapsedTextColor = cOUICollapseTextHelper.getCollapsedTextColor();
        mExpandedTextColor = cOUICollapseTextHelper.getExpandedTextColor();
        mOplusCollapseTextHelper.setCollapsedTextColor(mCollapsedTextColor);
        mOplusCollapseTextHelper.setExpandedTextColor(mExpandedTextColor);
    }

    private void setErrorState(boolean z2, boolean z3) {
        setErrorState(z2, z3, true);
    }

    public void setErrorState(boolean z2, boolean z3, boolean z4) {
        if (mErrorState == z2) {
            return;
        }
        mErrorState = z2;
        performOnErrorStateChanged(z2);
        if (z3) {
            setErrorStateWithAnimation(z2, z4);
        } else {
            setErrorStateWithoutAnimation(z2, z4);
        }
    }

    public static class ShakeInterpolator implements Interpolator {
        static final int TOTAL_DURATION = 450;
        private static final int[] DURATIONS;
        private static final float[] OFFSETS = {0.0f, -1.0f, 0.5f, -0.5f, 0.0f};
        private static final float[] progresses;

        static {
            int[] iArr = {83, DURATION_MASK_ANIMATOR, 117, 117};
            DURATIONS = iArr;
            progresses = new float[iArr.length + 1];
            int i = 0;
            int total = 0;
            while (i < iArr.length) {
                total += iArr[i];
                progresses[i + 1] = (float) total / TOTAL_DURATION;
                i++;
            }
        }

        private final Interpolator mBetweenInterpolator;

        private ShakeInterpolator() {
            mBetweenInterpolator = new OplusEaseInterpolator();
        }

        @Override
        public float getInterpolation(float f2) {
            int i2 = 1;
            while (true) {
                float[] fArr = progresses;
                if (i2 >= fArr.length) {
                    return 0.0f;
                }
                float f3 = fArr[i2];
                if (f2 <= f3) {
                    int i3 = i2 - 1;
                    float f4 = fArr[i3];
                    float interpolation = mBetweenInterpolator.getInterpolation((f2 - f4) / (f3 - f4));
                    float[] fArr2 = OFFSETS;
                    return (fArr2[i3] * (1.0f - interpolation)) + (fArr2[i2] * interpolation);
                }
                i2++;
            }
        }
    }
}
