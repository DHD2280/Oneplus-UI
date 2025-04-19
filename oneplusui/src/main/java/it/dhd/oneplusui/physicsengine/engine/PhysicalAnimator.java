package it.dhd.oneplusui.physicsengine.engine;

import static it.dhd.oneplusui.physicsengine.engine.FloatPropertyHolder.PROPERTY_TYPE_SCALE;

import android.content.Context;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import androidx.collection.ArraySet;

import it.dhd.oneplusui.physicsengine.common.Compat;
import it.dhd.oneplusui.physicsengine.common.Debug;
import it.dhd.oneplusui.physicsengine.common.Vector;
import it.dhd.oneplusui.physicsengine.dynamics.Body;
import it.dhd.oneplusui.physicsengine.dynamics.World;
import it.dhd.oneplusui.physicsengine.dynamics.spring.Spring;
import it.dhd.oneplusui.physicsengine.dynamics.spring.SpringDef;
import java.util.HashMap;
import java.util.Iterator;

public class PhysicalAnimator implements ChoreographerCompat.AnimationFrameCallback {

    private HashMap<BaseBehavior, AnimationListener> mAnimationListeners;
    private final Context mContext;
    private Body mGround;
    private HashMap<BaseBehavior, AnimationUpdateListener> mUpdateListeners;
    private World mWorld;
    private final ArraySet<BaseBehavior> mCurrentRunningBehaviors = new ArraySet<>(1);
    private final ArraySet<BaseBehavior> mAllBehaviors = new ArraySet<>(1);
    private boolean mIsSteady = true;
    private boolean mIsAnimatorRunning = false;
    private boolean mIsCancel = false;
    private ChoreographerCompat mChoreographer = null;

    private PhysicalAnimator(Context context) {
        this.mContext = context;
        init();
    }

    private Body buildBodyProperty(UIItem uIItem, int i2) {
        Body createBody = createBody(this.mWorld.getVectorTemp().set(Compat.pixelsToPhysicalSize(uIItem.mStartPosition.mX), Compat.pixelsToPhysicalSize(uIItem.mStartPosition.mY)), 1, i2, Compat.pixelsToPhysicalSize(uIItem.mWidth), Compat.pixelsToPhysicalSize(uIItem.mHeight), descriptionOfPropertyType(i2));
        createBody.mLinearVelocity.setZero();
        createBody.setAwake(true);
        return createBody;
    }

    private void clearBehaviors() {
        int i = 0;
        while (i < this.mAllBehaviors.size()) {
            BaseBehavior valueAt = this.mAllBehaviors.valueAt(i);
            if (valueAt != null && removeBehavior(valueAt)) {
                i--;
            }
            i++;
        }
        this.mAllBehaviors.clear();
    }

    private void clearBodies() {
        for (int i2 = 0; i2 < this.mAllBehaviors.size(); i2++) {
            BaseBehavior valueAt = this.mAllBehaviors.valueAt(i2);
            if (valueAt != null) {
                destroyBody(valueAt.mPropertyBody);
            }
        }
    }

    private void clearListeners() {
        HashMap<BaseBehavior, AnimationListener> hashMap = this.mAnimationListeners;
        if (hashMap != null) {
            hashMap.clear();
        }
        HashMap<BaseBehavior, AnimationUpdateListener> hashMap2 = this.mUpdateListeners;
        if (hashMap2 != null) {
            hashMap2.clear();
        }
    }

    public static PhysicalAnimator create(Context context) {
        return new PhysicalAnimator(context);
    }

    private void createWorld() {
        this.mWorld = new World();
        this.mGround = createBody(new Vector(), 0, 5, 0.0f, 0.0f, "Ground");
        if (Debug.isDebugMode()) {
            Debug.logD("createWorld : " + this);
        }
    }

    private static String descriptionOfPropertyType(int i2) {
        return i2 != 1 ? i2 != 2 ? i2 != 3 ? i2 != 4 ? "custom" : "alpha" : "rotation" : "scale" : "position";
    }

    private void init() {
        ChoreographerCompat choreographerCompat = new ChoreographerCompat();
        this.mChoreographer = choreographerCompat;
        choreographerCompat.setFrameCallback(this);
        initConfig();
        createWorld();
    }

    private void initConfig() {
        Compat.updatePhysicalSizeToPixelsRatio(this.mContext.getResources().getDisplayMetrics().density);
        Display defaultDisplay = ((WindowManager) this.mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        if (defaultDisplay != null) {
            Compat.updateRefreshRate(1.0f / defaultDisplay.getRefreshRate());
        }
        if (Debug.isDebugMode()) {
            Debug.logD("initConfig : sPhysicalSizeToPixelsRatio =:" + Compat.sPhysicalSizeToPixelsRatio + ",sSteadyAccuracy =:" + Compat.sSteadyAccuracy + ",sRefreshRate =:" + Compat.sRefreshRate);
        }
    }

    private void onAnimationCancel(BaseBehavior baseBehavior) {
        AnimationListener animationListener;
        HashMap<BaseBehavior, AnimationListener> hashMap = this.mAnimationListeners;
        if (hashMap == null || (animationListener = hashMap.get(baseBehavior)) == null) {
            return;
        }
        animationListener.onAnimationCancel(baseBehavior);
    }

    private void onAnimationEnd(BaseBehavior baseBehavior) {
        AnimationListener animationListener;
        HashMap<BaseBehavior, AnimationListener> hashMap = this.mAnimationListeners;
        if (hashMap == null || (animationListener = hashMap.get(baseBehavior)) == null) {
            return;
        }
        animationListener.onAnimationEnd(baseBehavior);
    }

    private void onAnimationStart(BaseBehavior baseBehavior) {
        AnimationListener animationListener;
        HashMap<BaseBehavior, AnimationListener> hashMap = this.mAnimationListeners;
        if (hashMap == null || (animationListener = hashMap.get(baseBehavior)) == null) {
            return;
        }
        animationListener.onAnimationStart(baseBehavior);
    }

    private void onAnimationUpdate(BaseBehavior baseBehavior) {
        AnimationUpdateListener animationUpdateListener;
        HashMap<BaseBehavior, AnimationUpdateListener> hashMap = this.mUpdateListeners;
        if (hashMap == null || (animationUpdateListener = hashMap.get(baseBehavior)) == null) {
            return;
        }
        animationUpdateListener.onAnimationUpdate(baseBehavior);
    }

    private void pause() {
        if (this.mIsAnimatorRunning) {
            this.mChoreographer.unScheduleNextFrame();
            this.mIsAnimatorRunning = false;
        }
    }

    private void resume() {
        if (this.mIsAnimatorRunning) {
            return;
        }
        this.mChoreographer.scheduleNextFrame();
        this.mIsAnimatorRunning = true;
    }

    public static final FloatPropertyHolder scaleX() {

        return new FloatPropertyHolder<UIItem<View>>("scaleX", PROPERTY_TYPE_SCALE) {
            @Override
            public float getValue(UIItem<View> uIItem) {
                return uIItem.mTarget.getScaleX();
            }

            @Override
            public void onValueSet(UIItem<View> uIItem, float f2) {
                uIItem.mTarget.setScaleX(f2);
            }

            @Override
            public void update(UIItem uIItem) {
                setValue(uIItem, uIItem.mTransform.scaleX);
            }

            @Override
            public void verifyStartValue(UIItem<View> uIItem) {
                if (this.mIsStartValueSet) {
                    uIItem.mStartScale.mX = this.mStartValue;
                } else {
                    uIItem.mStartScale.mX = uIItem.mTarget.getScaleX();
                    this.mStartValue = uIItem.mStartScale.mX;
                }
            }
        };
    }

    public static final FloatPropertyHolder scaleY() {
        return new FloatPropertyHolder<UIItem<View>>("scaleY", PROPERTY_TYPE_SCALE) {
            @Override
            public float getValue(UIItem<View> uIItem) {
                return uIItem.mTarget.getScaleY();
            }

            @Override
            public void onValueSet(UIItem<View> uIItem, float f2) {
                uIItem.mTarget.setScaleY(f2);
            }

            @Override
            public void update(UIItem uIItem) {
                setValue(uIItem, uIItem.mTransform.scaleY);
            }

            @Override
            public void verifyStartValue(UIItem<View> uIItem) {
                if (this.mIsStartValueSet) {
                    uIItem.mStartScale.mY = this.mStartValue;
                } else {
                    uIItem.mStartScale.mY = uIItem.mTarget.getScaleY();
                    this.mStartValue = uIItem.mStartScale.mY;
                }
            }
        };
    }

    private void stepWorld() {
        this.mWorld.step(Compat.sRefreshRate);
        syncMoverChanging();
    }

    private void syncMoverChanging() {
        if (Debug.debugFrame()) {
            Debug.logD(Debug.FRAME_LOG_TAG, "syncMoverChanging start ===========> mCurrentRunningBehaviors =:" + this.mCurrentRunningBehaviors.size());
        }
        for (BaseBehavior behavior : this.mCurrentRunningBehaviors) {
            if (behavior != null) {
                behavior.dispatchChanging();
                updateValue(behavior);
                onAnimationUpdate(behavior);
                if (Debug.debugFrame()) {
                    Debug.logD(Debug.FRAME_LOG_TAG, "updateBehavior : " + behavior);
                }
                if (behavior.isSteady()) {
                    if (Debug.isDebugMode()) {
                        Debug.logD("syncMoverChanging : behavior is steady");
                    }
                    behavior.stopBehavior();
                }
            }
        }
        this.mIsSteady = this.mCurrentRunningBehaviors.isEmpty();
        if (Debug.debugFrame()) {
            Debug.logD(Debug.FRAME_LOG_TAG, "syncMoverChanging end ===========> mCurrentRunningBehaviors =:" + this.mCurrentRunningBehaviors.size());
        }
        if (this.mIsSteady) {
            pause();
        } else {
            this.mChoreographer.scheduleNextFrame();
        }
    }

    public static final FloatPropertyHolder x() {
        return new FloatPropertyHolder<UIItem<View>>("x", 1) {
            @Override
            public float getValue(UIItem<View> uIItem) {
                return uIItem.mTarget.getX();
            }

            @Override
            public void onValueSet(UIItem<View> uIItem, float f2) {
                uIItem.mTarget.setX(f2);
            }

            @Override
            public void update(UIItem uIItem) {
                setValue(uIItem, uIItem.mTransform.x);
            }

            @Override
            public void verifyStartValue(UIItem<View> uIItem) {
                if (this.mIsStartValueSet) {
                    uIItem.mStartPosition.mX = this.mStartValue;
                } else {
                    uIItem.mStartPosition.mX = uIItem.mTarget.getX();
                    this.mStartValue = uIItem.mStartPosition.mX;
                }
            }
        };
    }

    public static final FloatPropertyHolder y() {
        return new FloatPropertyHolder<UIItem<View>>("y", 1) {
            @Override
            public float getValue(UIItem<View> uIItem) {
                return uIItem.mTarget.getY();
            }

            @Override
            public void onValueSet(UIItem<View> uIItem, float f2) {
                uIItem.mTarget.setY(f2);
            }

            @Override
            public void update(UIItem uIItem) {
                setValue(uIItem, uIItem.mTransform.y);
            }

            @Override
            public void verifyStartValue(UIItem<View> uIItem) {
                if (this.mIsStartValueSet) {
                    uIItem.mStartPosition.mY = this.mStartValue;
                } else {
                    uIItem.mStartPosition.mY = uIItem.mTarget.getY();
                    this.mStartValue = uIItem.mStartPosition.mY;
                }
            }
        };
    }

    public void addAnimationListener(BaseBehavior baseBehavior, AnimationListener animationListener) {
        if (this.mAnimationListeners == null) {
            this.mAnimationListeners = new HashMap<>(1);
        }
        this.mAnimationListeners.put(baseBehavior, animationListener);
    }

    public void addAnimationUpdateListener(BaseBehavior baseBehavior, AnimationUpdateListener animationUpdateListener) {
        if (this.mUpdateListeners == null) {
            this.mUpdateListeners = new HashMap<>(1);
        }
        this.mUpdateListeners.put(baseBehavior, animationUpdateListener);
    }

    public void addBehavior(BaseBehavior... baseBehaviorArr) {
        for (BaseBehavior baseBehavior : baseBehaviorArr) {
            addBehavior(baseBehavior);
        }
    }

    public void cancel(String str) {
        if (this.mIsCancel) {
            return;
        }
        if (Debug.isDebugMode()) {
            Debug.logD("cancel with reason : " + str);
        }
        for (int i2 = 0; i2 < this.mCurrentRunningBehaviors.size(); i2++) {
            BaseBehavior valueAt = this.mCurrentRunningBehaviors.valueAt(i2);
            if (valueAt != null) {
                onAnimationCancel(valueAt);
            }
        }
        pause();
        this.mIsCancel = true;
    }

    public Body createBody(Vector vector, int i2, int i3, float f2, float f3, String str) {
        return this.mWorld.createBody(vector, i2, i3, f2, f3, str);
    }

    public Spring createSpring(SpringDef springDef) {
        return this.mWorld.createSpring(springDef);
    }

    public boolean destroyBody(Body body) {
        if (body == null) {
            return false;
        }
        this.mWorld.destroyBody(body);
        return true;
    }

    public boolean destroySpring(Spring spring) {
        this.mWorld.destroySpring(spring);
        return true;
    }

    @Override
    public void doFrame(long j2) {
        if (this.mIsCancel) {
            return;
        }
        stepWorld();
    }

    public Body getGround() {
        return this.mGround;
    }

    public Body getOrCreatePropertyBody(UIItem uIItem, int i2) {
        Body body;
        if (Debug.isDebugMode()) {
            Debug.logD("getOrCreatePropertyBody : uiItem =:" + uIItem + ",propertyType =:" + i2);
        }
        for (BaseBehavior behavior : this.mAllBehaviors) {
            UIItem activeUIItem = behavior.mActiveUIItem;
            if (activeUIItem != null && activeUIItem == uIItem && (body = behavior.mPropertyBody) != null && body.getProperty() == i2) {
                return behavior.mPropertyBody;
            }
        }
        return buildBodyProperty(uIItem, i2);
    }

    public UIItem getOrCreateUIItem(Object obj) {
        Object obj2;
        if (Debug.isDebugMode()) {
            Debug.logD("getOrCreateUIItem : target =:" + obj);
        }
        Iterator<BaseBehavior> it = this.mAllBehaviors.iterator();
        while (it.hasNext()) {
            UIItem uIItem = it.next().mActiveUIItem;
            if (uIItem != null && (obj2 = uIItem.mTarget) != null && obj != null && obj2 == obj) {
                return uIItem;
            }
        }
        if (!(obj instanceof View)) {
            return obj instanceof UIItem ? (UIItem) obj : new UIItem().setSize(0.0f, 0.0f);
        }
        View view = (View) obj;
        UIItem size = new UIItem(obj).setSize(view.getMeasuredWidth(), view.getMeasuredHeight());
        size.setStartPosition(view.getX(), view.getY());
        size.setStartScale(view.getScaleX(), view.getScaleY());
        return size;
    }

    public boolean isAnimatorRunning() {
        return this.mIsAnimatorRunning;
    }

    public boolean isCancel() {
        return this.mIsCancel;
    }

    public void release() {
        cancel("release");
        clearBodies();
        clearListeners();
        clearBehaviors();
        if (Debug.isDebugMode()) {
            Debug.logD("release : " + this);
        }
    }

    public boolean removeBehavior(BaseBehavior baseBehavior) {
        if (baseBehavior == null) {
            return false;
        }
        boolean remove = this.mAllBehaviors.remove(baseBehavior);
        if (Debug.isDebugMode()) {
            Debug.logD("removeBehavior behavior =:" + baseBehavior + ",removed =:" + remove);
        }
        if (remove) {
            baseBehavior.onRemove();
        }
        return remove;
    }

    public void removeListener(BaseBehavior baseBehavior) {
        HashMap<BaseBehavior, AnimationListener> hashMap = this.mAnimationListeners;
        if (hashMap != null) {
            hashMap.remove(baseBehavior);
        }
        HashMap<BaseBehavior, AnimationUpdateListener> hashMap2 = this.mUpdateListeners;
        if (hashMap2 != null) {
            hashMap2.remove(baseBehavior);
        }
    }

    public void restart() {
        if (this.mIsCancel) {
            if (Debug.isDebugMode()) {
                Debug.logD("restart");
            }
            this.mIsCancel = false;
            resume();
            for (int i2 = 0; i2 < this.mCurrentRunningBehaviors.size(); i2++) {
                BaseBehavior valueAt = this.mCurrentRunningBehaviors.valueAt(i2);
                if (valueAt != null) {
                    onAnimationStart(valueAt);
                }
            }
        }
    }

    public void setDebugMode(Boolean bool) {
        Debug.setDebugMode(bool.booleanValue());
    }

    public void startBehavior(BaseBehavior baseBehavior) {
        Object obj;
        Object obj2;
        Body body;
        Body body2;
        if (this.mIsCancel) {
            return;
        }
        if (this.mCurrentRunningBehaviors.contains(baseBehavior) && this.mIsAnimatorRunning) {
            return;
        }
        if (Debug.isDebugMode()) {
            Debug.logD("startBehavior behavior =:" + baseBehavior);
        }
        int i2 = 0;
        while (i2 < this.mCurrentRunningBehaviors.size()) {
            BaseBehavior valueAt = this.mCurrentRunningBehaviors.valueAt(i2);
            if (valueAt != null && (obj = valueAt.mTarget) != null && (obj2 = baseBehavior.mTarget) != null && obj == obj2 && (body = valueAt.mPropertyBody) != null && (body2 = baseBehavior.mPropertyBody) != null && body == body2 && valueAt.stopBehavior()) {
                i2--;
            }
            i2++;
        }
        this.mCurrentRunningBehaviors.add(baseBehavior);
        this.mIsSteady = false;
        resume();
        onAnimationStart(baseBehavior);
    }

    public void stopBehavior(BaseBehavior baseBehavior) {
        this.mCurrentRunningBehaviors.remove(baseBehavior);
        if (Debug.isDebugMode()) {
            Debug.logD("stopBehavior behavior =:" + baseBehavior + ",mCurrentRunningBehaviors.size() =:" + this.mCurrentRunningBehaviors.size());
        }
        onAnimationEnd(baseBehavior);
    }

    public void updateValue(BaseBehavior baseBehavior) {
        baseBehavior.updateProperties();
    }

    public <T extends BaseBehavior> T addBehavior(T t2) {
        Object obj;
        Object obj2;
        t2.bindAnimator(this);
        int i2 = 0;
        while (i2 < this.mAllBehaviors.size()) {
            BaseBehavior valueAt = this.mAllBehaviors.valueAt(i2);
            if (valueAt != null && (obj = valueAt.mTarget) != null && (obj2 = t2.mTarget) != null && obj == obj2 && valueAt.getType() == t2.getType() && removeBehavior(valueAt)) {
                i2--;
            }
            i2++;
        }
        this.mAllBehaviors.add(t2);
        if (Debug.isDebugMode()) {
            Debug.logD("addBehavior behavior =:" + t2 + ",mAllBehaviors.size =:" + this.mAllBehaviors.size());
        }
        return t2;
    }

    public void addAnimationListener(AnimationListener animationListener, BaseBehavior... baseBehaviorArr) {
        for (BaseBehavior baseBehavior : baseBehaviorArr) {
            addAnimationListener(baseBehavior, animationListener);
        }
    }

    public void addAnimationUpdateListener(AnimationUpdateListener animationUpdateListener, BaseBehavior... baseBehaviorArr) {
        for (BaseBehavior baseBehavior : baseBehaviorArr) {
            addAnimationUpdateListener(baseBehavior, animationUpdateListener);
        }
    }
}
