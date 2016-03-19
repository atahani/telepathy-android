package com.atahani.telepathy.ui.component;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.atahani.telepathy.utility.AndroidUtilities;

/**
 * TextView with Detect language
 * since we in two language if user enter farsi change typeface
 */
public class DetectLTextView extends AppCompatTextView {
    public DetectLTextView(Context context) {
        super(context);

    }

    public DetectLTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DetectLTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (!TextUtils.isEmpty(text)) {
            SpannableStringBuilder builder = new SpannableStringBuilder(text);
            builder.setSpan(AndroidUtilities.getCalligraphyTypefaceSpanByText(builder.toString()), 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            text = builder;
        }
        super.setText(text, type);
    }
}
