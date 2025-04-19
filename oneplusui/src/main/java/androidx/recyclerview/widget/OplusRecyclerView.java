package androidx.recyclerview.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import it.dhd.oneplusui.R;

/**
 * An extension of RecyclerView
 * This class uses a custom divider to look like Oneplus RecyclerView
 * See {@link RecyclerView} for more information.
 */
@SuppressWarnings("unused")
public class OplusRecyclerView extends RecyclerView {

    public static final String TAG = "OplusRecyclerView";

    public static class OplusDividerItemDecoration extends RecyclerView.ItemDecoration {
        private Drawable mDivider;
        private int mDividerColor;
        private int mDividerStrokeWidth;
        private Paint mPaint;

        public OplusDividerItemDecoration(Context context) {
            init(context);
        }

        private void init(Context context) {
            mDividerColor = ContextCompat.getColor(context, R.color.colorDivider);
            mDividerStrokeWidth = context.getResources().getDimensionPixelOffset(R.dimen.list_divider_height);
            mPaint = new Paint(1);
            mPaint.setColor(mDividerColor);
        }

        public void drawDividerOuterBackground(Canvas canvas, RecyclerView recyclerView, View view) {
        }

        public Drawable getDivider() {
            return mDivider;
        }

        public int getDividerColor() {
            return mDividerColor;
        }

        public int getDividerInsetEnd(RecyclerView recyclerView, int i) {
            return 0;
        }

        public int getDividerInsetStart(RecyclerView recyclerView, int i) {
            return 0;
        }

        public int getDividerStrokeWidth() {
            return mDividerStrokeWidth;
        }

        public Paint getPaint() {
            return mPaint;
        }

        @Override
        public void onDrawOver(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.State state) {
            boolean isRtl;
            int dividerInsetStart;
            int dividerInsetEnd;
            int childCount = recyclerView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = recyclerView.getChildAt(i);
                if (shouldDrawDivider(recyclerView, i)) {
                    drawDividerOuterBackground(canvas, recyclerView, childAt);
                    isRtl = childAt.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
                    int y = (int) (childAt.getY() + childAt.getHeight());
                    int max = Math.max(1, mDividerStrokeWidth) + y;
                    float x = childAt.getX();
                    if (isRtl) {
                        dividerInsetStart = getDividerInsetEnd(recyclerView, i);
                    } else {
                        dividerInsetStart = getDividerInsetStart(recyclerView, i);
                    }
                    int i2 = (int) (x + dividerInsetStart);
                    float x2 = childAt.getX() + childAt.getWidth();
                    if (isRtl) {
                        dividerInsetEnd = getDividerInsetStart(recyclerView, i);
                    } else {
                        dividerInsetEnd = getDividerInsetEnd(recyclerView, i);
                    }
                    int i3 = (int) (x2 - dividerInsetEnd);
                    Drawable drawable = mDivider;
                    if (drawable == null) {
                        canvas.drawRect(i2, y, i3, max, mPaint);
                    } else {
                        drawable.setBounds(i2, y, i3, max);
                        mDivider.draw(canvas);
                    }
                }
            }
        }

        public void setDivider(RecyclerView recyclerView, Drawable drawable) {
            mDivider = drawable;
            if (recyclerView != null) {
                recyclerView.invalidateItemDecorations();
            }
        }

        public void setDividerColor(RecyclerView recyclerView, int i) {
            mDividerColor = i;
            mPaint.setColor(i);
            if (recyclerView != null) {
                recyclerView.invalidateItemDecorations();
            }
        }

        public void setDividerStrokeWidth(RecyclerView recyclerView, int i) {
            mDividerStrokeWidth = i;
            mPaint.setStrokeWidth(i);
            if (recyclerView != null) {
                recyclerView.invalidateItemDecorations();
            }
        }

        public boolean shouldDrawDivider(RecyclerView recyclerView, int i) {
            RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
            return adapter == null || adapter.getItemCount() - 1 != i;
        }
    }

    public static class OplusRecyclerViewItemDecoration extends OplusDividerItemDecoration {

        private final int[] mChildLocation;
        private final int[] mItemLocation;

        public OplusRecyclerViewItemDecoration(Context context) {
            super(context);
            mItemLocation = new int[2];
            mChildLocation = new int[2];
        }

        @Override
        public int getDividerInsetEnd(RecyclerView recyclerView, int i) {
            int width;
            int width2;
            View childAt = recyclerView.getChildAt(i);
            if (childAt != null) {
                RecyclerView.ViewHolder childViewHolder = recyclerView.getChildViewHolder(childAt);
                if (childViewHolder instanceof IOplusDividerDecorationInterface oplusDividerDecorationInterface) {
                    boolean isRtl = childAt.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
                    View dividerEndAlignView = oplusDividerDecorationInterface.getDividerEndAlignView();
                    if (dividerEndAlignView != null) {
                        childAt.getLocationInWindow(mItemLocation);
                        dividerEndAlignView.getLocationInWindow(mChildLocation);
                        if (isRtl) {
                            width = mChildLocation[0] + dividerEndAlignView.getPaddingEnd();
                            width2 = mItemLocation[0];
                        } else {
                            width = mItemLocation[0] + childAt.getWidth();
                            width2 = (mChildLocation[0] + dividerEndAlignView.getWidth()) - dividerEndAlignView.getPaddingEnd();
                        }
                        return width - width2;
                    }
                    return oplusDividerDecorationInterface.getDividerEndInset();
                }
            }
            return super.getDividerInsetEnd(recyclerView, i);
        }

        @Override
        public int getDividerInsetStart(RecyclerView recyclerView, int i) {
            int paddingStart;
            int i2;
            View childAt = recyclerView.getChildAt(i);
            if (childAt != null) {
                RecyclerView.ViewHolder childViewHolder = recyclerView.getChildViewHolder(childAt);
                if (childViewHolder instanceof IOplusDividerDecorationInterface oplusDividerDecorationInterface) {
                    boolean isRtl = childAt.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
                    View dividerStartAlignView = oplusDividerDecorationInterface.getDividerStartAlignView();
                    if (dividerStartAlignView != null) {
                        childAt.getLocationInWindow(mItemLocation);
                        dividerStartAlignView.getLocationInWindow(mChildLocation);
                        if (isRtl) {
                            paddingStart = mItemLocation[0] + childAt.getWidth();
                            i2 = (mChildLocation[0] + dividerStartAlignView.getWidth()) - dividerStartAlignView.getPaddingStart();
                        } else {
                            paddingStart = mChildLocation[0] + dividerStartAlignView.getPaddingStart();
                            i2 = mItemLocation[0];
                        }
                        return paddingStart - i2;
                    }
                    return oplusDividerDecorationInterface.getDividerStartInset();
                }
            }
            return super.getDividerInsetStart(recyclerView, i);
        }

        @Override
        public boolean shouldDrawDivider(RecyclerView recyclerView, int i) {
            View childAt = recyclerView.getChildAt(i);
            if (childAt != null) {
                RecyclerView.ViewHolder childViewHolder = recyclerView.getChildViewHolder(childAt);
                if (childViewHolder instanceof IOplusDividerDecorationInterface) {
                    return ((IOplusDividerDecorationInterface) childViewHolder).drawDivider();
                }
                return true;
            }
            return true;
        }
    }

    /**
     * Interface to implement custom the divider
     */
    public interface IOplusDividerDecorationInterface {

        /**
         * Draw the divider
         * Use your custom logic to draw the divider
         *
         * @return true if the divider should be drawn
         */
        default boolean drawDivider() {
            return false;
        }

        /**
         * Return the view to align the end of the divider
         *
         * @return the view to align the end of the divider
         */
        default View getDividerEndAlignView() {
            return null;
        }

        /**
         * Return the inset for the end of the divider
         *
         * @return the end padding
         */
        default int getDividerEndInset() {
            return 0;
        }

        /**
         * Return the view to align the start of the divider
         *
         * @return the view to align the start of the divider
         */
        default View getDividerStartAlignView() {
            return null;
        }

        /**
         * Return the inset for the start of the divider
         *
         * @return the start padding
         */
        default int getDividerStartInset() {
            return 0;
        }
    }

    public OplusRecyclerView(@NonNull Context context) {
        this(context, null);
    }


    public OplusRecyclerView(@NonNull Context context, @Nullable AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OplusRecyclerView(@NonNull Context context, @Nullable AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }
}