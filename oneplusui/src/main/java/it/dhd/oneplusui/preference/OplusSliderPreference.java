package it.dhd.oneplusui.preference;

/*
 * From Siavash79/rangesliderpreference
 * https://github.com/siavash79/rangesliderpreference
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceViewHolder;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.RangeSlider;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;

import it.dhd.oneplusui.R;


public class OplusSliderPreference extends OplusPreference {
    @SuppressWarnings("unused")
    private static final String TAG = "Slider Preference";
    private float valueFrom;
    private float valueTo;
    private final float tickInterval;
    private boolean showResetButton;
    public final List<Float> defaultValue = new ArrayList<>();
    public RangeSlider slider;
    private MaterialButton mResetButton;
    int valueCount;
    private String valueFormat;
    private final float outputScale;
    private final boolean isDecimalFormat;
    private String decimalFormat = "#.#";

    boolean updateConstantly, showValueLabel;

    @SuppressWarnings("unused")
    public OplusSliderPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OplusSliderPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.oplus_preference_slider);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OplusSliderPreference);
        updateConstantly = a.getBoolean(R.styleable.OplusSliderPreference_updatesContinuously, false);
        valueCount = a.getInteger(R.styleable.OplusSliderPreference_valueCount, 1);
        valueFrom = a.getFloat(R.styleable.OplusSliderPreference_minVal, 0f);
        valueTo = a.getFloat(R.styleable.OplusSliderPreference_maxVal, 100f);
        tickInterval = a.getFloat(R.styleable.OplusSliderPreference_tickInterval, 1f);
        showResetButton = a.getBoolean(R.styleable.OplusSliderPreference_showResetButton, true);
        showValueLabel = a.getBoolean(R.styleable.OplusSliderPreference_showValueLabel, true);
        valueFormat = a.getString(R.styleable.OplusSliderPreference_valueFormat);
        isDecimalFormat = a.getBoolean(R.styleable.OplusSliderPreference_isDecimalFormat, false);
        if (a.hasValue(R.styleable.OplusSliderPreference_decimalFormat)) {
            decimalFormat = a.getString(R.styleable.OplusSliderPreference_decimalFormat);
        } else {
            decimalFormat = "#.#"; // Default decimal format
        }
        if (TextUtils.isEmpty(decimalFormat) || decimalFormat.equals("null")) decimalFormat = "#.#";
        outputScale = a.getFloat(R.styleable.OplusSliderPreference_outputScale, 1f);
        String defaultValStr = a.getString(androidx.preference.R.styleable.Preference_defaultValue);

        if (!TextUtils.isEmpty(defaultValStr) && !defaultValStr.equals("null")) {
            try {
                Scanner scanner = new Scanner(defaultValStr);
                scanner.useDelimiter(",");
                scanner.useLocale(Locale.ENGLISH);

                while (scanner.hasNext()) {
                    defaultValue.add(scanner.nextFloat());
                }
            } catch (Exception ignored) {
                Log.e(TAG, String.format("OplusSliderPreference: Error parsing default values for key: %s", getKey()));
            }
        }

        a.recycle();
    }

    public void savePrefs() {
        if (slider == null) return;
        setValues(getSharedPreferences(), getKey(), slider.getValues());
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
        BigDecimal step = new BigDecimal(String.valueOf(slider.getStepSize())); //float and double are not accurate when it comes to decimal points

        for (int i = 0; i < values.size(); i++) {
            BigDecimal round = new BigDecimal(Math.round(values.get(i) / slider.getStepSize()));
            double v = Math.min(Math.max(step.multiply(round).doubleValue(), slider.getValueFrom()), slider.getValueTo());
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
            slider.setValues(values);
            if (needsCommit) savePrefs();
        } catch (Throwable t) {
            values.clear();
        }
    }

    RangeSlider.OnChangeListener changeListener = (slider, value, fromUser) -> {
        if (!getKey().equals(slider.getTag())) return;

        if (updateConstantly && fromUser) {
            savePrefs();
        }
    };

    RangeSlider.OnSliderTouchListener sliderTouchListener = new RangeSlider.OnSliderTouchListener() {
        @Override
        public void onStartTrackingTouch(@NonNull RangeSlider slider) {
        }

        @Override
        public void onStopTrackingTouch(@NonNull RangeSlider slider) {
            if (!getKey().equals(slider.getTag())) return;

            handleResetButton();

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
            if (valueFormat != null && (valueFormat.isBlank() || valueFormat.isEmpty())) {
                result = !isDecimalFormat
                        ? Integer.toString((int) (slider.getValues().get(0) / outputScale))
                        : new DecimalFormat(decimalFormat).format(slider.getValues().get(0) / outputScale);
            } else {
                result = !isDecimalFormat
                        ? Integer.toString((int) (slider.getValues().get(0) / 1f))
                        : new DecimalFormat(decimalFormat).format(slider.getValues().get(0) / outputScale);
            }
            if (!TextUtils.isEmpty(valueFormat) && !valueFormat.equals("null")) result += valueFormat;

            return result;
        }
    };

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        slider = (RangeSlider) holder.findViewById(R.id.slider);
        slider.setTag(getKey());

        slider.addOnSliderTouchListener(sliderTouchListener);
        slider.addOnChangeListener(changeListener);

        slider.setLabelFormatter(labelFormatter);

        mResetButton = (MaterialButton) holder.findViewById(R.id.reset_button);
        if (showResetButton) {
            mResetButton.setVisibility(View.VISIBLE);
            mResetButton.setOnClickListener(v -> {
                handleResetButton();
                slider.setValues(defaultValue);
                savePrefs();
            });
        } else {
            mResetButton.setVisibility(View.GONE);
        }

        slider.setValueFrom(valueFrom);
        slider.setValueTo(valueTo);
        slider.setStepSize(tickInterval);

        syncState();

        handleResetButton();
    }

    public void setMin(float value) {
        valueFrom = value;
        slider.setValueFrom(value);
    }

    public void setMax(float value) {
        valueTo = value;
        slider.setValueTo(value);
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

        if (showResetButton) {
            mResetButton.setVisibility(View.VISIBLE);
            mResetButton.setEnabled(isEnabled() && !Objects.equals(slider.getValues().get(0), defaultValue.get(0)));
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

    @Override
    protected void onSetInitialValue(Object defaultValueObj) {
        List<Float> defaultValues = parseDefaultValue(defaultValueObj);
        List<Float> savedValues = getValues(getSharedPreferences(), getKey(), valueFrom);

        if (savedValues.isEmpty() && !defaultValues.isEmpty()) {
            savedValues = defaultValues;
        }

        if (slider != null) {
            slider.setValues(savedValues);
        }
        savePrefs();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        String defaultValueStr = a.getString(index);
        return parseDefaultValue(defaultValueStr);
    }

    private List<Float> parseDefaultValue(Object defaultValueObj) {
        List<Float> parsedValues = new ArrayList<>();
        if (defaultValueObj instanceof String) {
            try {
                Scanner scanner = new Scanner((String) defaultValueObj);
                scanner.useDelimiter(",");
                scanner.useLocale(Locale.ENGLISH);

                while (scanner.hasNext()) {
                    parsedValues.add(scanner.nextFloat());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing default values", e);
            }
        }
        return parsedValues.isEmpty() ? Collections.singletonList(valueFrom) : parsedValues;
    }

}