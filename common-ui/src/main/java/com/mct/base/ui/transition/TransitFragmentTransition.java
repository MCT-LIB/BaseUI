package com.mct.base.ui.transition;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;

import com.mct.base.ui.transition.annotation.Transit;

public class TransitFragmentTransition implements FragmentTransition {

    @Transit
    private final int transit;

    public TransitFragmentTransition(@Transit int transit) {
        this.transit = transit;
    }

    @Override
    public void applyTransition(@NonNull FragmentTransaction transaction) {
        transaction.setTransition(transit);
    }

    @Override
    public boolean couldPopImmediate() {
        return false;
    }
}
