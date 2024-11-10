package it.dhd.oneplusui.preference;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceViewHolder;

import it.dhd.oneplusui.R;

/**
 * An extension of PreferenceCategory
 * This class uses a custom layout to look like Oneplus PreferenceCategory
 * See {@link R.layout#oplus_preference_category} for layout
 */
public class OplusPreferenceCategory extends PreferenceCategory {

    public OplusPreferenceCategory(@NonNull Context context) {
        this(context, null);
    }

    public OplusPreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OplusPreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs,
                               int defStyle) {
        this(context, attrs, defStyle, R.style.Preferences_Oplus_Category);
    }

    public OplusPreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs,
                               int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.setDividerAllowedAbove(false);
        holder.setDividerAllowedBelow(false);
    }

}
