package it.dhd.oneplusui.physicsengine.dynamics;

import it.dhd.oneplusui.physicsengine.common.Debug;
import it.dhd.oneplusui.physicsengine.common.Vector;
import it.dhd.oneplusui.physicsengine.dynamics.spring.Edge;
import it.dhd.oneplusui.physicsengine.dynamics.spring.Spring;
import it.dhd.oneplusui.physicsengine.dynamics.spring.SpringDef;

public class World {

    private int mBodyCount;
    private Body mBodyList;
    private int mSpringCount;
    private Spring mSpringList;
    private final Vector mVectorTemp;

    public World() {
        this(new Vector());
    }

    private void dumpBodyList() {
        for (Body body = this.mBodyList; body != null; body = body.mNext) {
            Debug.logD("world has body ====>>> " + body);
        }
    }

    private void solve(float f2) {
        for (Body body = this.mBodyList; body != null; body = body.mNext) {
            body.mIsSolved = false;
        }
        for (Spring spring = this.mSpringList; spring != null; spring = spring.mNext) {
            spring.mIsSolved = false;
        }
        for (Body body2 = this.mBodyList; body2 != null; body2 = body2.mNext) {
            if (!body2.mIsSolved && body2.mIsAwake && body2.getType() != 0) {
                solve(body2, f2);
                body2.mIsSolved = true;
                body2.mForce.setZero();
            }
        }
    }

    public Body createBody(Vector vector, int i2, int i3, float f2, float f3, String str) {
        Body body = new Body(vector, i2, i3, f2, f3);
        body.setTag(str);
        body.mPrev = null;
        Body body2 = this.mBodyList;
        body.mNext = body2;
        if (body2 != null) {
            body2.mPrev = body;
        }
        this.mBodyList = body;
        this.mBodyCount++;
        if (Debug.isDebugMode()) {
            dumpBodyList();
        }
        return body;
    }

    public Spring createSpring(SpringDef springDef) {
        Spring create = Spring.create(this, springDef);
        create.mPrev = null;
        Spring spring = this.mSpringList;
        create.mNext = spring;
        if (spring != null) {
            spring.mPrev = create;
        }
        this.mSpringList = create;
        this.mSpringCount++;
        Edge edge = create.mEdgeA;
        edge.spring = create;
        edge.other = create.getBodyB();
        Edge edge2 = create.mEdgeA;
        edge2.prev = null;
        edge2.next = create.getBodyA().mEdgeList;
        if (create.getBodyA().mEdgeList != null) {
            create.getBodyA().mEdgeList.prev = create.mEdgeA;
        }
        create.getBodyA().mEdgeList = create.mEdgeA;
        Edge edge3 = create.mEdgeB;
        edge3.spring = create;
        edge3.other = create.getBodyA();
        Edge edge4 = create.mEdgeB;
        edge4.prev = null;
        edge4.next = create.getBodyB().mEdgeList;
        if (create.getBodyB().mEdgeList != null) {
            create.getBodyB().mEdgeList.prev = create.mEdgeB;
        }
        create.getBodyB().mEdgeList = create.mEdgeB;
        return create;
    }

    public void destroyBody(Body body) {
        if (this.mBodyCount <= 0) {
            return;
        }
        Edge edge = body.mEdgeList;
        while (edge != null) {
            Edge edge2 = edge.next;
            Spring spring = edge.spring;
            if (spring != null) {
                destroySpring(spring);
            }
            body.mEdgeList = edge2;
            edge = edge2;
        }
        Body body2 = body.mPrev;
        if (body2 != null) {
            body2.mNext = body.mNext;
        }
        Body body3 = body.mNext;
        if (body3 != null) {
            body3.mPrev = body2;
        }
        if (body == this.mBodyList) {
            this.mBodyList = body3;
        }
        this.mBodyCount--;
    }

    public void destroySpring(Spring spring) {
        if (this.mSpringCount <= 0) {
            return;
        }
        Spring spring2 = spring.mPrev;
        if (spring2 != null) {
            spring2.mNext = spring.mNext;
        }
        Spring spring3 = spring.mNext;
        if (spring3 != null) {
            spring3.mPrev = spring2;
        }
        if (spring == this.mSpringList) {
            this.mSpringList = spring3;
        }
        Body bodyA = spring.getBodyA();
        Body bodyB = spring.getBodyB();
        Edge edge = spring.mEdgeA;
        Edge edge2 = edge.prev;
        if (edge2 != null) {
            edge2.next = edge.next;
        }
        Edge edge3 = edge.next;
        if (edge3 != null) {
            edge3.prev = edge2;
        }
        if (edge == bodyA.mEdgeList) {
            bodyA.mEdgeList = edge3;
        }
        edge.prev = null;
        edge.next = null;
        Edge edge4 = spring.mEdgeB;
        Edge edge5 = edge4.prev;
        if (edge5 != null) {
            edge5.next = edge4.next;
        }
        Edge edge6 = edge4.next;
        if (edge6 != null) {
            edge6.prev = edge5;
        }
        if (edge4 == bodyB.mEdgeList) {
            bodyB.mEdgeList = edge6;
        }
        edge4.prev = null;
        edge4.next = null;
        this.mSpringCount--;
    }

    public Vector getVectorTemp() {
        return this.mVectorTemp;
    }

    public void step(float f2) {
        solve(f2);
    }

    public World(Vector vector) {
        this.mVectorTemp = vector;
        this.mBodyList = null;
        this.mSpringList = null;
        this.mBodyCount = 0;
        this.mSpringCount = 0;
    }

    private void solve(Body body, float f2) {
        body.updateActiveConstraintForce();
        body.mLinearVelocity.addLocal(body.mForce.mulLocal(body.mInvMass).mulLocal(f2));
        body.mLinearVelocity.mulLocal(1.0f / ((body.mLinearDamping * f2) + 1.0f));
        for (Edge edge = body.mEdgeList; edge != null; edge = edge.next) {
            Spring spring = edge.spring;
            if (!spring.mIsSolved) {
                spring.mIsSolved = true;
                Body body2 = edge.other;
                if (!body2.mIsSolved && body2.mIsAwake) {
                    spring.initVelocityConstraints(body, f2);
                    for (int i2 = 0; i2 < 4; i2++) {
                        edge.spring.solveVelocityConstraints(body);
                    }
                }
            }
        }
        body.mWorldCenter.mX = body.mWorldCenter.mX + (body.mLinearVelocity.mX * f2);
        body.mWorldCenter.mY += f2 * body.mLinearVelocity.mY;
        body.synchronizeTransform();
    }
}