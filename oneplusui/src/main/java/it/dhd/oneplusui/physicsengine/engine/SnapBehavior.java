package it.dhd.oneplusui.physicsengine.engine;

import it.dhd.oneplusui.physicsengine.common.Compat;
import it.dhd.oneplusui.physicsengine.common.Debug;
import it.dhd.oneplusui.physicsengine.common.Vector;

public class SnapBehavior extends BaseBehavior {

    private final Vector mTargetPosition;
    private Vector mTargetPositionInPhysics;

    public SnapBehavior() {
        this(0.0f);
    }

    private void calculateTargetPosInPhysics() {
        if (mTargetPositionInPhysics == null) {
            mTargetPositionInPhysics = new Vector();
        }
        mTargetPositionInPhysics.set((Compat.pixelsToPhysicalSize(mTargetPosition.mX) + mPropertyBody.getHookPosition().mX) / mValueThreshold, (Compat.pixelsToPhysicalSize(mTargetPosition.mY) + mPropertyBody.getHookPosition().mY) / mValueThreshold);
    }

    private void createSpring() {
        if (createDefaultSpring(mSpringDef)) {
            updateSpringPosition();
        }
    }

    private void destroySpring() {
        destroyDefaultSpring();
    }

    private void setTargetPosition(float f2, float f3) {
        mTargetPosition.set(f2, f3);
    }

    private void updateSpringPosition() {
        calculateTargetPosInPhysics();
        mSpring.setTarget(mTargetPositionInPhysics);
    }

    @Override
    public void dispatchChanging() {
        mActiveUIItem.mMoveTarget.set(mPropertyBody.getPosition());
        super.dispatchChanging();
    }

    @Override
    public int getType() {
        return BEHAVIOR_TYPE_SNAP;
    }

    public void start() {
        startBehavior();
    }

    @Override
    public void startBehavior() {
        super.startBehavior();
        if (mSpring == null) {
            createSpring();
        } else {
            updateSpringPosition();
        }
    }

    public void stop() {
        stopBehavior();
    }

    @Override
    public boolean stopBehavior() {
        destroySpring();
        return super.stopBehavior();
    }

    public SnapBehavior(float x) {
        this(x, 0.0f);
    }

    public void start(float f2) {
        start(f2, 0.0f);
    }

    public SnapBehavior(float x, float y) {
        createSpringDef();
        mTargetPosition = new Vector(x, y);
    }

    public void start(float f2, float f3) {
        if (Debug.isDebugMode()) {
            Debug.logD("SnapBehavior : start : x =:" + f2 + ",y =:" + f3);
        }
        setTargetPosition(f2, f3);
        start();
    }
}