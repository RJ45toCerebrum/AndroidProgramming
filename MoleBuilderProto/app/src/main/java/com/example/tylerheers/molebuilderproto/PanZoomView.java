package com.example.tylerheers.molebuilderproto;

/*
 * Copyright (C) 2015 Wglxy.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
//import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import android.view.ViewConfiguration;

import com.wglxy.example.gameboard.util.AndroidCompatibility;


/**
 * This view supports both zooming and panning.
 * What gets shown in the view depends on the onDraw method provided by subclasses.
 * The default onDraw method defined here comes from R.drawable.zoom_view_sample.
 *
 * <p> Subclasses can change behavior by overriding methods sampleDrawableId,
 * supportsZoom, supportsPan, supportsScaleAroundFocusPoint, drawOnCanvas.
 */

public class PanZoomView extends View {

    static protected final boolean ScaleAtFocusPoint = false;
    static protected final int DefaultDrawableId = R.drawable.no_marker_default;

    protected Drawable mSampleImage;
    protected Context mContext;
    protected float mPosX;
    protected float mPosY;
    protected float mPosX0 = 0;     // initial displacement values
    protected float mPosY0 = 0;;
    protected float mFocusX;    // these two focus variables are not needed
    protected float mFocusY;
    
    protected float mLastTouchX;
    protected float mLastTouchY;
    protected float mInitialTouchX;
    protected float mInitialTouchY;
    protected boolean mDoTouchUp = false;          // true = looks like a touch event (click) so far,
                                                   // rather than a scale or move (pan) touch
    protected boolean mHandlingTouchUp = false;

    protected boolean mTouchable = true;
    
protected static final int INVALID_POINTER_ID = -1;

// The ‘active pointer’ is the one currently moving our object.
protected int mActivePointerId = INVALID_POINTER_ID;

protected ScaleGestureDetector mScaleDetector;
protected float mScaleFactor = 1.f;
protected float mMinScaleFactor = 0.1f;
protected float mMaxScaleFactor = 5.0f;

// The next ones are set by calling supportsPan, supportsZoom, ...
protected boolean mSupportsPan = true;
protected boolean mSupportsZoom = true;
protected boolean mSupportsScaleAtFocus = true;
protected boolean mSupportsOnTouchDown = false;
protected boolean mSupportsOnTouchUp = false;

    // Long press detection
    protected long mLongPressTimeOut;          // time in nanoseconds
    protected long mDownTime;                  // time of the last call to onTouchDown, in nanoseconds

    protected boolean mIsMove;
    protected boolean mIsLongPress;

    static private final float SCROLL_THRESHOLD = 20;
    
/**
 */
public PanZoomView (Context context) {
    this(context, null, 0);
}

public PanZoomView (Context context, AttributeSet attrs) {
    this(context, attrs, 0);
}

public PanZoomView (Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    mContext = context;
    setupToDraw (context, attrs, defStyle);
    setupScaleDetector (context, attrs, defStyle);
}

/**
 * Calculate the inSampleSize to use in BitmapFactory.Options in order
 * to load a drawable resource into a bitmap of the specified size.
 */

public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {
        if (width > height) {
            inSampleSize = Math.round((float)height / (float)reqHeight);
        } else {
            inSampleSize = Math.round((float)width / (float)reqWidth);
        }
    }
    return inSampleSize;
}

/**
 * Decode a resource into a bitmap of the specified size.
 */

public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
        int reqWidth, int reqHeight) {

    // First decode with inJustDecodeBounds=true to check dimensions
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeResource(res, resId, options);

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false;
    return BitmapFactory.decodeResource(res, resId, options);
}

/**
* Disable all handling of touch events.
*
* @param disable boolean - true means to disable touch handling
* @return void
*/
 
public void disableTouch (boolean disable) {
    mTouchable = disable;
}

public void disableTouch () {
    disableTouch (true);
}
 
/**
 * Return the current scale factor for the view. Values less than 1 mean the view is zoomed out.
 * Values greater than 1 mean the view is zoomed in.
 * 
 * @return flaot
 */

public float getScaleFactor () {
    return mScaleFactor;
}

/**
 * Do whatever drawing is appropriate for this view.
 * The canvas object is already set up to be drawn on. That means that all translations and scaling
 * operations have already been done.
 * 
 * @param canvas Canvas
 * @return void
 */

public void drawOnCanvas (Canvas canvas) {
    mSampleImage.draw(canvas);
}

/**
 * onDraw
 * The onDraw method for PanZoomView calls the super.onDraw method and then calls onDrawPz.
 * Any subclass can therefore override onDraw handling here by defining onDrawPz.
 */
@Override public void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    onDrawPz (canvas);
}

/**
 * onDrawPz
 */

protected void onDrawPz (Canvas canvas) {
    canvas.save();

    float x = 0, y = 0;
    x = mPosX + mPosX0;
    y = mPosY + mPosY0;
    if (mSupportsZoom || mSupportsPan) {
       if (mSupportsPan && !mSupportsZoom) {
          canvas.translate(x, y);
          canvas.scale(mScaleFactor, mScaleFactor);
          //Log.d ("Multitouch", "+p-z x, y : " + x + " " + y);
       } else if (mSupportsPan && mSupportsZoom) {
         if (mScaleDetector.isInProgress()) {
           // Pinch zoom is in progress
           // if (mSupportsPan) canvas.translate(mPosX, mPosY);
           mFocusX = mScaleDetector.getFocusX ();
           mFocusY = mScaleDetector.getFocusY ();
           canvas.scale(mScaleFactor, mScaleFactor, mFocusX, mFocusY);
           //Log.d ("Multitouch", "+p+z x, y, focusX, focusY: " + x + " " + y + " " + mFocusX + " " + mFocusY);
         } else {
           // Pinch zoom is not in progress. Just do translation of canvas at whatever the current scale is.
           canvas.translate(x, y);
           canvas.scale(mScaleFactor, mScaleFactor);
           //Log.d ("Multitouch", "+p+z x, y : " + x + " " + y);
         }
       } else if (mSupportsZoom) {
         // Not working perfectly when mPosX0 is set. 
         canvas.translate(mPosX0, mPosY0);         // Translate canvas so center point can be chosen.
         mFocusX = mScaleDetector.getFocusX ();
         mFocusY = mScaleDetector.getFocusY ();
         canvas.scale(mScaleFactor, mScaleFactor, mFocusX -mPosX0, mFocusY -mPosY0);
   
         //Log.d ("Multitouch", "-p+s x, y, focusX, focusY: " + mPosX0 + " " + mPosY0 + " " + mFocusX + " " + mFocusY);
       }
    }

    // Do whatever drawing is appropriate for this class
    drawOnCanvas (canvas);

    canvas.restore();
}

/**
 * Handle touch and multitouch events so panning and zooming can be supported.
 *
 */

public boolean onTouchEvent (MotionEvent ev) {

    // If touch is disabled, return early.
    if (!mTouchable) return false;

    // If we are not supporting either zoom or pan, return early.
    if (!mSupportsZoom && !mSupportsPan) return false;
    // If we are finishing up with touch up, return early.
//    if (mHandlingTouchUp) return true;

    // Let the ScaleGestureDetector inspect all events.
    mScaleDetector.onTouchEvent(ev);
    
    final int action = ev.getAction();
    switch (action & MotionEvent.ACTION_MASK) {
    case MotionEvent.ACTION_DOWN: {
        final float x = ev.getX();
        final float y = ev.getY();

        mIsMove = false;            // Assume action is a click until the scroll threshold is met.
        
        mLastTouchX = x;
        mLastTouchY = y;
        mActivePointerId = ev.getPointerId(0);
        if (mSupportsOnTouchDown) {
           onTouchDown (x, y);
        }
        if (mSupportsOnTouchUp) {
           // The very first time, save the initial touch.
           mInitialTouchX = x;
           mInitialTouchY = y;
           if (AppConfig.DEBUG) Log.d (Constants.LOG_UI, "initial x, y : " + mInitialTouchX + ", " + mInitialTouchY);
           mDoTouchUp = true;    // Looks like a touch event so far.
        }
        break;
    }
        
    case MotionEvent.ACTION_MOVE: {
        final int pointerIndex = ev.findPointerIndex(mActivePointerId);
        final float x = ev.getX(pointerIndex);
        final float y = ev.getY(pointerIndex);

        if (!mIsMove && (Math.abs(mInitialTouchX - x) > SCROLL_THRESHOLD
                         || Math.abs (mInitialTouchY - y) > SCROLL_THRESHOLD)) {
                if (AppConfig.DEBUG) Log.d (Constants.LOG_UI, "movement detected");
                mIsMove = true;
        }

        // Only move if the view supports panning and
        // ScaleGestureDetector isn't processing a gesture.
        boolean scalingInProgress = mScaleDetector.isInProgress ();
        if (mSupportsPan && !scalingInProgress) {
           if (mIsMove) {
              final float dx = x - mLastTouchX;
              final float dy = y - mLastTouchY;

              mPosX += dx;
              mPosY += dy;
              //mFocusX = mPosX;
              //mFocusY = mPosY;

              invalidate();
              //Log.d (Constants.LOG_NAME, "Touch move mPos x, y : " + mPosX + " " + mPosY);
           }
        } else if (scalingInProgress) mDoTouchUp = false;

        mLastTouchX = x;
        mLastTouchY = y;

        break;
    }
        
    case MotionEvent.ACTION_UP: {
        if (mIsMove) {
           mHandlingTouchUp = false;
           mDoTouchUp = false;
        } else {
          mActivePointerId = INVALID_POINTER_ID;
          if (mSupportsOnTouchUp && mDoTouchUp) { //  && !mScaleDetector.isInProgress()) {
             final float x = ev.getX();
             final float y = ev.getY();
             try {
                 mHandlingTouchUp = true;
                 onTouchUp (mInitialTouchX, mInitialTouchY, x, y);
             } finally {
               mHandlingTouchUp = false;
             }
             mDoTouchUp = false;
          }
        }
        break;
    }
        
    case MotionEvent.ACTION_CANCEL: {
        mActivePointerId = INVALID_POINTER_ID;
        break;
    }
    
    case MotionEvent.ACTION_POINTER_UP: {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) 
                >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastTouchX = ev.getX(newPointerIndex);
            mLastTouchY = ev.getY(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
        break;
    }
    }

    this.performClick ();           // Do this to get rid of warning message.
                                    // Not sure what it does.
    return true;
}

/**
 * This method is called when supportsOnTouchDown is true.
 * It can be used when subclasses want to perform a simple action when a touch occurs.
 * <p> The default action is to record the time when the down touch occurred.
 * That information can be used in onTouchUp to determine if the touch was a press or a long press.
 * 
 * @param downX float - x value of the down action
 * @param downY float - y value of the down action
 * @return void
 */

public void onTouchDown (float downX, float downY) {
    mDownTime = System.nanoTime ();

}

/**
 * This method is called when supportsOnTouchUp is true and panning and scaling are not in progress.
 * It can be used when subclasses want to support a simple touch action in addition to scaling.
 * <p> The default action is to do nothing. Subclasses should override this method.
 * 
 * @param downX float - x value of the down action
 * @param downY float - y value of the down action
 * @param upX float - x value of the up action
 * @param upY float - x value of the up action
 * @return void
 */

public void onTouchUp (float downX, float downY, float upX, float upY) {
    // Do nothing. Subclasses should override.
}

/**
 * This method is here to get rid of a warning
 */ 

@Override public boolean performClick() {
   super.performClick();
   return true;
}

/**
 * Return the resource id of the sample image.
 * 
 * @return int
 */

public int sampleDrawableId () {
    return DefaultDrawableId;
}

/**
 * Set the values for minimum scaling factor and maximum scaling factor.
 * The values will be used the next time the view is drawn.
 * 
 * @param minScale float
 * @param maxScale float
 * @return void
 */
public void setScaleMinMax (float minScale, float maxScale) {
    mMinScaleFactor = minScale;
    mMaxScaleFactor = maxScale;
}

/**
 * This method sets up the scale detector object used by the view. It is called by the constructor.
 * 
 * @return void
 */

protected void setupScaleDetector (Context context, AttributeSet attrs, int defStyle) {
    // Create our ScaleGestureDetector
    mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
}

/**
 * This method performs whatever set up is necessary to do drawing. It is called by the constructor.
 * The default implementation checks to see if both panning and zooming are supported.
 * And it also locates the sample drawable resource by calling sampleDrawableId, which subclasses
 * are free to override.
 * If that method returns 0, the sample image is not set up.
 * 
 * @return void
 */

protected void setupToDraw (Context context, AttributeSet attrs, int defStyle) {
    mIsMove = false;
    mIsLongPress = false;

    mTouchable = true;
    mSupportsOnTouchDown = supportsOnTouchDown ();
    mSupportsOnTouchUp  = supportsOnTouchUp ();
    mSupportsPan = supportsPan ();
    mSupportsZoom = supportsZoom ();
    mSupportsScaleAtFocus = supportsScaleAtFocusPoint ();

    // Long press time out value
    mLongPressTimeOut = ViewConfiguration.getLongPressTimeout () * 1000000;

    int resourceId = sampleDrawableId ();
    if (resourceId == 0) return;
    Resources res = context.getResources ();
    Theme theme = context.getTheme ();
    mSampleImage = AndroidCompatibility.getDrawable(res, resourceId, theme);
    //mSampleImage = context.getResources().getDrawable (resourceId);
    if (mSampleImage != null) {
    	mSampleImage.setBounds(0, 0, mSampleImage.getIntrinsicWidth(), 
    			                     mSampleImage.getIntrinsicHeight());
    }
}

/**
 * Return true if this view wants to receive onTouchDown calls.
 * Those calls are made only if this method returns true. (See setupToDraw.)
 * 
 * @return boolean
 */

public boolean supportsOnTouchDown () {
    return false;
}

/**
 * Return true if this view wants to receive onTouchUp calls.
 * Those calls are made only if this method returns true 
 * and if scaling is not in progress.
 * 
 * @return boolean
 */

public boolean supportsOnTouchUp () {
    return false;
}

/**
 * Return true if panning is supported.
 * 
 * @return boolean
 */

public boolean supportsPan () {
    return true;
}

/**
 * Return true if scaling is done around the focus point of the pinch.
 * 
 * @return boolean
 */

public boolean supportsScaleAtFocusPoint () {
    return ScaleAtFocusPoint;
}

/**
 * Return true if pinch zooming is supported.
 * 
 * @return boolean
 */

public boolean supportsZoom () {
    return true;
}

/**
 * Return true if the view is not zoomed in or out. That is, it is at normal view.
 * 
 * @return boolean
 */

public boolean unzoomed () {
    return (mScaleFactor == 1.0f);
}

/**
 */
// Class definitions

/**
 * ScaleListener 
 *
 */

protected class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (!mSupportsZoom) return true;
        mScaleFactor *= detector.getScaleFactor();
        
        // Don't let the object get too small or too large.
        mScaleFactor = Math.max(mMinScaleFactor, Math.min(mScaleFactor, mMaxScaleFactor));
        mFocusX = detector.getFocusX ();
        mFocusY = detector.getFocusY ();

        invalidate();
        return true;
    }
}

} // end class
