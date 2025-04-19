package it.dhd.oneplusui.physicsengine.engine;

import androidx.annotation.NonNull;

public class Transform {

    public float x = 0.0f;
    public float y = 0.0f;
    public float scaleX = 1.0f;
    public float scaleY = 1.0f;

    @Override
    @NonNull
    public String toString() {
        return "Transform{x=" + this.x + ", y=" + this.y + ", scaleX=" + this.scaleX + ", scaleY=" + this.scaleY + '}';
    }
}