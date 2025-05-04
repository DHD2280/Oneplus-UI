package it.dhd.oneplusui.appcompat.edittext;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.Selection;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.widget.ExploreByTouchHelper;

import java.util.List;

import it.dhd.oneplusui.R;
import it.dhd.oneplusui.appcompat.animation.OplusInEaseInterpolator;
import it.dhd.oneplusui.appcompat.animation.OplusMoveEaseInterpolator;

/**
 * Custom EditText with various features including hint animation, error state handling, and delete icon functionality.
 */
public class OplusEditText extends AppCompatEditText {

    public static final int MODE_BACKGROUND_LINE = 1;
    public static final int MODE_BACKGROUND_NONE = 0;
    public static final int MODE_BACKGROUND_NO_LINE = 3;
    public static final int MODE_BACKGROUND_RECT = 2;
    private static final int ALPHA_VALUE = 255;
    private static final int BACKGROUND_ANIMATION_DURATION = 250;
    private static final int LABEL_SCALE_ANIMATION_DURATION = 200;
    @SuppressWarnings("unused")
    private static final boolean LOG_DBG = false;
    private static final String TAG = "OplusEditText";
    private final OplusCutoutDrawable.OplusCollapseTextHelper mOplusCollapseTextHelper;
    private final Runnable mCancelDeleteIcon;
    private ValueAnimator mAnimator;
    private ValueAnimator mAnimator1;
    private ValueAnimator mAnimator2;
    private GradientDrawable mBoxBackground;
    private int mBoxBackgroundMode;
    private float mBoxCornerRadiusBottomEnd;
    private float mBoxCornerRadiusBottomStart;
    private float mBoxCornerRadiusTopEnd;
    private float mBoxCornerRadiusTopStart;
    private int mBoxStrokeColor;
    private OplusTextWatcher mOplusTextWatcher;
    private int mClickSelectionPosition;
    private final Context mContext;
    private OnTouchListener mCustomEditTextTouchListener;
    private ColorStateList mDefaultHintTextColor;
    private int mDefaultStrokeColor;
    private boolean mDeletable;
    private String mDeleteButton;
    private int mDeleteIconHeight;
    private int mDeleteIconWidth;
    private Drawable mDeleteNormal;
    private Drawable mDeletePressed;
    private int mDisabledColor;
    private Paint mDisabledPaint;
    private float mDrawXProgress;
    private int mDrawableSizeRight;
    private OnFocusChangeListener mEditFocusChangeListener;
    private Paint mEmptyTextPaint;
    private int mErrorColor;
    private boolean mErrorState;
    private final OplusErrorEditTextHelper mErrorStateHelper;
    private int mFocusedAlpha;
    private Paint mFocusedPaint;
    private int mFocusedStrokeColor;
    private ColorStateList mFocusedTextColor;
    private boolean mForceFinishDetach;
    private CharSequence mHint;
    private boolean mHintAnimationEnabled;
    private boolean mHintEnabled;
    private boolean mHintExpanded;
    private boolean mInDrawableStateChanged;
    private InputConnectionListener mInputConnectionListener;
    private String mInputText;
    private boolean mIsEllipsis;
    private boolean mIsEllipsisEnabled;
    private boolean mIsProvidingHint;
    private boolean mJustShowFocusLine;
    private int mLabelCutoutPadding;
    private boolean mLineExpanded;
    private int mLineModePaddingMiddle;
    private int mLineModePaddingTop;
    private int mLinePadding;
    private Paint mNormalPaint;
    private CharSequence mOriginalHint;
    private OnPasswordDeletedListener mPasswordDeleteListener;
    private Interpolator mPathInterpolator1;
    private Interpolator mPathInterpolator2;
    private boolean mQuickDelete;
    private int mRectModePaddingTop;
    private int mRefreshStyle;
    private final Runnable mSetDeleteIcon;
    private boolean mShouldHandleDelete;
    private boolean mShowDeleteIcon;
    private int mStrokeWidth;
    private int mStrokeWidthFocused;
    private OnTextDeletedListener mTextDeleteListener;
    private TextPaint mTextPaint;
    private final RectF mTmpRectF;
    private final AccessibilityTouchHelper mTouchHelper;

    public OplusEditText(Context context) {
        this(context, null);
    }

    public OplusEditText(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.oplusEditTextStyle);
    }

    public OplusEditText(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        mOplusCollapseTextHelper = new OplusCutoutDrawable.OplusCollapseTextHelper(this);
        mShouldHandleDelete = false;
        mQuickDelete = false;
        mDeletable = false;
        mTextDeleteListener = null;
        mPasswordDeleteListener = null;
        mForceFinishDetach = false;
        mDeleteButton = null;
        mOplusTextWatcher = null;
        mStrokeWidth = 1;
        mStrokeWidthFocused = 3;
        mTmpRectF = new RectF();
        mIsEllipsis = false;
        mIsEllipsisEnabled = false;
        mInputText = "";
        mClickSelectionPosition = 0;
        mShowDeleteIcon = true;
        mJustShowFocusLine = false;
        mCancelDeleteIcon = () -> setCompoundDrawables(null, null, null, null);
        mSetDeleteIcon = () -> setCompoundDrawables(null, null, mDeleteNormal, null);
        if (attributeSet != null) {
            mRefreshStyle = attributeSet.getStyleAttribute();
        }
        if (mRefreshStyle == 0) {
            mRefreshStyle = defStyleAttr;
        }
        mContext = context;
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.OplusEditText, defStyleAttr, 0);
        boolean quickDelete = obtainStyledAttributes.getBoolean(R.styleable.OplusEditText_quickDelete, false);
        mErrorColor = obtainStyledAttributes.getColor(R.styleable.OplusEditText_editTextErrorColor, getResources().getColor(R.color.oplus_color_error_text_bg, getContext().getTheme()));
        mDeleteNormal = obtainStyledAttributes.getDrawable(R.styleable.OplusEditText_editTextDeleteIconNormal);
        mDeletePressed = obtainStyledAttributes.getDrawable(R.styleable.OplusEditText_editTextDeleteIconPressed);
        mIsEllipsisEnabled = obtainStyledAttributes.getBoolean(R.styleable.OplusEditText_editTextIsEllipsis, true);
        int hintLines = obtainStyledAttributes.getInt(R.styleable.OplusEditText_editTextHintLines, 1);
        mOplusCollapseTextHelper.setHintLines(hintLines);
        obtainStyledAttributes.recycle();
        setFastDeletable(quickDelete);
        Drawable drawable = mDeleteNormal;
        if (drawable != null) {
            mDeleteIconWidth = drawable.getIntrinsicWidth();
            int intrinsicHeight = mDeleteNormal.getIntrinsicHeight();
            mDeleteIconHeight = intrinsicHeight;
            mDeleteNormal.setBounds(0, 0, mDeleteIconWidth, intrinsicHeight);
        }
        if (mDeletePressed != null) {
            mDeletePressed.setBounds(0, 0, mDeleteIconWidth, mDeleteIconHeight);
        }
        mOplusCollapseTextHelper.setHintPaddingStart(context.getResources().getDimensionPixelSize(R.dimen.oplus_edit_text_hint_start_padding));
        AccessibilityTouchHelper accessibilityTouchHelper = new AccessibilityTouchHelper(this);
        mTouchHelper = accessibilityTouchHelper;
        ViewCompat.setAccessibilityDelegate(this, accessibilityTouchHelper);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        mDeleteButton = mContext.getString(android.R.string.cancel);
        mTouchHelper.invalidateRoot();
        mErrorStateHelper = new OplusErrorEditTextHelper(this, hintLines);
        initHintMode(context, attributeSet, defStyleAttr);
        mErrorStateHelper.init(mErrorColor, mStrokeWidthFocused, mBoxBackgroundMode, getCornerRadiiAsArray(), mOplusCollapseTextHelper);
    }

    private void animateToExpansionFraction(float f2) {
        if (mOplusCollapseTextHelper.getExpansionFraction() == f2) {
            return;
        }
        if (mAnimator == null) {
            mAnimator = new ValueAnimator();
            mAnimator.setInterpolator(mPathInterpolator1);
            mAnimator.setDuration(LABEL_SCALE_ANIMATION_DURATION);
            mAnimator.addUpdateListener(valueAnimator2 -> mOplusCollapseTextHelper.setExpansionFraction((Float) valueAnimator2.getAnimatedValue()));
        }
        mAnimator.setFloatValues(mOplusCollapseTextHelper.getExpansionFraction(), f2);
        mAnimator.start();
    }

    private void animateToHideBackground() {
        if (mAnimator2 == null) {
            mAnimator2 = new ValueAnimator();
            mAnimator2.setInterpolator(mPathInterpolator2);
            mAnimator2.setDuration(BACKGROUND_ANIMATION_DURATION);
            mAnimator2.addUpdateListener(valueAnimator2 -> {
                mFocusedAlpha = (Integer) valueAnimator2.getAnimatedValue();
                invalidate();
            });
        }
        mAnimator2.setIntValues(255, 0);
        mAnimator2.start();
        mLineExpanded = false;
    }

    private void animateToShowBackground() {
        if (mAnimator1 == null) {
            mAnimator1 = new ValueAnimator();
            mAnimator1.setInterpolator(mPathInterpolator2);
            mAnimator1.setDuration(BACKGROUND_ANIMATION_DURATION);
            mAnimator1.addUpdateListener(valueAnimator2 -> {
                mDrawXProgress = (Float) valueAnimator2.getAnimatedValue();
                invalidate();
            });
        }
        mFocusedAlpha = ALPHA_VALUE;
        mAnimator1.setFloatValues(0.0f, 1.0f);
        ValueAnimator valueAnimator2 = mAnimator2;
        if (valueAnimator2 != null && valueAnimator2.isRunning()) {
            mAnimator2.cancel();
        }
        mAnimator1.start();
        mLineExpanded = true;
    }

    private void applyBoxAttributes() {
        int i2;
        if (mBoxBackground == null) {
            return;
        }
        setBoxAttributes();
        int i3 = mStrokeWidth;
        if (i3 > -1 && (i2 = mBoxStrokeColor) != 0) {
            mBoxBackground.setStroke(i3, i2);
        }
        mBoxBackground.setCornerRadii(getCornerRadiiAsArray());
        invalidate();
    }

    private void applyCutoutPadding(RectF rectF) {
        rectF.left = rectF.left - mLabelCutoutPadding;
        rectF.top -= mLabelCutoutPadding;
        rectF.right += mLabelCutoutPadding;
        rectF.bottom += mLabelCutoutPadding;
    }

    private void assignBoxBackgroundByMode() {
        int i2 = mBoxBackgroundMode;
        if (i2 == MODE_BACKGROUND_NONE) {
            mBoxBackground = null;
            return;
        }
        if (i2 == MODE_BACKGROUND_RECT && mHintEnabled && !(mBoxBackground instanceof OplusCutoutDrawable)) {
            mBoxBackground = new OplusCutoutDrawable();
        } else if (mBoxBackground == null) {
            mBoxBackground = new GradientDrawable();
        }
    }

    private int calculateCollapsedTextTopBounds() {
        int i2 = mBoxBackgroundMode;
        if (i2 == 1) {
            if (getBoxBackground() != null) {
                return getBoxBackground().getBounds().top;
            }
            return 0;
        }
        if (i2 != 2 && i2 != 3) {
            return getPaddingTop();
        }
        if (getBoxBackground() != null) {
            return getBoxBackground().getBounds().top - getLabelMarginTop();
        }
        return 0;
    }

    private void closeCutout() {
        if (cutoutEnabled()) {
            ((OplusCutoutDrawable) mBoxBackground).removeCutout();
        }
    }

    private void collapseHint(boolean z2) {
        ValueAnimator valueAnimator = mAnimator;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            mAnimator.cancel();
        }
        if (z2 && mHintAnimationEnabled) {
            animateToExpansionFraction(1.0f);
        } else {
            mOplusCollapseTextHelper.setExpansionFraction(1.0f);
        }
        mHintExpanded = false;
        if (cutoutEnabled()) {
            openCutout();
        }
    }

    private boolean cutoutEnabled() {
        return mHintEnabled && !TextUtils.isEmpty(mHint) && (mBoxBackground instanceof OplusCutoutDrawable);
    }

    private void expandHint(boolean z2) {
        if (mBoxBackground != null) {
            Log.d(TAG, "mBoxBackground: " + mBoxBackground.getBounds());
        }
        ValueAnimator valueAnimator = mAnimator;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            mAnimator.cancel();
        }
        if (z2 && mHintAnimationEnabled) {
            animateToExpansionFraction(0.0f);
        } else {
            mOplusCollapseTextHelper.setExpansionFraction(0.0f);
        }
        if (cutoutEnabled() && ((OplusCutoutDrawable) mBoxBackground).hasCutout()) {
            closeCutout();
        }
        mHintExpanded = true;
    }

    private int getBoundsTop() {
        int i2 = mBoxBackgroundMode;
        if (i2 == MODE_BACKGROUND_LINE) {
            return mLineModePaddingTop;
        }
        if (i2 == MODE_BACKGROUND_RECT || i2 == MODE_BACKGROUND_NO_LINE) {
            return (int) (mOplusCollapseTextHelper.getCollapsedTextHeight() / 2.0f);
        }
        return 0;
    }

    private Drawable getBoxBackground() {
        int i2 = mBoxBackgroundMode;
        if (i2 == MODE_BACKGROUND_LINE || i2 == MODE_BACKGROUND_RECT) {
            return mBoxBackground;
        }
        return null;
    }

    private boolean getContentRect(Rect rect) {
        int compoundPaddingLeft = isRtlMode() ? (getCompoundPaddingLeft() - mDeleteIconWidth) - getCompoundDrawablePadding() : (getWidth() - getCompoundPaddingRight()) + getCompoundDrawablePadding();
        int i2 = mDeleteIconWidth + compoundPaddingLeft;
        int height = ((((getHeight() - getCompoundPaddingTop()) - getCompoundPaddingBottom()) - mDeleteIconWidth) / 2) + getCompoundPaddingTop();
        rect.set(compoundPaddingLeft, height, i2, mDeleteIconWidth + height);
        return true;
    }

    private float[] getCornerRadiiAsArray() {
        return new float[]{
                mBoxCornerRadiusTopEnd, mBoxCornerRadiusTopEnd,
                mBoxCornerRadiusTopStart, mBoxCornerRadiusTopStart,
                mBoxCornerRadiusBottomStart, mBoxCornerRadiusBottomStart,
                mBoxCornerRadiusBottomEnd, mBoxCornerRadiusBottomEnd};
    }

    private int getModePaddingTop() {
        int hintHeight;
        int padding;
        if (mBoxBackgroundMode == MODE_BACKGROUND_LINE) {
            hintHeight = mLineModePaddingTop + ((int) mOplusCollapseTextHelper.getHintHeight());
            padding = mLineModePaddingMiddle;
        } else if (mBoxBackgroundMode != MODE_BACKGROUND_RECT && mBoxBackgroundMode != MODE_BACKGROUND_NO_LINE) {
            return 0;
        } else {
            hintHeight = mRectModePaddingTop;
            padding = (int) (mOplusCollapseTextHelper.getCollapsedTextHeight() / 2.0f);
        }
        return hintHeight + padding;
    }

    private void initHintMode(Context context, AttributeSet attributeSet, int defStyleRes) {
        mOplusCollapseTextHelper.setTextSizeInterpolator(new LinearInterpolator());
        mOplusCollapseTextHelper.setPositionInterpolator(new LinearInterpolator());
        mOplusCollapseTextHelper.setCollapsedTextGravity(Gravity.START | Gravity.TOP);
        mPathInterpolator1 = new OplusMoveEaseInterpolator();
        mPathInterpolator2 = new OplusInEaseInterpolator();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.OplusEditText, defStyleRes, R.style.Widget_Oplus_EditText_HintAnim_Line);
        mHintEnabled = obtainStyledAttributes.getBoolean(R.styleable.OplusEditText_hintEnabled, false);
        setTopHint(obtainStyledAttributes.getText(R.styleable.OplusEditText_android_hint));
        if (mHintEnabled) {
            mHintAnimationEnabled = obtainStyledAttributes.getBoolean(R.styleable.OplusEditText_hintAnimationEnabled, true);
        }
        mRectModePaddingTop = obtainStyledAttributes.getDimensionPixelOffset(R.styleable.OplusEditText_rectModePaddingTop, 0);
        float dimension = obtainStyledAttributes.getDimension(R.styleable.OplusEditText_cornerRadius, 0.0f);
        mBoxCornerRadiusTopStart = dimension;
        mBoxCornerRadiusTopEnd = dimension;
        mBoxCornerRadiusBottomEnd = dimension;
        mBoxCornerRadiusBottomStart = dimension;
        mFocusedStrokeColor = obtainStyledAttributes.getColor(R.styleable.OplusEditText_strokeColor, getResources().getColor(android.R.color.system_accent1_400, getContext().getTheme()));
        mStrokeWidth = obtainStyledAttributes.getDimensionPixelSize(R.styleable.OplusEditText_strokeWidth, 0);
        mStrokeWidthFocused = obtainStyledAttributes.getDimensionPixelSize(R.styleable.OplusEditText_focusStrokeWidth, mStrokeWidthFocused);
        mLinePadding = context.getResources().getDimensionPixelOffset(R.dimen.oplus_textinput_line_padding);
        if (mHintEnabled) {
            mLabelCutoutPadding = context.getResources().getDimensionPixelOffset(R.dimen.oplus_textinput_label_cutout_padding);
            mLineModePaddingTop = context.getResources().getDimensionPixelOffset(R.dimen.oplus_textinput_line_padding_top);
            mLineModePaddingMiddle = context.getResources().getDimensionPixelOffset(R.dimen.oplus_textinput_line_padding_middle);
        }
        int backgroundMode = obtainStyledAttributes.getInt(R.styleable.OplusEditText_backgroundMode, MODE_BACKGROUND_NONE);
        setBoxBackgroundMode(backgroundMode);
        if (mBoxBackgroundMode != MODE_BACKGROUND_NONE) {
            setBackgroundDrawable(null);
        }
        int i4 = R.styleable.OplusEditText_android_textColorHint;
        if (obtainStyledAttributes.hasValue(i4)) {
            ColorStateList colorStateList = obtainStyledAttributes.getColorStateList(i4);
            mDefaultHintTextColor = colorStateList;
            mFocusedTextColor = colorStateList;
        }
        mDefaultStrokeColor = obtainStyledAttributes.getColor(R.styleable.OplusEditText_defaultStrokeColor, 0);
        mDisabledColor = obtainStyledAttributes.getColor(R.styleable.OplusEditText_disabledStrokeColor, 0);
        String string = obtainStyledAttributes.getString(R.styleable.OplusEditText_editTextNoEllipsisText);
        mInputText = string;
        setText(string);
        setCollapsedTextAppearance(obtainStyledAttributes.getDimensionPixelSize(R.styleable.OplusEditText_collapsedTextSize, 0), obtainStyledAttributes.getColorStateList(R.styleable.OplusEditText_collapsedTextColor));
        if (backgroundMode == MODE_BACKGROUND_RECT) {
            mOplusCollapseTextHelper.setTypefaces(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        }
        obtainStyledAttributes.recycle();
        mEmptyTextPaint = new Paint();
        mTextPaint = new TextPaint();
        mTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_color_primary));
        mTextPaint.setTextSize(getTextSize());
        mNormalPaint = new Paint();
        mNormalPaint.setColor(mDefaultStrokeColor);
        mDisabledPaint = new Paint();
        mDisabledPaint.setColor(mDisabledColor);
        mFocusedPaint = new Paint();
        mFocusedPaint.setColor(mFocusedStrokeColor);
        setEditText();
    }

    private boolean isEmpty(String str) {
        if (str == null) {
            return false;
        }
        return TextUtils.isEmpty(str);
    }

    private boolean isGravityCenterHorizontal() {
        return (getGravity() & 7) == 1;
    }

    private boolean isRtlMode() {
        return getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    private void onApplyBoxBackgroundMode() {
        assignBoxBackgroundByMode();
        updateTextInputBoxBounds();
    }

    private void openCutout() {
        if (cutoutEnabled()) {
            mOplusCollapseTextHelper.getCollapsedTextActualBounds(mTmpRectF);
            applyCutoutPadding(mTmpRectF);
            ((OplusCutoutDrawable) mBoxBackground).setCutout(mTmpRectF);
        }
    }

    private void setBoxAttributes() {
        if (mBoxBackgroundMode == MODE_BACKGROUND_RECT && mFocusedStrokeColor == 0) {
            mFocusedStrokeColor = mFocusedTextColor.getColorForState(getDrawableState(), mFocusedTextColor.getDefaultColor());
        }
    }

    private void setEditText() {
        onApplyBoxBackgroundMode();
        mOplusCollapseTextHelper.setExpandedTextSize(getTextSize());
        int gravity = getGravity();
        mOplusCollapseTextHelper.setCollapsedTextGravity((gravity & (-113)) | 48);
        mOplusCollapseTextHelper.setExpandedTextGravity(gravity);
        if (mDefaultHintTextColor == null) {
            mDefaultHintTextColor = getHintTextColors();
        }
        setHint(mHintEnabled ? null : "");
        if (TextUtils.isEmpty(mHint)) {
            CharSequence hint = getHint();
            mOriginalHint = hint;
            setTopHint(hint);
            setHint(mHintEnabled ? null : "");
        }
        mIsProvidingHint = true;
        updateLabelState(false, true);
        if (mHintEnabled) {
            updateModePadding();
        }
    }

    private void setEllipsize() {
        if (isFocused()) {
            if (mIsEllipsis) {
                setText(mInputText);
                setSelection(Math.min(mClickSelectionPosition, getSelectionEnd()));
            }
            mIsEllipsis = false;
            return;
        }
        if (mTextPaint.measureText(String.valueOf(getText())) <= getWidth() || mIsEllipsis) {
            return;
        }
        mInputText = String.valueOf(getText());
        mIsEllipsis = true;
        setText(TextUtils.ellipsize(getText(), mTextPaint, getWidth(), TextUtils.TruncateAt.END));
        if (mErrorState) {
            setErrorState(true);
        }
    }

    private void setHintInternal(CharSequence charSequence) {
        if (TextUtils.equals(charSequence, mHint)) {
            return;
        }
        mHint = charSequence;
        mOplusCollapseTextHelper.setText(charSequence);
        if (!mHintExpanded) {
            openCutout();
        }
        OplusErrorEditTextHelper cOUIErrorEditTextHelper = mErrorStateHelper;
        if (cOUIErrorEditTextHelper != null) {
            cOUIErrorEditTextHelper.setHintInternal(mOplusCollapseTextHelper);
        }
    }

    public void updateDeletableStatus(boolean deletable) {
        if (TextUtils.isEmpty(getText().toString())) {
            if (isGravityCenterHorizontal()) {
                setPaddingRelative(0, getPaddingTop(), getPaddingEnd(), getPaddingBottom());
            }
            if (mDeletable) {
                setCompoundDrawables(null, null, null, null);
            } else {
                post(mCancelDeleteIcon);
            }
            mDeletable = false;
            return;
        }
        if (!deletable) {
            if (mDeletable) {
                if (isGravityCenterHorizontal()) {
                    setPaddingRelative(0, getPaddingTop(), getPaddingEnd(), getPaddingBottom());
                }
                post(mCancelDeleteIcon);
                mDeletable = false;
                return;
            }
            return;
        }
        if (mDeleteNormal == null || mDeletable) {
            return;
        }
        if (isGravityCenterHorizontal()) {
            setPaddingRelative(mDeleteIconWidth + getCompoundDrawablePadding(), getPaddingTop(), getPaddingEnd(), getPaddingBottom());
        }
        if (isFastDeletable() && mShowDeleteIcon) {
            post(mSetDeleteIcon);
        }
        mDeletable = true;
    }

    private void updateLineModeBackground() {
        if (mBoxBackgroundMode != MODE_BACKGROUND_LINE) {
            return;
        }
        if (!isEnabled()) {
            mDrawXProgress = 0.0f;
            return;
        }
        if (hasFocus()) {
            if (mLineExpanded) {
                return;
            }
            animateToShowBackground();
        } else if (mLineExpanded) {
            animateToHideBackground();
        }
    }

    private void updateModePadding() {
        setPaddingRelative(isRtlMode() ? getPaddingRight() : getPaddingLeft(), getModePaddingTop(), isRtlMode() ? getPaddingLeft() : getPaddingRight(), getPaddingBottom());
    }

    private void updateTextInputBoxBounds() {
        if (mBoxBackgroundMode == MODE_BACKGROUND_NONE || mBoxBackground == null || getRight() == 0) {
            return;
        }
        mBoxBackground.setBounds(0, getBoundsTop(), getWidth(), getHeight());
        applyBoxAttributes();
    }

    private void updateTextInputBoxState() {
        int i2;
        if (mBoxBackground == null || (i2 = mBoxBackgroundMode) == MODE_BACKGROUND_NONE || i2 != MODE_BACKGROUND_RECT) {
            return;
        }
        if (!isEnabled()) {
            mBoxStrokeColor = mDisabledColor;
        } else if (hasFocus()) {
            mBoxStrokeColor = mFocusedStrokeColor;
        } else {
            mBoxStrokeColor = mDefaultStrokeColor;
        }
        applyBoxAttributes();
    }

    public void addOnErrorStateChangedListener(OnErrorStateChangedListener onErrorStateChangedListener) {
        mErrorStateHelper.addOnErrorStateChangedListener(onErrorStateChangedListener);
    }

    public boolean cutoutIsOpen() {
        return cutoutEnabled() && ((OplusCutoutDrawable) mBoxBackground).hasCutout();
    }

    public void destroyAnimators() {
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator.removeAllListeners();
            mAnimator.removeAllUpdateListeners();
            mAnimator = null;
        }
        if (mAnimator1 != null) {
            mAnimator1.cancel();
            mAnimator1.removeAllListeners();
            mAnimator1.removeAllUpdateListeners();
            mAnimator1 = null;
        }
        if (mAnimator2 != null) {
            mAnimator2.cancel();
            mAnimator2.removeAllListeners();
            mAnimator2.removeAllUpdateListeners();
            mAnimator2 = null;
        }
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent motionEvent) {
        AccessibilityTouchHelper accessibilityTouchHelper;
        if (isDeleteButtonExist() && (accessibilityTouchHelper = mTouchHelper) != null && accessibilityTouchHelper.dispatchHoverEvent(motionEvent)) {
            return true;
        }
        return super.dispatchHoverEvent(motionEvent);
    }

    @Override
    public void dispatchStartTemporaryDetach() {
        super.dispatchStartTemporaryDetach();
        if (mForceFinishDetach) {
            onStartTemporaryDetach();
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (getMaxLines() < 2 && mIsEllipsisEnabled) {
            setEllipsize();
        }
        if (getHintTextColors() != mDefaultHintTextColor) {
            updateLabelState(false);
        }
        int save = canvas.save();
        canvas.translate(getScrollX(), getScrollY());
        if (mHintEnabled || getText().length() == 0) {
            mOplusCollapseTextHelper.draw(canvas);
        } else {
            canvas.drawText(" ", 0.0f, 0.0f, mEmptyTextPaint);
        }
        if (mBoxBackground != null && mBoxBackgroundMode == MODE_BACKGROUND_RECT) {
            if (getScrollX() != 0) {
                updateTextInputBoxBounds();
            }
            if (mErrorStateHelper.isErrorState()) {
                mErrorStateHelper.drawModeBackgroundRect(canvas, mBoxBackground, mBoxStrokeColor);
            } else {
                mBoxBackground.draw(canvas);
            }
        }
        if (mBoxBackgroundMode == MODE_BACKGROUND_LINE) {
            int height = getHeight();
            mFocusedPaint.setAlpha(mFocusedAlpha);
            if (isEnabled()) {
                if (mErrorStateHelper.isErrorState()) {
                    mErrorStateHelper.drawModeBackgroundLine(canvas, height, getWidth(), (int) (mDrawXProgress * getWidth()), mNormalPaint, mFocusedPaint);
                } else {
                    if (!mJustShowFocusLine) {
                        canvas.drawRect(0.0f, height - mStrokeWidth, getWidth(), height, mNormalPaint);
                    }
                    if (hasFocus()) {
                        canvas.drawRect(0.0f, height - mStrokeWidthFocused, mDrawXProgress * getWidth(), height, mFocusedPaint);
                    }
                }
            } else if (!mJustShowFocusLine) {
                canvas.drawRect(0.0f, height - mStrokeWidth, getWidth(), height, mDisabledPaint);
            }
        }
        canvas.restoreToCount(save);
        super.draw(canvas);
    }

    @Override
    public void drawableStateChanged() {
        boolean z2;
        if (mInDrawableStateChanged) {
            return;
        }
        mInDrawableStateChanged = true;
        super.drawableStateChanged();
        int[] drawableState = getDrawableState();
        if (mHintEnabled) {
            updateLabelState(ViewCompat.isLaidOut(this) && isEnabled());
        } else {
            updateLabelState(false);
        }
        updateLineModeBackground();
        if (mHintEnabled) {
            updateTextInputBoxBounds();
            updateTextInputBoxState();
            if (mOplusCollapseTextHelper != null) {
                z2 = mOplusCollapseTextHelper.setState(drawableState);
                mErrorStateHelper.drawableStateChanged(drawableState);
                if (z2) {
                    invalidate();
                }
                mInDrawableStateChanged = false;
            }
        }
        mInDrawableStateChanged = false;
    }

    public void forceFinishDetach() {
        mForceFinishDetach = true;
    }

    public Rect getBackgroundRect() {
        int i2 = mBoxBackgroundMode;
        if ((i2 == MODE_BACKGROUND_LINE || i2 == MODE_BACKGROUND_RECT || i2 == MODE_BACKGROUND_NO_LINE) && getBoxBackground() != null) {
            getBoxBackground().getBounds();
        }
        return null;
    }

    public int getBoxStrokeColor() {
        return mFocusedStrokeColor;
    }

    public void setBoxStrokeColor(int i2) {
        if (mFocusedStrokeColor != i2) {
            mFocusedStrokeColor = i2;
            mFocusedPaint.setColor(i2);
            updateTextInputBoxState();
        }
    }

    public String getCouiEditTexttNoEllipsisText() {
        return mIsEllipsis ? mInputText : String.valueOf(getText());
    }

    public void setCouiEditTexttNoEllipsisText(String str) {
        mInputText = str;
        setText(str);
    }

    public int getDeleteButtonLeft() {
        Drawable drawable = mDeleteNormal;
        return ((getRight() - getLeft()) - getPaddingRight()) - (drawable != null ? drawable.getIntrinsicWidth() : 0);
    }

    public int getDeleteIconWidth() {
        return mDeleteIconWidth;
    }

    @Override
    public CharSequence getHint() {
        if (mHintEnabled) {
            return mHint;
        }
        return null;
    }

    public int getLabelMarginTop() {
        if (mHintEnabled) {
            return (int) (mOplusCollapseTextHelper.getCollapsedTextHeight() / 2.0f);
        }
        return 0;
    }

    public OnTextDeletedListener getTextDeleteListener() {
        return mTextDeleteListener;
    }

    public boolean isDeleteButtonExist() {
        return mQuickDelete && !isEmpty(getText().toString()) && hasFocus();
    }

    public boolean isEllipsisEnabled() {
        return mIsEllipsisEnabled;
    }

    public boolean isErrorState() {
        return mErrorStateHelper.isErrorState();
    }

    public void setErrorState(boolean z2) {
        mErrorState = z2;
        mErrorStateHelper.setErrorState(z2);
    }

    public boolean isFastDeletable() {
        return mQuickDelete;
    }

    public void setFastDeletable(boolean z2) {
        if (mQuickDelete != z2) {
            mQuickDelete = z2;
            if (z2 && mOplusTextWatcher == null) {
                OplusTextWatcher oplusTextWatcher = new OplusTextWatcher();
                mOplusTextWatcher = oplusTextWatcher;
                addTextChangedListener(oplusTextWatcher);
            }
        }
    }

    public boolean isHintEnabled() {
        return mHintEnabled;
    }

    public void setHintEnabled(boolean enabled) {
        if (enabled != mHintEnabled) {
            mHintEnabled = enabled;
            if (!enabled) {
                mIsProvidingHint = false;
                if (!TextUtils.isEmpty(mHint) && TextUtils.isEmpty(getHint())) {
                    setHint(mHint);
                }
                setHintInternal(null);
                return;
            }
            CharSequence hint = getHint();
            if (!TextUtils.isEmpty(hint)) {
                if (TextUtils.isEmpty(mHint)) {
                    setTopHint(hint);
                }
                setHint(null);
            }
            mIsProvidingHint = true;
        }
    }

    public boolean isProvidingHint() {
        return mIsProvidingHint;
    }

    public boolean isShowDeleteIcon() {
        return mShowDeleteIcon;
    }

    public void setShowDeleteIcon(boolean z2) {
        mShowDeleteIcon = z2;
    }

    public boolean ismHintAnimationEnabled() {
        return mHintAnimationEnabled;
    }

    public void setmHintAnimationEnabled(boolean z2) {
        mHintAnimationEnabled = z2;
    }

    @Override
    @Nullable
    public InputConnection onCreateInputConnection(@NonNull EditorInfo editorInfo) {
        InputConnectionListener inputConnectionListener = mInputConnectionListener;
        if (inputConnectionListener != null) {
            inputConnectionListener.onCreateInputConnection();
        }
        return super.onCreateInputConnection(editorInfo);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mInputConnectionListener != null) {
            mInputConnectionListener = null;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mErrorStateHelper.onDraw(canvas);
    }

    public void onFastDelete() {
        Editable text = getText();
        text.delete(0, text.length());
    }

    @Override
    public void onFocusChanged(boolean z2, int i2, Rect rect) {
        super.onFocusChanged(z2, i2, rect);
        if (mQuickDelete) {
            updateDeletableStatus(z2);
        }
        Activity ac = getActivityFromContext(getContext());
        if (hasFocus()) {
            if (ac != null) ac.getWindow().setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        } else {
            if (ac != null) ac.getWindow().setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
        OnFocusChangeListener onFocusChangeListener = mEditFocusChangeListener;
        if (onFocusChangeListener != null) {
            onFocusChangeListener.onFocusChange(this, z2);
        }
    }

    public Activity getActivityFromContext(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) return (Activity) context;
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    @Override
    public boolean onKeyDown(int i2, KeyEvent keyEvent) {
        if (!mQuickDelete || i2 != 67) {
            return super.onKeyDown(i2, keyEvent);
        }
        super.onKeyDown(i2, keyEvent);
        OnPasswordDeletedListener onPasswordDeletedListener = mPasswordDeleteListener;
        if (onPasswordDeletedListener == null) {
            return true;
        }
        onPasswordDeletedListener.onPasswordDeleted();
        return true;
    }

    @Override
    public void onLayout(boolean z2, int i2, int i3, int i4, int i5) {
        super.onLayout(z2, i2, i3, i4, i5);
        if (mBoxBackground != null) {
            updateTextInputBoxBounds();
        }
        if (mHintEnabled) {
            updateModePadding();
        }
        int compoundPaddingLeft = getCompoundPaddingLeft();
        int width = getWidth() - getCompoundPaddingRight();
        int calculateCollapsedTextTopBounds = calculateCollapsedTextTopBounds();
        mOplusCollapseTextHelper.setExpandedBounds(compoundPaddingLeft, getCompoundPaddingTop(), width, getHeight() - getCompoundPaddingBottom());
        mOplusCollapseTextHelper.setCollapsedBounds(compoundPaddingLeft, calculateCollapsedTextTopBounds, width, getHeight() - getCompoundPaddingBottom());
        mOplusCollapseTextHelper.recalculate();
        if (cutoutEnabled() && !mHintExpanded) {
            openCutout();
        }
        mErrorStateHelper.onLayout(mOplusCollapseTextHelper);
    }

    @Override
    public void onMeasure(int i2, int i3) {
        super.onMeasure(i2, i3);
    }

    @Override
    public void onRestoreInstanceState(Parcelable parcelable) {
        String str;
        if (getMaxLines() < 2 && mIsEllipsisEnabled && (parcelable instanceof OplusSavedState) && (str = ((OplusSavedState) parcelable).mText) != null) {
            setText(str);
        }
        super.onRestoreInstanceState(parcelable);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable onSaveInstanceState = super.onSaveInstanceState();
        if (getMaxLines() >= 2 || !mIsEllipsisEnabled || isFocused()) {
            return onSaveInstanceState;
        }
        OplusSavedState oplusSavedState = new OplusSavedState(onSaveInstanceState);
        oplusSavedState.mText = getCouiEditTexttNoEllipsisText();
        return oplusSavedState;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (mShowDeleteIcon && mQuickDelete && !TextUtils.isEmpty(getText()) && hasFocus()) {
            Rect rect = new Rect();
            boolean z2 = getContentRect(rect) && rect.contains((int) motionEvent.getX(), (int) motionEvent.getY());
            if (mDeletable && z2) {
                int action = motionEvent.getAction();
                if (action == 0) {
                    mShouldHandleDelete = true;
                    return true;
                }
                if (action != 1) {
                    if (action == 2 && mShouldHandleDelete) {
                        return true;
                    }
                } else if (mShouldHandleDelete) {
                    OnTextDeletedListener onTextDeletedListener = mTextDeleteListener;
                    if (onTextDeletedListener != null && onTextDeletedListener.onTextDeleted()) {
                        return true;
                    }
                    onFastDelete();
                    mShouldHandleDelete = false;
                    return true;
                }
            }
        }
        OnTouchListener onTouchListener = mCustomEditTextTouchListener;
        if (onTouchListener != null) {
            onTouchListener.onTouch(this, motionEvent);
        }
        boolean onTouchEvent = super.onTouchEvent(motionEvent);
        mClickSelectionPosition = getSelectionEnd();
        return onTouchEvent;
    }

    public void refresh() {
        TypedArray obtainStyledAttributes;
        Drawable drawable;
        String resourceTypeName = getResources().getResourceTypeName(mRefreshStyle);
        if ("attr".equals(resourceTypeName)) {
            obtainStyledAttributes = getContext().getTheme().obtainStyledAttributes(null, R.styleable.OplusEditText, mRefreshStyle, 0);
        } else if (!TextUtils.equals(resourceTypeName, "style")) {
            return;
        } else {
            obtainStyledAttributes = getContext().getTheme().obtainStyledAttributes(null, R.styleable.OplusEditText, 0, mRefreshStyle);
        }
        int i2 = R.styleable.OplusEditText_android_textColorHint;
        if (obtainStyledAttributes.hasValue(i2)) {
            ColorStateList colorStateList = obtainStyledAttributes.getColorStateList(i2);
            mDefaultHintTextColor = colorStateList;
            mFocusedTextColor = colorStateList;
            if (colorStateList == null) {
                mDefaultHintTextColor = getHintTextColors();
            }
        }
        mErrorColor = obtainStyledAttributes.getColor(R.styleable.OplusEditText_editTextErrorColor, getResources().getColor(R.color.oplus_color_error_text_bg, getContext().getTheme()));
        mFocusedStrokeColor = obtainStyledAttributes.getColor(R.styleable.OplusEditText_strokeColor, getResources().getColor(android.R.color.system_accent1_400, getContext().getTheme()));
        mDefaultStrokeColor = obtainStyledAttributes.getColor(R.styleable.OplusEditText_defaultStrokeColor, 0);
        mDisabledColor = obtainStyledAttributes.getColor(R.styleable.OplusEditText_disabledStrokeColor, 0);
        mErrorStateHelper.setErrorColor(mErrorColor);
        mNormalPaint.setColor(mDefaultStrokeColor);
        mDisabledPaint.setColor(mDisabledColor);
        mFocusedPaint.setColor(mFocusedStrokeColor);
        mDeleteNormal = obtainStyledAttributes.getDrawable(R.styleable.OplusEditText_editTextDeleteIconNormal);
        mDeletePressed = obtainStyledAttributes.getDrawable(R.styleable.OplusEditText_editTextDeleteIconPressed);
        Drawable drawable2 = mDeleteNormal;
        if (drawable2 != null) {
            mDeleteIconWidth = drawable2.getIntrinsicWidth();
            int intrinsicHeight = mDeleteNormal.getIntrinsicHeight();
            mDeleteIconHeight = intrinsicHeight;
            mDeleteNormal.setBounds(0, 0, mDeleteIconWidth, intrinsicHeight);
        }
        Drawable drawable3 = mDeletePressed;
        if (drawable3 != null) {
            drawable3.setBounds(0, 0, mDeleteIconWidth, mDeleteIconHeight);
        }
        if (mQuickDelete && mShowDeleteIcon && !TextUtils.isEmpty(getText()) && hasFocus() && mDeletable && (drawable = mDeleteNormal) != null) {
            setCompoundDrawables(null, null, drawable, null);
        }
        updateTextInputBoxState();
        obtainStyledAttributes.recycle();
        invalidate();
    }

    public void removeOnErrorStateChangedListener(OnErrorStateChangedListener onErrorStateChangedListener) {
        mErrorStateHelper.removeOnErrorStateChangedListener(onErrorStateChangedListener);
    }

    public void setBoxBackgroundMode(int i2) {
        if (i2 == mBoxBackgroundMode) {
            return;
        }
        mBoxBackgroundMode = i2;
        onApplyBoxBackgroundMode();
    }

    public void setCollapsedTextAppearance(int i2, ColorStateList colorStateList) {
        mOplusCollapseTextHelper.setCollapsedTextAppearance(i2, colorStateList);
        mFocusedTextColor = mOplusCollapseTextHelper.getCollapsedTextColor();
        updateLabelState(false);
        mErrorStateHelper.setCollapsedTextAppearance(i2, colorStateList);
    }

    @Override
    public void setCompoundDrawables(Drawable drawable, Drawable drawable2, Drawable drawable3, Drawable drawable4) {
        setCompoundDrawablesRelative(drawable, drawable2, drawable3, drawable4);
        if (drawable3 != null) {
            mDrawableSizeRight = drawable3.getBounds().width();
        } else {
            mDrawableSizeRight = 0;
        }
    }

    public void setCustomEditTextOnTouchListener(OnTouchListener onTouchListener) {
        mCustomEditTextTouchListener = onTouchListener;
    }

    public void setDefaultStrokeColor(int i2) {
        if (mDefaultStrokeColor != i2) {
            mDefaultStrokeColor = i2;
            mNormalPaint.setColor(i2);
            updateTextInputBoxState();
        }
    }

    public void setDisabledStrokeColor(int i2) {
        if (mDisabledColor != i2) {
            mDisabledColor = i2;
            mDisabledPaint.setColor(i2);
            updateTextInputBoxState();
        }
    }

    public void setEditFocusChangeListener(OnFocusChangeListener onFocusChangeListener) {
        mEditFocusChangeListener = onFocusChangeListener;
    }

    public void setEditTextColor(int i2) {
        setTextColor(i2);
        mErrorStateHelper.setOriginalTextColors(getTextColors());
    }

    public void setEditTextDeleteIconNormal(Drawable drawable) {
        if (drawable != null) {
            mDeleteNormal = drawable;
            mDeleteIconWidth = drawable.getIntrinsicWidth();
            int intrinsicHeight = mDeleteNormal.getIntrinsicHeight();
            mDeleteIconHeight = intrinsicHeight;
            mDeleteNormal.setBounds(0, 0, mDeleteIconWidth, intrinsicHeight);
            invalidate();
        }
    }

    public void setEditTextDeleteIconPressed(Drawable drawable) {
        if (drawable != null) {
            mDeletePressed = drawable;
            drawable.setBounds(0, 0, mDeleteIconWidth, mDeleteIconHeight);
            invalidate();
        }
    }

    public void setEditTextErrorColor(int i2) {
        if (i2 != mErrorColor) {
            mErrorColor = i2;
            mErrorStateHelper.setErrorColor(i2);
            invalidate();
        }
    }

    public void setInputConnectionListener(InputConnectionListener inputConnectionListener) {
        mInputConnectionListener = inputConnectionListener;
    }

    public void setIsEllipsisEnabled(boolean z2) {
        mIsEllipsisEnabled = z2;
    }

    public void setJustShowFocusLine(boolean z2) {
        mJustShowFocusLine = z2;
    }

    public void setOnTextDeletedListener(OnTextDeletedListener onTextDeletedListener) {
        mTextDeleteListener = onTextDeletedListener;
    }

    @Override
    public void setText(CharSequence charSequence, BufferType bufferType) {
        super.setText(charSequence, bufferType);
        Selection.setSelection(getText(), length());
    }

    public void setTextDeletedListener(OnPasswordDeletedListener onPasswordDeletedListener) {
        mPasswordDeleteListener = onPasswordDeletedListener;
    }

    public void setTopHint(CharSequence charSequence) {
        setHintInternal(charSequence);
    }

    public void updateLabelState(boolean z2) {
        updateLabelState(z2, false);
    }

    private void updateLabelState(boolean z2, boolean z3) {
        boolean z4 = !TextUtils.isEmpty(getText());
        if (mDefaultHintTextColor != null) {
            mDefaultHintTextColor = getHintTextColors();
            if (mOplusCollapseTextHelper != null) {
                mOplusCollapseTextHelper.setCollapsedTextColor(mFocusedTextColor);
                mOplusCollapseTextHelper.setExpandedTextColor(mDefaultHintTextColor);
            }
        }
        if (mOplusCollapseTextHelper != null) {
            if (!isEnabled()) {
                mOplusCollapseTextHelper.setCollapsedTextColor(ColorStateList.valueOf(mDisabledColor));
                mOplusCollapseTextHelper.setExpandedTextColor(ColorStateList.valueOf(mDisabledColor));
            } else if (hasFocus() && (mFocusedTextColor != null)) {
                mOplusCollapseTextHelper.setCollapsedTextColor(mFocusedTextColor);
            }
        }
        if (z4 || (isEnabled() && hasFocus())) {
            if (z3 || mHintExpanded) {
                collapseHint(z2);
            }
        } else if ((z3 || !mHintExpanded) && isHintEnabled()) {
            expandHint(z2);
        }
        if (mErrorStateHelper == null || mOplusCollapseTextHelper == null) {
            return;
        }
        mErrorStateHelper.updateLabelState(mOplusCollapseTextHelper);
    }

    public interface InputConnectionListener {
        void onCreateInputConnection();
    }

    public interface OnErrorStateChangedListener {
        void onErrorStateChangeAnimationEnd(boolean z2);

        void onErrorStateChanged(boolean z2);
    }

    public interface OnPasswordDeletedListener {
        boolean onPasswordDeleted();
    }

    public interface OnTextDeletedListener {
        boolean onTextDeleted();
    }

    public static class OplusSavedState extends BaseSavedState {
        public static final Creator<OplusSavedState> CREATOR = new Creator<>() {
            @Override
            public OplusSavedState createFromParcel(Parcel parcel) {
                return new OplusSavedState(parcel);
            }

            @Override
            public OplusSavedState[] newArray(int i2) {
                return new OplusSavedState[i2];
            }
        };
        String mText;

        public OplusSavedState(Parcelable parcelable) {
            super(parcelable);
        }

        private OplusSavedState(Parcel parcel) {
            super(parcel);
            mText = parcel.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public void readFromParcel(Parcel parcel) {
            mText = parcel.readString();
        }

        @Override
        public void writeToParcel(Parcel parcel, int i2) {
            super.writeToParcel(parcel, i2);
            parcel.writeString(mText);
        }
    }

    public class AccessibilityTouchHelper extends ExploreByTouchHelper implements OnClickListener {
        private final View mHostView;
        private Rect mUninstallRect;
        private Rect mViewRect;

        public AccessibilityTouchHelper(View view) {
            super(view);
            mUninstallRect = null;
            mViewRect = null;
            mHostView = view;
        }

        private Rect getItemBounds(int i2) {
            if (i2 != 0) {
                return new Rect();
            }
            if (mUninstallRect == null) {
                initUninstallRect();
            }
            return mUninstallRect;
        }

        private void initUninstallRect() {
            Rect rect = new Rect();
            mUninstallRect = rect;
            rect.left = getDeleteButtonLeft();
            mUninstallRect.right = getWidth();
            Rect rect2 = mUninstallRect;
            rect2.top = 0;
            rect2.bottom = getHeight();
        }

        private void initViewRect() {
            Rect rect = new Rect();
            mViewRect = rect;
            rect.left = 0;
            rect.right = getWidth();
            Rect rect2 = mViewRect;
            rect2.top = 0;
            rect2.bottom = getHeight();
        }

        @Override
        public int getVirtualViewAt(float f2, float f3) {
            if (mUninstallRect == null) {
                initUninstallRect();
            }
            Rect rect = mUninstallRect;
            return (f2 < ((float) rect.left) || f2 > ((float) rect.right) || f3 < ((float) rect.top) || f3 > ((float) rect.bottom) || !isDeleteButtonExist()) ? Integer.MIN_VALUE : 0;
        }

        @Override
        public void getVisibleVirtualViews(List<Integer> list) {
            if (isDeleteButtonExist()) {
                list.add(0);
            }
        }

        @Override
        public boolean onPerformActionForVirtualView(int i2, int i3, Bundle bundle) {
            if (i3 != 16) {
                return false;
            }
            if (i2 != 0 || !isDeleteButtonExist()) {
                return true;
            }
            onFastDelete();
            return true;
        }

        @Override
        public void onPopulateEventForVirtualView(int i2, AccessibilityEvent accessibilityEvent) {
            accessibilityEvent.setContentDescription(mDeleteButton);
        }

        @Override
        public void onPopulateNodeForVirtualView(int i2, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            if (i2 == 0) {
                accessibilityNodeInfoCompat.setContentDescription(mDeleteButton);
                accessibilityNodeInfoCompat.setClassName(Button.class.getName());
                accessibilityNodeInfoCompat.addAction(16);
            }
            accessibilityNodeInfoCompat.setBoundsInParent(getItemBounds(i2));
        }

        @Override
        public void onClick(View view) {
        }
    }

    public class OplusTextWatcher implements TextWatcher {
        private OplusTextWatcher() {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            OplusEditText cOUIEditText = OplusEditText.this;
            cOUIEditText.updateDeletableStatus(cOUIEditText.hasFocus());
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i2, int i3, int i4) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i2, int i3, int i4) {
        }
    }
}
