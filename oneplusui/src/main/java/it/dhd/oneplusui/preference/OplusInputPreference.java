package it.dhd.oneplusui.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceViewHolder;

import it.dhd.oneplusui.R;
import it.dhd.oneplusui.appcompat.cardlist.CardListHelper;
import it.dhd.oneplusui.appcompat.edittext.OplusEditText;
import it.dhd.oneplusui.appcompat.edittext.OplusInputView;
import it.dhd.oneplusui.appcompat.edittext.OplusScrolledEditText;

public class OplusInputPreference extends OplusPreference {
    public CharSequence mContent;
    public OplusEditText mEditText;
    public OplusCardListItemInputView mInputView;
    public View mPreferenceView;
    public CharSequence mTitle;

    public OplusInputPreference(@NonNull Context context) {
        this(context, null);
    }

    public OplusInputPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.oplusInputPreferenceStyle);
    }

    public OplusInputPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preferences_Oplus_Preference_Input);
    }

    public OplusInputPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attrs, R.styleable.OplusInputPreference, defStyleAttr, defStyleRes);
        mContent = obtainStyledAttributes.getText(R.styleable.OplusInputPreference_couiContent);
        boolean justShowFocusLine = obtainStyledAttributes.getBoolean(R.styleable.OplusInputPreference_couiJustShowFocusLine, true);
        obtainStyledAttributes.recycle();
        TypedArray obtainStyledAttributes2 = context.obtainStyledAttributes(attrs, androidx.preference.R.styleable.Preference, defStyleAttr, defStyleRes);
        mTitle = obtainStyledAttributes2.getText(androidx.preference.R.styleable.Preference_android_title);
        obtainStyledAttributes2.recycle();
        mInputView = new OplusCardListItemInputView(context, attrs);
        mInputView.setId(R.id.input);
        mInputView.setTitle(mTitle);
        mEditText = mInputView.getEditText();
        mInputView.setJustShowFocusLine(justShowFocusLine);
    }

    public CharSequence getContent() {
        if (mEditText != null) {
            return mEditText.getCouiEditTexttNoEllipsisText();
        }
        return mContent;
    }

    /**
     * Sets the content of the EditText.
     * This method will also save the content to the persistent storage if the preference is persistent.
     * @param charSequence The content to set.
     */
    public void setContent(CharSequence charSequence) {
        if (mEditText != null) {
            mEditText.setCouiEditTexttNoEllipsisText((String) charSequence);
            mContent = charSequence;
            return;
        }
        if (!TextUtils.equals(mContent, charSequence)) {
            notifyChanged();
        }
        boolean shouldDisableDependents = shouldDisableDependents();
        mContent = charSequence;
        if (charSequence != null) {
            persistString(charSequence.toString());
        }
        boolean shouldDisableDependents2 = shouldDisableDependents();
        if (shouldDisableDependents2 != shouldDisableDependents) {
            notifyDependencyChange(shouldDisableDependents2);
        }
    }

    /**
     * Get the hint of the InputView {@link OplusInputView}
     * @return The hint of the InputView
     */
    public CharSequence getHint() {
        return mInputView.getHint();
    }

    /**
     * Set the hint of the InputView {@link OplusInputView}
     * @param charSequence The hint to set
     */
    public void setHint(CharSequence charSequence) {
        CharSequence hint = getHint();
        if ((charSequence != null || hint == null) && (charSequence == null || charSequence.equals(hint))) {
            return;
        }
        mInputView.setHint(charSequence);
        notifyChanged();
    }

    public View getPreferenceView() {
        return mPreferenceView;
    }

    /**
     * Get the EditText {@link OplusEditText} of the InputView {@link OplusInputView}
     * @return The EditText of the InputView
     */
    public OplusEditText getEditText() {
        return mEditText;
    }

    public OplusInputView getInputView() {
        return mInputView;
    }

    @Override
    public Object onGetDefaultValue(TypedArray typedArray, int i2) {
        return typedArray.getString(i2);
    }

    @Override
    public void onSetInitialValue(Object defaultValue) {
        setContent(getPersistedString((String) defaultValue));
    }

    @Override
    public boolean shouldDisableDependents() {
        return TextUtils.isEmpty(mContent) || super.shouldDisableDependents();
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        mPreferenceView = preferenceViewHolder.itemView;
        ViewGroup viewGroup = mPreferenceView.findViewById(R.id.edittext_container);
        if (viewGroup != null) {
            if (!mInputView.equals(viewGroup.findViewById(R.id.input))) {
                ViewParent parent = mInputView.getParent();
                if (parent != null) {
                    ((ViewGroup) parent).removeView(mInputView);
                }
                viewGroup.removeAllViews();
                viewGroup.addView(mInputView, -1, -2);
                int positionInGroup = CardListHelper.getPositionInGroup(this);
                if (positionInGroup == CardListHelper.TAIL || positionInGroup == CardListHelper.FULL) {
                    mInputView.getEditText().setBoxBackgroundMode(OplusEditText.MODE_BACKGROUND_NO_LINE);
                }
                mEditText.setEditFocusChangeListener((v, hasFocus) -> {
                        if (hasFocus) {
                            View parent1 = (View) mPreferenceView.getParent();
                            ViewCompat.setOnApplyWindowInsetsListener(parent1, (v1, insets) -> {
                                Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
                                v1.setPadding(v1.getPaddingLeft(), v1.getPaddingTop(),
                                        v1.getPaddingRight(), Math.max(imeInsets.bottom, 0));
                                return insets;
                            });
                        }
                });
                mEditText.setOnEditorActionListener((v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE ||
                            (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                        InputMethodManager imm = (InputMethodManager) v.getContext()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        return true;
                    }
                    return false;
                });
                mEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (callChangeListener(mEditText.getText())) {
                            persistString(mEditText.getText().toString());
                        }
                    }
                });
                mEditText.setText(mContent);
            }
        }
        mInputView.setEnabled(isEnabled());
    }

    @Override
    public boolean drawDivider() {
        if (mEditText.isErrorState()) {
            return false;
        }
        return super.drawDivider();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable onSaveInstanceState = super.onSaveInstanceState();
        if (isPersistent()) {
            return onSaveInstanceState;
        }
        SavedState savedState = new SavedState(onSaveInstanceState);
        CharSequence charSequence = mContent;
        if (charSequence != null) {
            savedState.mText = charSequence.toString();
        }
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable parcelable) {
        if (parcelable == null || !parcelable.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        setContent(savedState.mText);
    }

    public static class SavedState extends Preference.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator() { // from class: com.coui.appcompat.preference.OplusInputPreference.SavedState.1
            @Override
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            @Override
            public SavedState[] newArray(int i2) {
                return new SavedState[i2];
            }
        };
        public String mText;

        public SavedState(Parcel parcel) {
            super(parcel);
            mText = parcel.readString();
        }

        public SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        @Override
        public void writeToParcel(Parcel parcel, int i2) {
            super.writeToParcel(parcel, i2);
            parcel.writeString(mText);
        }
    }

    public class OplusCardListItemInputView extends OplusInputView {
        public boolean mJustShowFocusLine;

        @Override
        public boolean isIsCardSingleInput() {
            return true;
        }

        public OplusCardListItemInputView(Context context) {
            this(context, null);
        }

        public OplusCardListItemInputView(Context context, AttributeSet attributeSet) {
            this(context, attributeSet, 0);
        }

        public OplusCardListItemInputView(Context context, AttributeSet attributeSet, int i2) {
            super(context, attributeSet, i2);
            mJustShowFocusLine = false;
        }

        @Override
        public int getEdittextPaddingTop() {
            return !TextUtils.isEmpty(mTitle) ? getResources().getDimensionPixelSize(R.dimen.oplus_input_edit_text_has_title_padding_top) : (int) getResources().getDimension(R.dimen.oplus_input_edit_text_no_title_padding_top_inPreference);
        }

        @Override
        public int getEdittextPaddingBottom() {
            return !TextUtils.isEmpty(mTitle) ? getResources().getDimensionPixelSize(R.dimen.oplus_input_edit_error_text_has_title_padding_bottom) : (int) getResources().getDimension(R.dimen.oplus_input_edit_text_no_title_padding_bottom_inPreference);
        }

        @Override
        public OplusEditText instanceOplusEditText(Context context, AttributeSet attributeSet) {
            OplusScrolledEditText oplusScrolledEditText = new OplusScrolledEditText(context, attributeSet, R.attr.oplusInputPreferenceEditTextStyle);
            oplusScrolledEditText.setShowDeleteIcon(false);
            oplusScrolledEditText.setVerticalScrollBarEnabled(false);
            return oplusScrolledEditText;
        }

        public void setJustShowFocusLine(boolean justShowFocusLine) {
            if (mJustShowFocusLine != justShowFocusLine) {
                mJustShowFocusLine = justShowFocusLine;
                if (mEditText != null) {
                    mEditText.setJustShowFocusLine(justShowFocusLine);
                }
            }
        }
    }
}
