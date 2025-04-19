package it.dhd.oneplusui.physicsengine.engine;

public class FloatValueHolder extends FloatPropertyHolder<UIItem<?>> {

    static final String FLOAT_VALUE_HOLDER = "floatValue";
    float mValue;

    public FloatValueHolder() {
        this(0.0f);
    }

    public FloatValueHolder(float f2) {
        this(FLOAT_VALUE_HOLDER, f2);
    }

    @Override
    public float getValue(UIItem<?> uIItem) {
        return this.mValue;
    }

    @Override
    public void onValueSet(UIItem<?> uIItem, float f2) {
        this.mValue = f2;
    }

    @Override
    public void update(UIItem<?> uIItem) {
        setValue(uIItem, uIItem.mTransform.x);
    }

    @Override
    public void verifyStartValue(UIItem<?> uIItem) {
        super.verifyStartValue(uIItem);
        uIItem.mStartPosition.mX = this.mStartValue;
    }

    public FloatValueHolder(String str) {
        this(str, 0.0f);
    }

    public FloatValueHolder(String str, float f2) {
        this(str, f2, 1.0f);
    }

    public FloatValueHolder(String str, float f2, float f3) {
        super(str, f3);
        this.mValue = f2;
    }
}