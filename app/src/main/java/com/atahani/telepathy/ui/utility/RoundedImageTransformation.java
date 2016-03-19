package com.atahani.telepathy.ui.utility;

import android.graphics.*;
import com.squareup.picasso.Transformation;

/**
 * BitmapTransformation used in Glide when want load image
 */
public class RoundedImageTransformation implements Transformation {

    private int radius;
    private int margin;
    private boolean onlyUpRound;

    /**
     * the initial method
     * @param radius      int radius in dp scale
     * @param margin      int margin in dp scale
     * @param onlyUpRound boolean if true only round up
     */
    public RoundedImageTransformation(int radius, int margin, boolean onlyUpRound) {
        this.radius = radius;
        this.margin = margin;
        this.onlyUpRound = onlyUpRound;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(new RectF(margin, margin, source.getWidth() - margin, source.getHeight() - margin), radius, radius, paint);
        //        0, source.getHeight()/2, source.getWidth(), source.getHeight()
        final Rect bottomRect = new Rect(0, source.getHeight() - (2 * radius), source.getWidth(), source.getHeight());

        if (onlyUpRound) {
            canvas.drawRect(bottomRect, paint);
        }
        if (source != output) {
            source.recycle();
        }
        return output;
    }

    @Override
    public String key() {
        return "rounded(radius=" + radius + ", margin=" + margin + ")";
    }
}
