package it.dhd.oneplusui.appcompat.edittext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class OplusScrolledEditText extends OplusEditText {

    private int mMaxHeight;

    public OplusScrolledEditText(Context context) {
        super(context);
    }

    public OplusScrolledEditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public OplusScrolledEditText(Context context, AttributeSet attributeSet, int defStyleRes) {
        super(context, attributeSet, defStyleRes);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            this.mMaxHeight = (getLineHeight() * getMaxLines()) + getPaddingTop() + getPaddingBottom();
            if (getHeight() >= this.mMaxHeight && getLineCount() > 1) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
        }
        return super.onTouchEvent(motionEvent);
    }
}