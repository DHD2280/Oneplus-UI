package it.dhd.oneplusui.physicsengine.engine;

import static it.dhd.oneplusui.physicsengine.common.Compat.UNSET_FREQUENCY;

import android.graphics.RectF;
import it.dhd.oneplusui.physicsengine.common.Vector;
import it.dhd.oneplusui.physicsengine.dynamics.Body;
import it.dhd.oneplusui.physicsengine.dynamics.spring.Spring;
import it.dhd.oneplusui.physicsengine.dynamics.spring.SpringDef;

public abstract class ConstraintBehavior extends BaseBehavior {

    public static final int COLLISION_MODE_ATTACH = 1;
    public static final int COLLISION_MODE_ERASE = 3;
    public static final int COLLISION_MODE_NO_LIMIT = 0;
    public static final int COLLISION_MODE_REBOUND = 2;
    public static final int COLLISION_MODE_SIMPLE_LIMIT = 4;
    private static final float DEFAULT_REBOUND_DECAY_RATIO = 0.5f;
    private static final float DEFAULT_SPRING_DAMPING_RATIO = 0.4f;
    private static final float DEFAULT_SPRING_FREQUENCY = 1.0f;
    static final int NOT_OVER_BOUNDS = 0;
    static final int OVER_BOTTOM_BOUND = 8;
    static final int OVER_LEFT_BOUND = 1;
    static final int OVER_RIGHT_BOUND = 4;
    static final int OVER_TOP_BOUND = 2;
    protected Body mAssistBody;
    protected int mCollisionMode;
    protected final RectF mConstraintRect = new RectF();
    protected boolean mShouldFixXSide = false;
    protected boolean mShouldFixYSide = false;
    protected float mConstraintPointX = 0.0f;
    protected float mConstraintPointY = 0.0f;
    protected int mOverBoundsState = 0;

    public ConstraintBehavior(int collisionMode, RectF rectF) {
        mCollisionMode = collisionMode;
        setConstraintRect(rectF);
        if (isCollisionLimitMode()) {
            SpringDef springDef = new SpringDef();
            mSpringDef = springDef;
            springDef.frequencyHz = DEFAULT_SPRING_FREQUENCY;
            springDef.dampingRatio = DEFAULT_SPRING_DAMPING_RATIO;
        }
    }

    private void createSpring() {
        if (createDefaultSpring(mSpringDef)) {
            mSpring.setTarget(mConstraintPointX, mConstraintPointY);
        }
    }

    private void destroySpring() {
        destroyDefaultSpring();
        resetOverBoundsState();
    }

    private boolean isCollisionAttachMode() {
        return mCollisionMode == COLLISION_MODE_ATTACH;
    }

    private boolean isCollisionEraseMode() {
        return mCollisionMode == COLLISION_MODE_ERASE;
    }

    private boolean isCollisionLimitMode() {
        return isCollisionAttachMode() || isCollisionEraseMode() || isCollisionReboundMode() || isCollisionSimpleLimitMode();
    }

    private boolean isCollisionNoLimitMode() {
        return mCollisionMode == COLLISION_MODE_NO_LIMIT;
    }

    private boolean isCollisionReboundMode() {
        return mCollisionMode == COLLISION_MODE_REBOUND;
    }

    private boolean isCollisionSimpleLimitMode() {
        return mCollisionMode == COLLISION_MODE_SIMPLE_LIMIT;
    }

    private void resetOverBoundsState() {
        mOverBoundsState = NOT_OVER_BOUNDS;
        mShouldFixXSide = false;
        mShouldFixYSide = false;
    }

    @Override
    public BaseBehavior applySizeChanged(float f2, float f3) {
        super.applySizeChanged(f2, f3);
        Body body = mAssistBody;
        if (body != null) {
            Body body2 = mPropertyBody;
            body.setSize(body2.mWidth, body2.mHeight);
        }
        return this;
    }

    public void calculateConstraintPosition() {
        mShouldFixXSide = isOverXBounds();
        mShouldFixYSide = isOverYBounds();
        mConstraintPointX = getFixedXInActive(mPropertyBody.getPosition().mX);
        mConstraintPointY = getFixedYInActive(mPropertyBody.getPosition().mY);
    }

    public void checkOverBoundsState(float f2, float f3) {
        mOverBoundsState = NOT_OVER_BOUNDS;
        RectF rectF = mPropertyBody.mActiveRect;
        if (rectF != null) {
            if (mIsStarted || !rectF.isEmpty()) {
                RectF rectF2 = mPropertyBody.mActiveRect;
                if (f2 < rectF2.left) {
                    mOverBoundsState |= OVER_LEFT_BOUND;
                } else if (f2 > rectF2.right) {
                    mOverBoundsState |= OVER_RIGHT_BOUND;
                }
                if (f3 < rectF2.top) {
                    mOverBoundsState |= OVER_TOP_BOUND;
                } else if (f3 > rectF2.bottom) {
                    mOverBoundsState |= OVER_BOTTOM_BOUND;
                }
            }
        }
    }

    @Override
    public void dispatchChanging() {
        Body body = mPropertyBody;
        if (body.mActiveRect != null) {
            checkOverBoundsState(body.getPosition().mX, mPropertyBody.getPosition().mY);
        }
        handlePositionChanging();
        super.dispatchChanging();
    }

    public float getFixedXInActive(float f2) {
        RectF rectF = mPropertyBody.mActiveRect;
        if (rectF != null && (mIsStarted || !rectF.isEmpty())) {
            RectF rectF2 = mPropertyBody.mActiveRect;
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
        RectF rectF = mPropertyBody.mActiveRect;
        if (rectF != null && (mIsStarted || !rectF.isEmpty())) {
            RectF rectF2 = mPropertyBody.mActiveRect;
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
        return 1;
    }

    public void handlePositionChanging() {
        if (mCollisionMode == COLLISION_MODE_NO_LIMIT) {
            mActiveUIItem.mMoveTarget.set(mPropertyBody.getPosition());
            transformBodyTo(mPropertyBody, mActiveUIItem.mMoveTarget);
            return;
        }
        if (mCollisionMode == COLLISION_MODE_ATTACH) {
            mActiveUIItem.mMoveTarget.set(mPropertyBody.getPosition());
            if (mShouldFixXSide) {
                mActiveUIItem.mMoveTarget.mX = mAssistBody.getPosition().mX;
            } else {
                mConstraintPointX = getFixedXInActive(mActiveUIItem.mMoveTarget.mX);
            }
            if (isOverXBounds()) {
                mShouldFixXSide = true;
            }
            if (mShouldFixYSide) {
                mActiveUIItem.mMoveTarget.mY = mAssistBody.getPosition().mY;
            } else {
                mConstraintPointY = getFixedYInActive(mActiveUIItem.mMoveTarget.mY);
            }
            if (isOverYBounds()) {
                mShouldFixYSide = true;
            }
            transform(mActiveUIItem.mMoveTarget);
            return;
        }
        if (mCollisionMode == 2) {
            if (mShouldFixXSide || mShouldFixYSide) {
                mActiveUIItem.mMoveTarget.set(mAssistBody.getPosition());
            } else {
                if (isOverBounds()) {
                    Body body = mPropertyBody;
                    body.setLinearVelocity(body.getLinearVelocity().mulLocal(DEFAULT_REBOUND_DECAY_RATIO).negateLocal());
                }
                mActiveUIItem.mMoveTarget.set(getFixedXInActive(mPropertyBody.getPosition().mX), getFixedYInActive(mPropertyBody.getPosition().mY));
                mConstraintPointX = getFixedXInActive(mActiveUIItem.mMoveTarget.mX);
                mConstraintPointY = getFixedYInActive(mActiveUIItem.mMoveTarget.mY);
            }
            transform(mActiveUIItem.mMoveTarget);
            return;
        }
        if (mCollisionMode == COLLISION_MODE_ERASE) {
            if (mShouldFixXSide || mShouldFixYSide) {
                mActiveUIItem.mMoveTarget.set(mAssistBody.getPosition());
            } else {
                if (isOverBounds()) {
                    mPropertyBody.getLinearVelocity().setZero();
                }
                mActiveUIItem.mMoveTarget.set(getFixedXInActive(mPropertyBody.getPosition().mX), getFixedYInActive(mPropertyBody.getPosition().mY));
                mConstraintPointX = getFixedXInActive(mActiveUIItem.mMoveTarget.mX);
                mConstraintPointY = getFixedYInActive(mActiveUIItem.mMoveTarget.mY);
            }
            transform(mActiveUIItem.mMoveTarget);
            return;
        }
        if (mCollisionMode != COLLISION_MODE_SIMPLE_LIMIT) {
            return;
        }
        mActiveUIItem.mMoveTarget.set(mPropertyBody.getPosition());
        if (mShouldFixXSide) {
            mActiveUIItem.mMoveTarget.mX = mAssistBody.getPosition().mX;
        } else {
            mConstraintPointX = getFixedXInActive(mActiveUIItem.mMoveTarget.mX);
        }
        mShouldFixXSide = isOverXBounds();
        if (mShouldFixYSide) {
            mActiveUIItem.mMoveTarget.mY = mAssistBody.getPosition().mY;
        } else {
            mConstraintPointY = getFixedYInActive(mActiveUIItem.mMoveTarget.mY);
        }
        mShouldFixYSide = isOverYBounds();
        transform(mActiveUIItem.mMoveTarget);
    }

    public boolean isOverBottomBound() {
        return (mOverBoundsState & 8) != 0;
    }

    public boolean isOverBounds() {
        return mOverBoundsState != 0;
    }

    public boolean isOverLeftBound() {
        return (mOverBoundsState & 1) != 0;
    }

    public boolean isOverRightBound() {
        return (mOverBoundsState & 4) != 0;
    }

    public boolean isOverTopBound() {
        return (mOverBoundsState & 2) != 0;
    }

    public boolean isOverXBounds() {
        return isOverLeftBound() || isOverRightBound();
    }

    public boolean isOverYBounds() {
        return isOverTopBound() || isOverBottomBound();
    }

    @Override
    public boolean isSteady() {
        return isCollisionLimitMode() ? super.isSteady() : isVelocitySteady(mPropertyBody.mLinearVelocity);
    }

    @Override
    public void linkGroundToSpring(Body body) {
        if (isCollisionLimitMode()) {
            super.linkGroundToSpring(body);
        }
    }

    @Override
    public void moveToStartValue() {
        super.moveToStartValue();
        Body body = mAssistBody;
        if (body != null) {
            transformBodyTo(body, mActiveUIItem.mMoveTarget);
        }
    }

    @Override
    public void onPropertyBodyCreated() {
        RectF rectF = mConstraintRect;
        if (rectF != null && !rectF.isEmpty()) {
            mPropertyBody.setOriginActiveRect(mConstraintRect);
            mPropertyBody.updateActiveRect(this);
            if (isCollisionLimitMode()) {
                Body body = mPropertyBody;
                if (body.mActiveConstraintFrequency == UNSET_FREQUENCY) {
                    body.setActiveConstraintFrequency(mSpringDef.frequencyHz);
                }
            }
        }
        if (mSpringDef != null) {
            Body copyBodyFromPropertyBody = copyBodyFromPropertyBody("Assist", mAssistBody);
            mAssistBody = copyBodyFromPropertyBody;
            mSpringDef.bodyB = copyBodyFromPropertyBody;
        }
    }

    @Override
    public void onRemove() {
        super.onRemove();
        mPropertyBody.clearActiveConstraint(this);
        if (isCollisionLimitMode()) {
            destroySpring();
            destroyBody(mAssistBody);
        }
    }

    public void onStartBehavior() {
        if (mPropertyBody.updateActiveRect(this) && isCollisionLimitMode()) {
            checkOverBoundsState(mPropertyBody.getPosition().mX, mPropertyBody.getPosition().mY);
            calculateConstraintPosition();
            mAssistBody.setAwake(true);
            mAssistBody.setLinearVelocity(mPropertyBody.getLinearVelocity());
            transformBodyTo(mAssistBody, mPropertyBody.getPosition());
            createSpring();
        }
    }

    public void setCollisionLimitMode(int i2) {
        if (mCollisionMode == 0 || i2 == 0) {
            return;
        }
        mCollisionMode = i2;
    }

    public void setConstraintRect(RectF rectF) {
        if (rectF == null || rectF.isEmpty()) {
            return;
        }
        mConstraintRect.set(rectF);
        Body body = mPropertyBody;
        if (body != null) {
            body.setOriginActiveRect(mConstraintRect);
            mPropertyBody.updateActiveRect(this);
        }
    }

    @Override
    public <T extends BaseBehavior> T setSpringProperty(float f2, float f3) {
        if (mPropertyBody != null && isCollisionLimitMode()) {
            if (mPropertyBody.mActiveConstraintFrequency == UNSET_FREQUENCY) {
                mPropertyBody.setActiveConstraintFrequency(f2);
            }
        }
        return super.setSpringProperty(f2, f3);
    }

    @Override
    public void startBehavior() {
        super.startBehavior();
        onStartBehavior();
    }

    @Override
    public boolean stopBehavior() {
        mPropertyBody.clearActiveRect(this);
        if (isCollisionLimitMode()) {
            destroySpring();
            mAssistBody.setAwake(false);
        }
        return super.stopBehavior();
    }

    public void transform(Vector vector) {
        transformBodyTo(mPropertyBody, vector);
        Spring spring = mSpring;
        if (spring != null) {
            spring.setTarget(mConstraintPointX, mConstraintPointY);
            transformBodyTo(mAssistBody, vector);
        }
    }
}