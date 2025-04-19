package it.dhd.oneplusui.physicsengine.engine;

import android.view.Choreographer;
import it.dhd.oneplusui.physicsengine.common.Debug;

class ChoreographerCompat {

    private AnimationFrameCallback mFrameCallback;

    private final Choreographer.FrameCallback mChoreographerFrameCallback = ChoreographerCompat.this::onFrame;
    private boolean mFrameScheduled = false;
    private final Choreographer mChoreographer = Choreographer.getInstance();

    public interface AnimationFrameCallback {
        void doFrame(long j2);
    }

    public void onFrame(long j2) {
        this.mFrameScheduled = false;
        if (this.mFrameCallback != null) {
            if (Debug.debugFrame()) {
                Debug.logD(Debug.FRAME_LOG_TAG, "doFrame ----------------------- frameTime =:" + j2);
            }
            this.mFrameCallback.doFrame(j2);
        }
    }

    public void scheduleNextFrame() {
        if (this.mFrameScheduled || this.mFrameCallback == null) {
            return;
        }
        this.mChoreographer.postFrameCallback(this.mChoreographerFrameCallback);
        if (Debug.debugFrame()) {
            Debug.logD(Debug.FRAME_LOG_TAG, "scheduleNextFrame ----------------------- ");
        }
        this.mFrameScheduled = true;
    }

    public void setFrameCallback(AnimationFrameCallback animationFrameCallback) {
        this.mFrameCallback = animationFrameCallback;
    }

    public void unScheduleNextFrame() {
        if (this.mFrameScheduled) {
            if (Debug.debugFrame()) {
                Debug.logD(Debug.FRAME_LOG_TAG, "unScheduleNextFrame ----------------------- ");
            }
            this.mChoreographer.removeFrameCallback(this.mChoreographerFrameCallback);
            this.mFrameScheduled = false;
        }
    }
}