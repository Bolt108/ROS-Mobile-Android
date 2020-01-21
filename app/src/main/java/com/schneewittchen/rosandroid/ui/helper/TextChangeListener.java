package com.schneewittchen.rosandroid.ui.helper;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * TODO: Description
 *
 * @author Nico Studt
 * @version 1.0.0
 * @created on 19.01.20
 * @updated on 19.01.20
 * @modified by
 */

public abstract class TextChangeListener<T> implements TextWatcher {

    private T target;


    public TextChangeListener(T target) {
        this.target = target;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        System.out.println("Before Text Changed: " + s);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        System.out.println("On Text Changed: " + s);
    }

    @Override
    public void afterTextChanged(Editable s) {
        System.out.println("After Text Changed: " + s.toString());
        this.onTextChanged(target, s);
    }

    public abstract void onTextChanged(T target, Editable s);
}