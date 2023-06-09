package com.mct.base.ui;

import android.util.Log;
import android.util.Pair;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

public abstract class BaseOverlayLifecycle implements LifecycleOwner {

    private static final String TAG = "OverlayController";

    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }

    /**
     * Tells if the overlay is shown.
     */
    protected boolean isShowing = false;

    /**
     * OverlayController for an overlay shown from this OverlayController using [showSubOverlay].
     * Null if none has been shown, or if a previous sub OverlayController has been dismissed.
     */
    private BaseOverlayLifecycle subBaseOverlayLifecycle;
    /**
     * Listener called when the overlay shown by the controller is dismissed.
     * Null unless the overlay is shown.
     */
    private OnDismissListener onDismissListener;

    /**
     * Call to [showSubOverlay] that has been made while hidden.
     * It will be executed once [start] is called.
     */
    private Pair<BaseOverlayLifecycle, Boolean> pendingSubOverlayRequest;


    /**
     * Creates the ui object to be shown.
     */
    protected abstract void onCreate();

    /**
     * Show the ui object to the user.
     */
    protected void onStart() {
    }

    /**
     * Hide the ui object from the user.
     */
    protected void onStop() {
    }

    /**
     * Destroys the ui object.
     */
    protected abstract void onDismissed();

    /**
     * Creates and show the ui object.
     * If the lifecycle doesn't allows it, does nothing.
     *
     * @param dismissListener object notified upon the shown ui dismissing.
     */
    public void create(OnDismissListener dismissListener) {
        if (lifecycleRegistry.getCurrentState() != Lifecycle.State.INITIALIZED) {
            return;
        }
        Log.d(TAG, "create overlay " + hashCode());
        onDismissListener = dismissListener;
        lifecycleRegistry.setCurrentState(Lifecycle.State.CREATED);
        onCreate();
        lifecycleRegistry.setCurrentState(Lifecycle.State.STARTED);
        start();
    }

    /**
     * Show the ui object.
     * If the lifecycle doesn't allows it, does nothing.
     */
    @CallSuper
    public void start() {
        if (lifecycleRegistry.getCurrentState() != Lifecycle.State.STARTED) {
            return;
        }
        Log.d(TAG, "show overlay " + hashCode());
        lifecycleRegistry.setCurrentState(Lifecycle.State.RESUMED);
        onStart();
    }

    /**
     * Hide the ui object.
     * If the lifecycle doesn't allows it, does nothing.
     */
    @CallSuper
    public void stop(boolean hideUi) {
        if (!lifecycleRegistry.getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            return;
        }
        Log.d(TAG, "hide overlay " + hashCode());
        lifecycleRegistry.setCurrentState(Lifecycle.State.STARTED);
        onStop();
    }

    /**
     * Dismiss the ui object. If not hidden, hide it first.
     * If the lifecycle doesn't allows it, does nothing.
     */
    public void dismiss() {
        if (!lifecycleRegistry.getCurrentState().isAtLeast(Lifecycle.State.CREATED)) {
            lifecycleRegistry.setCurrentState(Lifecycle.State.DESTROYED);
            return;
        }
        Log.d(TAG, "dismiss overlay " + hashCode());
        stop(false);
        lifecycleRegistry.setCurrentState(Lifecycle.State.DESTROYED);
        if (subBaseOverlayLifecycle != null) {
            subBaseOverlayLifecycle.dismiss();
        }
        onDismissed();
        if (onDismissListener != null) {
            onDismissListener.onDismiss();
            onDismissListener = null;
        }
    }

    public boolean isShowing() {
        return isShowing;
    }

    /**
     * Creates and show another overlay managed by a OverlayController from this dialog.
     * <p>
     * Using this method instead of directly calling [create] and [start] on the new OverlayController will allow to keep
     * a back stack of OverlayController, allowing to resume the current overlay once the new overlay is dismissed.
     *
     * @param baseOverlayLifecycle the controller of the new overlay to be shown.
     * @param hideCurrent       true to hide the current overlay, false to display the new overlay over it.
     */
    @CallSuper
    protected void showSubOverlay(BaseOverlayLifecycle baseOverlayLifecycle, Boolean hideCurrent) {
        if (!lifecycleRegistry.getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            Log.e(TAG, "Can't show " + baseOverlayLifecycle.hashCode() + ", parent " + hashCode() + " is not created");
            return;
        } else if (!lifecycleRegistry.getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            Log.i(TAG,
                    "Delaying sub overlay: " + baseOverlayLifecycle.hashCode() +
                            "; hide=" + hideCurrent +
                            "; parent=" + hashCode());
            pendingSubOverlayRequest = new Pair<>(baseOverlayLifecycle, hideCurrent);
            return;
        }

        Log.d(TAG, "show sub overlay: " + baseOverlayLifecycle.hashCode() +
                "; hide=" + hideCurrent +
                "; parent=" + hashCode());

        subBaseOverlayLifecycle = baseOverlayLifecycle;
        stop(hideCurrent);

        baseOverlayLifecycle.create(() -> onSubOverlayDismissed(baseOverlayLifecycle));
    }

    /**
     * Listener upon the closing of a overlay opened with [showSubOverlay].
     *
     * @param dismissedOverlay the sub overlay dismissed.
     */
    private void onSubOverlayDismissed(@NonNull BaseOverlayLifecycle dismissedOverlay) {
        Log.d(TAG, "sub overlay dismissed: " + dismissedOverlay.hashCode() + " parent=" + hashCode());

        if (dismissedOverlay == subBaseOverlayLifecycle) {
            subBaseOverlayLifecycle = null;
            if (lifecycleRegistry.getCurrentState() == Lifecycle.State.DESTROYED) return;

            start();

            if (pendingSubOverlayRequest != null) {
                showSubOverlay(pendingSubOverlayRequest.first, pendingSubOverlayRequest.second);
                pendingSubOverlayRequest = null;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // INTERFACE
    ///////////////////////////////////////////////////////////////////////////

    public interface OnDismissListener {
        void onDismiss();
    }

}
