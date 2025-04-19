package it.dhd.oneplusui.physicsengine.engine;

import android.graphics.RectF;
import it.dhd.oneplusui.physicsengine.common.Compat;
import it.dhd.oneplusui.physicsengine.common.Debug;
import it.dhd.oneplusui.physicsengine.dynamics.Body;

public class FlingBehavior extends ConstraintBehavior {
    private float mCustomLinearDamping;
    private boolean mHasSetVelocity;
    private float mOriginLinearDamping;

    public FlingBehavior() {
        this(COLLISION_MODE_NO_LIMIT, (RectF) null);
    }

    @Override
    public int getType() {
        return BEHAVIOR_TYPE_FLING;
    }

    public void setActiveFrame(float leftTop, float rightBottom) {
        setActiveFrame(new RectF(leftTop, leftTop, rightBottom, rightBottom));
    }

    public void setLinearDamping(float linearDamping) {
        mCustomLinearDamping = linearDamping;
    }

    public void start() {
        startBehavior();
    }

    @Override
    public void startBehavior() {
        super.startBehavior();
        if (mCustomLinearDamping != 0.0f) {
            mOriginLinearDamping = mPropertyBody.mLinearDamping;
            mPropertyBody.setLinearDamping(mCustomLinearDamping);
            if (mAssistBody != null) {
                mAssistBody.setLinearDamping(mCustomLinearDamping);
            }
        }
    }

    public void stop() {
        stopBehavior();
    }

    @Override
    public boolean stopBehavior() {
        float f2 = mOriginLinearDamping;
        if (f2 != 0.0f) {
            mPropertyBody.setLinearDamping(f2);
            Body body = mAssistBody;
            if (body != null) {
                body.setLinearDamping(mOriginLinearDamping);
            }
        }
        return super.stopBehavior();
    }

    @Override
    public void updateStartVelocity() {
        if (mHasSetVelocity) {
            return;
        }
        super.updateStartVelocity();
    }

    public FlingBehavior(float f2, float f3) {
        this(3, f2, f3);
    }

    public void setActiveFrame(RectF rectF) {
        super.setConstraintRect(rectF);
    }

    public void start(float f2) {
        start(f2, 0.0f);
    }

    public FlingBehavior(int collisionMode, float leftTop, float rightBottom) {
        this(collisionMode, new RectF(leftTop, leftTop, rightBottom, rightBottom));
    }

    public void start(float f2, float f3) {
        if (Debug.isDebugMode()) {
            Debug.logD("FlingBehavior : Fling : start : xVel =:" + f2 + ",yVel =:" + f3);
        }
        mHasSetVelocity = true;
        mPropertyBody.getLinearVelocity().set(Compat.pixelsToPhysicalSize(f2), Compat.pixelsToPhysicalSize(f3));
        start();
        mHasSetVelocity = false;
    }

    public FlingBehavior(RectF rectF) {
        this(1, rectF);
    }

    public FlingBehavior(int collisionMode, RectF rectF) {
        super(collisionMode, rectF);
        mOriginLinearDamping = 0.0f;
        mCustomLinearDamping = 0.0f;
        mHasSetVelocity = false;
    }
}