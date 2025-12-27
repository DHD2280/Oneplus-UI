package it.dhd.oneplusui.physicsengine.engine;

import androidx.annotation.NonNull;

import it.dhd.oneplusui.physicsengine.common.Compat;
import it.dhd.oneplusui.physicsengine.common.Debug;
import it.dhd.oneplusui.physicsengine.common.MathUtils;
import it.dhd.oneplusui.physicsengine.common.Vector;
import it.dhd.oneplusui.physicsengine.dynamics.Body;
import it.dhd.oneplusui.physicsengine.dynamics.spring.Spring;
import it.dhd.oneplusui.physicsengine.dynamics.spring.SpringDef;
import java.util.HashMap;

public abstract class BaseBehavior {

    public static final int BEHAVIOR_TYPE_ATTACHMENT = 3;
    public static final int BEHAVIOR_TYPE_CONSTRAINT = 1;
    public static final int BEHAVIOR_TYPE_DRAG = 0;
    public static final int BEHAVIOR_TYPE_FLING = 2;
    public static final int BEHAVIOR_TYPE_PRESS = 5;
    public static final int BEHAVIOR_TYPE_SNAP = 4;
    private static final float DEFAULT_DAMPING_RATIO = 0.2f;
    private static final float DEFAULT_FREQUENCY = 4.0f;
    protected UIItem<?> mActiveUIItem;
    protected Body mPropertyBody;
    protected HashMap<String, FloatPropertyHolder<?>> mPropertyMap;
    protected SpringDef mSpringDef;
    protected Runnable mStartAction;
    protected Runnable mStopAction;
    protected Object mTarget;
    protected float mValueThreshold = 1.0f;
    protected boolean mIsSpringApplied = false;
    protected boolean mIsStarted = false;
    protected boolean mHasCustomStartVelocity = false;
    protected FloatPropertyHolder<?> mFirstProperty = null;
    protected PhysicalAnimator mAnimator = null;
    protected Spring mSpring = null;

    public BaseBehavior() {
        onBehaviorCreated();
    }

    private void addProperty(FloatPropertyHolder<?> floatPropertyHolder) {
        if (mPropertyMap == null) {
            mPropertyMap = new HashMap<>(1);
        }
        if (mFirstProperty == null) {
            mFirstProperty = floatPropertyHolder;
            verifyBodyProperty();
        }
        mPropertyMap.put(floatPropertyHolder.mPropertyName, floatPropertyHolder);
        mValueThreshold = MathUtils.min(mValueThreshold, floatPropertyHolder.mValueThreshold);
    }

    private Body createSubBody(Vector vector, int i2, int i3, float f2, float f3, String str) {
        return mAnimator.createBody(vector, i2, i3, f2, f3, str);
    }

    private void updateProperty(UIItem<?> uIItem, FloatPropertyHolder<?> floatPropertyHolder) {
        floatPropertyHolder.update(uIItem);
    }

    private void verifyBodyProperty() {
        if (mAnimator != null && mPropertyBody == null) {
            mActiveUIItem = mAnimator.getOrCreateUIItem(mTarget);
            mPropertyBody = mAnimator.getOrCreatePropertyBody(mActiveUIItem, mFirstProperty != null ? mFirstProperty.mPropertyType : 1);
            onPropertyBodyCreated();
            if (Debug.isDebugMode()) {
                Debug.logD("verifyBodyProperty : mActiveUIItem =:" + mActiveUIItem + ",mPropertyBody =:" + mPropertyBody + ",this =:" + this);
            }
        }
    }

    public BaseBehavior applySizeChanged(float f2, float f3) {
        if (Debug.isDebugMode()) {
            Debug.logD("applySizeChanged : width =:" + f2 + ",height =:" + f3);
        }
        if (mActiveUIItem != null) {
            mActiveUIItem.setSize(f2, f3);
        }
        if (mPropertyBody != null) {
            mPropertyBody.setSize(Compat.pixelsToPhysicalSize(f2), Compat.pixelsToPhysicalSize(f3));
            mPropertyBody.updateActiveRect(this);
        }
        return this;
    }

    public <T extends BaseBehavior> T applyTo(Object obj) {
        mTarget = obj;
        verifyBodyProperty();
        return (T) this;
    }

    public void bindAnimator(PhysicalAnimator physicalAnimator) {
        mAnimator = physicalAnimator;
        verifyBodyProperty();
        linkGroundToSpring(mAnimator.getGround());
    }

    public Body copyBodyFromPropertyBody(String str, Body body) {
        if (body == null) {
            Body body2 = mPropertyBody;
            Vector vector = body2.mOriginPosition;
            int type = body2.getType();
            int property = mPropertyBody.getProperty();
            Body body3 = mPropertyBody;
            body = createSubBody(vector, type, property, body3.mWidth, body3.mHeight, str);
        } else {
            Body body4 = mPropertyBody;
            body.setSize(body4.mWidth, body4.mHeight);
        }
        body.setLinearVelocity(mPropertyBody.getLinearVelocity());
        body.setAwake(false);
        return body;
    }

    public boolean createDefaultSpring(SpringDef springDef) {
        if (mIsSpringApplied) {
            return false;
        }
        Spring createSpring = createSpring(springDef, mPropertyBody);
        mSpring = createSpring;
        if (createSpring == null) {
            return false;
        }
        mIsSpringApplied = true;
        return true;
    }

    public Spring createSpring(SpringDef springDef, Body body) {
        if (springDef == null || body == null) {
            return null;
        }
        springDef.target.set(body.getWorldCenter());
        return mAnimator.createSpring(springDef);
    }

    public void createSpringDef(float frequency, float dampingRatio) {
        SpringDef springDef = new SpringDef();
        mSpringDef = springDef;
        springDef.frequencyHz = frequency;
        springDef.dampingRatio = dampingRatio;
    }

    public boolean destroyBody(Body body) {
        return mAnimator.destroyBody(body);
    }

    public boolean destroyDefaultSpring() {
        if (!mIsSpringApplied) {
            return false;
        }
        destroySpring(mSpring);
        mSpring = null;
        mIsSpringApplied = false;
        return true;
    }

    public void destroySpring(Spring spring) {
        mAnimator.destroySpring(spring);
    }

    public void dispatchChanging() {
        mActiveUIItem.setTransformPosition(Compat.physicalSizeToPixels(mPropertyBody.getPosition().mX - mPropertyBody.getHookPosition().mX), Compat.physicalSizeToPixels(mPropertyBody.getPosition().mY - mPropertyBody.getHookPosition().mY));
    }

    public Object getAnimatedValue() {
        FloatPropertyHolder<?> floatPropertyHolder = mFirstProperty;
        if (floatPropertyHolder != null) {
            return getPropertyValue(mActiveUIItem, floatPropertyHolder);
        }
        if (getTransform() != null) {
            return getTransform().x;
        }
        return null;
    }

    public Vector getMoverTarget() {
        if (mActiveUIItem == null) {
            return null;
        }
        return mActiveUIItem.mMoveTarget;
    }

    public Body getPropertyBody() {
        return mPropertyBody;
    }

    public float getPropertyBodyLinearDamping() {
        if (mPropertyBody != null) {
            return mPropertyBody.getLinearDamping();
        }
        return -1.0f;
    }

    public Vector getPropertyBodyVelocity() {
        return mPropertyBody != null ? mPropertyBody.getLinearVelocity() : new Vector();
    }


    public float getPropertyValue(Object obj, FloatPropertyHolder floatPropertyHolder) {
        return floatPropertyHolder.getValue(obj);
    }

    public Transform getTransform() {
        if (mActiveUIItem != null) {
            return mActiveUIItem.getTransform();
        }
        return null;
    }

    public abstract int getType();

    public boolean isCloseToConstraint(Vector vector) {
        if (mSpring != null) {
            return Compat.lessThanSteadyAccuracy(MathUtils.abs(mSpring.getTarget().mX - vector.mX) + MathUtils.abs(mSpring.getTarget().mY - vector.mY));
        }
        return true;
    }

    public boolean isSteady() {
        return isVelocitySteady(mPropertyBody.mLinearVelocity) && isCloseToConstraint(mPropertyBody.getPosition());
    }

    public boolean isVelocitySteady(Vector vector) {
        return Compat.lessThanSteadyAccuracy(MathUtils.abs(vector.mX)) && Compat.lessThanSteadyAccuracy(MathUtils.abs(vector.mY));
    }

    public void linkGroundToSpring(Body body) {
        if (mSpringDef != null) {
            mSpringDef.bodyA = body;
            body.setAwake(true);
        }
    }

    public void moveToStartValue() {
        mActiveUIItem.mMoveTarget.set((Compat.pixelsToPhysicalSize(mActiveUIItem.mStartPosition.mX) + mPropertyBody.getHookPosition().mX) / mValueThreshold, (Compat.pixelsToPhysicalSize(mActiveUIItem.mStartPosition.mY) + mPropertyBody.getHookPosition().mY) / mValueThreshold);
        transformBodyTo(mPropertyBody, mActiveUIItem.mMoveTarget);
    }

    public void onPropertyBodyCreated() {
        if (mSpringDef != null) {
            mSpringDef.bodyB = mPropertyBody;
        }
    }

    public void onRemove() {
        if (Debug.isDebugMode()) {
            Debug.logD("onRemove mIsStarted =:" + mIsStarted + ",this =:" + this);
        }
        mStopAction = null;
        stopBehavior();
    }

    public <T extends BaseBehavior> T setSpringProperty(float f2, float f3) {
        if (mSpringDef != null) {
            mSpringDef.frequencyHz = f2;
            mSpringDef.dampingRatio = f3;
            if (mSpring != null) {
                mSpring.setFrequency(f2);
                mSpring.setDampingRatio(f3);
            }
        }
        return (T) this;
    }

    public <T extends BaseBehavior> T setStartVelocity(float f2) {
        return setStartVelocity(f2, 0.0f);
    }

    public void startBehavior() {
        if (mIsStarted) {
            return;
        }
        updateStartVelocity();
        updateStartValue();
        moveToStartValue();
        dispatchChanging();
        mAnimator.updateValue(this);
        mAnimator.startBehavior(this);
        mIsStarted = true;
        Runnable runnable = mStartAction;
        if (runnable != null) {
            runnable.run();
        }
    }

    public boolean stopBehavior() {
        if (!mIsStarted) {
            return false;
        }
        if (getType() != BEHAVIOR_TYPE_DRAG) {
            mActiveUIItem.mStartVelocity.setZero();
        }
        mAnimator.stopBehavior(this);
        mIsStarted = false;
        Runnable runnable = mStopAction;
        if (runnable == null) {
            return true;
        }
        runnable.run();
        return true;
    }

    @NonNull
    @Override
    public String toString() {
        return "Behavior{type=" + getType() + ", mValueThreshold=" + mValueThreshold + ", mTarget=" + mTarget + ", mPropertyBody=" + mPropertyBody + "}@" + hashCode();
    }

    public void transformBodyTo(Body body, Vector vector) {
        body.setPosition(vector);
    }

    public void updateProperties() {
        HashMap<String, FloatPropertyHolder<?>> hashMap = mPropertyMap;
        if (hashMap == null) {
            return;
        }
        for (FloatPropertyHolder<?> floatPropertyHolder : hashMap.values()) {
            if (floatPropertyHolder != null) {
                updateProperty(mActiveUIItem, floatPropertyHolder);
            }
        }
    }

    public void updateStartValue() {
        HashMap<String, FloatPropertyHolder<?>> hashMap = mPropertyMap;
        if (hashMap == null) {
            mActiveUIItem.setStartPosition(mActiveUIItem.getTransform().x, mActiveUIItem.getTransform().y);
            return;
        }
        for (FloatPropertyHolder floatPropertyHolder : hashMap.values()) {
            if (floatPropertyHolder != null) {
                floatPropertyHolder.verifyStartValue(mActiveUIItem);
            }
        }
    }

    public void updateStartVelocity() {
        if (mHasCustomStartVelocity) {
            mHasCustomStartVelocity = false;
            mPropertyBody.getLinearVelocity().set(Compat.pixelsToPhysicalSize(mActiveUIItem.mStartVelocity.mX), Compat.pixelsToPhysicalSize(mActiveUIItem.mStartVelocity.mY));
        }
    }

    public <T extends BaseBehavior> T withProperty(FloatPropertyHolder... floatPropertyHolderArr) {
        for (FloatPropertyHolder floatPropertyHolder : floatPropertyHolderArr) {
            addProperty(floatPropertyHolder);
        }
        return (T) this;
    }

    public <T extends BaseBehavior> T withStartAction(Runnable runnable) {
        mStartAction = runnable;
        return (T) this;
    }

    public <T extends BaseBehavior> T withStopAction(Runnable runnable) {
        mStopAction = runnable;
        return (T) this;
    }

    public <T extends BaseBehavior> T setStartVelocity(float f2, float f3) {
        if (getType() != 0) {
            mHasCustomStartVelocity = true;
            mActiveUIItem.setStartVelocity(f2, f3);
        }
        return (T) this;
    }

    public void createSpringDef() {
        createSpringDef(DEFAULT_FREQUENCY, DEFAULT_DAMPING_RATIO);
    }

    public Object getAnimatedValue(String str) {
        FloatPropertyHolder<?> floatPropertyHolder;
        HashMap<String, FloatPropertyHolder<?>> hashMap = mPropertyMap;
        if (hashMap == null || (floatPropertyHolder = hashMap.get(str)) == null) {
            return null;
        }
        return getPropertyValue(mActiveUIItem, floatPropertyHolder);
    }

    public void onBehaviorCreated() {
    }
}