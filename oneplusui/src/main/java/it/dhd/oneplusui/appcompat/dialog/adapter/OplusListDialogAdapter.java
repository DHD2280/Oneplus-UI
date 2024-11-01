package it.dhd.oneplusui.appcompat.dialog.adapter;

import android.widget.BaseAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import it.dhd.oneplusui.R;


public class OplusListDialogAdapter extends BaseAdapter {

    private Context mContext;
    private CharSequence[] mItems;
    private int[] mTextAppearances;
    private final int LAYOUT = R.layout.oplus_list_dialog_item;
    private boolean mIsTop = false;
    private boolean mIsBottom = false;


    public class ViewHolder {
        public ImageView divider;
        public TextView mTextView;
        public LinearLayout mainLayout;

        public ViewHolder() {
        }
    }

    public OplusListDialogAdapter(Context context, CharSequence[] charSequenceArr, int[] iArr) {
        this.mContext = context;
        this.mItems = charSequenceArr;
        this.mTextAppearances = iArr;
    }

    private View getViewInternal(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(this.mContext).inflate(this.LAYOUT, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.mTextView = view.findViewById(android.R.id.text1);
            viewHolder.divider = view.findViewById(R.id.item_divider);
            viewHolder.mainLayout = view.findViewById(R.id.main_layout);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.mTextView.setText(getItem(i));
        int[] iArr = this.mTextAppearances;
        if (iArr != null) {
            int i2 = iArr[i];
            if (i2 > 0) {
                viewHolder.mTextView.setTextAppearance(this.mContext, i2);
            } else {
                viewHolder.mTextView.setTextAppearance(this.mContext, R.style.DefaultDialogItemTextStyle);
            }
        }
        if (viewHolder.divider != null) {
            if (getCount() > 1 && i != getCount() - 1) {
                viewHolder.divider.setVisibility(View.VISIBLE);
            } else {
                viewHolder.divider.setVisibility(View.GONE);
            }
        }
        return view;
    }

    private void resetPadding(int i, View view) {
        Resources resources = this.mContext.getResources();
        int i2 = R.dimen.oplus_bottom_alert_dialog_vertical_button_padding_vertical;
        int paddingVertical = resources.getDimensionPixelSize(i2);
        int paddingLeft = this.mContext.getResources().getDimensionPixelSize(R.dimen.alert_dialog_list_item_padding_left);
        int paddingRight = this.mContext.getResources().getDimensionPixelSize(R.dimen.alert_dialog_list_item_padding_right);
        this.mContext.getResources().getDimensionPixelSize(R.dimen.alert_dialog_list_item_min_height);
        if (i == getCount() - 1 && this.mIsBottom) {
            view.setPadding(paddingLeft, paddingVertical, paddingRight, paddingVertical);
        } else if (i == 0 && this.mIsTop) {
            view.setPadding(paddingLeft, paddingVertical, paddingRight, paddingVertical);
        } else {
            view.setPadding(paddingLeft, paddingVertical, paddingRight, paddingVertical);
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

    @Override // android.widget.Adapter
    public View getView(int i, View view, ViewGroup viewGroup) {
        View viewInternal = getViewInternal(i, view, viewGroup);
        resetPadding(i, viewInternal.findViewById(R.id.main_layout));
        return viewInternal;
    }

    public void setIsBottom(boolean z) {
        this.mIsBottom = z;
    }

    public void setIsTop(boolean z) {
        this.mIsTop = z;
    }

    @Override
    public CharSequence getItem(int i) {
        CharSequence[] charSequenceArr = this.mItems;
        if (charSequenceArr == null) {
            return null;
        }
        return charSequenceArr[i];
    }

    public void clear() {
        this.mItems = null;
        notifyDataSetChanged();
    }

    public void add(CharSequence item) {
        CharSequence[] charSequenceArr = this.mItems;
        if (charSequenceArr == null) {
            this.mItems = new CharSequence[1];
            this.mItems[0] = item;
        } else {
            int length = charSequenceArr.length;
            CharSequence[] charSequenceArr2 = new CharSequence[(length + 1)];
            System.arraycopy(charSequenceArr, 0, charSequenceArr2, 0, length);
            charSequenceArr2[length] = item;
            this.mItems = charSequenceArr2;
        }
        notifyDataSetChanged();
    }
}
