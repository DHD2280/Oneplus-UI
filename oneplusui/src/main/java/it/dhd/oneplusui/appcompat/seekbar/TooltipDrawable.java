package it.dhd.oneplusui.appcompat.seekbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import androidx.annotation.StyleableRes;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.internal.TextDrawableHelper;
import com.google.android.material.internal.TextDrawableHelper.TextDrawableDelegate;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.resources.TextAppearance;
import com.google.android.material.shape.EdgeTreatment;
import com.google.android.material.shape.MarkerEdgeTreatment;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.OffsetEdgeTreatment;

import it.dhd.oneplusui.R;


/**
 * A Tooltip based on {@link com.google.android.material.tooltip.TooltipDrawable} that supports shape theming and draws a pointer on the bottom in the center of the
 * supplied bounds. Additional margin can be applied which will prevent the main bubble of the
 * Tooltip from being drawn too close to the edge of the window.
 *
 * <p>Note: {@link #setRelativeToView(View)} should be called so {@code TooltipDrawable3} can
 * calculate where it is being drawn within the visible display.
 *
 */
public class TooltipDrawable extends MaterialShapeDrawable implements TextDrawableDelegate {

    @StyleRes
    private static final int DEFAULT_STYLE = R.style.Widget_Oplus_Tooltip;
    @AttrRes
    private static final int DEFAULT_THEME_ATTR = R.attr.oplusTooltipStyle;

    @Nullable
    private final FontMetrics fontMetrics = new FontMetrics();

    @SuppressLint("RestrictedApi")
    @NonNull
    private final TextDrawableHelper textDrawableHelper =
            new TextDrawableHelper(/* delegate= */ this);

    @NonNull
    private final Rect displayFrame = new Rect();
    private final Context mContext;
    private String mText = "";

    private int padding;
    private int minWidth;
    private int minHeight;
    private int layoutMargin;
    private boolean showMarker;
    private int arrowSize;
    @SuppressLint("RestrictedApi")
    private TextAppearance mTextAppearance;

    private float tooltipScaleX = 1F;
    private float tooltipScaleY = 1F;
    private float tooltipPivotX = 0.5F;
    private float tooltipPivotY = 0.5F;
    private float labelOpacity = 1.0F;
    private int locationOnScreenX;

    @NonNull
    private final View.OnLayoutChangeListener attachedViewLayoutChangeListener =
            new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(
                        View v,
                        int left,
                        int top,
                        int right,
                        int bottom,
                        int oldLeft,
                        int oldTop,
                        int oldRight,
                        int oldBottom) {
                    updateLocationOnScreen(v);
                }
            };

    /**
     * Returns a TooltipDrawable3 from the given attributes.
     */
    @NonNull
    public static TooltipDrawable createFromAttributes(
            @NonNull Context context,
            @Nullable AttributeSet attrs,
            @AttrRes int defStyleAttr,
            @StyleRes int defStyleRes) {
        TooltipDrawable tooltip = new TooltipDrawable(context, attrs, defStyleAttr, defStyleRes);
        tooltip.loadFromAttributes(attrs, defStyleAttr, defStyleRes);
        return tooltip;
    }

    /**
     * Returns a TooltipDrawable3 from the given attributes.
     */
    @NonNull
    public static TooltipDrawable createFromAttributes(
            @NonNull Context context, @Nullable AttributeSet attrs) {
        return createFromAttributes(context, attrs, DEFAULT_THEME_ATTR, DEFAULT_STYLE);
    }

    @NonNull
    public static TooltipDrawable create(@NonNull Context context) {
        return createFromAttributes(context, null, DEFAULT_THEME_ATTR, DEFAULT_STYLE);
    }

    @SuppressLint("RestrictedApi")
    private TooltipDrawable(
            @NonNull Context context,
            AttributeSet attrs,
            @AttrRes int defStyleAttr,
            @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
        this.mText = "";

        textDrawableHelper.getTextPaint().density = context.getResources().getDisplayMetrics().density;
        textDrawableHelper.getTextPaint().setTextAlign(Paint.Align.CENTER);
    }

    @SuppressLint("RestrictedApi")
    private void loadFromAttributes(
            @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        TypedArray a =
                mContext.obtainStyledAttributes(
                        attrs, R.styleable.OplusToolTip, defStyleAttr, defStyleRes);

        arrowSize = mContext.getResources().getDimensionPixelSize(com.google.android.material.R.dimen.mtrl_tooltip_arrowSize);
        showMarker = a.getBoolean(R.styleable.OplusToolTip_showMarker, true);
        if (showMarker) {
            setShapeAppearanceModel(
                    getShapeAppearanceModel().toBuilder().setBottomEdge(createMarkerEdge()).build());
        } else {
            arrowSize = 0;
        }

        setText(a.getString(R.styleable.OplusToolTip_android_text));
        TextAppearance textAppearance = MaterialResources.getTextAppearance(
                mContext, a, R.styleable.OplusToolTip_android_textAppearance);
        if (textAppearance != null && a.hasValue(R.styleable.OplusToolTip_android_textAppearance)) {
            textAppearance.setTextColor(
                    MaterialResources.getColorStateList(mContext, a, R.styleable.OplusToolTip_android_textColor));
        }
        mTextAppearance = textAppearance;
        setTextAppearance(mTextAppearance);

        ColorStateList backgroundTint = ColorStateList.valueOf(
                a.getColor(R.styleable.OplusToolTip_backgroundTint,
                        ResourcesCompat.getColor(mContext.getResources(), R.color.oplusColorSurfaces, mContext.getTheme())));
        setFillColor(backgroundTint);


        setStrokeColor(
                ColorStateList.valueOf(
                        a.getColor(R.styleable.OplusToolTip_backgroundTint,
                                ResourcesCompat.getColor(mContext.getResources(), R.color.oplusColorSurfaces, mContext.getTheme()))));

        padding = a.getDimensionPixelSize(R.styleable.OplusToolTip_android_padding, 0);
        minWidth = a.getDimensionPixelSize(R.styleable.OplusToolTip_android_minWidth, 0);
        minHeight = a.getDimensionPixelSize(R.styleable.OplusToolTip_android_minHeight, 0);
        layoutMargin = a.getDimensionPixelSize(R.styleable.OplusToolTip_android_layout_margin, 0);

        a.recycle();
    }

    /**
     * Sets this tooltip's text appearance.
     *
     * @param textAppearance This tooltip's text appearance.
     * @attr ref com.google.android.material.R.styleable#Tooltip_android_textAppearance
     */
    @SuppressLint("RestrictedApi")
    public void setTextAppearance(@Nullable TextAppearance textAppearance) {
        textDrawableHelper.setTextAppearance(textAppearance, mContext);
    }

    /**
     * Returns a TextAppearanceSpan object from the given attributes.
     *
     * <p>You only need this if you are drawing text manually. Normally, TextView takes care of this.
     */
    @SuppressLint("RestrictedApi")
    @Nullable
    public static TextAppearance getTextAppearance(
            @NonNull Context context, @NonNull TypedArray attributes, @StyleableRes int index) {
        if (attributes.hasValue(index)) {
            int resourceId = attributes.getResourceId(index, 0);
            if (resourceId != 0) {
                return new TextAppearance(context, resourceId);
            }
        }
        return null;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        // Update the marker edge since the location of the marker arrow can move depending on the the
        // bounds.
        if (showMarker) {
            setShapeAppearanceModel(
                    getShapeAppearanceModel().toBuilder().setBottomEdge(createMarkerEdge()).build());
        }
    }

    private EdgeTreatment createMarkerEdge() {
        float offset = -calculatePointerOffset();
        // The maximum distance the arrow can be offset before extends outside the bounds.
        float maxArrowOffset = (float) ((getBounds().width() - arrowSize * Math.sqrt(2)) / 2.0f);
        offset = Math.max(offset, -maxArrowOffset);
        offset = Math.min(offset, maxArrowOffset);
        return new OffsetEdgeTreatment(new MarkerEdgeTreatment(arrowSize), offset);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.save();

        // Translate the canvas by the same about that the pointer is offset to keep it pointing at the
        // same place relative to the bounds.
        float translateX = calculatePointerOffset();

        // Handle the extra space created by the arrow notch at the bottom of the tooltip by moving the
        // canvas. This allows the pointing part of the tooltip to align with the bottom of the bounds.
        float translateY = (float) -(arrowSize * Math.sqrt(2) - arrowSize);

        // Scale the tooltip. Use the bounds to set the pivot points relative to this drawable since
        // the supplied canvas is not necessarily the same size.
        canvas.scale(
                tooltipScaleX,
                tooltipScaleY,
                getBounds().left + (getBounds().width() * tooltipPivotX),
                getBounds().top + (getBounds().height() * tooltipPivotY));

        canvas.translate(translateX, translateY);

        // Draw the background.
        super.draw(canvas);

        // Draw the text.
        drawText(canvas);

        canvas.restore();
    }

    @SuppressLint("RestrictedApi")
    private void drawText(@NonNull Canvas canvas) {
        if (mText == null) {
            // If text is null there's nothing to draw.
            return;
        }

        Rect bounds = getBounds();
        int y = (int) calculateTextOriginAndAlignment(bounds);

        if (textDrawableHelper.getTextAppearance() != null) {
            textDrawableHelper.getTextPaint().drawableState = getState();
            textDrawableHelper.updateTextPaintDrawState(mContext);
            textDrawableHelper.getTextPaint().setAlpha((int) (labelOpacity * 255));
        }

        canvas.drawText(mText, 0, mText.length(), bounds.centerX(), y, textDrawableHelper.getTextPaint());
    }

    /**
     * Calculates the text origin and alignment based on the bounds.
     */
    private float calculateTextOriginAndAlignment(@NonNull Rect bounds) {
        return bounds.centerY() - calculateTextCenterFromBaseline();
    }

    /**
     * Calculates the offset from the visual center of the text to its baseline.
     *
     * <p>To draw the text, we provide the origin to {@link Canvas#drawText(CharSequence, int, int,
     * float, float, Paint)}. This origin always corresponds vertically to the text's baseline.
     * Because we need to vertically center the text, we need to calculate this offset.
     *
     * <p>Note that tooltips that share the same font must have consistent text baselines despite
     * having different text strings. This is why we calculate the vertical center using {@link
     * Paint#getFontMetrics(FontMetrics)} rather than {@link Paint#getTextBounds(String, int, int,
     * Rect)}.
     */
    @SuppressLint("RestrictedApi")
    private float calculateTextCenterFromBaseline() {
        textDrawableHelper.getTextPaint().getFontMetrics(fontMetrics);
        return (fontMetrics.descent + fontMetrics.ascent) / 2f;
    }

    private float calculatePointerOffset() {
        float pointerOffset = 0;
        if (displayFrame.right - getBounds().right - locationOnScreenX - layoutMargin < 0) {
            pointerOffset = displayFrame.right - getBounds().right - locationOnScreenX - layoutMargin;
        } else if (displayFrame.left - getBounds().left - locationOnScreenX + layoutMargin > 0) {
            pointerOffset = displayFrame.left - getBounds().left - locationOnScreenX + layoutMargin;
        }
        return pointerOffset;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public int getIntrinsicHeight() {
        return (int) Math.max(textDrawableHelper.getTextPaint().getTextSize(), minHeight);
    }

    @Override
    public int getIntrinsicWidth() {
        return (int) Math.max(2 * padding + getTextWidth(), minWidth);
    }

    @SuppressLint("RestrictedApi")
    private float getTextWidth() {
        if (mText == null) {
            return 0;
        }
        return textDrawableHelper.getTextWidth(mText);
    }

    @SuppressLint("RestrictedApi")
    public void setText(String text) {
        if (!TextUtils.equals(mText, text)) {
            this.mText = text;
            textDrawableHelper.setTextWidthDirty(true);
            invalidateSelf();
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mContext.getResources().getDisplayMetrics());
    }

    /**
     * A fraction that controls the scale of the tooltip and the opacity of its text.
     *
     * <p>When fraction is 0.0, the tooltip will be completely hidden, as fraction approaches 1.0, the
     * tooltip will scale up from its pointer and animate in its text.
     *
     * <p>This method is typically called from within an animator's update callback. The animator in
     * this case is what is driving the animation while this method handles configuring the tooltip's
     * appearance at each frame in the animation.
     *
     * @param fraction A value between 0.0 and 1.0 that defines how "shown" the tooltip will be.
     */
    public void setRevealFraction(@FloatRange(from = 0.0, to = 1.0) float fraction) {
        tooltipScaleX = fraction;
        tooltipScaleY = fraction;
        labelOpacity = lerp(0F, 1F, 0.19F, 1F, fraction);
        invalidateSelf();
    }

    /**
     * Set the pivot points for the tooltip.
     *
     * @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public void setPivots(float pivotX, float pivotY) {
        this.tooltipPivotX = pivotX;
        this.tooltipPivotY = pivotY;
        invalidateSelf();
    }

    private float lerp(
            float outputMin, float outputMax, float inputMin, float inputMax, float value) {
        if (value <= inputMin) {
            return outputMin;
        }
        if (value >= inputMax) {
            return outputMax;
        }

        return lerp(outputMin, outputMax, (value - inputMin) / (inputMax - inputMin));
    }

    private float lerp(float startValue, float endValue, float fraction) {
        return startValue + (fraction * (endValue - startValue));
    }

    private void updateLocationOnScreen(@NonNull View v) {
        int[] locationOnScreen = new int[2];
        v.getLocationOnScreen(locationOnScreen);
        locationOnScreenX = locationOnScreen[0];
        v.getWindowVisibleDisplayFrame(displayFrame);
    }

    /**
     * Should be called to allow this drawable to calculate its position within the current display
     * frame. This allows it to apply to specified window padding.
     *
     * @see #detachView(View)
     */
    public void setRelativeToView(@Nullable View view) {
        if (view == null) {
            return;
        }
        updateLocationOnScreen(view);
        // Listen for changes that indicate the view has moved so the location can be updated
        view.addOnLayoutChangeListener(attachedViewLayoutChangeListener);
    }

    /**
     * Should be called when the view is detached from the screen.
     *
     * @see #setRelativeToView(View)
     */
    public void detachView(@Nullable View view) {
        if (view == null) {
            return;
        }
        view.removeOnLayoutChangeListener(attachedViewLayoutChangeListener);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onTextSizeChange() {
        invalidateSelf();
    }

    @Override
    public boolean onStateChange(int[] state) {
        // Exposed for TextDrawableDelegate.
        return super.onStateChange(state);
    }
}
