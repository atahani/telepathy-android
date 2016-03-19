package com.atahani.telepathy.ui.component;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.AttributeSet;

/**
 * custom EditText for username with @ prefix
 */
public class UsernameETX extends AppCompatEditText {


    private final String PREFIX="@";
    public UsernameETX(Context context) {
        super(context);
        init();
    }

    public UsernameETX(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UsernameETX(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * initial editText view set prefix
     */
    private void init() {
        setText(PREFIX);
        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setSelection(s.length());
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().startsWith(PREFIX)){
                    setText(PREFIX);
                    Selection.setSelection(getText(), getText().length());
                }
            }
        });
    }

    @Override
    public void onSelectionChanged(int start, int end) {
        //prevent to place cursor before PREFIX
        if (getText().toString().startsWith(PREFIX)) {
            if(start==0 || end==0) {
                setSelection(1);
                return;
            }
        }
        super.onSelectionChanged(start, end);
    }
}
