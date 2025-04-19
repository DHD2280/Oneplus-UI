package it.dhd.oneplusui.physicsengine.dynamics.spring;

import static it.dhd.oneplusui.physicsengine.common.Compat.EPSILON;

import it.dhd.oneplusui.physicsengine.common.Mat22;
import it.dhd.oneplusui.physicsengine.common.Vector;
import it.dhd.oneplusui.physicsengine.dynamics.Body;
import it.dhd.oneplusui.physicsengine.dynamics.World;

public class Spring {

    private final Body mBodyA;
    private final Body mBodyB;
    private float mDampingRatio;
    public Edge mEdgeA;
    public Edge mEdgeB;
    private float mFrequencyHz;
    private final Vector mImpulse;
    private final Vector mImpulseTemp;
    private float mInvMass;
    public boolean mIsSolved;
    private final Vector mLocalAnchor;
    private final Mat22 mMass;
    private float mMaxForce;
    private final Vector mPositionCenter;
    private final Vector mTarget;
    public Spring mPrev = null;
    public Spring mNext = null;
    private float mBeta = 0.0f;
    private float mGamma = 0.0f;

    private Spring(Vector vector, SpringDef springDef) {
        Vector vector2 = new Vector();
        this.mLocalAnchor = vector2;
        this.mPositionCenter = new Vector();
        Vector vector3 = new Vector();
        this.mTarget = vector3;
        this.mImpulse = new Vector();
        this.mMass = new Mat22();
        this.mImpulseTemp = vector;
        this.mBodyA = springDef.bodyA;
        this.mBodyB = springDef.bodyB;
        this.mIsSolved = false;
        this.mEdgeA = new Edge();
        this.mEdgeB = new Edge();
        if (springDef.frequencyHz < 0.0f || springDef.maxForce < 0.0f || springDef.dampingRatio < 0.0f) {
            return;
        }
        vector3.set(springDef.target);
        vector2.set(vector3).subLocal(this.mBodyB.getPosition());
        this.mMaxForce = springDef.maxForce;
        this.mFrequencyHz = springDef.frequencyHz;
        this.mDampingRatio = springDef.dampingRatio;
    }

    public static Spring create(World world, SpringDef springDef) {
        return new Spring(world.getVectorTemp(), springDef);
    }

    public final Body getBodyA() {
        return this.mBodyA;
    }

    public final Body getBodyB() {
        return this.mBodyB;
    }

    public Vector getTarget() {
        return this.mTarget;
    }

    public void initVelocityConstraints(Body body, float f2) {
        this.mInvMass = body.mInvMass;
        float f3 = this.mFrequencyHz * 6.2831855f;
        float mass = body.getMass() * 2.0f * this.mDampingRatio * f3;
        float mass2 = body.getMass() * f3 * f3 * f2;
        float f4 = mass + mass2;
        if (f4 > EPSILON) {
            this.mGamma = f2 * f4;
        }
        float f5 = this.mGamma;
        if (f5 != 0.0f) {
            this.mGamma = 1.0f / f5;
        }
        float f6 = this.mGamma;
        this.mBeta = mass2 * f6;
        Mat22 mat22 = this.mMass;
        Vector vector = mat22.mX;
        float f7 = this.mInvMass;
        vector.mX = f7 + f6;
        mat22.mY.mY = f7 + f6;
        mat22.invertLocal();
        this.mPositionCenter.set(body.mWorldCenter).subLocal(this.mLocalAnchor).subLocal(this.mTarget).mulLocal(this.mBeta);
        Vector vector2 = body.mLinearVelocity;
        float f8 = vector2.mX;
        float f9 = this.mInvMass;
        Vector vector3 = this.mImpulse;
        vector2.mX = f8 + (vector3.mX * f9);
        vector2.mY += f9 * vector3.mY;
    }

    public void setDampingRatio(float f2) {
        this.mDampingRatio = f2;
    }

    public void setFrequency(float f2) {
        this.mFrequencyHz = f2;
    }

    public void setTarget(Vector vector) {
        this.mTarget.set(vector);
    }

    public void solveVelocityConstraints(Body body) {
        this.mImpulseTemp.set(this.mImpulse);
        this.mImpulseTemp.mulLocal(this.mGamma).addLocal(this.mPositionCenter).addLocal(body.mLinearVelocity).negateLocal();
        Mat22 mat22 = this.mMass;
        Vector vector = this.mImpulseTemp;
        Mat22.mulToOutUnsafe(mat22, vector, vector);
        this.mImpulse.addLocal(this.mImpulseTemp);
        body.mLinearVelocity.addLocal(this.mImpulseTemp.mulLocal(this.mInvMass));
    }

    public void setTarget(float f2, float f3) {
        Vector vector = this.mTarget;
        vector.mX = f2;
        vector.mY = f3;
    }
}