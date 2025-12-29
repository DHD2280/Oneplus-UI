package it.dhd.oneplusui.preference;

/*
 * From Siavash79/rangesliderpreference
 * https://github.com/siavash79/rangesliderpreference
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceViewHolder;

import com.google.android.material.button.MaterialButton;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;

import it.dhd.oneplusui.R;
import it.dhd.oneplusui.appcompat.seekbar.LabelFormatter;
import it.dhd.oneplusui.appcompat.seekbar.OplusSliderLegacy;


public class OplusSliderLegacyPreference extends OplusPreference {

    private static final String TAG = "OplusSliderLegacyPreference";
    private final Context mContext;
    private float valueFrom;
    private float valueTo;
    private final float tickInterval;
    private boolean mShowResetButton;
    public final List<Float> defaultValue = new ArrayList<>();
    public OplusSliderLegacy mOplusSlider;
    private int mLeftTipIcon, mRightTipIcon;
    private CharSequence mLeftTipText, mRightTipText;
    private TextView mLeftTipTextView, mRightTipTextView;
    private ImageView mLeftTipIconView, mRightTipIconView;
    private RelativeLayout mTipsLayout;
    private MaterialButton mResetButton;
    private TextView mValueText;
    int valueCount;
    private String valueFormat;
    private final float outputScale;
    private boolean isDecimalFormat;
    private String decimalFormat = "#.#";
    boolean updateConstantly;
    boolean showSeekBarValue;

    public OplusSliderLegacyPreference(@NonNull Context context) {
        this(context, null);
    }

    public OplusSliderLegacyPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.oplusSliderLegacyPreferenceStyle);
    }

    public OplusSliderLegacyPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                 int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preferences_Oplus_Preference_SliderLegacy);
    }

    public OplusSliderLegacyPreference(
            @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setSelectable(false);
        mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OplusSliderPreference);
        updateConstantly = a.getBoolean(R.styleable.OplusSliderPreference_updatesContinuously, false);
        showSeekBarValue = a.getBoolean(R.styleable.OplusSliderPreference_showSeekBarValue, true);
        valueCount = a.getInteger(R.styleable.OplusSliderPreference_valueCount, 1);
        valueFrom = a.getFloat(R.styleable.OplusSliderPreference_minVal, 0f);
        valueTo = a.getFloat(R.styleable.OplusSliderPreference_maxVal, 100f);
        tickInterval = a.getFloat(R.styleable.OplusSliderPreference_tickInterval, 1f);
        mLeftTipText = a.getText(R.styleable.OplusSliderPreference_tipLeft);
        mRightTipText = a.getText(R.styleable.OplusSliderPreference_tipRight);
        mLeftTipIcon = a.getResourceId(R.styleable.OplusSliderPreference_leftTipIcon, 0);
        mRightTipIcon = a.getResourceId(R.styleable.OplusSliderPreference_rightTipIcon, 0);
        mShowResetButton = a.getBoolean(R.styleable.OplusSliderPreference_showResetButton, true);
        valueFormat = a.getString(R.styleable.OplusSliderPreference_valueFormat);
        isDecimalFormat = a.getBoolean(R.styleable.OplusSliderPreference_isDecimalFormat, false);
        if (a.hasValue(R.styleable.OplusSliderPreference_decimalFormat)) {
            isDecimalFormat = true;
            decimalFormat = a.getString(R.styleable.OplusSliderPreference_decimalFormat);
        } else {
            decimalFormat = "#.#"; // Default decimal format
        }
        outputScale = a.getFloat(R.styleable.OplusSliderPreference_outputScale, 1f);
        String defaultValStr = a.getString(androidx.preference.R.styleable.Preference_defaultValue);

        if (valueFormat == null) valueFormat = "";

        try {
            Scanner scanner = new Scanner(defaultValStr);
            scanner.useDelimiter(",");
            scanner.useLocale(Locale.ENGLISH);

            while (scanner.hasNext()) {
                defaultValue.add(scanner.nextFloat());
            }
        } catch (Exception ignored) {
            Log.e(TAG, String.format("SliderPreference: Error parsing default values for key: %s", getKey()));
        }

        a.recycle();
    }

    public void savePrefs() {
        setValues(getSharedPreferences(), getKey(), mOplusSlider.getValues());
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean setValues(SharedPreferences sharedPreferences, String key, List<Float> values) {
        try {
            StringWriter writer = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.beginObject();
            jsonWriter.name("");
            jsonWriter.beginArray();

            for (float value : values) {
                jsonWriter.value(value);
            }
            jsonWriter.endArray();
            jsonWriter.endObject();
            jsonWriter.close();
            String jsonString = writer.toString();

            sharedPreferences.edit().putString(key, jsonString).apply();

            return true;

        } catch (Exception ignored) {
            return false;
        }
    }

    public void syncState() {
        boolean needsCommit = false;

        List<Float> values = getValues(getSharedPreferences(), getKey(), valueFrom);
        BigDecimal step = new BigDecimal(String.valueOf(mOplusSlider.getStepSize())); //float and double are not accurate when it comes to decimal points

        for (int i = 0; i < values.size(); i++) {
            BigDecimal round = new BigDecimal(Math.round(values.get(i) / mOplusSlider.getStepSize()));
            double v = Math.min(Math.max(step.multiply(round).doubleValue(), mOplusSlider.getValueFrom()), mOplusSlider.getValueTo());
            if (v != values.get(i)) {
                values.set(i, (float) v);
                needsCommit = true;
            }
        }
        if (values.size() < valueCount) {
            needsCommit = true;
            values = defaultValue;
            while (values.size() < valueCount) {
                values.add(valueFrom);
            }
        } else if (values.size() > valueCount) {
            needsCommit = true;
            while (values.size() > valueCount) {
                values.remove(values.size() - 1);
            }
        }

        try {
            mOplusSlider.setValues(values);
            if (needsCommit) savePrefs();
            updateLabel();
        } catch (Throwable t) {
            values.clear();
        }
    }

    private void updateLabel() {
        List<Float> values = mOplusSlider.getValues();
        if (values.size() > 1) mValueText.setText(labelFormatter.getFormattedValue(mOplusSlider.getValues().get(0)) + " - " + labelFormatter.getFormattedValue(mOplusSlider.getValues().get(1)));
        else mValueText.setText(labelFormatter.getFormattedValue(mOplusSlider.getValues().get(0)));
    }

    public void setShowValue(boolean show) {
        showSeekBarValue = show;
        if (mValueText != null) {
            mValueText.setVisibility(show ? View.VISIBLE : View.GONE);
        }
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

    /**
     * Sets the left tip text.
     *
     * @param text The left tip text
     */
    public void setLeftTip(String text) {
        if (!TextUtils.equals(text, mLeftTipText)) {
            mLeftTipText = text;
            notifyChanged();
        }
    }

    /**
     * Sets the left tip text.
     *
     * @param resId The left tip text resource id
     */
    public void setLeftTip(int resId) {
        setLeftTip(mContext.getString(resId));
    }

    /**
     * Sets the right tip text.
     *
     * @param text The right tip text
     */
    public void setRightTip(String text) {
        if (!TextUtils.equals(text, mRightTipText)) {
            mRightTipText = text;
            notifyChanged();
        }
    }

    /**
     * Sets the right tip text.
     *
     * @param resId The right tip text resource id
     */
    public void setRightTip(int resId) {
        setRightTip(mContext.getString(resId));
    }

    /**
     * Sets the left tip icon.
     *
     * @param resId The left tip icon resource id
     */
    public void setLeftTipIcon(@DrawableRes int resId) {
        setLeftTipIcon(mContext.getDrawable(resId));
    }

    /**
     * Sets the left tip icon.
     *
     * @param drawable The left tip icon drawable
     */
    public void setLeftTipIcon(Drawable drawable) {
        if (mLeftTipIconView != null) {
            mLeftTipIconView.setImageDrawable(drawable);
            notifyChanged();
        }
    }

    /**
     * Sets the right tip icon.
     *
     * @param resId The right tip icon resource id
     */
    public void setRightTipIcon(@DrawableRes int resId) {
        setRightTipIcon(mContext.getDrawable(resId));
    }

    /**
     * Sets the right tip icon.
     *
     * @param drawable The right tip icon drawable
     */
    public void setRightTipIcon(Drawable drawable) {
        if (mRightTipIconView != null) {
            mRightTipIconView.setImageDrawable(drawable);
            notifyChanged();
        }
    }

    OplusSliderLegacy.OnSliderChangeListener sliderTouchListener = new OplusSliderLegacy.OnSliderChangeListener() {
        @Override
        public void onProgressChanged(OplusSliderLegacy oplusSlider, boolean fromUser) {
            if (!getKey().equals(mOplusSlider.getTag())) return;

            updateLabel();
            if (updateConstantly && fromUser) {
                savePrefs();
            }
        }

        @Override
        public void onStartTrackingTouch(OplusSliderLegacy oplusSlider) {
        }

        @Override
        public void onStopTrackingTouch(OplusSliderLegacy oplusSlider) {
            if (!getKey().equals(mOplusSlider.getTag())) return;

            handleResetButton();
            updateLabel();

            if (!updateConstantly) {
                savePrefs();
            }
        }
    };

    LabelFormatter labelFormatter = new LabelFormatter() {
        @NonNull
        @Override
        public String getFormattedValue(float value) {
            String result;
            float scaledValue = value / outputScale;
            DecimalFormat df = new DecimalFormat(decimalFormat);
            df.setRoundingMode(RoundingMode.HALF_UP);
            df.setMinimumFractionDigits(decimalFormat.split("\\.")[1].length());
            if (valueFormat != null && (valueFormat.isBlank() || valueFormat.isEmpty())) {
                result = !isDecimalFormat
                        ? Integer.toString((int) scaledValue)
                        : df.format(scaledValue);
            } else {
                result = !isDecimalFormat
                        ? Integer.toString((int) scaledValue)
                        : df.format(scaledValue);
            }

            result += valueFormat;

            return result;
        }
    };

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        if (isEnabled()) {
            TextView title = holder.itemView.findViewById(android.R.id.title);
            title.setTextColor(ContextCompat.getColor(getContext(), R.color.textColorPrimary));
        }

        mOplusSlider = holder.itemView.findViewById(R.id.slider);
        mOplusSlider.setTag(getKey());

        mOplusSlider.clearSliderChangeListeners();
        mOplusSlider.addOnSliderChangeListener(sliderTouchListener);

        mOplusSlider.setLabelFormatter(labelFormatter);
        mTipsLayout = (RelativeLayout) holder.findViewById(R.id.tips_layout);
        mLeftTipTextView = (TextView) holder.findViewById(R.id.left_vertical_center_text);
        mRightTipTextView = (TextView) holder.findViewById(R.id.right_vertical_center_text);
        mLeftTipIconView = (ImageView) holder.findViewById(R.id.left_icon);
        mRightTipIconView = (ImageView) holder.findViewById(R.id.right_icon);
        mResetButton = holder.itemView.findViewById(R.id.reset_button);
        mResetButton.setOnClickListener(v -> {
            mOplusSlider.setValues(defaultValue);
            handleResetButton();
            savePrefs();
        });
        mResetButton.setVisibility(mShowResetButton ? View.VISIBLE : View.GONE);
        updateTipUI();

        mValueText = holder.itemView.findViewById(R.id.seekbar_value);
        mValueText.setVisibility(showSeekBarValue ? View.VISIBLE : View.GONE);

        mOplusSlider.setValueFrom(valueFrom);
        mOplusSlider.setValueTo(valueTo);
        mOplusSlider.setStepSize(tickInterval);

        syncState();

        handleResetButton();
    }

    private void updateTipUI() {
        if (mTipsLayout == null) return;

        boolean hasLeft = updateComponent(mLeftTipIconView, mLeftTipIcon)
                | updateComponent(mLeftTipTextView, mLeftTipText);

        boolean hasRight = updateComponent(mRightTipIconView, mRightTipIcon)
                | updateComponent(mRightTipTextView, mRightTipText);

        mTipsLayout.setVisibility((hasLeft || hasRight) ? View.VISIBLE : View.GONE);
    }

    private boolean updateComponent(ImageView view, int resId) {
        if (resId != 0) {
            view.setImageResource(resId);
            view.setVisibility(View.VISIBLE);
            return true;
        }
        view.setVisibility(View.GONE);
        return false;
    }

    private boolean updateComponent(TextView view, CharSequence text) {
        if (!TextUtils.isEmpty(text)) {
            view.setText(text);
            view.setVisibility(View.VISIBLE);
            return true;
        }
        view.setVisibility(View.GONE);
        return false;
    }

    /**
     * Sets the minimum value.
     * @param value The minimum value
     */
    public void setMin(float value) {
        valueFrom = value;
        if (mOplusSlider != null) mOplusSlider.setValueFrom(value);
    }

    /**
     * Sets the maximum value.
     * @param value The maximum value
     */
    public void setMax(float value) {
        valueTo = value;
        if (mOplusSlider != null) mOplusSlider.setValueTo(value);
    }

    public OplusSliderLegacy getOplusSlider() {
        return mOplusSlider;
    }

    public static List<Float> getValues(SharedPreferences prefs, String key, float defaultValue) {
        List<Float> values;

        try {
            String JSONString = prefs.getString(key, "");
            values = getValues(JSONString);
        } catch (Exception ignored) {
            try {
                float value = prefs.getFloat(key, defaultValue);
                values = Collections.singletonList(value);
            } catch (Exception ignored2) {
                try {
                    int value = prefs.getInt(key, Math.round(defaultValue));
                    values = Collections.singletonList((float) value);
                } catch (Exception ignored3) {
                    values = Collections.singletonList(defaultValue);
                }
            }
        }
        return values;
    }

    public static List<Float> getValues(String JSONString) throws Exception {
        List<Float> values = new ArrayList<>();

        if (JSONString.trim().isEmpty()) return values;

        JsonReader jsonReader = new JsonReader(new StringReader(JSONString));

        jsonReader.beginObject();
        try {
            jsonReader.nextName();
            jsonReader.beginArray();
        } catch (Exception ignored) {
        }

        while (jsonReader.hasNext()) {
            try {
                jsonReader.nextName();
            } catch (Exception ignored) {
            }
            values.add((float) jsonReader.nextDouble());
        }

        return values;
    }

    private void handleResetButton() {
        if (mResetButton == null) return;

        if (mShowResetButton) {
            mResetButton.setVisibility(View.VISIBLE);
            if (defaultValue.size() == 2 && mOplusSlider.getValues().size() == 2) {
                mResetButton.setEnabled(isEnabled() && (!Objects.equals(mOplusSlider.getValues().get(0), defaultValue.get(0)) || !Objects.equals(mOplusSlider.getValues().get(1), defaultValue.get(1))));
            } else {
                mResetButton.setEnabled(isEnabled() && !Objects.equals(mOplusSlider.getValues().get(0), defaultValue.get(0)));
            }
        } else {
            mResetButton.setVisibility(View.GONE);
        }
    }

    public static float getSingleFloatValue(SharedPreferences prefs, String key, float defaultValue) {
        float result = defaultValue;

        try {
            result = getValues(prefs, key, defaultValue).get(0);
        } catch (Throwable ignored) {
        }

        return result;
    }

    public static int getSingleIntValue(SharedPreferences prefs, String key, int defaultValue) {
        return Math.round(getSingleFloatValue(prefs, key, defaultValue));
    }
}