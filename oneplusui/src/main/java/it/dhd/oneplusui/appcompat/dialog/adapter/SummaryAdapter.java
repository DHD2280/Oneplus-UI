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

/**
 * An adapter for a list of items with summaries.
 * This adapter can show a list of items with summaries.
 * Is also able to set the text color for each item.
 */
public class SummaryAdapter extends BaseAdapter {

    private static final int LAYOUT = R.layout.oplus_alert_dialog_summary_item;
    private final Context mContext;
    private final CharSequence[] mItems;
    private final CharSequence[] mSummaries;
    private final int[] mTextColor;
    private boolean mTextCentered = true;

    public static class ViewHolder {
        public ImageView mDivider;
        public TextView mItemView;
        public LinearLayout mMainLayout;
        public TextView mSummaryView;

        private ViewHolder() {
        }
    }

    /**
     * Constructor for SummaryAdapter.
     *
     * @param context The context.
     * @param items The items to be shown.
     * @param descriptions The summaries for each item.
     * @param textColors The text color for each item.
     */
    public SummaryAdapter(Context context, CharSequence[] items, CharSequence[] descriptions, int[] textColors) {
        this.mContext = context;
        this.mItems = items;
        this.mSummaries = descriptions;
        this.mTextColor = textColors;
    }

    /**
     * Constructor for SummaryAdapter.
     * @param context The context.
     * @param items The items to be shown.
     * @param descriptions The summaries for each item.
     */
    public SummaryAdapter(Context context, CharSequence[] items, CharSequence[] descriptions) {
        this.mContext = context;
        this.mItems = items;
        this.mSummaries = descriptions;
        this.mTextColor = null;
    }

    /**
     * Constructor for SummaryAdapter.
     * @param context The context.
     * @param items The items to be shown.
     */
    public SummaryAdapter(Context context, CharSequence[] items) {
        this.mContext = context;
        this.mItems = items;
        this.mSummaries = null;
        this.mTextColor = null;
    }

    /**
     * Constructor for SummaryAdapter.
     * @param context The context.
     * @param items The items to be shown.
     * @param textColors The text color for each item.
     */
    public SummaryAdapter(Context context, CharSequence[] items, int[] textColors) {
        this.mContext = context;
        this.mItems = items;
        this.mSummaries = null;
        this.mTextColor = null;
    }

    private void resetPadding(View view) {
        int paddingVertical = this.mContext.getResources().getDimensionPixelSize(R.dimen.oplus_bottom_alert_dialog_vertical_button_padding_vertical_new);
        int paddingLeft = view.getPaddingLeft();
        int paddingRight = view.getPaddingRight();
        view.setPadding(paddingLeft, paddingVertical, paddingRight, paddingVertical);
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
            viewHolder.mSummaryView.setTextAlignment(mTextCentered ? View.TEXT_ALIGNMENT_CENTER : View.TEXT_ALIGNMENT_VIEW_START);
        }
        resetPadding(viewHolder.mMainLayout);
        if (mTextColor != null && position >= 0 && position < mTextColor.length) {
            viewHolder.mItemView.setTextColor(mTextColor[position]);
        }
        viewHolder.mItemView.setTextAlignment(mTextCentered ? View.TEXT_ALIGNMENT_CENTER : View.TEXT_ALIGNMENT_VIEW_START);
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

    public void setTextCentered(boolean centered) {
        mTextCentered = centered;
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
