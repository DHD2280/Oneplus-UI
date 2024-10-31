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
            this.mDividerColor = ContextCompat.getColor(context, R.color.colorDivider);
            this.mDividerStrokeWidth = context.getResources().getDimensionPixelOffset(R.dimen.list_divider_height);
            Paint paint = new Paint(1);
            this.mPaint = paint;
            paint.setColor(this.mDividerColor);
        }

        public void drawDividerOuterBackground(Canvas canvas, RecyclerView recyclerView, View view) {
        }

        public Drawable getDivider() {
            return this.mDivider;
        }

        public int getDividerColor() {
            return this.mDividerColor;
        }

        public int getDividerInsetEnd(RecyclerView recyclerView, int i) {
            return 0;
        }

        public int getDividerInsetStart(RecyclerView recyclerView, int i) {
            return 0;
        }

        public int getDividerStrokeWidth() {
            return this.mDividerStrokeWidth;
        }

        public Paint getPaint() {
            return this.mPaint;
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
                    int max = Math.max(1, this.mDividerStrokeWidth) + y;
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
                    Drawable drawable = this.mDivider;
                    if (drawable == null) {
                        canvas.drawRect(i2, y, i3, max, this.mPaint);
                    } else {
                        drawable.setBounds(i2, y, i3, max);
                        this.mDivider.draw(canvas);
                    }
                }
            }
        }

        public void setDivider(RecyclerView recyclerView, Drawable drawable) {
            this.mDivider = drawable;
            if (recyclerView != null) {
                recyclerView.invalidateItemDecorations();
            }
        }

        public void setDividerColor(RecyclerView recyclerView, int i) {
            this.mDividerColor = i;
            this.mPaint.setColor(i);
            if (recyclerView != null) {
                recyclerView.invalidateItemDecorations();
            }
        }

        public void setDividerStrokeWidth(RecyclerView recyclerView, int i) {
            this.mDividerStrokeWidth = i;
            this.mPaint.setStrokeWidth(i);
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
            this.mItemLocation = new int[2];
            this.mChildLocation = new int[2];
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

    public interface IOplusDividerDecorationInterface {
        default boolean drawDivider() {
            return false;
        }

        default View getDividerEndAlignView() {
            return null;
        }

        default int getDividerEndInset() {
            return 0;
        }

        default View getDividerStartAlignView() {
            return null;
        }

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