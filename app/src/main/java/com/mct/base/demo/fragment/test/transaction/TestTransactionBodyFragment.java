package com.mct.base.demo.fragment.test.transaction;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mct.base.demo.R;
import com.mct.base.demo.utils.Utils;
import com.mct.base.ui.BaseFragment;
import com.mct.base.ui.core.IExtraTransaction;
import com.mct.base.ui.transition.FragmentTransitionFactory;
import com.mct.base.ui.transition.annotation.AnimationStyle;
import com.mct.base.ui.transition.options.AnimOptions;

public class TestTransactionBodyFragment extends BaseFragment implements View.OnClickListener {

    private final int color = Utils.generateBrightColor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_test_transaction_body, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(color);

        TextView tvBackstackCount = view.findViewById(R.id.tv_backstack_count);
        tvBackstackCount.setText(String.valueOf(parentExtraTransaction().getBackStackCount()));

        view.findViewById(R.id.btn_replaceFragment).setOnClickListener(this);
        view.findViewById(R.id.btn_replaceFragmentToStack).setOnClickListener(this);
        view.findViewById(R.id.btn_replaceAndClearBackStack).setOnClickListener(this);
        view.findViewById(R.id.btn_clearBackStack).setOnClickListener(this);
        view.findViewById(R.id.btn_popFragment).setOnClickListener(this);
        view.findViewById(R.id.btn_popFragmentToPosition).setOnClickListener(this);
        view.findViewById(R.id.btn_popFragmentByAmount).setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(@NonNull View v) {
        IExtraTransaction extraTransaction = parentExtraTransaction();
        int id = v.getId();
        if (id == R.id.btn_replaceFragment) {
            extraTransaction.replaceFragment(
                    new TestTransactionBodyFragment(),
                    FragmentTransitionFactory.createCircularRevealTransition()
            );
        } else if (id == R.id.btn_replaceFragmentToStack) {
            extraTransaction.replaceFragmentToStack(
                    new TestTransactionBodyFragment(),
                    FragmentTransitionFactory.createTransition(
                            AnimOptions.animation(AnimationStyle.CUBE).left().build(),
                            AnimOptions.animation(AnimationStyle.CUBE).left().build(),
                            AnimOptions.animation(AnimationStyle.CUBE).right().build(),
                            AnimOptions.animation(AnimationStyle.CUBE).right().build()
                    )
            );
        } else if (id == R.id.btn_replaceAndClearBackStack) {
            extraTransaction.replaceAndClearBackStack(
                    new TestTransactionBodyFragment(),
                    FragmentTransitionFactory.createTransition(
                            AnimOptions.animation(AnimationStyle.CUBE).up().build(),
                            AnimOptions.animation(AnimationStyle.CUBE).up().build(),
                            AnimOptions.animation(AnimationStyle.CUBE).right().build(),
                            AnimOptions.animation(AnimationStyle.CUBE).right().build()
                    )
            );
        } else if (id == R.id.btn_clearBackStack) {
            extraTransaction.clearBackStack();
        } else if (id == R.id.btn_popFragment) {
            extraTransaction.popFragment();
        } else if (id == R.id.btn_popFragmentToPosition) {
            extraTransaction.popFragmentToPosition(getNumberFromEditText());
        } else if (id == R.id.btn_popFragmentByAmount) {
            extraTransaction.popFragmentByAmount(getNumberFromEditText());
        }
    }

    private int getNumberFromEditText() {
        if (getView() != null) {
            EditText editText = getView().findViewById(R.id.edt_number);
            try {
                return Integer.parseInt(editText.getText().toString());
            } catch (Throwable t) {
                return 0;
            }
        }
        return 0;
    }

}
