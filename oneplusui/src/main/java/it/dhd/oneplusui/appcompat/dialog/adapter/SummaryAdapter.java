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

    public SummaryAdapter(Context context, boolean z, boolean z2, CharSequence[] charSequenceArr, CharSequence[] charSequenceArr2, int[] iArr) {
        this.mIsTop = z;
        this.mIsBottom = z2;
        this.mContext = context;
        this.mItems = charSequenceArr;
        this.mSummaries = charSequenceArr2;
        this.mTextColor = iArr;
    }

    private void resetPadding(int i, View view) {
        int dimensionPixelSize2 = this.mContext.getResources().getDimensionPixelSize(R.dimen.oplus_bottom_alert_dialog_vertical_button_padding_vertical_new);
        int paddingLeft = view.getPaddingLeft();
        int paddingRight = view.getPaddingRight();
        if (i == getCount() - 1 && this.mIsBottom) {
            view.setPadding(paddingLeft, dimensionPixelSize2, paddingRight, dimensionPixelSize2);
        } else if (i == 0 && this.mIsTop) {
            view.setPadding(paddingLeft, dimensionPixelSize2, paddingRight, dimensionPixelSize2);
        } else {
            view.setPadding(paddingLeft, dimensionPixelSize2, paddingRight, dimensionPixelSize2);
        }
    }

    @Override // android.widget.Adapter
    public int getCount() {
        CharSequence[] charSequenceArr = this.mItems;
        if (charSequenceArr == null) {
            return 0;
        }
        return charSequenceArr.length;
    }

    @Override // android.widget.Adapter
    public long getItemId(int i) {
        return i;
    }

    public CharSequence getSummary(int i) {
        CharSequence[] charSequenceArr = this.mSummaries;
        if (charSequenceArr == null || i >= charSequenceArr.length) {
            return null;
        }
        return charSequenceArr[i];
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(this.mContext).inflate(LAYOUT, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.mItemView = (TextView) view.findViewById(android.R.id.text1);
            viewHolder.mSummaryView = (TextView) view.findViewById(R.id.summary_text2);
            viewHolder.mDivider = (ImageView) view.findViewById(R.id.item_divider);
            viewHolder.mMainLayout = (LinearLayout) view.findViewById(R.id.main_layout);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        CharSequence item = getItem(i);
        CharSequence summary = getSummary(i);
        viewHolder.mItemView.setText(item);
        if (TextUtils.isEmpty(summary)) {
            viewHolder.mSummaryView.setVisibility(View.GONE);
        } else {
            viewHolder.mSummaryView.setVisibility(View.VISIBLE);
            viewHolder.mSummaryView.setText(summary);
        }
        resetPadding(i, viewHolder.mMainLayout);
        int[] iArr = this.mTextColor;
        if (iArr != null && i >= 0 && i < iArr.length) {
            viewHolder.mItemView.setTextColor(iArr[i]);
        }
        if (viewHolder.mDivider != null) {
            if (getCount() > 1 && i != getCount() - 1) {
                viewHolder.mDivider.setVisibility(View.VISIBLE);
            } else {
                viewHolder.mDivider.setVisibility(View.GONE);
            }
        }
        view.requestLayout();
        return view;
    }

    @Override // android.widget.BaseAdapter, android.widget.Adapter
    public boolean hasStableIds() {
        return true;
    }

    /* JADX DEBUG: Method merged with bridge method */
    @Override // android.widget.Adapter
    public CharSequence getItem(int i) {
        CharSequence[] charSequenceArr = this.mItems;
        if (charSequenceArr == null) {
            return null;
        }
        return charSequenceArr[i];
    }
}
