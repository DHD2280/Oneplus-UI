package it.dhd.oneplusui.appcompat.checklayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.RelativeLayout;

public class CheckableLayout extends RelativeLayout implements Checkable {
    private Checkable mCheckable;

    public CheckableLayout(Context context) {
        super(context);
    }

    private void setCheckableView(ViewGroup viewGroup) {
        int childCount = viewGroup.getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                View childAt = viewGroup.getChildAt(i);
                if (childAt instanceof ViewGroup) {
                    setCheckableView((ViewGroup) childAt);
                } else if (childAt instanceof Checkable) {
                    this.mCheckable = (Checkable) childAt;
                    return;
                }
            }
        }
    }

    @Override
    public boolean isChecked() {
        Checkable checkable = this.mCheckable;
        return checkable != null && checkable.isChecked();
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        setCheckableView(this);
    }

    @Override
    public void setChecked(boolean z) {
        Checkable checkable = this.mCheckable;
        if (checkable != null) {
            checkable.setChecked(z);
        }
    }

    @Override
    public void toggle() {
        Checkable checkable = this.mCheckable;
        if (checkable != null) {
            checkable.toggle();
        }
    }

    public CheckableLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public CheckableLayout(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
    }
}
