package it.dhd.oneplusui.physicsengine.dynamics.spring;

import it.dhd.oneplusui.physicsengine.common.Vector;
import it.dhd.oneplusui.physicsengine.dynamics.Body;

public class SpringDef {

    public Body bodyA;
    public Body bodyB;
    public float dampingRatio;
    public float frequencyHz;
    public float maxForce;
    public final Vector target;

    public SpringDef() {
        Vector vector = new Vector();
        this.target = vector;
        vector.set(0.0f, 0.0f);
        this.maxForce = Float.MAX_VALUE;
        this.frequencyHz = 6.0f;
        this.dampingRatio = 0.8f;
    }

}