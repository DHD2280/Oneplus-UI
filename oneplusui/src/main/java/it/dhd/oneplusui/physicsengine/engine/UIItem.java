package it.dhd.oneplusui.physicsengine.engine;

import androidx.annotation.NonNull;

import it.dhd.oneplusui.physicsengine.common.Vector;

public class UIItem<K> {

    public float mHeight;
    public final Vector mMoveTarget;
    public final Vector mStartPosition;
    public final Vector mStartScale;
    public final Vector mStartVelocity;
    K mTarget;
    final Transform mTransform;
    public float mWidth;

    public UIItem() {
        this(null);
    }

    public Transform getTransform() {
        return this.mTransform;
    }

    public UIItem setSize(float f2, float f3) {
        this.mWidth = f2;
        this.mHeight = f3;
        return this;
    }

    public void setStartPosition(float f2, float f3) {
        this.mStartPosition.set(f2, f3);
    }

    public void setStartScale(float f2, float f3) {
        this.mStartScale.set(f2, f3);
    }

    public void setStartVelocity(float f2, float f3) {
        this.mStartVelocity.set(f2, f3);
    }

    public void setTransformPosition(float f2, float f3) {
        Transform transform = this.mTransform;
        transform.x = f2;
        transform.y = f3;
    }

    public void setTransformScale(float f2) {
        setTransformScale(f2, f2);
    }

    @Override
    @NonNull
    public String toString() {
        return "UIItem{mTarget=" + this.mTarget + ", size=( " + this.mWidth + "," + this.mHeight + "), startPos =:" + this.mStartPosition + ", startVel =:" + this.mStartVelocity + "}@" + hashCode();
    }

    public UIItem(K k2) {
        this.mWidth = 0.0f;
        this.mHeight = 0.0f;
        this.mMoveTarget = new Vector();
        this.mStartPosition = new Vector();
        this.mStartScale = new Vector(1.0f, 1.0f);
        this.mStartVelocity = new Vector();
        this.mTransform = new Transform();
        this.mTarget = k2;
    }

    public void setTransformScale(float f2, float f3) {
        Transform transform = this.mTransform;
        transform.scaleX = f2;
        transform.scaleY = f3;
    }
}