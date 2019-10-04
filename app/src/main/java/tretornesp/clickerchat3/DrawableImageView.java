package tretornesp.clickerchat3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

public class DrawableImageView extends android.support.v7.widget.AppCompatImageView {

    public DrawableImageView(Context context) {
        super(context);
    }

    public DrawableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        canvas.drawPaint(paint);
        paint.setColor(Color.BLACK);
        paint.setTextSize(16);
        canvas.drawText("Hi", 0, 0, paint);
    }
}
