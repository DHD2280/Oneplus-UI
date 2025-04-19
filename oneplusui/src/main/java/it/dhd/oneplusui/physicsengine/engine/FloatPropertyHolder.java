package it.dhd.oneplusui.physicsengine.engine;

public abstract class FloatPropertyHolder<T> {

    public static final int PROPERTY_TYPE_ALPHA = 4;
    public static final int PROPERTY_TYPE_CUSTOM = 0;
    public static final int PROPERTY_TYPE_POSITION = 1;
    public static final int PROPERTY_TYPE_ROTATION = 3;
    public static final int PROPERTY_TYPE_SCALE = 2;
    public boolean mIsStartValueSet;
    public String mPropertyName;
    public int mPropertyType;
    float mStartValue;
    public float mValueThreshold;

    public FloatPropertyHolder(String str) {
        this(str, 1);
    }

    public abstract float getValue(T t2);

    public abstract void onValueSet(T t2, float f2);

    public FloatPropertyHolder<?> setPropertyType(int i2) {
        this.mPropertyType = i2;
        return this;
    }

    public FloatPropertyHolder<?> setStartValue(float f2) {
        this.mStartValue = f2;
        this.mIsStartValueSet = true;
        return this;
    }

    public void setValue(T t2, float f2) {
        onValueSet(t2, f2 * this.mValueThreshold);
    }

    public FloatPropertyHolder<?> setValueThreshold(float valueThreshold) {
        this.mValueThreshold = valueThreshold;
        return this;
    }

    public void verifyStartValue(T t2) {
        if (this.mIsStartValueSet) {
            return;
        }
        this.mStartValue = getValue(t2);
    }

    public FloatPropertyHolder(String str, int propertyType) {
        this.mIsStartValueSet = false;
        float threshold = 1.0f;
        if (propertyType != PROPERTY_TYPE_POSITION) {
            if (propertyType == PROPERTY_TYPE_SCALE) {
                threshold = 0.002f;
            } else if (propertyType == PROPERTY_TYPE_ROTATION) {
                threshold = 0.1f;
            }
        }
        this.mPropertyName = str;
        this.mPropertyType = propertyType;
        setValueThreshold(threshold);
    }

    public FloatPropertyHolder(String str, float valueThreshold) {
        this.mIsStartValueSet = false;
        this.mPropertyName = str;
        this.mValueThreshold = valueThreshold;
        this.mPropertyType = PROPERTY_TYPE_CUSTOM;
    }

    public void update(UIItem<?> t2) {}
}