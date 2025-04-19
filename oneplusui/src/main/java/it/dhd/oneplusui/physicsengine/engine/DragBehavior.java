package it.dhd.oneplusui.physicsengine.engine;

import android.graphics.RectF;
import it.dhd.oneplusui.physicsengine.common.Compat;
import it.dhd.oneplusui.physicsengine.common.Debug;
import it.dhd.oneplusui.physicsengine.common.MathUtils;
import it.dhd.oneplusui.physicsengine.common.Vector;
import it.dhd.oneplusui.physicsengine.dynamics.Body;
import it.dhd.oneplusui.physicsengine.dynamics.spring.Spring;
import it.dhd.oneplusui.physicsengine.dynamics.spring.SpringDef;

public class DragBehavior extends BaseBehavior {

    private boolean mIsDragging = false;
    private boolean mIsEnableOut = true;
    private Body mSimulateBody;
    private Spring mSimulateSpring;
    private SpringDef mSimulateSpringDef;

    public DragBehavior() {
        createSpringDef();
        SpringDef springDef = new SpringDef();
        this.mSimulateSpringDef = springDef;
        springDef.frequencyHz = 2000000.0f;
        springDef.dampingRatio = 100.0f;
    }

    private void createSpring() {
        if (createDefaultSpring(this.mSpringDef)) {
            this.mSpring.setTarget(this.mActiveUIItem.mMoveTarget);
            Spring createSpring = createSpring(this.mSimulateSpringDef, this.mSimulateBody);
            this.mSimulateSpring = createSpring;
            if (createSpring != null) {
                createSpring.setTarget(this.mActiveUIItem.mMoveTarget);
                this.mSimulateBody.setAwake(true);
            }
        }
    }

    private void destroySpring() {
        if (destroyDefaultSpring()) {
            destroySpring(this.mSimulateSpring);
            this.mSimulateBody.setAwake(false);
        }
    }

    private void dragTo(float f2, float f3) {
        if (Debug.isDebugMode()) {
            Debug.logD("DragBehavior : dragTo : x =:" + f2 + ",y =:" + f3);
        }
        if (this.mSpring != null) {
            this.mActiveUIItem.mMoveTarget.set(getFixedXInActive(Compat.pixelsToPhysicalSize(f2)), getFixedYInActive(Compat.pixelsToPhysicalSize(f3)));
            this.mSpring.setTarget(this.mActiveUIItem.mMoveTarget);
            Spring spring = this.mSimulateSpring;
            if (spring != null) {
                spring.setTarget(this.mActiveUIItem.mMoveTarget);
            }
        }
    }

    private void transform(Vector vector) {
        transformBodyTo(this.mPropertyBody, vector);
        Body body = this.mSimulateBody;
        if (body != null) {
            transformBodyTo(body, vector);
        }
    }

    @Override
    public BaseBehavior applySizeChanged(float f2, float f3) {
        super.applySizeChanged(f2, f3);
        Body body = this.mSimulateBody;
        if (body != null) {
            Body body2 = this.mPropertyBody;
            body.setSize(body2.mWidth, body2.mHeight);
        }
        return this;
    }

    public void beginDrag(float f2, float f3) {
        beginDrag(f2, 0.0f, f3, 0.0f);
    }

    public void endDrag(float f2) {
        endDrag(f2, 0.0f);
    }

    public float getFixedXInActive(float f2) {
        RectF rectF;
        if (!this.mIsEnableOut && (rectF = this.mPropertyBody.mActiveRect) != null && (this.mIsStarted || !rectF.isEmpty())) {
            RectF rectF2 = this.mPropertyBody.mActiveRect;
            float f3 = rectF2.left;
            if (f2 < f3) {
                return f3;
            }
            float f4 = rectF2.right;
            if (f2 > f4) {
                return f4;
            }
        }
        return f2;
    }

    public float getFixedYInActive(float f2) {
        RectF rectF;
        if (!this.mIsEnableOut && (rectF = this.mPropertyBody.mActiveRect) != null && (this.mIsStarted || !rectF.isEmpty())) {
            RectF rectF2 = this.mPropertyBody.mActiveRect;
            float f3 = rectF2.top;
            if (f2 < f3) {
                return f3;
            }
            float f4 = rectF2.bottom;
            if (f2 > f4) {
                return f4;
            }
        }
        return f2;
    }

    @Override
    public int getType() {
        return BEHAVIOR_TYPE_DRAG;
    }

    public boolean isDragging() {
        return this.mIsDragging;
    }

    @Override
    public boolean isSteady() {
        return !this.mIsDragging;
    }

    @Override
    public void linkGroundToSpring(Body body) {
        super.linkGroundToSpring(body);
        SpringDef springDef = this.mSimulateSpringDef;
        if (springDef != null) {
            springDef.bodyA = body;
        }
    }

    public void onDragging(float f2) {
        dragTo(f2, 0.0f);
    }

    @Override
    public void onPropertyBodyCreated() {
        super.onPropertyBodyCreated();
        this.mPropertyBody.setActiveConstraintFrequency(this.mSpringDef.frequencyHz);
        if (this.mSimulateSpringDef != null) {
            Body copyBodyFromPropertyBody = copyBodyFromPropertyBody("SimulateTouch", this.mSimulateBody);
            this.mSimulateBody = copyBodyFromPropertyBody;
            this.mSimulateSpringDef.bodyB = copyBodyFromPropertyBody;
        }
    }

    @Override
    public void onRemove() {
        super.onRemove();
        Body body = this.mSimulateBody;
        if (body != null) {
            destroyBody(body);
        }
    }

    public DragBehavior setEnableOut(boolean z2) {
        this.mIsEnableOut = z2;
        return this;
    }

    @Override
    public <T extends BaseBehavior> T setSpringProperty(float f2, float f3) {
        Body body = this.mPropertyBody;
        if (body != null) {
            body.setActiveConstraintFrequency(f2);
        }
        return (T) super.setSpringProperty(f2, f3);
    }

    @Override
    public void startBehavior() {
        super.startBehavior();
        createSpring();
    }

    @Override
    public boolean stopBehavior() {
        destroySpring();
        return super.stopBehavior();
    }

    public void beginDrag(float f2, float f3, float f4, float f5) {
        if (Debug.isDebugMode()) {
            Debug.logD("DragBehavior : beginDrag : x =:" + f2 + ",y =:" + f3 + ",currentX =:" + f4 + ",currentY =:" + f5);
        }
        this.mPropertyBody.setHookPosition(f2 - f4, f3 - f5);
        this.mPropertyBody.updateActiveRect(this);
        this.mPropertyBody.mLinearVelocity.setZero();
        Body body = this.mSimulateBody;
        if (body != null) {
            body.mLinearVelocity.setZero();
        }
        this.mActiveUIItem.mMoveTarget.set(getFixedXInActive(Compat.pixelsToPhysicalSize(f2)), getFixedYInActive(Compat.pixelsToPhysicalSize(f3)));
        transform(this.mActiveUIItem.mMoveTarget);
        this.mIsDragging = true;
        startBehavior();
    }

    public void endDrag(float f2, float f3) {
        if (Debug.isDebugMode()) {
            Debug.logD("DragBehavior : endDrag : xVel =:" + f2 + ",yVel =:" + f3);
        }
        destroySpring();
        Body body = this.mSimulateBody;
        if (body != null) {
            Vector vector = body.mLinearVelocity;
            float f4 = vector.mX;
            f2 = f4 == 0.0f ? 0.0f : (f4 / MathUtils.abs(f4)) * MathUtils.abs(f2);
            float f5 = vector.mY;
            f3 = f5 == 0.0f ? 0.0f : MathUtils.abs(f3) * (f5 / MathUtils.abs(f5));
        }
        this.mActiveUIItem.setStartVelocity(f2, f3);
        this.mIsDragging = false;
        this.mPropertyBody.clearActiveRect(this);
    }

    public void onDragging(float f2, float f3) {
        dragTo(f2, f3);
    }

    @Override
    public void moveToStartValue() {
    }
}