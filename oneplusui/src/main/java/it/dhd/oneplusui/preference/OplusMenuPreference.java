package it.dhd.oneplusui.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceViewHolder;

import android.content.res.TypedArray;
import android.view.ContextThemeWrapper;

import it.dhd.oneplusui.R;
import it.dhd.oneplusui.appcompat.poplist.SimpleMenuPopupWindow;

/**
 * A version of {@link OplusListPreference} that use
 * <a href="https://material.io/guidelines/components/menus.html#menus-simple-menus">Simple Menus</a>
 * in Material Design as drop down.
 */

public class OplusMenuPreference extends OplusListPreference {

    private View mTitleView;
    private View mItemView;
    private SimpleMenuPopupWindow mPopupWindow;

    public OplusMenuPreference(Context context) {
        this(context, null);
    }

    public OplusMenuPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OplusMenuPreference(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, R.style.Preferences_OplusMenuPreference);
    }

    public OplusMenuPreference(Context context, AttributeSet attrs, int defStyleAttr,
                                int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.OplusMenuPreference, defStyleAttr, defStyleRes);

        int popupStyle = a.getResourceId(R.styleable.OplusMenuPreference_android_popupMenuStyle, R.style.Widget_Preference_SimpleMenuPreference_PopupMenu);
        int popupTheme = a.getResourceId(R.styleable.OplusMenuPreference_android_popupTheme, R.style.Widget_App_PopupMenu);
        Context popupContext;
        if (popupTheme != 0) {
            popupContext = new ContextThemeWrapper(context, popupTheme);
        } else {
            popupContext = context;
        }

        mPopupWindow = new SimpleMenuPopupWindow(popupContext, attrs, R.styleable.OplusMenuPreference_android_popupMenuStyle, popupStyle);
        mPopupWindow.setOnItemClickListener(i -> {
            String value = getEntryValues()[i].toString();
            if (callChangeListener(value)) {
                setValue(value);
            }
        });

        a.recycle();
    }

    @Override
    protected void onClick() {
        if (getEntries() == null || getEntries().length == 0) {
            return;
        }

        if (mPopupWindow == null) {
            return;
        }

        mPopupWindow.setEntries(getEntries());
        mPopupWindow.setSelectedIndex(findIndexOfValue(getValue()));

        View container = (View) mItemView   // itemView
                .getParent();               // -> list (RecyclerView)

        mPopupWindow.show(mItemView, container, 0);
    }

    @Override
    public void setEntries(@NonNull CharSequence[] entries) {
        super.setEntries(entries);
        mPopupWindow.requestMeasure();
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder view) {
        super.onBindViewHolder(view);

        mItemView = view.itemView;
        mTitleView = view.itemView.findViewById(android.R.id.title);

    }
}