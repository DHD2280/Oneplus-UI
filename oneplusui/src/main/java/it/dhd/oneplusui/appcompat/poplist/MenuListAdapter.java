package it.dhd.oneplusui.appcompat.poplist;


import static it.dhd.oneplusui.appcompat.poplist.SimpleMenuPopupWindow.DIALOG;
import static it.dhd.oneplusui.appcompat.poplist.SimpleMenuPopupWindow.HORIZONTAL;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.OplusRecyclerView;
import androidx.recyclerview.widget.RecyclerView;

import it.dhd.oneplusui.R;

class MenuListAdapter extends RecyclerView.Adapter<MenuListAdapter.ViewHolder> {

    private SimpleMenuPopupWindow mWindow;
    private static int mDividerDefaultHorizontalPadding;

    public MenuListAdapter(Context context, SimpleMenuPopupWindow window) {
        super();
        mDividerDefaultHorizontalPadding = context.getResources().getDimensionPixelSize(R.dimen.preference_divider_default_horizontal_padding);
        mWindow = window;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.oplus_menu_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.bind(mWindow, position);
        holder.setDrawDivider(position != getItemCount() - 1);
    }

    @Override
    public int getItemCount() {
        return mWindow.getEntries() == null ? 0 : mWindow.getEntries().length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, OplusRecyclerView.IOplusDividerDecorationInterface {

        public TextView mCheckedTextView;
        public ImageView mCheckedMark;
        private boolean drawDivider = true;
        private SimpleMenuPopupWindow mWindow;

        public ViewHolder(View itemView) {
            super(itemView);

            mCheckedTextView = itemView.findViewById(android.R.id.text1);
            mCheckedMark = itemView.findViewById(R.id.check_mark);
            itemView.setOnClickListener(this);
        }

        public void bind(SimpleMenuPopupWindow window, int position) {
            mWindow = window;
            mCheckedTextView.setText(mWindow.getEntries()[position]);
            mCheckedMark.setVisibility(position == mWindow.getSelectedIndex() ? View.VISIBLE : View.GONE);
            mCheckedTextView.setTextColor(position == mWindow.getSelectedIndex() ?
                    ContextCompat.getColor(mCheckedTextView.getContext(), android.R.color.system_accent1_400) :
                    ContextCompat.getColor(mCheckedTextView.getContext(), R.color.text_color_primary));
//            mCheckedTextView.setChecked(position == mWindow.getSelectedIndex());
            mCheckedTextView.setMaxLines(mWindow.getMode() == DIALOG ? Integer.MAX_VALUE : 1);

            int padding = mWindow.listPadding[mWindow.getMode()][HORIZONTAL];
            int paddingVertical = mCheckedTextView.getPaddingTop();
            mCheckedTextView.setPadding(padding, paddingVertical, padding, paddingVertical);
        }

        public void setDrawDivider(boolean drawDivider) {
            this.drawDivider = drawDivider;
        }

        @Override
        public boolean drawDivider() {
            return drawDivider;
        }

        @Override
        public View getDividerEndAlignView() {
            return null;
        }

        @Override
        public int getDividerEndInset() {
            return mDividerDefaultHorizontalPadding;
        }

        @Override
        public View getDividerStartAlignView() {
            return mCheckedTextView;
        }

        @Override
        public int getDividerStartInset() {
            return mDividerDefaultHorizontalPadding;
        }

        @Override
        public void onClick(View view) {
            if (mWindow.getOnItemClickListener() != null) {
                mWindow.getOnItemClickListener().onClick(getAdapterPosition());
            }

            if (mWindow.isShowing()) {
                mWindow.dismiss();
            }
        }
    }
}
