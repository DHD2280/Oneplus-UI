package it.dhd.oneplusui.physicsengine.common;

import androidx.annotation.NonNull;

public class Vector {

    public float mX;

    public float mY;

    public Vector() {
        this(0.0f, 0.0f);
    }

    /**
     * Adds the given value to this vector's x and y components.
     * @param addend the value to add
     * @return this vector for chaining
     */
    public final Vector addLocal(float addend) {
        mX += addend;
        mY += addend;
        return this;
    }

    /**
     * Divides this vector by the given value in place.
     * @param dividend the value to divide by
     * @return this vector for chaining
     */
    public final Vector divLocal(float dividend) {
        mX /= dividend;
        mY /= dividend;
        return this;
    }

    /**
     * Calculate the length of this vector using Pythagoras.
     * @return the length of this vector
     */
    public final float length() {
        return MathUtils.sqrt((mX * mX) + (mY * mY));
    }

    /**
     * Returns the squared length of this vector.
     * @return the squared length of this vector
     */
    public final float lengthSquared() {
        return (mX * mX) + (mY * mY);
    }

    /**
     * Multiplies this vector by the given value in place.
     * @param multiplier the value to multiply by
     * @return this vector for chaining
     */
    public final Vector mulLocal(float multiplier) {
        mX *= multiplier;
        mY *= multiplier;
        return this;
    }

    /**
     * Negates this vector in place.
     * @return this vector for chaining
     */
    public final Vector negateLocal() {
        mX = -mX;
        mY = -mY;
        return this;
    }

    /**
     * Sets this vector to the given value for both x and y components.
     * @param xy the value to set for both x and y components
     * @return this vector for chaining
     */
    public final Vector set(float xy) {
        mX = xy;
        mY = xy;
        return this;
    }

    /**
     * Sets this vector to zero.
     */
    public final void setZero() {
        mX = 0.0f;
        mY = 0.0f;
    }

    /**
     * Subtracts the given value from this vector's x and y components.
     * @param subtract the value to subtract
     * @return this vector for chaining
     */
    public final Vector subLocal(float subtract) {
        mX -= subtract;
        mY -= subtract;
        return this;
    }

    @NonNull
    @Override
    public final String toString() {
        return "(" + mX + "," + mY + ")";
    }

    public final Vector unitLocal() {
        mX = mX / MathUtils.abs(mX);
        mY = mY / MathUtils.abs(mY);
        return this;
    }

    public Vector(float x, float y) {
        mX = x;
        mY = y;
    }

    public final Vector addLocal(Vector vector) {
        mX += vector.mX;
        mY += vector.mY;
        return this;
    }

    public final Vector divLocal(Vector vector) {
        mX /= vector.mX;
        mY /= vector.mY;
        return this;
    }

    public final Vector mulLocal(Vector vector) {
        mX *= vector.mX;
        mY *= vector.mY;
        return this;
    }

    public final Vector set(float x, float y) {
        mX = x;
        mY = y;
        return this;
    }

    public final Vector subLocal(Vector vector) {
        mX -= vector.mX;
        mY -= vector.mY;
        return this;
    }

    public Vector(Vector vector) {
        mX = vector.mX;
        mY = vector.mY;
    }

    public final Vector set(Vector vector) {
        mX = vector.mX;
        mY = vector.mY;
        return this;
    }
}