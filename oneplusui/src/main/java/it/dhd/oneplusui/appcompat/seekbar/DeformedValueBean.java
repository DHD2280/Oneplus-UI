package it.dhd.oneplusui.appcompat.seekbar;

public class DeformedValueBean {

    private float mHeightBottomDeformedDownValue;
    private float mHeightBottomDeformedUpValue;
    private float mHeightTopDeformedDownValue;
    private float mHeightTopDeformedUpValue;
    private float mProgress;
    private float mScale;
    private float mWidthDeformedValue;

    public DeformedValueBean(float f2, float f3, float f4, float f5, float f6, float i2) {
        this.mHeightBottomDeformedUpValue = f2;
        this.mHeightTopDeformedUpValue = f3;
        this.mWidthDeformedValue = f4;
        this.mHeightBottomDeformedDownValue = f5;
        this.mHeightTopDeformedDownValue = f6;
        this.mProgress = i2;
    }

    public float getHeightBottomDeformedDownValue() {
        return this.mHeightBottomDeformedDownValue;
    }

    public void setHeightBottomDeformedDownValue(float f2) {
        this.mHeightBottomDeformedDownValue = f2;
    }

    public float getHeightBottomDeformedUpValue() {
        return this.mHeightBottomDeformedUpValue;
    }

    public void setHeightBottomDeformedUpValue(float f2) {
        this.mHeightBottomDeformedUpValue = f2;
    }

    public float getHeightTopDeformedDownValue() {
        return this.mHeightTopDeformedDownValue;
    }

    public void setHeightTopDeformedDownValue(float f2) {
        this.mHeightTopDeformedDownValue = f2;
    }

    public float getHeightTopDeformedUpValue() {
        return this.mHeightTopDeformedUpValue;
    }

    public void setHeightTopDeformedUpValue(float f2) {
        this.mHeightTopDeformedUpValue = f2;
    }

    public float getProgress() {
        return this.mProgress;
    }

    public void setProgress(int progress) {
        this.mProgress = progress;
    }

    public float getScale() {
        return this.mScale;
    }

    public void setScale(float scale) {
        this.mScale = scale;
    }

    public float getWidthDeformedValue() {
        return this.mWidthDeformedValue;
    }

    public void setWidthDeformedValue(float widthDeformedValue) {
        this.mWidthDeformedValue = widthDeformedValue;
    }
}
