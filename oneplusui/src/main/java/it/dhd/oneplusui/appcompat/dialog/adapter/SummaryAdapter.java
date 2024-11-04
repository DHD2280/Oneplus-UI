package it.dhd.oneplusui.appcompat.dialog.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import it.dhd.oneplusui.R;

public class SummaryAdapter extends BaseAdapter {

    private static final int LAYOUT = R.layout.oplus_alert_dialog_summary_item;
    private Context mContext;
    private boolean mIsBottom;
    private boolean mIsTop;
    private CharSequence[] mItems;
    private CharSequence[] mSummaries;
    private int[] mTextColor;

    public static class ViewHolder {
        public ImageView mDivider;
        public TextView mItemView;
        public LinearLayout mMainLayout;
        public TextView mSummaryView;

        private ViewHolder() {
        }
    }

    public SummaryAdapter(Context context, boolean isTop, boolean isBottom, CharSequence[] items, CharSequence[] descriptions, int[] textColors) {
        this.mIsTop = isTop;
        this.mIsBottom = isBottom;
        this.mContext = context;
        this.mItems = items;
        this.mSummaries = descriptions;
        this.mTextColor = textColors;
    }

    private void resetPadding(int position, View view) {
        int paddingVertical = this.mContext.getResources().getDimensionPixelSize(R.dimen.oplus_bottom_alert_dialog_vertical_button_padding_vertical_new);
        int paddingLeft = view.getPaddingLeft();
        int paddingRight = view.getPaddingRight();
        if (position == getCount() - 1 && this.mIsBottom) {
            view.setPadding(paddingLeft, paddingVertical, paddingRight, paddingVertical);
        } else if (position == 0 && this.mIsTop) {
            view.setPadding(paddingLeft, paddingVertical, paddingRight, paddingVertical);
        } else {
            view.setPadding(paddingLeft, paddingVertical, paddingRight, paddingVertical);
        }
    }

    @Override
    public int getCount() {
        if (mItems == null) {
            return 0;
        }
        return mItems.length;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public CharSequence getSummary(int position) {
        if (mSummaries == null || position >= mSummaries.length) {
            return null;
        }
        return mSummaries[position];
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(this.mContext).inflate(LAYOUT, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.mItemView = view.findViewById(android.R.id.text1);
            viewHolder.mSummaryView = view.findViewById(R.id.summary_text2);
            viewHolder.mDivider = view.findViewById(R.id.item_divider);
            viewHolder.mMainLayout = view.findViewById(R.id.main_layout);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        CharSequence item = getItem(position);
        CharSequence summary = getSummary(position);
        viewHolder.mItemView.setText(item);
        if (TextUtils.isEmpty(summary)) {
            viewHolder.mSummaryView.setVisibility(View.GONE);
        } else {
            viewHolder.mSummaryView.setVisibility(View.VISIBLE);
            viewHolder.mSummaryView.setText(summary);
        }
        resetPadding(position, viewHolder.mMainLayout);
        if (mTextColor != null && position >= 0 && position < mTextColor.length) {
            viewHolder.mItemView.setTextColor(mTextColor[position]);
        }
        if (viewHolder.mDivider != null) {
            if (getCount() > 1 && position != getCount() - 1) {
                viewHolder.mDivider.setVisibility(View.VISIBLE);
            } else {
                viewHolder.mDivider.setVisibility(View.GONE);
            }
        }
        view.requestLayout();
        return view;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public CharSequence getItem(int i) {
        if (mItems == null) {
            return null;
        }
        return mItems[i];
    }
}
