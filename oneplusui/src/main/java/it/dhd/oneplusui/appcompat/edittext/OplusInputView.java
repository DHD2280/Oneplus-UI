package it.dhd.oneplusui.appcompat.edittext;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.PathInterpolator;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import it.dhd.oneplusui.R;
import it.dhd.oneplusui.appcompat.animation.OplusEaseInterpolator;

/**
 * Custom input view that can have any EditText inside it.
 * Something like {@link com.google.android.material.textfield.TextInputLayout}
 */
public class OplusInputView extends ConstraintLayout {

    public static final int INPUT_TYPE_NUMBER = 1;
    public static final int INPUT_TYPE_NUMBER_PASSWORD = 2;
    public static final int INPUT_TYPE_TEXT = 0;
    private static final int APPEAR_DURATION = 217;
    private static final int BUTTON_LAYOUT_MORE_PADDING = 3;
    private static final int COUNT_TEXTVIEW_MORE_PADDING = 10;
    private static final int COUNT_VIEW_PADDING = 8;
    private static final int DISAPPEAR_DURATION = 283;
    private static final int MAX_BUTTON_ICON_COUNT = 2;
    private static final int MAX_LINE = 5;
    private static final int PASSWORD_STATUES_TYPE_CLOSE = 1;
    private static final int PASSWORD_STATUES_TYPE_OPEN = 0;
    private static final int SPACE_STEP = 4;
    protected View mButtonLayout;
    protected TextView mCountTextView;
    protected OplusEditText mEditText;
    protected boolean mEnableInputCount;
    protected int mInputType;
    protected int mMaxCount;
    protected OnEditTextChangeListener mOnEditTextChangeListener;
    protected CharSequence mTitle;
    protected TextView mTitleTextView;
    CheckBox mPasswordButton;
    private ErrorStateChangeCallback mCallback;
    private Paint mCountPaint;
    private boolean mCustomFormat;
    private final ImageButton mDeleteButton;
    private final int mDeleteIconMarginEndWithPsd;
    private boolean mEditLineColor;
    private final LinearLayout mEdittextContainer;
    private boolean mEnableError;
    private boolean mEnablePassword;
    private final TextView mErrorText;
    private ValueAnimator mHideErrorTextAnimator;
    private CharSequence mHint;
    private OnFocusChangeListener mOnFocusChangeListener;
    private int mPasswordType;
    private final PathInterpolator mPathInterpolator;
    private ValueAnimator mShowErrorTextAnimator;
    private String mSpaceString;
    private final int mTextMinHeightInInputView;
    private TextWatcher mTextWatcher;
    private final Runnable mUpdateRunnable;
    private String replaceString;

    public OplusInputView(Context context) {
        this(context, null);
    }

    public OplusInputView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OplusInputView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        mOnEditTextChangeListener = null;
        mPathInterpolator = new OplusEaseInterpolator();
        mCountPaint = null;
        mEditLineColor = false;
        mCustomFormat = true;
        mUpdateRunnable = () -> {
            mEditText.setPaddingRelative(0, getEdittextPaddingTop(), getEdittextPaddingEnd(), getEdittextPaddingBottom());
            TextView textView = mTitleTextView;
            textView.setPaddingRelative(textView.getPaddingStart(), getTitlePaddingTop(), mTitleTextView.getPaddingEnd(), mTitleTextView.getPaddingBottom());
            setMargin(mButtonLayout, 1, (getEdittextPaddingTop() - getEdittextPaddingBottom()) / 2);
        };
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.OplusInputView, defStyleAttr, 0);
        mTitle = obtainStyledAttributes.getText(R.styleable.OplusInputView_title);
        mHint = obtainStyledAttributes.getText(R.styleable.OplusInputView_hint);
        mEnablePassword = obtainStyledAttributes.getBoolean(R.styleable.OplusInputView_enablePassword, false);
        mPasswordType = obtainStyledAttributes.getInt(R.styleable.OplusInputView_passwordType, 0);
        mEnableError = obtainStyledAttributes.getBoolean(R.styleable.OplusInputView_enableError, false);
        mMaxCount = obtainStyledAttributes.getInt(R.styleable.OplusInputView_inputMaxCount, 0);
        mEnableInputCount = obtainStyledAttributes.getBoolean(R.styleable.OplusInputView_enableInputCount, false);
        mInputType = obtainStyledAttributes.getInt(R.styleable.OplusInputView_inputType, -1);
        mCustomFormat = obtainStyledAttributes.getBoolean(R.styleable.OplusInputView_inputCustomFormat, true);
        mEditLineColor = obtainStyledAttributes.getBoolean(R.styleable.OplusInputView_editLineColor, false);
        obtainStyledAttributes.recycle();
        LayoutInflater.from(getContext()).inflate(getLayoutResId(), this, true);
        mTitleTextView = findViewById(R.id.title);
        mCountTextView = findViewById(R.id.input_count);
        mErrorText = findViewById(R.id.text_input_error);
        mButtonLayout = findViewById(R.id.button_layout);
        mEdittextContainer = findViewById(R.id.edittext_container);
        mDeleteButton = findViewById(R.id.delete_button);
        mPasswordButton = findViewById(R.id.checkbox_password);
        mDeleteIconMarginEndWithPsd = getResources().getDimensionPixelSize(R.dimen.oplus_inputview_delete_button_margin_end_with_passwordicon);
        mTextMinHeightInInputView = getResources().getDimensionPixelOffset(R.dimen.oplus_inputView_edittext_content_minheight);
        nowInit(context, attributeSet);
    }

    private int getCountTextWidth() {
        if (!mEnableInputCount) {
            return 0;
        }
        if (mCountPaint == null) {
            mCountPaint = new Paint();
            mCountPaint.setTextSize(mCountTextView.getTextSize());
        }
        return ((int) mCountPaint.measureText((String) mCountTextView.getText())) + 8;
    }

    private int getCustomButtonShowNum() {
        TextView textView;
        View view = mButtonLayout;
        if (!(view instanceof ViewGroup viewGroup)) {
            return 0;
        }
        int i2 = 0;
        for (int i3 = 0; i3 < viewGroup.getChildCount(); i3++) {
            View childAt = viewGroup.getChildAt(i3);
            if (childAt.getVisibility() == View.VISIBLE && (textView = mCountTextView) != null && textView.getId() != childAt.getId()) {
                i2++;
            }
        }
        return i2;
    }

    public int getTitlePaddingTop() {
        return getResources().getDimensionPixelSize(R.dimen.oplus_input_preference_title_padding_top);
    }

    public void handleCustomStyleText(CharSequence charSequence) {

    }

    private void handleWithError() {
        if (!mEnableError) {
            mErrorText.setVisibility(View.GONE);
            return;
        }
        if (!TextUtils.isEmpty(mErrorText.getText())) {
            mErrorText.setVisibility(View.VISIBLE);
        }
        mEditText.addOnErrorStateChangedListener(new OplusEditText.OnErrorStateChangedListener() {
            @Override
            public void onErrorStateChanged(boolean z2) {
                mEditText.setSelectAllOnFocus(z2);
                if (z2) {
                    showErrorMsgAnim();
                } else {
                    hideErrorMsgAnim();
                }
                if (mCallback != null) {
                    mCallback.callback(z2);
                }
            }

            @Override
            public void onErrorStateChangeAnimationEnd(boolean z2) {
            }
        });
    }

    private void handleWithTitle() {
        if (TextUtils.isEmpty(mTitle)) {
            return;
        }
        mTitleTextView.setText(mTitle);
        mTitleTextView.setVisibility(View.VISIBLE);
    }

    public void hideErrorMsgAnim() {
        ValueAnimator valueAnimator = mShowErrorTextAnimator;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            mShowErrorTextAnimator.cancel();
        }
        if (mHideErrorTextAnimator == null) {
            mHideErrorTextAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
            mHideErrorTextAnimator.setDuration(DISAPPEAR_DURATION).setInterpolator(mPathInterpolator);
            mHideErrorTextAnimator.addUpdateListener(valueAnimator2 -> mErrorText.setAlpha(((Float) valueAnimator2.getAnimatedValue()).floatValue()));
            mHideErrorTextAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animator) {
                    mErrorText.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    mErrorText.setVisibility(View.GONE);
                }
            });
        }
        if (mHideErrorTextAnimator.isStarted()) {
            mHideErrorTextAnimator.cancel();
        }
        mHideErrorTextAnimator.start();
    }

    private void init() {
        handleWithTitle();
        mEditText.setTopHint(mHint);
        if (mEditLineColor) {
            mEditText.setDefaultStrokeColor(getResources().getColor(android.R.color.system_accent1_400, getContext().getTheme()));
        }
        handleWithCount();
        handleWithPassword();
        handleWithError();
        initDeleteButton();
        updatePadding(false);
    }

    private void initDeleteButton() {
        if (mDeleteButton == null || mEditText.isShowDeleteIcon()) {
            return;
        }
        mDeleteButton.setOnClickListener(view -> {
            if (mEditText.getTextDeleteListener() == null || !mEditText.getTextDeleteListener().onTextDeleted()) {
                mEditText.onFastDelete();
            }
        });
    }

    private boolean isEnablePassword() {
        return mPasswordButton.getVisibility() == View.VISIBLE ? mEnablePassword : mEnablePassword && getCustomButtonShowNum() < 2;
    }

    public void setEnablePassword(boolean enablePassword) {
        if (mEnablePassword != enablePassword) {
            mEnablePassword = enablePassword;
            handleWithPassword();
            updatePadding(true);
        }
    }

    private boolean isShowDeleteButton() {
        return mDeleteButton.getVisibility() == View.VISIBLE ? mEditText.isFastDeletable() : mEditText.isFastDeletable() && getCustomButtonShowNum() < 2;
    }

    private void resetCustomStyleText(CharSequence charSequence) {
        if (replaceString != null) {
            String valueOf = String.valueOf(charSequence);
            mEditText.setText(valueOf);
            mEditText.setSelection(valueOf.length());
            replaceString = null;
        }
    }

    private void setInputType() {
        if (mInputType == -1) {
            return;
        }
        switch (mInputType) {
            case INPUT_TYPE_TEXT:
                mEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            case INPUT_TYPE_NUMBER:
                mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case INPUT_TYPE_NUMBER_PASSWORD:
                mEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                break;
            default:
                mEditText.setInputType(InputType.TYPE_NULL);
                break;
        }
    }

    public void showErrorMsgAnim() {
        if (mHideErrorTextAnimator != null && mHideErrorTextAnimator.isRunning()) {
            mHideErrorTextAnimator.cancel();
        }
        mErrorText.setVisibility(View.VISIBLE);
        if (mShowErrorTextAnimator == null) {
            mShowErrorTextAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            mShowErrorTextAnimator.setDuration(APPEAR_DURATION).setInterpolator(mPathInterpolator);
            mShowErrorTextAnimator.addUpdateListener(valueAnimator2 -> mErrorText.setAlpha(((Float) valueAnimator2.getAnimatedValue()).floatValue()));
        }
        if (mShowErrorTextAnimator.isStarted()) {
            mShowErrorTextAnimator.cancel();
        }
        mShowErrorTextAnimator.start();
    }

    public void updateDeleteButton(boolean z2) {
        if (mDeleteButton != null) {
            if (!isShowDeleteButton() || !z2 || TextUtils.isEmpty(mEditText.getText().toString())) {
                mDeleteButton.setVisibility(View.GONE);
            } else {
                if (isInVisibleRect(mDeleteButton)) {
                    return;
                }
                mDeleteButton.setVisibility(View.INVISIBLE);
                post(() -> mDeleteButton.setVisibility(View.VISIBLE));
            }
        }
    }

    public void updatePadding(boolean z2) {
        if (!z2) {
            mUpdateRunnable.run();
        } else {
            mEditText.removeCallbacks(mUpdateRunnable);
            mEditText.post(mUpdateRunnable);
        }
    }

    public void addCustomButton(View view) {
        if (mButtonLayout == null || view == null || !(mButtonLayout instanceof ViewGroup viewGroup)) {
            return;
        }
        if (getCustomButtonShowNum() < 2) {
            int buttonSize = getResources().getDimensionPixelSize(R.dimen.oplus_inputview_custom_button_size);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(buttonSize, buttonSize);
            layoutParams.setMarginStart(mDeleteIconMarginEndWithPsd);
            layoutParams.setMarginEnd(0);
            viewGroup.addView(view, layoutParams);
            updatePadding(true);
        }
    }

    public TextView getCountTextView() {
        return mCountTextView;
    }

    public OplusEditText getEditText() {
        return mEditText;
    }

    public int getEdittextPaddingBottom() {
        return !TextUtils.isEmpty(mTitle) ? getResources().getDimensionPixelSize(R.dimen.oplus_input_edit_error_text_has_title_padding_bottom) : (int) getResources().getDimension(R.dimen.oplus_input_edit_text_no_title_padding_bottom);
    }

    public int getEdittextPaddingEnd() {
        return mButtonLayout.getWidth();
    }

    public int getEdittextPaddingTop() {
        return !TextUtils.isEmpty(mTitle) ? getResources().getDimensionPixelSize(R.dimen.oplus_input_edit_text_has_title_padding_top) : (int) getResources().getDimension(R.dimen.oplus_input_edit_text_no_title_padding_top);
    }

    public CharSequence getHint() {
        return mHint;
    }

    public void setHint(CharSequence charSequence) {
        mHint = charSequence;
        mEditText.setTopHint(charSequence);
    }

    public int getLayoutResId() {
        return R.layout.oplus_input_view;
    }

    public int getMaxCount() {
        return mMaxCount;
    }

    public void setMaxCount(int i2) {
        mMaxCount = i2;
        handleWithCount();
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public void setTitle(CharSequence charSequence) {
        if (charSequence == null || charSequence.equals(mTitle)) {
            return;
        }
        mTitle = charSequence;
        handleWithTitle();
        updatePadding(false);
    }

    public void handleWithCount() {
        handleWithCountTextView();
        if (mTextWatcher == null) {
            TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {
                    OplusInputView cOUIInputView = OplusInputView.this;
                    if (cOUIInputView.mEnableInputCount && cOUIInputView.mMaxCount > 0) {
                        OnEditTextChangeListener onEditTextChangeListener = cOUIInputView.mOnEditTextChangeListener;
                        if (onEditTextChangeListener != null) {
                            onEditTextChangeListener.afterTextChange(editable);
                        } else {
                            int length = editable.length();
                            if (length < mMaxCount) {
                                mCountTextView.setText(length + "/" + mMaxCount);
                                mCountTextView.setTextColor(getResources().getColor(R.color.oplus_color_hint_neutral, getContext().getTheme()));
                            } else {
                                mCountTextView.setText(mMaxCount + "/" + mMaxCount);
                                mCountTextView.setTextColor(getResources().getColor(R.color.oplus_color_error_text_bg, getContext().getTheme()));
                                if (length > mMaxCount) {
                                    mEditText.setText(editable.subSequence(0, mMaxCount));
                                }
                            }
                        }
                    }
                    updateDeleteButton(hasFocus());
                    updatePadding(true);
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i2, int i3, int i4) {
                    if (isIsCardSingleInput() && mCustomFormat) {
                        handleCustomStyleText(charSequence);
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i2, int i3, int i4) {
                }
            };
            mTextWatcher = textWatcher;
            mEditText.addTextChangedListener(textWatcher);
        }
        if (mOnFocusChangeListener == null) {
            OnFocusChangeListener onFocusChangeListener = (view, hasFocus) -> {
                updateDeleteButton(hasFocus);
                updatePadding(true);
            };
            mOnFocusChangeListener = onFocusChangeListener;
            mEditText.setOnFocusChangeListener(onFocusChangeListener);
        }
    }

    public void handleWithCountTextView() {
        if (!mEnableInputCount || mMaxCount <= 0) {
            mCountTextView.setVisibility(View.GONE);
            return;
        }
        mCountTextView.setVisibility(View.VISIBLE);
        mCountTextView.setText(mEditText.getText().length() + "/" + mMaxCount);
    }

    public void handleWithPassword() {
        if (!isEnablePassword()) {
            mPasswordButton.setVisibility(View.GONE);
            setInputType();
            return;
        }
        mPasswordButton.setVisibility(View.VISIBLE);
        if (mPasswordType == PASSWORD_STATUES_TYPE_CLOSE) {
            mPasswordButton.setChecked(false);
            if (mInputType == INPUT_TYPE_NUMBER || mInputType == INPUT_TYPE_NUMBER_PASSWORD) {
                mEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            } else {
                mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        } else {
            mPasswordButton.setChecked(true);
            if (mInputType == INPUT_TYPE_NUMBER || mInputType == INPUT_TYPE_NUMBER_PASSWORD) {
                mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
            } else {
                mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
        }
        mPasswordButton.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                if (mInputType == INPUT_TYPE_NUMBER || mInputType == INPUT_TYPE_NUMBER_PASSWORD) {
                    mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                } else {
                    mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
                return;
            }
            if (mInputType == INPUT_TYPE_NUMBER || mInputType == INPUT_TYPE_NUMBER_PASSWORD) {
                mEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            } else {
                mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });
    }

    public OplusEditText instanceOplusEditText(Context context, AttributeSet attributeSet) {
        OplusEditText oplusEditText = new OplusEditText(context, attributeSet, R.attr.oplusInputPreferenceEditTextStyle);
        oplusEditText.setShowDeleteIcon(false);
        oplusEditText.setVerticalScrollBarEnabled(false);
        oplusEditText.setMinHeight(mTextMinHeightInInputView);
        return oplusEditText;
    }

    public boolean isEnableInputCount() {
        return mEnableInputCount;
    }

    public void setEnableInputCount(boolean enableInputCount) {
        mEnableInputCount = enableInputCount;
        handleWithCount();
    }

    public boolean isIsCardSingleInput() {
        return false;
    }

    public void lazyInit(Context context, AttributeSet attributeSet) {
        mEditText = instanceOplusEditText(context, attributeSet);
        mEditText.setMaxLines(MAX_LINE);
        mEdittextContainer.addView(mEditText, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        init();
    }

    public void nowInit(Context context, AttributeSet attributeSet) {
        lazyInit(context, attributeSet);
    }

    public void removeCustomButton(View view) {
        if (mButtonLayout == null || view == null || !(mButtonLayout instanceof ViewGroup)) {
            return;
        }
        ((ViewGroup) mButtonLayout).removeView(view);
        updatePadding(true);
    }

    public void setCustomFormat(Boolean bool) {
        mCustomFormat = bool.booleanValue();
        if (mEditText.getText() == null) {
            return;
        }
        if (isIsCardSingleInput() && mCustomFormat) {
            handleCustomStyleText(mEditText.getText());
        } else {
            resetCustomStyleText(mEditText.getText());
        }
    }

    public void setEnableError(boolean enableError) {
        if (mEnableError != enableError) {
            mEnableError = enableError;
            handleWithError();
            updatePadding(false);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mEditText.setEnabled(enabled);
        mTitleTextView.setEnabled(enabled);
    }

    public void setErrorStateChangeCallBack(ErrorStateChangeCallback errorStateChangeCallback) {
        mCallback = errorStateChangeCallback;
    }

    public void setOnEditTextChangeListener(OnEditTextChangeListener onEditTextChangeListener) {
        mOnEditTextChangeListener = onEditTextChangeListener;
    }

    public void setPasswordType(int i2) {
        if (mPasswordType != i2) {
            mPasswordType = i2;
            handleWithPassword();
            updatePadding(true);
        }
    }

    public void showError(CharSequence charSequence) {
        if (TextUtils.isEmpty(charSequence)) {
            mEditText.setErrorState(false);
        } else {
            mEditText.setErrorState(true);
            if (mEnableError) {
                mErrorText.setVisibility(View.VISIBLE);
            }
        }
        mErrorText.setText(charSequence);
    }

    private boolean isInVisibleRect(View view) {
        return view.getLocalVisibleRect(new Rect()) && view.getVisibility() == View.VISIBLE && view.isShown();
    }

    private void setMargin(View view, int i2, int i3) {
        if (view != null) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                if (i2 == 0) {
                    ((ViewGroup.MarginLayoutParams) layoutParams).leftMargin = i3;
                } else if (i2 == 1) {
                    ((ViewGroup.MarginLayoutParams) layoutParams).topMargin = i3;
                } else if (i2 == 2) {
                    ((ViewGroup.MarginLayoutParams) layoutParams).rightMargin = i3;
                } else if (i2 == 3) {
                    ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin = i3;
                } else if (i2 == 4) {
                    ((ViewGroup.MarginLayoutParams) layoutParams).setMarginEnd(i3);
                } else if (i2 == 5) {
                    ((ViewGroup.MarginLayoutParams) layoutParams).setMarginStart(i3);
                }
                view.setLayoutParams(layoutParams);
            }
        }
    }

    public interface ErrorStateChangeCallback {
        void callback(boolean z2);
    }

    public interface OnEditTextChangeListener {
        void afterTextChange(Editable editable);
    }


}
