package it.dhd.oneplusui.preference;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;

import it.dhd.oneplusui.R;


public class OplusJumpPreference extends OplusPreference {

    private TextView mJumpText;
    private String mPendingJumpText;
    private int mJumpVisibility = View.VISIBLE;
    private View mJumpView;

    public OplusJumpPreference(@NonNull Context context) {
        this(context, null);
    }

    public OplusJumpPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.style.Preferences_Oplus_Preference_Jump);
    }

    public OplusJumpPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preferences_Oplus_Preference_Jump);
    }

    public OplusJumpPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                               int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mJumpText = (TextView) holder.findViewById(R.id.jump_text);
        mJumpView = holder.findViewById(android.R.id.widget_frame);

        if (!TextUtils.isEmpty(mPendingJumpText)) {
            mJumpText.setText(mPendingJumpText);
        }
        mJumpView.setVisibility(mJumpVisibility);
    }

    public void setJumpText(String text) {
        mPendingJumpText = text;
        if (mJumpText != null) {
            mJumpText.setText(text);
        }
    }

    public void setJumpEnabled(boolean enabled) {
        mJumpVisibility = enabled ? View.VISIBLE : View.GONE;
        if (mJumpView != null) {
            mJumpView.setVisibility(enabled ? View.VISIBLE : View.GONE);
        }
    }
}
