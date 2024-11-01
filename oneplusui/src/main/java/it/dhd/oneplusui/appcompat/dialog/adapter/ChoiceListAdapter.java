package it.dhd.oneplusui.appcompat.dialog.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.checkbox.MaterialCheckBox;

import it.dhd.oneplusui.R;


public class ChoiceListAdapter extends BaseAdapter {

    private boolean[] mCheckBoxStates;
    private Context mContext;
    private boolean[] mDisableStatus;
    private int[] mIcons;
    private boolean mIsBottom;
    private boolean mIsMultiChoice;
    private boolean mIsTop;
    private CharSequence[] mItems;
    private int mLayoutResId;
    private MaxCheckedListener mMaxCheckedListener;
    private int mMaxCheckedNum;
    private MultiChoiceItemClickListener mMultiChoiceItemClickListener;
    private CharSequence[] mSummaries;

    public interface MaxCheckedListener {
        void maxCheckedNotice(int i);
    }

    public interface MultiChoiceItemClickListener {
        void onClick(int i, boolean z);
    }

    public static class ViewHolder {
        public MaterialCheckBox checkBox;
        public ImageView divider;
        public ImageView icon;
        public TextView itemText;
        public RadioButton radioButton;
        public FrameLayout radioLayout;
        public TextView summaryText;
        public LinearLayout textLayout;
    }

    public ChoiceListAdapter(Context context, int i, CharSequence[] charSequenceArr, CharSequence[] charSequenceArr2, boolean[] zArr, boolean isMultiSelect) {
        this(context, i, charSequenceArr, charSequenceArr2, zArr, null, isMultiSelect);
    }

    public int getCheckedNum() {
        int checkedNum = 0;
        for (boolean z : this.mCheckBoxStates) {
            if (z) {
                checkedNum++;
            }
        }
        return checkedNum;
    }

    private void initCheckboxStates(boolean[] zArr) {
        for (int i = 0; i < zArr.length; i++) {
            boolean[] zArr2 = this.mCheckBoxStates;
            if (i < zArr2.length) {
                zArr2[i] = zArr[i];
            } else {
                return;
            }
        }
    }

    private void initCheckboxStatesDisable(boolean[] zArr) {
        for (int i = 0; i < zArr.length; i++) {
            boolean[] zArr2 = this.mDisableStatus;
            if (i < zArr2.length) {
                zArr2[i] = zArr[i];
            } else {
                return;
            }
        }
    }

    private void setPaddingBottom(View view, int i) {
        if (view == null) {
            return;
        }
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), i);
    }

    public boolean[] getCheckBoxStates() {
        return this.mCheckBoxStates;
    }

    @Override
    public int getCount() {
        CharSequence[] charSequenceArr = this.mItems;
        if (charSequenceArr == null) {
            return 0;
        }
        return charSequenceArr.length;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemViewType(int i) {
        return i;
    }

    public MultiChoiceItemClickListener getMultiChoiceItemClickListener() {
        return this.mMultiChoiceItemClickListener;
    }

    public CharSequence getSummary(int i) {
        CharSequence[] charSequenceArr = this.mSummaries;
        if (charSequenceArr == null || i >= charSequenceArr.length) {
            return null;
        }
        return charSequenceArr[i];
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        View view2;
        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            view2 = LayoutInflater.from(this.mContext).inflate(this.mLayoutResId, viewGroup, false);
            viewHolder.icon = view2.findViewById(R.id.alertdialog_choice_icon);
            viewHolder.textLayout = view2.findViewById(R.id.text_layout);
            viewHolder.itemText = view2.findViewById(android.R.id.text1);
            viewHolder.summaryText = view2.findViewById(R.id.summary_text2);
            viewHolder.divider = view2.findViewById(R.id.item_divider);

            if (this.mIsMultiChoice) {
                viewHolder.checkBox = view2.findViewById(R.id.checkbox);
            } else {
                viewHolder.radioLayout = view2.findViewById(R.id.radio_layout);
                viewHolder.radioButton = view2.findViewById(R.id.radio_button);
            }

            if (this.mDisableStatus[i]) {
                viewHolder.itemText.setEnabled(false);
                viewHolder.summaryText.setEnabled(false);
                if (this.mIsMultiChoice) {
                    viewHolder.checkBox.setEnabled(false);
                } else {
                    viewHolder.radioButton.setEnabled(false);
                }
                view2.setOnTouchListener((v, motionEvent) -> true);
            }
            view2.setTag(viewHolder);
        } else {
            view2 = view;
            viewHolder = (ViewHolder) view.getTag();
        }

        if (this.mIsMultiChoice) {
            viewHolder.checkBox.setChecked(this.mCheckBoxStates[i]);
            view2.setOnClickListener(v -> {
                boolean newCheckedState = !viewHolder.checkBox.isChecked();
                viewHolder.checkBox.setChecked(newCheckedState);
                this.mCheckBoxStates[i] = newCheckedState;

                if (newCheckedState && this.mMaxCheckedNum > 0 && getCheckedNum() > this.mMaxCheckedNum) {
                    viewHolder.checkBox.setChecked(false);
                    this.mCheckBoxStates[i] = false;
                    if (this.mMaxCheckedListener != null) {
                        this.mMaxCheckedListener.maxCheckedNotice(this.mMaxCheckedNum);
                    }
                } else if (this.mMultiChoiceItemClickListener != null) {
                    this.mMultiChoiceItemClickListener.onClick(i, newCheckedState);
                }
            });
        } else {
            viewHolder.radioButton.setChecked(this.mCheckBoxStates[i]);
        }

        CharSequence item = getItem(i);
        CharSequence summary = getSummary(i);
        viewHolder.itemText.setText(item);
        if (TextUtils.isEmpty(summary)) {
            viewHolder.summaryText.setVisibility(View.GONE);
        } else {
            viewHolder.summaryText.setVisibility(View.VISIBLE);
            viewHolder.summaryText.setText(summary);
        }

        if (viewHolder.divider != null) {
            viewHolder.divider.setVisibility((getCount() != 1 && i != getCount() - 1) ? View.VISIBLE : View.GONE);
        }

        if (mIcons != null) {
            Drawable drawable = AppCompatResources.getDrawable(mContext, mIcons[i]);
            if (drawable != null) {
                viewHolder.icon.setVisibility(View.VISIBLE);
                viewHolder.icon.setImageDrawable(drawable);
            } else {
                viewHolder.icon.setVisibility(View.GONE);
            }
        }

        return view2;
    }

    public void setCheckboxState(int state, int position, @NonNull ListView listView) {
        int firstVisiblePosition = position - listView.getFirstVisiblePosition();
        if (firstVisiblePosition < 0) {
            return;
        }

        View childAt = listView.getChildAt(firstVisiblePosition);
        if (childAt == null) {
            return;
        }

        ViewHolder viewHolder = (ViewHolder) childAt.getTag();
        if (this.mIsMultiChoice && viewHolder.checkBox != null) {
            boolean isChecked = (state == 2);
            viewHolder.checkBox.setChecked(isChecked);
            this.mCheckBoxStates[position] = isChecked;
        }
    }

    public void setIcons(int[] icons) {
        this.mIcons = icons;
    }

    public void setIsBottom(boolean isBottom) {
        this.mIsBottom = isBottom;
    }

    public void setIsTop(boolean isTop) {
        this.mIsTop = isTop;
    }

    public void setMaxCheckedListener(MaxCheckedListener maxCheckedListener) {
        this.mMaxCheckedListener = maxCheckedListener;
    }

    public void setMultiChoiceItemClickListener(MultiChoiceItemClickListener multiChoiceItemClickListener) {
        this.mMultiChoiceItemClickListener = multiChoiceItemClickListener;
    }

    public ChoiceListAdapter(Context context, int i, CharSequence[] charSequenceArr, CharSequence[] charSequenceArr2, boolean[] zArr, boolean[] zArr2, boolean z) {
        this(context, i, charSequenceArr, charSequenceArr2, zArr, zArr2, z, 0);
    }

    @Override
    public CharSequence getItem(int i) {
        CharSequence[] charSequenceArr = this.mItems;
        if (charSequenceArr == null) {
            return null;
        }
        return charSequenceArr[i];
    }

    public ChoiceListAdapter(Context context, int i, CharSequence[] charSequenceArr, CharSequence[] charSequenceArr2, boolean[] zArr, boolean[] zArr2, boolean isMultiSelect, int maxCheckedItems) {
        this.mIsTop = false;
        this.mIsBottom = false;
        this.mContext = context;
        this.mLayoutResId = i;
        this.mItems = charSequenceArr;
        this.mSummaries = charSequenceArr2;
        this.mIsMultiChoice = isMultiSelect;
        this.mCheckBoxStates = new boolean[charSequenceArr.length];
        if (zArr != null) {
            initCheckboxStates(zArr);
        }
        this.mDisableStatus = new boolean[this.mItems.length];
        if (zArr2 != null) {
            initCheckboxStatesDisable(zArr2);
        }
        this.mMaxCheckedNum = maxCheckedItems;
    }

    public ChoiceListAdapter(Context context, int i, CharSequence[] charSequenceArr, CharSequence[] charSequenceArr2) {
        this(context, i, charSequenceArr, charSequenceArr2, null, false);
    }
}
