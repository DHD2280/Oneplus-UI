package it.dhd.oneplusui.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.google.android.material.button.MaterialButton;

import it.dhd.oneplusui.R;
import it.dhd.oneplusui.appcompat.seekbar.OplusSeekBar;

/**
 * Preference based on android.preference.SeekBarPreference but uses support preference as a base
 * . It contains a title and a {@link OplusSeekBar} and an optional OplusSeekBar value {@link TextView}.
 * The actual preference layout is customizable by setting {@code android:layout} on the
 * preference widget layout or {@code seekBarPreferenceStyle} attribute.
 *
 * <p>The {@link OplusSeekBar} within the preference can be defined adjustable or not by setting {@code
 * adjustable} attribute. If adjustable, the preference will be responsive to DPAD left/right keys.
 * Otherwise, it skips those keys.
 *
 * <p>The {@link OplusSeekBar} value view can be shown or disabled by setting {@code showSeekBarValue}
 * attribute to true or false, respectively.
 *
 * <p>Other {@link OplusSeekBar} specific attributes (e.g. {@code title, summary, defaultValue, min,
 * max})
 * can be set directly on the preference widget layout.
 */
public class OplusSeekbarPreference extends OplusPreference {

    private static final String TAG = "OplusSeekbarPreference";
    @SuppressWarnings("WeakerAccess") /* synthetic access */
            int mSeekBarValue;
    @SuppressWarnings("WeakerAccess") /* synthetic access */
            int mMin;
    private int mMax;
    private int mSeekBarIncrement;
    @SuppressWarnings("WeakerAccess") /* synthetic access */
            boolean mTrackingTouch;
    @SuppressWarnings("WeakerAccess") /* synthetic access */
            OplusSeekBar mSeekBar;
    private TextView mSeekBarValueTextView;
    private MaterialButton mResetButton;
    private final int mDefaultValue;
    // Whether the OplusSeekBar should respond to the left/right keys
    @SuppressWarnings("WeakerAccess") /* synthetic access */
            boolean mAdjustable;
    // Whether to show the OplusSeekBar value TextView next to the bar
    private boolean mShowSeekBarValue;
    // Whether the SeekBarPreference should continuously save the OplusSeekBar value while it is being
    // dragged.
    @SuppressWarnings("WeakerAccess") /* synthetic access */
            boolean mUpdatesContinuously;

    // Whether the SeekBarPreference should show the reset button
    @SuppressWarnings("WeakerAccess") /* synthetic access */
            boolean mShowResetButton;
    /**
     * Listener reacting to the {@link OplusSeekBar} changing value by the user
     */
    private final OplusSeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new OplusSeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(OplusSeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser && (mUpdatesContinuously || !mTrackingTouch)) {
                syncValueInternal(seekBar);
            } else {
                // We always want to update the text while the seekbar is being dragged
                updateLabelValue(progress + mMin);
            }
        }

        @Override
        public void onStartTrackingTouch(OplusSeekBar OplusSeekbar) {
            mTrackingTouch = true;
        }

        @Override
        public void onStopTrackingTouch(OplusSeekBar OplusSeekbar) {
            mTrackingTouch = false;
            if (OplusSeekbar.getProgress() + mMin != mSeekBarValue) {
                syncValueInternal(OplusSeekbar);
            }
        }
    };

    /**
     * Listener reacting to the user pressing DPAD left/right keys if {@code
     * adjustable} attribute is set to true; it transfers the key presses to the {@link OplusSeekBar}
     * to be handled accordingly.
     */
    private final View.OnKeyListener mSeekBarKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            if (!mAdjustable && (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                    || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
                // Right or left keys are pressed when in non-adjustable mode; Skip the keys.
                return false;
            }

            // We don't want to propagate the click keys down to the OplusSeekBar view since it will
            // create the ripple effect for the thumb.
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                return false;
            }

            if (mSeekBar == null) {
                Log.e(TAG, "OplusSeekBar view is null and hence cannot be adjusted.");
                return false;
            }
            return mSeekBar.onKeyDown(keyCode, event);
        }
    };

    /** Listener reacting to the reset button click */
    private final View.OnClickListener mResetButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (callChangeListener(mDefaultValue)) {
                mSeekBar.setProgress(mDefaultValue, true);
                setValueInternal(mDefaultValue, false);
            }
        }
    };

    @SuppressLint("PrivateResource")
    public OplusSeekbarPreference(
            @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.OplusSeekbarPreference, defStyleAttr, defStyleRes);

        // The ordering of these two statements are important. If we want to set max first, we need
        // to perform the same steps by changing min/max to max/min as following:
        // mMax = a.getInt(...) and setMin(...).
        mMin = a.getInt(R.styleable.OplusSeekbarPreference_android_min, 0);
        setMax(a.getInt(R.styleable.OplusSeekbarPreference_android_max, 100));
        setSeekBarIncrement(a.getInt(R.styleable.OplusSeekbarPreference_seekBarIncrement, 0));
        mAdjustable = a.getBoolean(R.styleable.OplusSeekbarPreference_adjustable, true);
        mUpdatesContinuously = a.getBoolean(R.styleable.OplusSeekbarPreference_updatesContinuously,
                false);
        mShowSeekBarValue = a.getBoolean(R.styleable.OplusSeekbarPreference_showSeekBarValue, true);
        mShowResetButton = a.getBoolean(R.styleable.OplusSeekbarPreference_showResetButton, true);
        mDefaultValue = a.getInt(R.styleable.OplusSeekbarPreference_android_defaultValue, 0);
        a.recycle();
    }

    public OplusSeekbarPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                             int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preferences_Oplus_Preference_Seekbar);
    }

    public OplusSeekbarPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.oplusSeekBarPreferenceStyle);
    }

    public OplusSeekbarPreference(@NonNull Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setOnKeyListener(mSeekBarKeyListener);
        mSeekBar = (OplusSeekBar) holder.findViewById(R.id.slider);
        mSeekBarValueTextView = (TextView) holder.findViewById(R.id.seekbar_value);
        mResetButton = (MaterialButton) holder.findViewById(R.id.reset_button);
        if (mShowSeekBarValue) {
            mSeekBarValueTextView.setVisibility(View.VISIBLE);
        } else {
            mSeekBarValueTextView.setVisibility(View.GONE);
            mSeekBarValueTextView = null;
        }
        if (mShowResetButton) {
            mResetButton.setVisibility(View.VISIBLE);
        } else {
            mResetButton.setVisibility(View.GONE);
        }

        if (mSeekBar == null) {
            Log.e(TAG, "OplusSeekBar view is null in onBindViewHolder.");
            return;
        }
        mResetButton.setOnClickListener(mResetButtonClickListener);
        mSeekBar.clearListeners();
        mSeekBar.addOnSeekBarChangeListener(mSeekBarChangeListener);
        mSeekBar.setMax(mMax - mMin);
        // If the increment is not zero, use that. Otherwise, use the default mKeyProgressIncrement
        // in AbsSeekBar when it's zero. This default increment value is set by AbsSeekBar
        // after calling setMax. That's why it's important to call setKeyProgressIncrement after
        // calling setMax() since setMax() can change the increment value.
        if (mSeekBarIncrement != 0) {
            mSeekBar.setKeyProgressIncrement(mSeekBarIncrement);
        } else {
            mSeekBarIncrement = mSeekBar.getKeyProgressIncrement();
        }

        mSeekBar.setProgress(mSeekBarValue - mMin);
        handleResetButton();
        updateLabelValue(mSeekBarValue);
        mSeekBar.setEnabled(isEnabled());
    }

    private void handleResetButton() {
        if (mResetButton == null) return;
        mResetButton.setEnabled(isEnabled() && mSeekBarValue != mDefaultValue);
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        if (defaultValue == null) {
            defaultValue = 0;
        }
        setValue(getPersistedInt((Integer) defaultValue));
    }

    @Override
    protected @Nullable Object onGetDefaultValue(@NonNull TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    /**
     * Gets the lower bound set on the {@link OplusSeekBar}.
     *
     * @return The lower bound set
     */
    public int getMin() {
        return mMin;
    }

    /**
     * Sets the lower bound on the {@link OplusSeekBar}.
     *
     * @param min The lower bound to set
     */
    public void setMin(int min) {
        if (min > mMax) {
            min = mMax;
        }
        if (min != mMin) {
            mMin = min;
            notifyChanged();
        }
    }

    /**
     * Returns the amount of increment change via each arrow key click. This value is derived from
     * user's specified increment value if it's not zero. Otherwise, the default value is picked
     * from the default mKeyProgressIncrement value in {@link android.widget.AbsSeekBar}.
     *
     * @return The amount of increment on the {@link OplusSeekBar} performed after each user's arrow
     * key press
     */
    public final int getSeekBarIncrement() {
        return mSeekBarIncrement;
    }

    /**
     * Sets the increment amount on the {@link OplusSeekBar} for each arrow key press.
     *
     * @param seekBarIncrement The amount to increment or decrement when the user presses an
     *                         arrow key.
     */
    public final void setSeekBarIncrement(int seekBarIncrement) {
        if (seekBarIncrement != mSeekBarIncrement) {
            mSeekBarIncrement = Math.min(mMax - mMin, Math.abs(seekBarIncrement));
            notifyChanged();
        }
    }

    /**
     * Gets the upper bound set on the {@link OplusSeekBar}.
     *
     * @return The upper bound set
     */
    public int getMax() {
        return mMax;
    }

    /**
     * Sets the upper bound on the {@link OplusSeekBar}.
     *
     * @param max The upper bound to set
     */
    public final void setMax(int max) {
        if (max < mMin) {
            max = mMin;
        }
        if (max != mMax) {
            mMax = max;
            notifyChanged();
        }
    }

    /**
     * Gets whether the {@link OplusSeekBar} should respond to the left/right keys.
     *
     * @return Whether the {@link OplusSeekBar} should respond to the left/right keys
     */
    public boolean isAdjustable() {
        return mAdjustable;
    }

    /**
     * Sets whether the {@link OplusSeekBar} should respond to the left/right keys.
     *
     * @param adjustable Whether the {@link OplusSeekBar} should respond to the left/right keys
     */
    public void setAdjustable(boolean adjustable) {
        mAdjustable = adjustable;
    }

    /**
     * Gets whether the {@link OplusSeekbarPreference} should continuously save the {@link OplusSeekBar} value
     * while it is being dragged. Note that when the value is true,
     * {@link Preference.OnPreferenceChangeListener} will be called continuously as well.
     *
     * @return Whether the {@link OplusSeekbarPreference} should continuously save the {@link OplusSeekBar}
     * value while it is being dragged
     * @see #setUpdatesContinuously(boolean)
     */
    public boolean getUpdatesContinuously() {
        return mUpdatesContinuously;
    }

    /**
     * Sets whether the {@link OplusSeekbarPreference} should continuously save the {@link OplusSeekBar} value
     * while it is being dragged.
     *
     * @param updatesContinuously Whether the {@link OplusSeekbarPreference} should continuously save
     *                            the {@link OplusSeekBar} value while it is being dragged
     * @see #getUpdatesContinuously()
     */
    public void setUpdatesContinuously(boolean updatesContinuously) {
        mUpdatesContinuously = updatesContinuously;
    }

    /**
     * Gets whether the current {@link OplusSeekBar} value is displayed to the user.
     *
     * @return Whether the current {@link OplusSeekBar} value is displayed to the user
     * @see #setShowSeekBarValue(boolean)
     */
    public boolean getShowSeekBarValue() {
        return mShowSeekBarValue;
    }

    /**
     * Sets whether the current {@link OplusSeekBar} value is displayed to the user.
     *
     * @param showSeekBarValue Whether the current {@link OplusSeekBar} value is displayed to the user
     * @see #getShowSeekBarValue()
     */
    public void setShowSeekBarValue(boolean showSeekBarValue) {
        mShowSeekBarValue = showSeekBarValue;
        notifyChanged();
    }

    /**
     * Gets whether the reset button is shown.
     *
     * @return Whether the reset button is shown
     */
    public boolean getShowResetButton() {
        return mShowResetButton;
    }

    /**
     * Sets whether the reset button is shown.
     *
     * @param showResetButton Whether the reset button is shown
     */
    public void setShowResetButton(boolean showResetButton) {
        mShowResetButton = showResetButton;
        notifyChanged();
    }

    private void setValueInternal(int seekBarValue, boolean notifyChanged) {
        if (seekBarValue < mMin) {
            seekBarValue = mMin;
        }
        if (seekBarValue > mMax) {
            seekBarValue = mMax;
        }

        if (seekBarValue != mSeekBarValue) {
            mSeekBarValue = seekBarValue;
            updateLabelValue(mSeekBarValue);
            persistInt(seekBarValue);
            handleResetButton();
            if (notifyChanged) {
                notifyChanged();
            }
        }
    }

    /**
     * Gets the current progress of the {@link OplusSeekBar}.
     *
     * @return The current progress of the {@link OplusSeekBar}
     */
    public int getValue() {
        return mSeekBarValue;
    }

    /**
     * Sets the current progress of the {@link OplusSeekBar}.
     *
     * @param seekBarValue The current progress of the {@link OplusSeekBar}
     */
    public void setValue(int seekBarValue) {
        setValueInternal(seekBarValue, true);
    }

    /**
     * Persist the {@link OplusSeekBar}'s OplusSeekBar value if callChangeListener returns true, otherwise
     * set the {@link OplusSeekBar}'s value to the stored value.
     */
    @SuppressWarnings("WeakerAccess") /* synthetic access */
    void syncValueInternal(@NonNull OplusSeekBar seekBar) {
        int seekBarValue = mMin + seekBar.getProgress();
        if (seekBarValue != mSeekBarValue) {
            if (callChangeListener(seekBarValue)) {
                setValueInternal(seekBarValue, false);
            } else {
                seekBar.setProgress(mSeekBarValue - mMin);
                updateLabelValue(mSeekBarValue);
            }
            handleResetButton();
        }
    }

    /**
     * Attempts to update the TextView label that displays the current value.
     *
     * @param value the value to display next to the {@link OplusSeekBar}
     */
    @SuppressWarnings("WeakerAccess") /* synthetic access */
    void updateLabelValue(int value) {
        if (mSeekBarValueTextView != null) {
            mSeekBarValueTextView.setText(String.valueOf(value));
        }
    }

    @Override
    protected @Nullable Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        // Save the instance state
        final SavedState myState = new SavedState(superState);
        myState.mSeekBarValue = mSeekBarValue;
        myState.mMin = mMin;
        myState.mMax = mMax;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(@Nullable Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // Restore the instance state
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        mSeekBarValue = myState.mSeekBarValue;
        mMin = myState.mMin;
        mMax = myState.mMax;
        notifyChanged();
    }

    /**
     * SavedState, a subclass of {@link BaseSavedState}, will store the state of this preference.
     *
     * <p>It is important to always call through to super methods.
     */
    private static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<>() {
                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

        int mSeekBarValue;
        int mMin;
        int mMax;

        SavedState(Parcel source) {
            super(source);

            // Restore the click counter
            mSeekBarValue = source.readInt();
            mMin = source.readInt();
            mMax = source.readInt();
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            // Save the click counter
            dest.writeInt(mSeekBarValue);
            dest.writeInt(mMin);
            dest.writeInt(mMax);
        }
    }
}
