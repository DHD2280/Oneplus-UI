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
import androidx.recyclerview.widget.OplusRecyclerView;

import it.dhd.oneplusui.R;
import it.dhd.oneplusui.appcompat.cardlist.CardListHelper;
import it.dhd.oneplusui.appcompat.cardlist.CardListSelectedItemLayout;

public class OplusPreference extends Preference implements OplusRecyclerView.IOplusDividerDecorationInterface {

    private String mForcePosition = null;
    private boolean mTintTitle = false;
    private boolean mTitleCentered = false;
    private View mItemView, mTitleView;
    private final int mDividerDefaultHorizontalPadding;

    public OplusPreference(@NonNull Context context) {
        this(context, null);
    }

    public OplusPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.style.Preferences_Oplus_Preference);
    }

    public OplusPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preferences_Oplus_Preference);
    }

    public OplusPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                           int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OplusPreference);
        if (a.hasValue(R.styleable.OplusPreference_forcePosition)) {
            mForcePosition = a.getString(R.styleable.OplusPreference_forcePosition);
        }
        mTintTitle = a.getBoolean(R.styleable.OplusPreference_tintTitle, false);
        mTitleCentered = a.getBoolean(R.styleable.OplusPreference_centerTitle, false);
        this.mDividerDefaultHorizontalPadding = context.getResources().getDimensionPixelSize(R.dimen.preference_divider_default_horizontal_padding);
        a.recycle();
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mItemView = holder.itemView;
        mTitleView = holder.findViewById(android.R.id.title);
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
        if (mTintTitle) {
            ((TextView)mTitleView).setTextColor(ContextCompat.getColor(getContext(), android.R.color.system_accent1_400));
        }
        if (mTitleCentered) {
            mTitleView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
    }

    @Override
    public boolean drawDivider() {
        if (!(this.mItemView instanceof CardListSelectedItemLayout)) {
            return false;
        }
        if (mForcePosition != null) {
            return mForcePosition.equals("middle") || mForcePosition.equals("top");
        }
        int positionInGroup = CardListHelper.getPositionInGroup(this);
        return positionInGroup == 1 || positionInGroup == 2;
    }

    @Override
    public View getDividerEndAlignView() {
        return null;
    }

    @Override
    public int getDividerEndInset() {
        return this.mDividerDefaultHorizontalPadding;
    }

    @Override
    public View getDividerStartAlignView() {
        return this.mTitleView;
    }

    @Override
    public int getDividerStartInset() {
        return this.mDividerDefaultHorizontalPadding;
    }


}
