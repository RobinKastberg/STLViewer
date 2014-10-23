package org.kastberg.stlviewer;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
* Created by robin.kastberg on 2014-10-22.
*/
public class STLSurfaceView extends GLSurfaceView {
    STLRenderer mRenderer;
    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private static final String TAG = "STLSurfaceView";
    public STLSurfaceView(Context context, AttributeSet set) {
        super(context, set);
            boolean isTransparent = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("transparent", true);
            setEGLContextClientVersion(2);
            if (isTransparent) {
                setEGLConfigChooser(8, 8, 8, 8, 16, 0);
                getHolder().setFormat(PixelFormat.TRANSLUCENT);
            }
            mRenderer = new STLRenderer(this);
            setRenderer(mRenderer);
        //setRenderMode(RENDERMODE_WHEN_DIRTY);
    }
    float mPreviousX;
    float mPreviousY;
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2) {
                    dx = dx * -1 ;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2) {
                    dy = dy * -1 ;
                }

                mRenderer.mAngle = mRenderer.mAngle +
                                ((dx + dy) * TOUCH_SCALE_FACTOR);  // = 180.0f / 320
                requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }
}
