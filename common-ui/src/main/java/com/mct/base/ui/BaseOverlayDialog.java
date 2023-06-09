package com.mct.base.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AlertDialog;

import com.mct.base.ui.utils.ScreenUtils;

public abstract class BaseOverlayDialog extends BaseOverlayLifecycle {

    private static final String TAG = "MCT_B_Dialog";

    /**
     * A Context object used to create the dialog.
     */
    protected Context mContext;

    /**
     * The Android InputMethodManger, for ensuring the keyboard dismiss on dialog dismiss.
     */
    private final InputMethodManager mInputManager;

    /**
     * The View currently being displayed by the dialog.
     * Initially, the value of mView is null and will be set
     * when method {@link #onCreateView(LayoutInflater)} called.
     */
    private View mView = null;

    /**
     * The AlertDialog currently being displayed by this controller.
     * Initially, the value of mDialog is null and will be set
     * when method {@link #onCreateDialog()} called.
     */
    private AlertDialog mDialog = null;

    /**
     * Constructor
     *
     * @param context A Context object used to create the dialog.
     */
    public BaseOverlayDialog(@NonNull Context context) {
        mContext = context;
        mInputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    /**
     * An abstract method to create a View for the dialog.
     * This method is called in the {@link #onCreateDialog()} method.
     *
     * @param inflater helper inflater
     * @return a View to be displayed by the dialog.
     */
    protected abstract View onCreateView(@NonNull LayoutInflater inflater);

    /**
     * An abstract method to create an AlertDialog.Builder for the dialog.
     * This method is called in the @{link onCreate()} method and returns an AlertDialog.Builder
     * to be used to create an AlertDialog.
     */
    @NonNull
    protected abstract AlertDialog.Builder onCreateDialog();

    /**
     * An abstract method called after the dialog is created.
     * This method provides the subclass with an opportunity to perform any
     * necessary operations on the dialog and View that was created.
     *
     * @param dialog the dialog was created
     * @param view   the view was created
     */
    protected abstract void onDialogCreated(@NonNull AlertDialog dialog, View view);

    /**
     * Creates the dialog option
     *
     * @return The option can be modify the dialog window
     */
    @Nullable
    protected abstract DialogOption onCreateDialogOption();

    @Override
    protected final void onCreate() {
        DialogInterface.OnDismissListener onDismissListener = d -> {
            dismiss();
            hideSoftInput();
            onDialogDismissed();
        };
        DialogInterface.OnKeyListener onKeyListener = (d, i, e) -> {
            if (e.getKeyCode() == KeyEvent.KEYCODE_BACK && e.getAction() == KeyEvent.ACTION_UP) {
                if (!onBackPressed()) {
                    dismiss();
                }
                return true;
            }
            return false;
        };
        mView = onCreateView(LayoutInflater.from(mContext));
        mDialog = onCreateDialog()
                .setView(mView)
                .setOnDismissListener(onDismissListener)
                .setOnKeyListener(onKeyListener)
                .create();
        initWindow(mDialog.getWindow(), onCreateDialogOption());
        onDialogCreated(mDialog, mView);
    }

    @Override
    public final void start() {
        if (!isShowing) {
            isShowing = true;
            mDialog.show();
            onVisibilityChanged(true);
        }
        super.start();
    }

    @Override
    public final void stop(boolean hideUi) {
        if (hideUi && isShowing) {
            hideSoftInput();
            isShowing = false;
            mDialog.hide();
            onVisibilityChanged(false);
        }
        super.stop(hideUi);
    }

    @Override
    protected final void onDismissed() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    @Override
    public final void showSubOverlay(BaseOverlayLifecycle baseOverlayLifecycle, Boolean hideCurrent) {
        super.showSubOverlay(baseOverlayLifecycle, hideCurrent);
        hideSoftInput();
    }

    @Nullable
    public AlertDialog getDialog() {
        return mDialog;
    }

    @Nullable
    public View getView() {
        return mView;
    }

    public <T extends View> T findViewById(@IdRes int id) {
        return mView.findViewById(id);
    }

    @NonNull
    public final <T extends View> T requireViewById(@IdRes int id) {
        T view = findViewById(id);
        if (view == null) {
            throw new IllegalArgumentException("ID does not reference a View inside this Window");
        }
        return view;
    }

    /**
     * Show the soft keyboard.
     */
    public void showSoftInput(@NonNull View view) {
        view.requestFocus();
        mInputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * Hide the soft keyboard.
     */
    public void hideSoftInput() {
        mInputManager.hideSoftInputFromWindow(mDialog.getWindow().getDecorView().getWindowToken(), 0);
    }

    ///////////////////////////////////////////////////////////////////////////
    // PROTECTED AREA < can override to set or change property >
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Called when the visibility of the dialog has changed due
     * to a call to {@link #start()} or {@link #stop(boolean)}.
     * <p>
     * Once the sub element is dismissed, this method will be called again,
     * notifying for the new visibility of the dialog.
     *
     * @param visible the dialog visibility value. True for visible, false for hidden.
     */
    protected void onVisibilityChanged(boolean visible) {
        Log.e(TAG, (visible ? "show overlay " : "hide overlay ") + hashCode());
    }

    @SuppressWarnings("unused")
    protected void onInitWindow(@NonNull Window window) {
    }

    /**
     * Handle backPress if need
     *
     * @return true if have handle backPress.
     */
    protected boolean onBackPressed() {
        return false;
    }

    /**
     * Handle the dialog dismissing.
     */
    @CallSuper
    protected void onDialogDismissed() {
        isShowing = false;
        mView = null;
        mDialog = null;
        mContext = null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // PRIVATE AREA
    ///////////////////////////////////////////////////////////////////////////

    private void initWindow(Window window, DialogOption opt) {
        if (opt == null) {
            opt = new DialogOption.Builder().build();
        }
        // overlay
        if (opt.type != DialogOption.UNSET) {
            window.setType(opt.type);
        }
        // animations
        if (opt.windowAnimation != DialogOption.UNSET) {
            window.getAttributes().windowAnimations = opt.windowAnimation;
        }
        // background
        if (opt.backgroundResource != DialogOption.UNSET) {
            window.setBackgroundDrawableResource(opt.backgroundResource);
        } else {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(opt.backgroundColor);
            drawable.setCornerRadius(ScreenUtils.dp2px(opt.backgroundCornerRadius));
            window.setBackgroundDrawable(new InsetDrawable(drawable, ScreenUtils.dp2px(16)));
        }
        // soft input
        window.setSoftInputMode(opt.softInputMode);
        // auto hide soft input
        window.getDecorView().setOnTouchListener((view, e) -> {
            hideSoftInput();
            view.performClick();
            return false;
        });
        onInitWindow(window);
    }


    public static class DialogOption {
        public static final int UNSET = 0;
        int type;
        int windowAnimation;
        int backgroundResource;
        int backgroundColor;
        int backgroundCornerRadius;
        int softInputMode;

        DialogOption(int type, int windowAnimation, int backgroundResource,
                     int backgroundColor, int backgroundCornerRadius, int softInputMode) {
            this.type = type;
            this.windowAnimation = windowAnimation;
            this.backgroundResource = backgroundResource;
            this.backgroundColor = backgroundColor;
            this.backgroundCornerRadius = backgroundCornerRadius;
            this.softInputMode = softInputMode;
        }

        public static class Builder {
            private int type = UNSET;
            private int windowAnimation = UNSET;
            private int backgroundResource = UNSET;
            private int backgroundColor = Color.WHITE;
            private int backgroundCornerRadius = 0;
            private int softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;

            /**
             * See more<br/>
             * {@link WindowManager.LayoutParams#TYPE_PHONE}<br/>
             * {@link WindowManager.LayoutParams#TYPE_APPLICATION_OVERLAY}<br/>
             * {@link WindowManager.LayoutParams#TYPE_ACCESSIBILITY_OVERLAY}<br/>
             */
            public Builder setType(int type) {
                this.type = type;
                return this;
            }

            public Builder setWindowAnimation(@StyleRes int windowAnimation) {
                this.windowAnimation = windowAnimation;
                return this;
            }

            /**
             * BackgroundResource
             * Background of dialog window
             * Note : BackgroundDrawableResource | just accept res has tail ".xml"
             */
            public Builder setBackgroundResource(int backgroundResource) {
                this.backgroundResource = backgroundResource;
                return this;
            }

            /**
             * BackgroundColor
             * Color of dialog window
             */
            public Builder setBackgroundColor(@ColorInt int backgroundColor) {
                this.backgroundColor = backgroundColor;
                return this;
            }

            /**
             * CornerRadius
             *
             * @return corner of dialog </br>
             * corner >= 0 | Unit : dp
             */
            public Builder setBackgroundCornerRadius(int backgroundCornerRadius) {
                this.backgroundCornerRadius = backgroundCornerRadius;
                return this;
            }

            /**
             * See more:<br/>
             * {@link WindowManager.LayoutParams#SOFT_INPUT_STATE_UNSPECIFIED}<br/>
             * {@link WindowManager.LayoutParams#SOFT_INPUT_STATE_UNCHANGED}<br/>
             * {@link WindowManager.LayoutParams#SOFT_INPUT_STATE_HIDDEN}<br/>
             * {@link WindowManager.LayoutParams#SOFT_INPUT_STATE_ALWAYS_HIDDEN}<br/>
             * {@link WindowManager.LayoutParams#SOFT_INPUT_STATE_VISIBLE}<br/>
             * {@link WindowManager.LayoutParams#SOFT_INPUT_STATE_ALWAYS_VISIBLE}<br/>
             * {@link WindowManager.LayoutParams#SOFT_INPUT_ADJUST_UNSPECIFIED}<br/>
             * {@link WindowManager.LayoutParams#SOFT_INPUT_ADJUST_RESIZE}<br/>
             * {@link WindowManager.LayoutParams#SOFT_INPUT_ADJUST_PAN}<br/>
             * {@link WindowManager.LayoutParams#SOFT_INPUT_ADJUST_NOTHING}<br/>
             * {@link WindowManager.LayoutParams#SOFT_INPUT_IS_FORWARD_NAVIGATION}<br/>
             */
            public Builder setSoftInputMode(int softInputMode) {
                this.softInputMode = softInputMode;
                return this;
            }

            public DialogOption build() {
                return new DialogOption(type, windowAnimation, backgroundResource, backgroundColor, backgroundCornerRadius, softInputMode);
            }
        }
    }

}