package it.dhd.oneplusui.appcompat.edittext;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.EditText;

import androidx.core.view.GravityCompat;

import java.util.ArrayList;
import java.util.Locale;

public class OplusCutoutDrawable extends GradientDrawable {

    private final RectF mCutoutBounds;
    private final Paint mCutoutPaint = new Paint(Paint.CURSOR_AT_OR_AFTER);
    private int mSavedLayer;

    public OplusCutoutDrawable() {
        setPaintStyles();
        mCutoutBounds = new RectF();
    }

    private void postDraw(Canvas canvas) {
        if (useHardwareLayer(getCallback())) {
            return;
        }
        canvas.restoreToCount(mSavedLayer);
    }

    private void preDraw(Canvas canvas) {
        Callback callback = getCallback();
        if (useHardwareLayer(callback)) {
            ((View) callback).setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            saveCanvasLayer(canvas);
        }
    }

    private void saveCanvasLayer(Canvas canvas) {
        mSavedLayer = canvas.saveLayer(0.0f, 0.0f, canvas.getWidth(), canvas.getHeight(), null);
    }

    private void setPaintStyles() {
        mCutoutPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mCutoutPaint.setColor(-1);
        mCutoutPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
    }

    private boolean useHardwareLayer(Callback callback) {
        return callback instanceof View;
    }

    @Override
    public void draw(Canvas canvas) {
        preDraw(canvas);
        super.draw(canvas);
        canvas.drawRect(mCutoutBounds, mCutoutPaint);
        postDraw(canvas);
    }

    public RectF getCutout() {
        return mCutoutBounds;
    }

    public void setCutout(RectF rectF) {
        setCutout(rectF.left, rectF.top, rectF.right, rectF.bottom);
    }

    public boolean hasCutout() {
        return !mCutoutBounds.isEmpty();
    }

    public void removeCutout() {
        setCutout(0.0f, 0.0f, 0.0f, 0.0f);
    }

    public void setCutout(float left, float top, float right, float bottom) {
        if (left == mCutoutBounds.left && top == mCutoutBounds.top && right == mCutoutBounds.right && bottom == mCutoutBounds.bottom) {
            return;
        }
        mCutoutBounds.set(left, top, right, bottom);
        invalidateSelf();
    }

    public static final class OplusCollapseTextHelper {

        public static final int DEFAULT_HINT_LINES = 1;
        public static final int MAX_HINT_LINES = 3;
        private static final boolean DEBUG_DRAW = false;
        private static final float POINT_001 = 0.001f;
        private static final float SCALE_MY = 1.3f;
        private static final String TAG = "OplusCollapseTextHelper";
        private static final boolean USE_SCALING_TEXTURE = false;
        private static final Paint DEBUG_DRAW_PAINT = null;
        private final Rect mCollapsedBounds;
        private final RectF mCurrentBounds;
        private final Rect mExpandedBounds;
        private final TextPaint mTextPaint;
        private final TextPaint mTmpPaint;
        private final View mView;
        private boolean mBoundsChanged;
        private float mCollapsedDrawX;
        private float mCollapsedDrawY;
        private ColorStateList mCollapsedTextColor;
        private float mCurrentDrawX;
        private float mCurrentDrawY;
        private float mCurrentTextSize;
        private boolean mDrawTitle;
        private float mExpandedDrawX;
        private float mExpandedDrawY;
        private float mExpandedFraction;
        private ColorStateList mExpandedTextColor;
        private Bitmap mExpandedTitleTexture;
        private float mHintPaddingStart;
        private boolean mIsRtl;
        private Interpolator mPositionInterpolator;
        private float mScale;
        private int[] mState;
        private CharSequence mText;
        private Interpolator mTextSizeInterpolator;
        private CharSequence mTextToDraw;
        private float mTextureAscent;
        private float mTextureDescent;
        private Paint mTexturePaint;
        private boolean mUseTexture;
        private int mExpandedTextGravity = 16;
        private int mCollapsedTextGravity = 16;
        private float mExpandedTextSize = 30.0f;
        private float mCollapsedTextSize = 30.0f;
        private final ArrayList<CharSequence> mTextToDrawList = new ArrayList<>();
        private int mHintLines = 1;

        public OplusCollapseTextHelper(View view) {
            mView = view;
            mTextPaint = new TextPaint(129);
            mTmpPaint = new TextPaint(mTextPaint);
            mCollapsedBounds = new Rect();
            mExpandedBounds = new Rect();
            mCurrentBounds = new RectF();
        }

        private static int blendColors(int i2, int i3, float f2) {
            float f3 = 1.0f - f2;
            return Color.argb((int) ((Color.alpha(i2) * f3) + (Color.alpha(i3) * f2)), (int) ((Color.red(i2) * f3) + (Color.red(i3) * f2)), (int) ((Color.green(i2) * f3) + (Color.green(i3) * f2)), (int) ((Color.blue(i2) * f3) + (Color.blue(i3) * f2)));
        }

        private static boolean isClose(float f2, float f3) {
            return Math.abs(f2 - f3) < POINT_001;
        }

        private static float lerp(float f2, float f3, float f4) {
            return f2 + (f4 * (f3 - f2));
        }

        private static boolean rectEquals(Rect rect, int left, int top, int right, int bottom) {
            return rect.left == left && rect.top == top && rect.right == right && rect.bottom == bottom;
        }

        private static float lerp(float f2, float f3, float f4, Interpolator interpolator) {
            if (interpolator != null) {
                f4 = interpolator.getInterpolation(f4);
            }
            return lerp(f2, f3, f4);
        }

        private void calculateBaseOffsets() {
            calculateUsingTextSize(mCollapsedTextSize);
            float measureText = mTextToDraw != null ? mTextPaint.measureText(mTextToDraw, 0, mTextToDraw.length()) : 0.0f;
            int absoluteGravity = GravityCompat.getAbsoluteGravity(mCollapsedTextGravity, mIsRtl ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
            if (mHintLines <= 1) {
                int verticalGravity = absoluteGravity & Gravity.VERTICAL_GRAVITY_MASK;
                if (verticalGravity != Gravity.TOP) {
                    if (verticalGravity != Gravity.BOTTOM) {
                        mCollapsedDrawY = mCollapsedBounds.centerY() + (((mTextPaint.descent() - mTextPaint.ascent()) / 2.0f) - mTextPaint.descent());
                    } else {
                        mCollapsedDrawY = mCollapsedBounds.bottom;
                    }
                } else if (Locale.getDefault().getLanguage().equals("my")) {
                    mCollapsedDrawY = mCollapsedBounds.top - (mTextPaint.ascent() * SCALE_MY);
                } else {
                    mCollapsedDrawY = mCollapsedBounds.top - mTextPaint.ascent();
                }
            } else if (Locale.getDefault().getLanguage().equals("my")) {
                mCollapsedDrawY = mCollapsedBounds.top - (mTextPaint.ascent() * SCALE_MY);
            } else {
                mCollapsedDrawY = mCollapsedBounds.top - mTextPaint.ascent();
            }
            int horizontalGravity = absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK;
            if (horizontalGravity == Gravity.CENTER_HORIZONTAL) {
                mCollapsedDrawX = mCollapsedBounds.centerX() - (measureText / 2.0f);
            } else if (horizontalGravity == Gravity.END) {
                mCollapsedDrawX = mCollapsedBounds.right - measureText;
            } else {
                mCollapsedDrawX = mCollapsedBounds.left;
            }
            // Expanded
            calculateUsingTextSize(mExpandedTextSize);
            float measureText2 = mTextToDraw != null ? mTextPaint.measureText(mTextToDraw, 0, mTextToDraw.length()) : 0.0f;
            int absoluteGravity2 = GravityCompat.getAbsoluteGravity(mExpandedTextGravity, mIsRtl ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
            if (mHintLines > 1) {
                mExpandedDrawY = mExpandedBounds.top - mTextPaint.ascent();
            } else {
                int verticalGravity2 = absoluteGravity2 & Gravity.VERTICAL_GRAVITY_MASK;
                if (verticalGravity2 == Gravity.TOP) {
                    mExpandedDrawY = mExpandedBounds.top - mTextPaint.ascent();
                } else if (verticalGravity2 != Gravity.BOTTOM) {
                    mExpandedDrawY = mExpandedBounds.centerY() + (((mTextPaint.getFontMetrics().bottom - mTextPaint.getFontMetrics().top) / 2.0f) - mTextPaint.getFontMetrics().bottom);
                } else {
                    mExpandedDrawY = mExpandedBounds.bottom;
                }
            }
            int horizontalGravity2 = absoluteGravity2 & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK;
            if (horizontalGravity2 == Gravity.CENTER_HORIZONTAL) {
                mExpandedDrawX = mExpandedBounds.centerX() - (measureText2 / 2.0f);
            } else if (horizontalGravity2 == Gravity.END) {
                mExpandedDrawX = mExpandedBounds.right - measureText2;
            } else {
                mExpandedDrawX = mExpandedBounds.left;
            }
            clearTexture();
            setInterpolatedTextSize(mCurrentTextSize);
        }

        private void calculateCurrentOffsets() {
            calculateOffsets(mExpandedFraction);
        }

        private boolean calculateIsRtl(CharSequence charSequence) {
            return isRtlMode();
        }

        private void calculateOffsets(float f2) {
            interpolateBounds(f2);
            mCurrentDrawX = lerp(mExpandedDrawX, mCollapsedDrawX, f2, mPositionInterpolator);
            mCurrentDrawY = lerp(mExpandedDrawY, mCollapsedDrawY, f2, mPositionInterpolator);
            setInterpolatedTextSize(lerp(mExpandedTextSize, mCollapsedTextSize, f2, mTextSizeInterpolator));
            if (mCollapsedTextColor != mExpandedTextColor) {
                mTextPaint.setColor(blendColors(getCurrentExpandedTextColor(), getCurrentCollapsedTextColor(), f2));
            } else {
                mTextPaint.setColor(getCurrentCollapsedTextColor());
            }
            mView.postInvalidate();
        }

        private void calculateUsingTextSize(float f2) {
            float f3;
            boolean z2;
            if (mText == null) {
                return;
            }
            float width = mCollapsedBounds.width();
            float width2 = mExpandedBounds.width();
            if (isClose(f2, mCollapsedTextSize)) {
                f3 = mCollapsedTextSize;
                mScale = 1.0f;
            } else {
                float f4 = mExpandedTextSize;
                if (isClose(f2, f4)) {
                    mScale = 1.0f;
                } else {
                    mScale = f2 / mExpandedTextSize;
                }
                float f5 = mCollapsedTextSize / mExpandedTextSize;
                width = width2 * f5 > width ? Math.min(width / f5, width2) : width2;
                f3 = f4;
            }
            if (width > 0.0f) {
                z2 = mCurrentTextSize != f3 || mBoundsChanged;
                mCurrentTextSize = f3;
                mBoundsChanged = false;
            } else {
                z2 = false;
            }
            if (mTextToDraw == null || z2) {
                mTextPaint.setTextSize(mCurrentTextSize);
                mTextPaint.setLinearText(mScale != 1.0f);
                CharSequence ellipsize = TextUtils.ellipsize(mText, mTextPaint, width - mHintPaddingStart, TextUtils.TruncateAt.END);
                if (!TextUtils.equals(ellipsize, mTextToDraw)) {
                    mTextToDraw = ellipsize;
                }
                if (mHintLines > 1 && !TextUtils.equals(ellipsize, mText) && mText.length() > ellipsize.length()) {
                    mTextToDrawList.clear();
                    int length = ellipsize.length();
                    if (TextUtils.equals(ellipsize, TextUtils.ellipsize(mText.subSequence(0, length), mTextPaint, width - mHintPaddingStart, TextUtils.TruncateAt.END))) {
                        length--;
                    }
                    mTextToDrawList.add(mText.subSequence(0, length));
                    CharSequence charSequence = mText;
                    setTextToDrawList(charSequence.subSequence(length, charSequence.length()), width - mHintPaddingStart);
                }
            }
            mIsRtl = isRtlMode();
        }

        private void clearTexture() {
            Bitmap bitmap = mExpandedTitleTexture;
            if (bitmap != null) {
                bitmap.recycle();
                mExpandedTitleTexture = null;
            }
        }

        private float constrain(float f2, float f3, float f4) {
            return f2 < f3 ? f3 : f2 > f4 ? f4 : f2;
        }

        private void ensureExpandedTexture() {
            if (mExpandedTitleTexture != null || mExpandedBounds.isEmpty() || TextUtils.isEmpty(mTextToDraw)) {
                return;
            }
            calculateOffsets(0.0f);
            mTextureAscent = mTextPaint.ascent();
            mTextureDescent = mTextPaint.descent();
            int round = Math.round(mTextPaint.measureText(mTextToDraw, 0, mTextToDraw.length()));
            int round2 = Math.round(mTextureDescent - mTextureAscent);
            if (round <= 0 || round2 <= 0) {
                return;
            }
            mExpandedTitleTexture = Bitmap.createBitmap(round, round2, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mExpandedTitleTexture);
            canvas.drawText(mTextToDraw, 0, mTextToDraw.length(), 0.0f, round2 - mTextPaint.descent(), mTextPaint);
            if (mTexturePaint == null) {
                mTexturePaint = new Paint(Paint.CURSOR_AT_OR_BEFORE);
            }
        }

        private int getCurrentExpandedTextColor() {
            int[] iArr = mState;
            return iArr != null ? mExpandedTextColor.getColorForState(iArr, 0) : mExpandedTextColor.getDefaultColor();
        }

        private void getTextPaintCollapsed(TextPaint textPaint) {
            textPaint.setTextSize(mCollapsedTextSize);
        }

        private void interpolateBounds(float f2) {
            mCurrentBounds.left = lerp(mExpandedBounds.left, mCollapsedBounds.left, f2, mPositionInterpolator);
            mCurrentBounds.top = lerp(mExpandedDrawY, mCollapsedDrawY, f2, mPositionInterpolator);
            mCurrentBounds.right = lerp(mExpandedBounds.right, mCollapsedBounds.right, f2, mPositionInterpolator);
            mCurrentBounds.bottom = lerp(mExpandedBounds.bottom, mCollapsedBounds.bottom, f2, mPositionInterpolator);
        }

        private boolean isRtlMode() {
            return mView.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        }

        private void onBoundsChanged() {
            mDrawTitle = mCollapsedBounds.width() > 0 && mCollapsedBounds.height() > 0 && mExpandedBounds.width() > 0 && mExpandedBounds.height() > 0;
        }

        private void setInterpolatedTextSize(float f2) {
            calculateUsingTextSize(f2);
            boolean z2 = USE_SCALING_TEXTURE && mScale != 1.0f;
            mUseTexture = z2;
            if (z2) {
                ensureExpandedTexture();
            }
            mView.postInvalidate();
        }

        private void setTextToDrawList(CharSequence charSequence, float f2) {
            for (int i2 = 1; i2 < mHintLines; i2++) {
                CharSequence ellipsize = TextUtils.ellipsize(charSequence, mTextPaint, f2, TextUtils.TruncateAt.END);
                if (i2 == mHintLines - 1 || TextUtils.equals(ellipsize, charSequence)) {
                    mTextToDrawList.add(ellipsize);
                    return;
                }
                int length = ellipsize.length();
                if (TextUtils.equals(ellipsize, TextUtils.ellipsize(charSequence.subSequence(0, length), mTextPaint, f2, TextUtils.TruncateAt.END))) {
                    length--;
                }
                mTextToDrawList.add(charSequence.subSequence(0, length));
                charSequence = charSequence.subSequence(length, charSequence.length());
            }
        }

        public float calculateCollapsedTextWidth() {
            if (mText == null) {
                return 0.0f;
            }
            getTextPaintCollapsed(mTmpPaint);
            TextPaint textPaint = mTmpPaint;
            CharSequence charSequence = mText;
            return textPaint.measureText(charSequence, 0, charSequence.length());
        }

        public void draw(Canvas canvas) {
            float ascent;
            int save = canvas.save();
            if (mTextToDraw == null || !mDrawTitle) {
                canvas.drawText(" ", 0.0f, 0.0f, mTextPaint);
            } else {
                float f2 = mCurrentDrawX;
                float f3 = mCurrentDrawY;
                boolean z2 = mUseTexture && mExpandedTitleTexture != null;
                if (z2) {
                    ascent = mTextureAscent * mScale;
                } else {
                    ascent = mTextPaint.ascent() * mScale;
                    mTextPaint.descent();
                }
                if (z2) {
                    f3 += ascent;
                }
                float f4 = f3;
                float f5 = mScale;
                if (f5 != 1.0f) {
                    canvas.scale(f5, f5, f2, f4);
                }
                if (z2) {
                    canvas.drawBitmap(mExpandedTitleTexture, f2, f4, mTexturePaint);
                } else if (mHintLines != 1 && mTextToDrawList.size() > 1) {
                    View view = mView;
                    int lineHeight = view instanceof EditText ? ((EditText) view).getLineHeight() : 0;
                    for (int i2 = 0; i2 < mTextToDrawList.size(); i2++) {
                        int i3 = lineHeight * i2;
                        CharSequence charSequence = mTextToDrawList.get(i2);
                        if (isRtlMode()) {
                            canvas.drawText(charSequence, 0, charSequence.length(), Math.max(0.0f, f2 - mHintPaddingStart), f4 + i3, mTextPaint);
                        } else {
                            canvas.drawText(charSequence, 0, charSequence.length(), mHintPaddingStart + f2, f4 + i3, mTextPaint);
                        }
                    }
                } else if (isRtlMode()) {
                    CharSequence charSequence2 = mTextToDraw;
                    canvas.drawText(charSequence2, 0, charSequence2.length(), Math.max(0.0f, f2 - mHintPaddingStart), f4, mTextPaint);
                } else {
                    CharSequence charSequence3 = mTextToDraw;
                    canvas.drawText(charSequence3, 0, charSequence3.length(), mHintPaddingStart + f2, f4, mTextPaint);
                }
            }
            canvas.restoreToCount(save);
        }

        public Rect getCollapsedBounds() {
            return mCollapsedBounds;
        }

        public void getCollapsedTextActualBounds(RectF rectF) {
            boolean calculateIsRtl = calculateIsRtl(mText);
            float calculateCollapsedTextWidth = !calculateIsRtl ? mCollapsedBounds.left : mCollapsedBounds.right - calculateCollapsedTextWidth();
            rectF.left = calculateCollapsedTextWidth;
            Rect rect = mCollapsedBounds;
            rectF.top = rect.top;
            rectF.right = !calculateIsRtl ? calculateCollapsedTextWidth + calculateCollapsedTextWidth() : rect.right;
            rectF.bottom = mCollapsedBounds.top + getCollapsedTextHeight();
        }

        public ColorStateList getCollapsedTextColor() {
            return mCollapsedTextColor;
        }

        public void setCollapsedTextColor(ColorStateList colorStateList) {
            if (mCollapsedTextColor != colorStateList) {
                mCollapsedTextColor = colorStateList;
                recalculate();
            }
        }

        public int getCollapsedTextGravity() {
            return mCollapsedTextGravity;
        }

        public void setCollapsedTextGravity(int gravity) {
            if (mCollapsedTextGravity != gravity) {
                mCollapsedTextGravity = gravity;
                recalculate();
            }
        }

        public float getCollapsedTextHeight() {
            getTextPaintCollapsed(mTmpPaint);
            return Locale.getDefault().getLanguage().equals("my") ? (-mTmpPaint.ascent()) * SCALE_MY : -mTmpPaint.ascent();
        }

        public float getCollapsedTextSize() {
            return mCollapsedTextSize;
        }

        public void setCollapsedTextSize(float f2) {
            if (mCollapsedTextSize != f2) {
                mCollapsedTextSize = f2;
                recalculate();
            }
        }

        public int getCurrentCollapsedTextColor() {
            if (mCollapsedTextColor == null) {
                return 0;
            }
            int[] iArr = mState;
            return iArr != null ? mCollapsedTextColor.getColorForState(iArr, 0) : mCollapsedTextColor.getDefaultColor();
        }

        public Rect getExpandedBounds() {
            return mExpandedBounds;
        }

        public float getExpandedFraction() {
            return mExpandedFraction;
        }

        public ColorStateList getExpandedTextColor() {
            return mExpandedTextColor;
        }

        public void setExpandedTextColor(ColorStateList colorStateList) {
            if (mExpandedTextColor != colorStateList) {
                mExpandedTextColor = colorStateList;
                recalculate();
            }
        }

        public int getExpandedTextGravity() {
            return mExpandedTextGravity;
        }

        public void setExpandedTextGravity(int gravity) {
            if (mExpandedTextGravity != gravity) {
                mExpandedTextGravity = gravity;
                recalculate();
            }
        }

        public float getExpandedTextSize() {
            return mExpandedTextSize;
        }

        public void setExpandedTextSize(float f2) {
            if (mExpandedTextSize != f2) {
                mExpandedTextSize = f2;
                recalculate();
            }
        }

        public float getExpansionFraction() {
            return mExpandedFraction;
        }

        public void setExpansionFraction(float f2) {
            float constrain = constrain(f2, 0.0f, 1.0f);
            if (constrain != mExpandedFraction) {
                mExpandedFraction = constrain;
                calculateCurrentOffsets();
            }
        }

        public float getHintHeight() {
            getTextPaintCollapsed(mTmpPaint);
            float descent = mTmpPaint.descent() - mTmpPaint.ascent();
            return Locale.getDefault().getLanguage().equals("my") ? descent * SCALE_MY : descent;
        }

        public CharSequence getText() {
            return mText;
        }

        public void setText(CharSequence charSequence) {
            if (charSequence == null || !charSequence.equals(mText)) {
                mText = charSequence;
                mTextToDraw = null;
                mTextToDrawList.clear();
                clearTexture();
                recalculate();
            }
        }

        public boolean isStateful() {
            ColorStateList colorStateList;
            ColorStateList colorStateList2 = mCollapsedTextColor;
            return (colorStateList2 != null && colorStateList2.isStateful()) || ((colorStateList = mExpandedTextColor) != null && colorStateList.isStateful());
        }

        public void recalculate() {
            if (mView.getHeight() <= 0 || mView.getWidth() <= 0) {
                return;
            }
            calculateBaseOffsets();
            calculateCurrentOffsets();
        }

        public void setCollapsedBounds(int i2, int i3, int i4, int i5) {
            if (rectEquals(mCollapsedBounds, i2, i3, i4, i5)) {
                return;
            }
            mCollapsedBounds.set(i2, i3, i4, i5);
            mBoundsChanged = true;
            onBoundsChanged();
            Log.d(TAG, "setCollapsedBounds: " + mCollapsedBounds);
        }

        public void setCollapsedTextAppearance(int i2, ColorStateList colorStateList) {
            mCollapsedTextColor = colorStateList;
            mCollapsedTextSize = i2;
            recalculate();
        }

        public void setExpandedBounds(int i2, int i3, int i4, int i5) {
            if (rectEquals(mExpandedBounds, i2, i3, i4, i5)) {
                return;
            }
            mExpandedBounds.set(i2, i3, i4, i5);
            mBoundsChanged = true;
            onBoundsChanged();
            Log.d(TAG, "setExpandedBounds: " + mExpandedBounds);
        }

        public void setHintLines(int i2) {
            mHintLines = Math.min(3, Math.max(1, i2));
        }

        public void setHintPaddingStart(float f2) {
            if (f2 > 0.0f) {
                mHintPaddingStart = f2;
            }
        }

        public void setPositionInterpolator(Interpolator interpolator) {
            mPositionInterpolator = interpolator;
            recalculate();
        }

        public boolean setState(int[] iArr) {
            mState = iArr;
            if (!isStateful()) {
                return false;
            }
            recalculate();
            return true;
        }

        public void setTextSizeInterpolator(Interpolator interpolator) {
            mTextSizeInterpolator = interpolator;
            recalculate();
        }

        public void setTypefaces(Typeface typeface) {
            adaptBoldAndMediumFont(mTextPaint, true);
            adaptBoldAndMediumFont(mTmpPaint, true);
            recalculate();
        }

        private void adaptBoldAndMediumFont(Paint paint, boolean isNormal) {
            if (paint != null) {
                paint.setTypeface(isNormal ? Typeface.create("sans-serif-medium", Typeface.NORMAL) : Typeface.DEFAULT);
            }
        }

        private int constrain(int i2, int i3, int i4) {
            return i2 < i3 ? i3 : i2 > i4 ? i4 : i2;
        }
    }
}
