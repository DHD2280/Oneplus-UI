package it.dhd.oneplusui.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreferenceCompat;
import androidx.recyclerview.widget.OplusRecyclerView;

import it.dhd.oneplusui.R;
import it.dhd.oneplusui.appcompat.cardlist.CardListHelper;
import it.dhd.oneplusui.appcompat.cardlist.CardListSelectedItemLayout;


public class OplusSwitchPreference extends SwitchPreferenceCompat implements OplusRecyclerView.IOplusDividerDecorationInterface {

    private String mForcePosition = null;
    private View mItemView, mTitleView;
    private final int mDividerDefaultHorizontalPadding;

    public OplusSwitchPreference(@NonNull Context context) {
        this(context, null);
    }

    public OplusSwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.style.Preferences_Oplus_Preference_Switch);
    }

    public OplusSwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                 int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preferences_Oplus_Preference_Switch);
    }

    public OplusSwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                 int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OplusSwitchPreference);
        if (a.hasValue(R.styleable.OplusSwitchPreference_forcePosition)) {
            mForcePosition = a.getString(R.styleable.OplusSwitchPreference_forcePosition);
        }
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