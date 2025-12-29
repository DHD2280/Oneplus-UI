package it.dhd.oneplusui.appcompat.animation.dynamic;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Choreographer;
import androidx.annotation.RequiresApi;
import androidx.collection.SimpleArrayMap;
import java.util.ArrayList;

class OplusAnimationHandler {

    private static final long FRAME_DELAY_MS = 10;
    public static final ThreadLocal<OplusAnimationHandler> sAnimatorHandler = new ThreadLocal<>();
    private AnimationFrameCallbackProvider mProvider;
    private final SimpleArrayMap<AnimationFrameCallback, Long> mDelayedCallbackStartTime = new SimpleArrayMap<>();
    final ArrayList<AnimationFrameCallback> mAnimationCallbacks = new ArrayList<>();
    private final AnimationCallbackDispatcher mCallbackDispatcher = new AnimationCallbackDispatcher();
    long mCurrentFrameTime = 0;
    private boolean mListDirty = false;

    public class AnimationCallbackDispatcher {
        public AnimationCallbackDispatcher() {
        }

        public void dispatchAnimationFrame() {
            mCurrentFrameTime = SystemClock.uptimeMillis();
            doAnimationFrame(mCurrentFrameTime);
            if (!mAnimationCallbacks.isEmpty()) {
                getProvider().postFrameCallback();
            }
        }
    }

    public interface AnimationFrameCallback {
        boolean doAnimationFrame(long j2);
    }

    public static abstract class AnimationFrameCallbackProvider {
        final AnimationCallbackDispatcher mDispatcher;

        public AnimationFrameCallbackProvider(AnimationCallbackDispatcher animationCallbackDispatcher) {
            mDispatcher = animationCallbackDispatcher;
        }

        public abstract void postFrameCallback();
    }

    public static class FrameCallbackProvider14 extends AnimationFrameCallbackProvider {
        private final Handler mHandler;
        long mLastFrameTime;
        private final Runnable mRunnable;

        public FrameCallbackProvider14(AnimationCallbackDispatcher animationCallbackDispatcher) {
            super(animationCallbackDispatcher);
            mLastFrameTime = -1L;
            mRunnable = () -> {
                mLastFrameTime = SystemClock.uptimeMillis();
                mDispatcher.dispatchAnimationFrame();
            };
            mHandler = new Handler(Looper.myLooper());
        }

        @Override
        public void postFrameCallback() {
            mHandler.postDelayed(mRunnable, Math.max(FRAME_DELAY_MS - (SystemClock.uptimeMillis() - mLastFrameTime), 0L));
        }
    }

    @RequiresApi(16)
    public static class FrameCallbackProvider16 extends AnimationFrameCallbackProvider {
        private final Choreographer mChoreographer;
        private final Choreographer.FrameCallback mChoreographerCallback;

        public FrameCallbackProvider16(AnimationCallbackDispatcher animationCallbackDispatcher) {
            super(animationCallbackDispatcher);
            mChoreographer = Choreographer.getInstance();
            mChoreographerCallback = j2 -> mDispatcher.dispatchAnimationFrame();
        }

        @Override
        public void postFrameCallback() {
            mChoreographer.postFrameCallback(mChoreographerCallback);
        }
    }

    private void cleanUpList() {
        if (mListDirty) {
            for (int size = mAnimationCallbacks.size() - 1; size >= 0; size--) {
                if (mAnimationCallbacks.get(size) == null) {
                    mAnimationCallbacks.remove(size);
                }
            }
            mListDirty = false;
        }
    }

    public static long getFrameTime() {
        ThreadLocal<OplusAnimationHandler> threadLocal = sAnimatorHandler;
        if (threadLocal.get() == null) {
            return 0L;
        }
        return threadLocal.get().mCurrentFrameTime;
    }

    public static OplusAnimationHandler getInstance() {
        ThreadLocal<OplusAnimationHandler> threadLocal = sAnimatorHandler;
        if (threadLocal.get() == null) {
            threadLocal.set(new OplusAnimationHandler());
        }
        return threadLocal.get();
    }

    private boolean isCallbackDue(AnimationFrameCallback animationFrameCallback, long j2) {
        Long l2 = mDelayedCallbackStartTime.get(animationFrameCallback);
        if (l2 == null) {
            return true;
        }
        if (l2 >= j2) {
            return false;
        }
        mDelayedCallbackStartTime.remove(animationFrameCallback);
        return true;
    }

    public void addAnimationFrameCallback(AnimationFrameCallback animationFrameCallback, long j2) {
        if (mAnimationCallbacks.isEmpty()) {
            getProvider().postFrameCallback();
        }
        if (!mAnimationCallbacks.contains(animationFrameCallback)) {
            mAnimationCallbacks.add(animationFrameCallback);
        }
        if (j2 > 0) {
            mDelayedCallbackStartTime.put(animationFrameCallback, Long.valueOf(SystemClock.uptimeMillis() + j2));
        }
    }

    public void doAnimationFrame(long j2) {
        long uptimeMillis = SystemClock.uptimeMillis();
        for (int i2 = 0; i2 < mAnimationCallbacks.size(); i2++) {
            AnimationFrameCallback animationFrameCallback = mAnimationCallbacks.get(i2);
            if (animationFrameCallback != null && isCallbackDue(animationFrameCallback, uptimeMillis)) {
                animationFrameCallback.doAnimationFrame(j2);
            }
        }
        cleanUpList();
    }

    public AnimationFrameCallbackProvider getProvider() {
        if (mProvider == null) {
            mProvider = new FrameCallbackProvider16(mCallbackDispatcher);
        }
        return mProvider;
    }

    public void removeCallback(AnimationFrameCallback animationFrameCallback) {
        mDelayedCallbackStartTime.remove(animationFrameCallback);
        int indexOf = mAnimationCallbacks.indexOf(animationFrameCallback);
        if (indexOf >= 0) {
            mAnimationCallbacks.set(indexOf, null);
            mListDirty = true;
        }
    }

    public void setProvider(AnimationFrameCallbackProvider animationFrameCallbackProvider) {
        mProvider = animationFrameCallbackProvider;
    }
}
