package it.dhd.oneplusui.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroupAdapter;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.OplusRecyclerView;
import androidx.recyclerview.widget.RecyclerView;

@SuppressLint("RestrictedApi")
public class OplusPreferenceItemDecoration extends OplusRecyclerView.OplusDividerItemDecoration {

    private final int[] mChildLocation;
    private final int[] mItemLocation;
    private PreferenceScreen mPreferenceScreen;

    public OplusPreferenceItemDecoration(Context context, @NonNull PreferenceScreen preferenceScreen) {
        super(context);
        this.mItemLocation = new int[2];
        this.mChildLocation = new int[2];
        this.mPreferenceScreen = preferenceScreen;
    }

    @Override
    public int getDividerInsetEnd(RecyclerView recyclerView, int i) {
        int width;
        int width2;
        if (this.mPreferenceScreen == null) {
            return super.getDividerInsetEnd(recyclerView, i);
        }
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (adapter instanceof PreferenceGroupAdapter) {
            View childAt = recyclerView.getChildAt(i);
            Preference item = ((PreferenceGroupAdapter) adapter).getItem(recyclerView.getChildAdapterPosition(childAt));
            if (item != null && (item instanceof OplusRecyclerView.IOplusDividerDecorationInterface iCOUIDividerDecorationInterface)) {
                boolean isRtl = childAt.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
                View dividerEndAlignView = iCOUIDividerDecorationInterface.getDividerEndAlignView();
                if (dividerEndAlignView != null) {
                    childAt.getLocationInWindow(this.mItemLocation);
                    dividerEndAlignView.getLocationInWindow(this.mChildLocation);
                    if (isRtl) {
                        width = this.mChildLocation[0] + dividerEndAlignView.getPaddingEnd();
                        width2 = this.mItemLocation[0];
                    } else {
                        width = this.mItemLocation[0] + childAt.getWidth();
                        width2 = (this.mChildLocation[0] + dividerEndAlignView.getWidth()) - dividerEndAlignView.getPaddingEnd();
                    }
                    return width - width2;
                }
                return iCOUIDividerDecorationInterface.getDividerEndInset();
            }
        }
        return super.getDividerInsetStart(recyclerView, i);
    }

    @Override
    public int getDividerInsetStart(RecyclerView recyclerView, int i) {
        int paddingStart;
        int i2;
        if (this.mPreferenceScreen == null) {
            return super.getDividerInsetStart(recyclerView, i);
        }
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (adapter instanceof PreferenceGroupAdapter) {
            View childAt = recyclerView.getChildAt(i);
            Preference item = ((PreferenceGroupAdapter) adapter).getItem(recyclerView.getChildAdapterPosition(childAt));
            if (item != null && (item instanceof OplusRecyclerView.IOplusDividerDecorationInterface oplusDividerDecorationInterface)) {
                boolean isRtl = childAt.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
                View dividerStartAlignView = oplusDividerDecorationInterface.getDividerStartAlignView();
                if (dividerStartAlignView != null) {
                    childAt.getLocationInWindow(this.mItemLocation);
                    dividerStartAlignView.getLocationInWindow(this.mChildLocation);
                    if (isRtl) {
                        paddingStart = this.mItemLocation[0] + childAt.getWidth();
                        i2 = (this.mChildLocation[0] + dividerStartAlignView.getWidth()) - dividerStartAlignView.getPaddingStart();
                    } else {
                        paddingStart = this.mChildLocation[0] + dividerStartAlignView.getPaddingStart();
                        i2 = this.mItemLocation[0];
                    }
                    return paddingStart - i2;
                }
                return oplusDividerDecorationInterface.getDividerStartInset();
            }
        }
        return super.getDividerInsetStart(recyclerView, i);
    }

    public PreferenceScreen getPreferenceScreen() {
        return this.mPreferenceScreen;
    }

    public void onDestroy() {
        this.mPreferenceScreen = null;
    }

    @Override
    public boolean shouldDrawDivider(RecyclerView recyclerView, int i) {
        Preference item;
        if (this.mPreferenceScreen == null) {
            return false;
        }
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (!(adapter instanceof PreferenceGroupAdapter) || (item = ((PreferenceGroupAdapter) adapter).getItem(recyclerView.getChildAdapterPosition(recyclerView.getChildAt(i)))) == null || !(item instanceof OplusRecyclerView.IOplusDividerDecorationInterface)) {
            return false;
        }
        return ((OplusRecyclerView.IOplusDividerDecorationInterface) item).drawDivider();
    }
}