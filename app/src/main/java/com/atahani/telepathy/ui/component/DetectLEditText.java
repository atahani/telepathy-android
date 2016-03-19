package com.atahani.telepathy.ui.component;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mobi.atahani.telepathy.R;
import com.atahani.telepathy.TApplication;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

/**
 * EditTextView with Detect language
 * since we in two language if user enter farsi change typeface
 */
public class DetectLEditText extends AppCompatEditText {

    private CustomKeyboardBehavior mCustomKeyboardBehavior;

    public DetectLEditText(Context context) {
        super(context);
    }

    public DetectLEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DetectLEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCustomKeyboardBehavior(CustomKeyboardBehavior customKeyboardBehavior) {
        this.mCustomKeyboardBehavior = customKeyboardBehavior;
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (!TextUtils.isEmpty(text)) {
            Pattern RTL_CHAR = Pattern.compile("[\u0600-\u06FF\u0750-\u077F\u0590-\u05FF\uFE70-\uFEFF]");
            Matcher matcher = RTL_CHAR.matcher(text.toString());
            if (matcher.find()) {
                //load persian default font
                setTypeface(TypefaceUtils.load(TApplication.applicationContext.getAssets(), TApplication.applicationContext.getString(R.string.normal_persian_font)));

            } else {
                setTypeface(TypefaceUtils.load(TApplication.applicationContext.getAssets(), TApplication.applicationContext.getString(R.string.normal_latin_font)));
            }
        }
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_UP) {
            if (mCustomKeyboardBehavior != null) {
                mCustomKeyboardBehavior.onDismissingKeyboard();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    public interface CustomKeyboardBehavior {
        void onDismissingKeyboard();
    }
}
