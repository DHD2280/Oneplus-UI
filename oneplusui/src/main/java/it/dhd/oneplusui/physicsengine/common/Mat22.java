package it.dhd.oneplusui.physicsengine.common;

public class Mat22 {

    public final Vector mX = new Vector();

    public final Vector mY = new Vector();

    public static void mulToOutUnsafe(Mat22 mat22, Vector vector, Vector vector2) {
        vector2.mX = mat22.mX.mX * vector.mX + (mat22.mY.mX * vector.mY);
        vector2.mY = (mat22.mX.mY * vector.mX) + (mat22.mY.mY * vector.mY);
    }

    public final void invertLocal() {
        Vector vector = this.mX;
        float f2 = vector.mX;
        Vector vector2 = this.mY;
        float f3 = vector2.mX;
        float f4 = vector.mY;
        float f5 = vector2.mY;
        float f6 = (f2 * f5) - (f3 * f4);
        if (f6 != 0.0f) {
            f6 = 1.0f / f6;
        }
        vector.mX = f5 * f6;
        float f7 = -f6;
        vector2.mX = f3 * f7;
        vector.mY = f7 * f4;
        vector2.mY = f6 * f2;
    }
}