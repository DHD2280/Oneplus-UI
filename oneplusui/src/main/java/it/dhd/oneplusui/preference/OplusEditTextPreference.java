package it.dhd.oneplusui.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.OplusRecyclerView;

import it.dhd.oneplusui.R;
import it.dhd.oneplusui.appcompat.cardlist.CardListHelper;
import it.dhd.oneplusui.appcompat.cardlist.CardListSelectedItemLayout;

public class OplusEditTextPreference extends EditTextPreference implements OplusRecyclerView.IOplusDividerDecorationInterface {

    private String mForcePosition = null;
    private View mItemView, mTitleView;
    private final int mDividerDefaultHorizontalPadding;

    public OplusEditTextPreference(@NonNull Context context) {
        this(context, null);
    }

    public OplusEditTextPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, androidx.preference.R.attr.editTextPreferenceStyle);
    }

    public OplusEditTextPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                               int defStyle) {
        this(context, attrs, defStyle, androidx.preference.R.attr.editTextPreferenceStyle);
    }

    public OplusEditTextPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                               int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OplusEditTextPreference);
        if (a.hasValue(R.styleable.OplusEditTextPreference_forcePosition)) {
            mForcePosition = a.getString(R.styleable.OplusEditTextPreference_forcePosition);
        }
        this.mDividerDefaultHorizontalPadding = context.getResources().getDimensionPixelSize(R.dimen.preference_divider_default_horizontal_padding);
        a.recycle();
        setLayoutResource(R.layout.oplus_preference);
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
