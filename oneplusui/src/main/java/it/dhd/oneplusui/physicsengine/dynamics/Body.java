package it.dhd.oneplusui.physicsengine.dynamics;

import static it.dhd.oneplusui.physicsengine.common.Compat.UNSET_FREQUENCY;

import android.graphics.RectF;

import androidx.annotation.NonNull;

import it.dhd.oneplusui.physicsengine.common.Compat;
import it.dhd.oneplusui.physicsengine.common.Vector;
import it.dhd.oneplusui.physicsengine.dynamics.spring.Edge;
import it.dhd.oneplusui.physicsengine.engine.BaseBehavior;

public class Body {

    @SuppressWarnings("unused")
    public static final int BODY_PROPERTY_ALPHA = 4;
    @SuppressWarnings("unused")
    public static final int BODY_PROPERTY_CUSTOM = 0;
    @SuppressWarnings("unused")
    public static final int BODY_PROPERTY_GROUND = 5;
    @SuppressWarnings("unused")
    public static final int BODY_PROPERTY_POSITION = 1;
    @SuppressWarnings("unused")
    public static final int BODY_PROPERTY_ROTATION = 3;
    @SuppressWarnings("unused")
    public static final int BODY_PROPERTY_SCALE = 2;
    @SuppressWarnings("unused")
    public static final int BODY_TYPE_DYNAMIC = 1;
    @SuppressWarnings("unused")
    public static final int BODY_TYPE_STATIC = 0;
    public float mActiveConstraintFrequency;
    public BaseBehavior mActiveConstraintOwner;
    public RectF mActiveRect;
    public float mDensity;
    public Edge mEdgeList;
    public final Vector mForce;
    boolean mHasSetCenter;
    public float mHeight;
    public final Vector mHookPosition;
    public float mInvMass;
    public boolean mIsAwake;
    boolean mIsSolved;
    public float mLinearDamping;
    public final Vector mLinearVelocity;
    public float mMass;
    public final Vector mMassCenter;
    public Body mNext;
    public RectF mOriginActiveRect;
    public final Vector mOriginPosition;
    public Body mPrev;
    public int mProperty;
    private String mTag;
    public int mType;
    public float mWidth;
    public final Vector mWorldCenter;

    public Body(Vector vector, int type, int property, float width, float height) {
        Vector vector2 = new Vector();
        mOriginPosition = vector2;
        mMassCenter = new Vector();
        mWorldCenter = new Vector();
        mHookPosition = new Vector(0.0f, 0.0f);
        mLinearVelocity = new Vector();
        mForce = new Vector();
        mActiveConstraintOwner = null;
        mIsAwake = false;
        mActiveConstraintFrequency = UNSET_FREQUENCY;
        mHasSetCenter = false;
        mIsSolved = false;
        mTag = "";
        setType(type);
        setProperty(property);
        vector2.set(vector);
        mDensity = 1.0f;
        setSize(width, height);
        mHasSetCenter = true;
        mEdgeList = null;
        mPrev = null;
        mNext = null;
    }

    private void resetMassData() {
        if (mType == 0) {
            setMass(1.0f);
            setLinearDamping(0.0f);
            return;
        }
        setMass(mWidth * mHeight * mDensity);
        setLinearDamping(Compat.calculateLinearDampingByMass(mMass));
        if (!mHasSetCenter || mProperty == 1) {
            mMassCenter.set(mWidth * 0.5f, mHeight * 0.5f);
            mWorldCenter.set(mOriginPosition).addLocal(mMassCenter);
        }
    }

    private void setMass(float f2) {
        if (f2 < 1.0f) {
            f2 = 1.0f;
        }
        mMass = f2;
        mInvMass = 1.0f / f2;
    }

    private void setProperty(int property) {
        mProperty = property;
    }

    private void setType(int type) {
        mType = type;
    }

    public final void applyForceToCenter(Vector vector) {
        if (mType != 1) {
            return;
        }
        Vector vector2 = mForce;
        vector2.mX += vector.mX;
        vector2.mY += vector.mY;
    }

    public void clearActiveConstraint(BaseBehavior baseBehavior) {
        RectF rectF = mOriginActiveRect;
        if (rectF == null || rectF.isEmpty() || mActiveConstraintOwner != baseBehavior) {
            return;
        }
        mOriginActiveRect = null;
        mActiveRect = null;
        setActiveConstraintFrequency(UNSET_FREQUENCY);
    }

    public void clearActiveRect(BaseBehavior baseBehavior) {
        BaseBehavior baseBehavior2;
        RectF rectF = mActiveRect;
        if (rectF == null || (baseBehavior2 = mActiveConstraintOwner) == null || baseBehavior2 != baseBehavior) {
            return;
        }
        rectF.setEmpty();
    }

    public final Vector getHookPosition() {
        return mHookPosition;
    }

    public final float getLinearDamping() {
        return mLinearDamping;
    }

    public final Vector getLinearVelocity() {
        return mLinearVelocity;
    }

    public final float getMass() {
        return mMass;
    }

    public final Vector getPosition() {
        return mOriginPosition;
    }

    public int getProperty() {
        return mProperty;
    }

    public String getTag() {
        return mTag;
    }

    public int getType() {
        return mType;
    }

    public final Vector getWorldCenter() {
        return mWorldCenter;
    }

    public void setActiveConstraintFrequency(float f2) {
        mActiveConstraintFrequency = f2;
    }

    public void setAwake(boolean awake) {
        mIsAwake = awake;
    }

    public void setDensity(float density) {
        mDensity = density;
    }

    public final void setHookPosition(float x, float y) {
        mHookPosition.set(Compat.pixelsToPhysicalSize(x), Compat.pixelsToPhysicalSize(y));
    }

    public final void setLinearDamping(float dampingFactor) {
        mLinearDamping = dampingFactor;
    }

    public final void setLinearVelocity(Vector vector) {
        if (mType == 0) {
            return;
        }
        mLinearVelocity.set(vector);
    }

    public void setOriginActiveRect(RectF rectF) {
        if (rectF == null || rectF.isEmpty()) {
            return;
        }
        if (mOriginActiveRect == null) {
            mOriginActiveRect = new RectF();
        }
        mOriginActiveRect.set(Compat.pixelsToPhysicalSize(rectF.left), Compat.pixelsToPhysicalSize(rectF.top), Compat.pixelsToPhysicalSize(rectF.right), Compat.pixelsToPhysicalSize(rectF.bottom));
    }

    public final void setPosition(Vector vector) {
        mOriginPosition.set(vector);
        mWorldCenter.set(vector).addLocal(mMassCenter);
    }

    public void setSize(float width, float height) {
        mWidth = width;
        mHeight = height;
        resetMassData();
    }

    public void setTag(String str) {
        mTag = str;
    }

    public void synchronizeTransform() {
        mOriginPosition.set(mWorldCenter.mX - mMassCenter.mX, mWorldCenter.mY - mMassCenter.mY);
    }

    @Override
    @NonNull
    public String toString() {
        return "Body{mType=" + mType + ", mProperty=" + mProperty + ", mLinearVelocity=" + mLinearVelocity + ", mLinearDamping=" + mLinearDamping + ", mPosition=" + mOriginPosition + ", mHookPosition=" + mHookPosition + ", mOriginActiveRect=" + mOriginActiveRect + ", mActiveRect=" + mActiveRect + ", mTag='" + mTag + "'}@" + hashCode();
    }

    public void updateActiveConstraintForce() {
        BaseBehavior baseBehavior;
        RectF rectF = mActiveRect;
        if (rectF == null || rectF.isEmpty() || (baseBehavior = mActiveConstraintOwner) == null || baseBehavior.getType() != BaseBehavior.BEHAVIOR_TYPE_DRAG) {
            return;
        }
        RectF rectF2 = mActiveRect;
        float f2 = rectF2.left;
        float f3 = rectF2.right;
        float f4 = rectF2.top;
        float f5 = rectF2.bottom;
        Vector vector = mOriginPosition;
        float f6 = vector.mX;
        if (f6 < f2) {
            mForce.mX = f2 - f6;
        } else if (f6 > f3) {
            mForce.mX = f3 - f6;
        }
        float f7 = vector.mY;
        if (f7 < f4) {
            mForce.mY = f4 - f7;
        } else if (f7 > f5) {
            mForce.mY = f5 - f7;
        }
        float f8 = mActiveConstraintFrequency * 6.2831855f;
        mForce.mulLocal(mMass * f8 * f8 * 1.0f);
    }

    public boolean updateActiveRect(BaseBehavior baseBehavior) {
        if (mOriginActiveRect == null || mOriginActiveRect.isEmpty()) {
            return false;
        }
        mActiveConstraintOwner = baseBehavior;
        if (mActiveRect == null) {
            mActiveRect = new RectF();
        }
        mActiveRect.set(mOriginActiveRect.left + mHookPosition.mX, mOriginActiveRect.top + mHookPosition.mY, mOriginActiveRect.right - (mWidth - mHookPosition.mX), mOriginActiveRect.bottom - (mHeight - mHookPosition.mY));
        return true;
    }
}