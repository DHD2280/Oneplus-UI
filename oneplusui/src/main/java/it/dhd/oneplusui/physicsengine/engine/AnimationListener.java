package it.dhd.oneplusui.physicsengine.engine;

public interface AnimationListener {
    default void onAnimationCancel(BaseBehavior baseBehavior) {
    }

    default void onAnimationEnd(BaseBehavior baseBehavior) {
    }

    default void onAnimationStart(BaseBehavior baseBehavior) {
    }
}