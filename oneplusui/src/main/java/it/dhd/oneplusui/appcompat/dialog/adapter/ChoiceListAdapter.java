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

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.checkbox.MaterialCheckBox;

import it.dhd.oneplusui.R;


public class ChoiceListAdapter extends BaseAdapter {

    private boolean[] mCheckBoxStates;
    private final Context mContext;
    private boolean[] mDisableStatus;
    private int[] mIcons;
    private final int mLayoutResId;
    private final CharSequence[] mItems;
    private final CharSequence[] mSummaries;
    private final boolean mIsMultiChoice;
    private final int mMaxCheckedNum;
    private MaxCheckedListener mMaxCheckedListener;
    private MultiChoiceItemClickListener mMultiChoiceItemClickListener;

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

    public ChoiceListAdapter(Context context, @LayoutRes int layoutResource, CharSequence[] items, CharSequence[] summaries, boolean[] checkboxStates, boolean isMultiSelect) {
        this(context, layoutResource, items, summaries, checkboxStates, null, isMultiSelect);
    }

    public ChoiceListAdapter(Context context, @LayoutRes int layoutResource, CharSequence[] items, CharSequence[] summaries) {
        this(context, layoutResource, items, summaries, null, false);
    }

    public ChoiceListAdapter(Context context, @LayoutRes int layoutResource, CharSequence[] charSequenceArr, CharSequence[] charSequenceArr2, boolean[] checkboxStates, boolean[] checkboxDisabled, boolean isMultiSelect) {
        this(context, layoutResource, charSequenceArr, charSequenceArr2, checkboxStates, checkboxDisabled, isMultiSelect, 0);
    }

    public ChoiceListAdapter(Context context, @LayoutRes int layoutResource, CharSequence[] items, CharSequence[] summaries, boolean[] checkboxStates, boolean[] checkboxDisabled, boolean isMultiSelect, int maxCheckedItems) {
        this.mContext = context;
        this.mLayoutResId = layoutResource;
        this.mItems = items;
        this.mSummaries = summaries;
        this.mIsMultiChoice = isMultiSelect;
        this.mCheckBoxStates = new boolean[items.length];
        if (checkboxStates != null) {
            initCheckboxStates(checkboxStates);
        }
        this.mDisableStatus = new boolean[this.mItems.length];
        if (checkboxDisabled != null) {
            initCheckboxStatesDisable(checkboxDisabled);
        }
        this.mMaxCheckedNum = maxCheckedItems;
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
    public View getView(final int position, View view, ViewGroup viewGroup) {
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

            if (this.mDisableStatus[position]) {
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
            viewHolder.checkBox.setChecked(this.mCheckBoxStates[position]);
            view2.setOnClickListener(v -> {
                boolean newCheckedState = !viewHolder.checkBox.isChecked();
                viewHolder.checkBox.setChecked(newCheckedState);
                this.mCheckBoxStates[position] = newCheckedState;

                if (newCheckedState && this.mMaxCheckedNum > 0 && getCheckedNum() > this.mMaxCheckedNum) {
                    viewHolder.checkBox.setChecked(false);
                    this.mCheckBoxStates[position] = false;
                    if (this.mMaxCheckedListener != null) {
                        this.mMaxCheckedListener.maxCheckedNotice(this.mMaxCheckedNum);
                    }
                } else if (this.mMultiChoiceItemClickListener != null) {
                    this.mMultiChoiceItemClickListener.onClick(position, newCheckedState);
                }
            });
        } else {
            viewHolder.radioButton.setChecked(this.mCheckBoxStates[position]);
        }

        CharSequence item = getItem(position);
        CharSequence summary = getSummary(position);
        viewHolder.itemText.setText(item);
        if (TextUtils.isEmpty(summary)) {
            viewHolder.summaryText.setVisibility(View.GONE);
        } else {
            viewHolder.summaryText.setVisibility(View.VISIBLE);
            viewHolder.summaryText.setText(summary);
        }

        if (viewHolder.divider != null) {
            viewHolder.divider.setVisibility((getCount() != 1 && position != getCount() - 1) ? View.VISIBLE : View.GONE);
        }

        if (mIcons != null) {
            Drawable drawable = AppCompatResources.getDrawable(mContext, mIcons[position]);
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

    public void setMaxCheckedListener(MaxCheckedListener maxCheckedListener) {
        this.mMaxCheckedListener = maxCheckedListener;
    }

    public void setMultiChoiceItemClickListener(MultiChoiceItemClickListener multiChoiceItemClickListener) {
        this.mMultiChoiceItemClickListener = multiChoiceItemClickListener;
    }

    @Override
    public CharSequence getItem(int i) {
        CharSequence[] charSequenceArr = this.mItems;
        if (charSequenceArr == null) {
            return null;
        }
        return charSequenceArr[i];
    }

}
