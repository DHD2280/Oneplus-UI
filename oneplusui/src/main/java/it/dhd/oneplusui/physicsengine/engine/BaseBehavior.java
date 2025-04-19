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
    protected UIItem mActiveUIItem;
    protected Body mPropertyBody;
    protected HashMap<String, FloatPropertyHolder> mPropertyMap;
    protected SpringDef mSpringDef;
    protected Runnable mStartAction;
    protected Runnable mStopAction;
    protected Object mTarget;
    protected float mValueThreshold = 1.0f;
    protected boolean mIsSpringApplied = false;
    protected boolean mIsStarted = false;
    protected boolean mHasCustomStartVelocity = false;
    protected FloatPropertyHolder mFirstProperty = null;
    protected PhysicalAnimator mAnimator = null;
    protected Spring mSpring = null;

    public BaseBehavior() {
        onBehaviorCreated();
    }

    private void addProperty(FloatPropertyHolder floatPropertyHolder) {
        if (this.mPropertyMap == null) {
            this.mPropertyMap = new HashMap<>(1);
        }
        if (this.mFirstProperty == null) {
            this.mFirstProperty = floatPropertyHolder;
            verifyBodyProperty();
        }
        this.mPropertyMap.put(floatPropertyHolder.mPropertyName, floatPropertyHolder);
        this.mValueThreshold = MathUtils.min(this.mValueThreshold, floatPropertyHolder.mValueThreshold);
    }

    private Body createSubBody(Vector vector, int i2, int i3, float f2, float f3, String str) {
        return this.mAnimator.createBody(vector, i2, i3, f2, f3, str);
    }

    private void updateProperty(UIItem uIItem, FloatPropertyHolder floatPropertyHolder) {
        floatPropertyHolder.update(uIItem);
    }

    private void verifyBodyProperty() {
        PhysicalAnimator physicalAnimator = this.mAnimator;
        if (mAnimator != null && this.mPropertyBody == null) {
            mActiveUIItem = mAnimator.getOrCreateUIItem(this.mTarget);
            FloatPropertyHolder floatPropertyHolder = this.mFirstProperty;
            this.mPropertyBody = mAnimator.getOrCreatePropertyBody(mActiveUIItem, mFirstProperty != null ? mFirstProperty.mPropertyType : 1);
            onPropertyBodyCreated();
            if (Debug.isDebugMode()) {
                Debug.logD("verifyBodyProperty : mActiveUIItem =:" + this.mActiveUIItem + ",mPropertyBody =:" + this.mPropertyBody + ",this =:" + this);
            }
        }
    }

    public BaseBehavior applySizeChanged(float f2, float f3) {
        if (Debug.isDebugMode()) {
            Debug.logD("applySizeChanged : width =:" + f2 + ",height =:" + f3);
        }
        UIItem uIItem = this.mActiveUIItem;
        if (uIItem != null) {
            uIItem.setSize(f2, f3);
        }
        Body body = this.mPropertyBody;
        if (body != null) {
            body.setSize(Compat.pixelsToPhysicalSize(f2), Compat.pixelsToPhysicalSize(f3));
            this.mPropertyBody.updateActiveRect(this);
        }
        return this;
    }

    public <T extends BaseBehavior> T applyTo(Object obj) {
        this.mTarget = obj;
        verifyBodyProperty();
        return (T) this;
    }

    public void bindAnimator(PhysicalAnimator physicalAnimator) {
        this.mAnimator = physicalAnimator;
        verifyBodyProperty();
        linkGroundToSpring(this.mAnimator.getGround());
    }

    public Body copyBodyFromPropertyBody(String str, Body body) {
        if (body == null) {
            Body body2 = this.mPropertyBody;
            Vector vector = body2.mOriginPosition;
            int type = body2.getType();
            int property = this.mPropertyBody.getProperty();
            Body body3 = this.mPropertyBody;
            body = createSubBody(vector, type, property, body3.mWidth, body3.mHeight, str);
        } else {
            Body body4 = this.mPropertyBody;
            body.setSize(body4.mWidth, body4.mHeight);
        }
        body.setLinearVelocity(this.mPropertyBody.getLinearVelocity());
        body.setAwake(false);
        return body;
    }

    public boolean createDefaultSpring(SpringDef springDef) {
        if (this.mIsSpringApplied) {
            return false;
        }
        Spring createSpring = createSpring(springDef, this.mPropertyBody);
        this.mSpring = createSpring;
        if (createSpring == null) {
            return false;
        }
        this.mIsSpringApplied = true;
        return true;
    }

    public Spring createSpring(SpringDef springDef, Body body) {
        if (springDef == null || body == null) {
            return null;
        }
        springDef.target.set(body.getWorldCenter());
        return this.mAnimator.createSpring(springDef);
    }

    public void createSpringDef(float frequency, float dampingRatio) {
        SpringDef springDef = new SpringDef();
        this.mSpringDef = springDef;
        springDef.frequencyHz = frequency;
        springDef.dampingRatio = dampingRatio;
    }

    public boolean destroyBody(Body body) {
        return this.mAnimator.destroyBody(body);
    }

    public boolean destroyDefaultSpring() {
        if (!this.mIsSpringApplied) {
            return false;
        }
        destroySpring(this.mSpring);
        this.mSpring = null;
        this.mIsSpringApplied = false;
        return true;
    }

    public void destroySpring(Spring spring) {
        this.mAnimator.destroySpring(spring);
    }

    public void dispatchChanging() {
        this.mActiveUIItem.setTransformPosition(Compat.physicalSizeToPixels(this.mPropertyBody.getPosition().mX - this.mPropertyBody.getHookPosition().mX), Compat.physicalSizeToPixels(this.mPropertyBody.getPosition().mY - this.mPropertyBody.getHookPosition().mY));
    }

    public Object getAnimatedValue() {
        FloatPropertyHolder floatPropertyHolder = this.mFirstProperty;
        if (floatPropertyHolder != null) {
            return Float.valueOf(getPropertyValue(this.mActiveUIItem, floatPropertyHolder));
        }
        if (getTransform() != null) {
            return Float.valueOf(getTransform().x);
        }
        return null;
    }

    public Vector getMoverTarget() {
        UIItem uIItem = this.mActiveUIItem;
        if (uIItem == null) {
            return null;
        }
        return uIItem.mMoveTarget;
    }

    public Body getPropertyBody() {
        return this.mPropertyBody;
    }

    public float getPropertyBodyLinearDamping() {
        Body body = this.mPropertyBody;
        if (body != null) {
            return body.getLinearDamping();
        }
        return -1.0f;
    }

    public float getPropertyValue(Object obj, FloatPropertyHolder floatPropertyHolder) {
        return floatPropertyHolder.getValue(obj);
    }

    public Transform getTransform() {
        UIItem uIItem = this.mActiveUIItem;
        if (uIItem != null) {
            return uIItem.getTransform();
        }
        return null;
    }

    public abstract int getType();

    public boolean isCloseToConstraint(Vector vector) {
        Spring spring = this.mSpring;
        if (spring != null) {
            return Compat.lessThanSteadyAccuracy(MathUtils.abs(spring.getTarget().mX - vector.mX) + MathUtils.abs(this.mSpring.getTarget().mY - vector.mY));
        }
        return true;
    }

    public boolean isSteady() {
        return isVelocitySteady(this.mPropertyBody.mLinearVelocity) && isCloseToConstraint(this.mPropertyBody.getPosition());
    }

    public boolean isVelocitySteady(Vector vector) {
        return Compat.lessThanSteadyAccuracy(MathUtils.abs(vector.mX)) && Compat.lessThanSteadyAccuracy(MathUtils.abs(vector.mY));
    }

    public void linkGroundToSpring(Body body) {
        SpringDef springDef = this.mSpringDef;
        if (springDef != null) {
            springDef.bodyA = body;
            body.setAwake(true);
        }
    }

    public void moveToStartValue() {
        UIItem uIItem = this.mActiveUIItem;
        uIItem.mMoveTarget.set((Compat.pixelsToPhysicalSize(uIItem.mStartPosition.mX) + this.mPropertyBody.getHookPosition().mX) / this.mValueThreshold, (Compat.pixelsToPhysicalSize(this.mActiveUIItem.mStartPosition.mY) + this.mPropertyBody.getHookPosition().mY) / this.mValueThreshold);
        transformBodyTo(this.mPropertyBody, this.mActiveUIItem.mMoveTarget);
    }

    public void onPropertyBodyCreated() {
        SpringDef springDef = this.mSpringDef;
        if (springDef != null) {
            springDef.bodyB = this.mPropertyBody;
        }
    }

    public void onRemove() {
        if (Debug.isDebugMode()) {
            Debug.logD("onRemove mIsStarted =:" + this.mIsStarted + ",this =:" + this);
        }
        this.mStopAction = null;
        stopBehavior();
    }

    public <T extends BaseBehavior> T setSpringProperty(float f2, float f3) {
        SpringDef springDef = this.mSpringDef;
        if (springDef != null) {
            springDef.frequencyHz = f2;
            springDef.dampingRatio = f3;
            Spring spring = this.mSpring;
            if (spring != null) {
                spring.setFrequency(f2);
                this.mSpring.setDampingRatio(f3);
            }
        }
        return (T) this;
    }

    public <T extends BaseBehavior> T setStartVelocity(float f2) {
        return setStartVelocity(f2, 0.0f);
    }

    public void startBehavior() {
        if (this.mIsStarted) {
            return;
        }
        updateStartVelocity();
        updateStartValue();
        moveToStartValue();
        dispatchChanging();
        this.mAnimator.updateValue(this);
        this.mAnimator.startBehavior(this);
        this.mIsStarted = true;
        Runnable runnable = this.mStartAction;
        if (runnable != null) {
            runnable.run();
        }
    }

    public boolean stopBehavior() {
        if (!this.mIsStarted) {
            return false;
        }
        if (getType() != BEHAVIOR_TYPE_DRAG) {
            this.mActiveUIItem.mStartVelocity.setZero();
        }
        this.mAnimator.stopBehavior(this);
        this.mIsStarted = false;
        Runnable runnable = this.mStopAction;
        if (runnable == null) {
            return true;
        }
        runnable.run();
        return true;
    }

    @NonNull
    @Override
    public String toString() {
        return "Behavior{type=" + getType() + ", mValueThreshold=" + this.mValueThreshold + ", mTarget=" + this.mTarget + ", mPropertyBody=" + this.mPropertyBody + "}@" + hashCode();
    }

    public void transformBodyTo(Body body, Vector vector) {
        body.setPosition(vector);
    }

    public void updateProperties() {
        HashMap<String, FloatPropertyHolder> hashMap = this.mPropertyMap;
        if (hashMap == null) {
            return;
        }
        for (FloatPropertyHolder floatPropertyHolder : hashMap.values()) {
            if (floatPropertyHolder != null) {
                updateProperty(this.mActiveUIItem, floatPropertyHolder);
            }
        }
    }

    public void updateStartValue() {
        HashMap<String, FloatPropertyHolder> hashMap = this.mPropertyMap;
        if (hashMap == null) {
            UIItem uIItem = this.mActiveUIItem;
            uIItem.setStartPosition(uIItem.getTransform().x, this.mActiveUIItem.getTransform().y);
            return;
        }
        for (FloatPropertyHolder floatPropertyHolder : hashMap.values()) {
            if (floatPropertyHolder != null) {
                floatPropertyHolder.verifyStartValue(this.mActiveUIItem);
            }
        }
    }

    public void updateStartVelocity() {
        if (this.mHasCustomStartVelocity) {
            this.mHasCustomStartVelocity = false;
            this.mPropertyBody.getLinearVelocity().set(Compat.pixelsToPhysicalSize(this.mActiveUIItem.mStartVelocity.mX), Compat.pixelsToPhysicalSize(this.mActiveUIItem.mStartVelocity.mY));
        }
    }

    public <T extends BaseBehavior> T withProperty(FloatPropertyHolder... floatPropertyHolderArr) {
        for (FloatPropertyHolder floatPropertyHolder : floatPropertyHolderArr) {
            addProperty(floatPropertyHolder);
        }
        return (T) this;
    }

    public <T extends BaseBehavior> T withStartAction(Runnable runnable) {
        this.mStartAction = runnable;
        return (T) this;
    }

    public <T extends BaseBehavior> T withStopAction(Runnable runnable) {
        this.mStopAction = runnable;
        return (T) this;
    }

    public <T extends BaseBehavior> T setStartVelocity(float f2, float f3) {
        if (getType() != 0) {
            this.mHasCustomStartVelocity = true;
            this.mActiveUIItem.setStartVelocity(f2, f3);
        }
        return (T) this;
    }

    public void createSpringDef() {
        createSpringDef(DEFAULT_FREQUENCY, DEFAULT_DAMPING_RATIO);
    }

    public Object getAnimatedValue(String str) {
        FloatPropertyHolder floatPropertyHolder;
        HashMap<String, FloatPropertyHolder> hashMap = this.mPropertyMap;
        if (hashMap == null || (floatPropertyHolder = hashMap.get(str)) == null) {
            return null;
        }
        return Float.valueOf(getPropertyValue(this.mActiveUIItem, floatPropertyHolder));
    }

    public void onBehaviorCreated() {
    }
}