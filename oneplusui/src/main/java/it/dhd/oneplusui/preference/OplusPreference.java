package it.dhd.oneplusui.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import it.dhd.oneplusui.R;
import it.dhd.oneplusui.appcompat.cardlist.CardListHelper;

public class OplusPreference extends Preference {

    private String mForcePosition = null;
    private boolean mTintTitle = false;
    private boolean mTitleCentered = false;

    public OplusPreference(@NonNull Context context) {
        this(context, null);
    }

    public OplusPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OplusPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
        initResources();
    }

    public OplusPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                           int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initResources();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OplusPreference);
        if (a.hasValue(R.styleable.OplusPreference_forcePosition)) {
            mForcePosition = a.getString(R.styleable.OplusPreference_forcePosition);
        }
        mTintTitle = a.getBoolean(R.styleable.OplusPreference_tintTitle, false);
        mTitleCentered = a.getBoolean(R.styleable.OplusPreference_centerTitle, false);
        a.recycle();
    }

    private void initResources() {
        setLayoutResource(R.layout.oplus_preference);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        if (mForcePosition != null) {
            int pos = switch (mForcePosition) {
                case "top" -> CardListHelper.HEAD;
                case "middle" -> CardListHelper.MIDDLE;
                case "bottom" -> CardListHelper.TAIL;
                case "full" -> CardListHelper.FULL;
                default -> CardListHelper.NONE;
            };
            CardListHelper.setItemCardBackground(holder.itemView, pos);
        } else {
            CardListHelper.setItemCardBackground(holder.itemView, CardListHelper.getPositionInGroup(this));
        }
        TextView title = (TextView) holder.findViewById(android.R.id.title);
        if (mTintTitle) {
            title.setTextColor(ContextCompat.getColor(getContext(), android.R.color.system_accent1_400));
        }
        if (mTitleCentered) {
            title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
    }


}

