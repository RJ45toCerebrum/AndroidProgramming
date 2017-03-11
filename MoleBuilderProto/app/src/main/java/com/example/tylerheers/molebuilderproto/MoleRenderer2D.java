package com.example.tylerheers.molebuilderproto;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.security.spec.EllipticCurve;

/**
 * Created by tylerheers on 3/10/17.
 */

public class MoleRenderer2D extends View
{
    Rect cropRect;
    Paint blue;

    Canvas canvas;
    Context myContext;

    float touchX = 0;
    float touchY = 0;

    public MoleRenderer2D(Context context){
        super(context);
        init(null, 0);

        myContext = context;
        blue = new Paint();
        blue.setColor(Color.BLACK);
    }

    public MoleRenderer2D(Context context, AttributeSet set){
        super(context, set);
        setFocusable(true);
        setFocusableInTouchMode(true);

    }

    public MoleRenderer2D(Context context, AttributeSet set, int defStyle){
        super(context, set, defStyle);
        init(set, 0);
    }

    public void init(AttributeSet set, int defStyle){

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);

        paint.setStrokeWidth(5);
        cropRect = new Rect(0,0,canvas.getWidth(),canvas.getHeight());

        canvas.drawRect(cropRect, paint);
        canvas.drawCircle(touchX, touchY, 50, paint);
        canvas.drawText("C", touchX, touchY, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchX = event.getX();
        touchY = event.getY();

        postInvalidate();
        return true;
    }



}
