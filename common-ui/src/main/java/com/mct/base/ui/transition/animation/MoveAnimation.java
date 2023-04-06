package com.mct.base.ui.transition.animation;

import static com.mct.base.ui.transition.annotation.AnimationDirection.DOWN;
import static com.mct.base.ui.transition.annotation.AnimationDirection.LEFT;
import static com.mct.base.ui.transition.annotation.AnimationDirection.NONE;
import static com.mct.base.ui.transition.annotation.AnimationDirection.RIGHT;
import static com.mct.base.ui.transition.annotation.AnimationDirection.UP;

import android.view.animation.Transformation;

import androidx.annotation.NonNull;

import com.mct.base.ui.transition.annotation.AnimationDirection;

/**
 * Move Animation
 */
public class MoveAnimation extends ViewPropertyAnimation {


    protected final @AnimationDirection int mDirection;
    protected final boolean mEnter;

    /**
     * Create new Animation.
     *
     * @param direction Direction of animation
     * @param enter     true for Enter / false for Exit
     * @param duration  Duration of Animation
     * @return MoveAnimation
     */
    public static @NonNull MoveAnimation create(@AnimationDirection int direction, boolean enter, long duration) {
        switch (direction) {
            case UP:
            case DOWN:
                return new VerticalMoveAnimation(direction, enter, duration);
            case LEFT:
            case RIGHT:
            case NONE:
            default:
                return new HorizontalMoveAnimation(direction, enter, duration);
        }
    }

    private MoveAnimation(@AnimationDirection int direction, boolean enter, long duration) {
        mDirection = direction;
        mEnter = enter;
        setDuration(duration);
    }

    private static class VerticalMoveAnimation extends MoveAnimation {

        private VerticalMoveAnimation(@AnimationDirection int direction, boolean enter, long duration) {
            super(direction, enter, duration);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            float value = mEnter ? (interpolatedTime - 1.0f) : interpolatedTime;
            if (mDirection == DOWN) value *= -1.0f;
            mTranslationY = -value * mHeight;

            super.applyTransformation(interpolatedTime, t);
            applyTransformation(t);
        }

    }

    private static class HorizontalMoveAnimation extends MoveAnimation {

        private HorizontalMoveAnimation(@AnimationDirection int direction, boolean enter, long duration) {
            super(direction, enter, duration);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            float value = mEnter ? (interpolatedTime - 1.0f) : interpolatedTime;
            if (mDirection == RIGHT) value *= -1.0f;
            mTranslationX = -value * mWidth;

            super.applyTransformation(interpolatedTime, t);
            applyTransformation(t);
        }

    }

}