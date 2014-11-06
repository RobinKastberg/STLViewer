package org.kastberg.stlviewer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by robin.kastberg on 2014-10-22.
 */
public class STLSurfaceView extends GLSurfaceView {
    private static final String TAG = "STLSurfaceView";
    private final STLRenderer mRenderer;
    private InputStream is = null;
    final STLSceneGraph sg;
    private float mPreviousX;
    private float mPreviousY;
    float mScaleFactor = 1.0f;
    private ScaleGestureDetector.OnScaleGestureListener mScaleListener = new
            ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    mScaleFactor *= detector.getScaleFactor();

                    // Don't let the object get too small or too large.
                    mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 100.0f));

                    return true;
                }
            };
    private ScaleGestureDetector mScaleDetector;
    public STLSurfaceView(Context context, AttributeSet set) {
        super(context, set);
        mScaleDetector = new ScaleGestureDetector(context, mScaleListener);
        setEGLContextClientVersion(2);
        setEGLConfigChooser(5, 6, 5, 0, 16, 0);
        setPreserveEGLContextOnPause(true);
        //getHolder().setFormat(PixelFormat.TRANSLUCENT);
        Intent in = ((Activity) getContext()).getIntent();
        if (in.getAction().equals("android.intent.action.VIEW"))
            try {
                is = getContext().getContentResolver().openInputStream(in.getData());
            } catch (FileNotFoundException e) {
                Log.e(TAG, in.toString());
                e.printStackTrace();
            }
        else
            is = getResources().openRawResource(R.raw.pump);
        mRenderer = new STLRenderer(this);
        setRenderer(mRenderer);
        //setRenderMode(RENDERMODE_WHEN_DIRTY);
        new STLLoaderTask(this.getContext()).execute(is);
        sg = new STLSceneGraph();
        sg.camera = new STLCamera();
        sg.camera.setPosition(2, 0, 0);
        sg.camera.lookAt(0, 0, 0);
        sg.children.add(sg.camera);

    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.
        if (!mScaleDetector.onTouchEvent(e))
            return true;
        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = -mPreviousX + x;
                float dy = mPreviousY - y;
                /*
                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2) {
                    dx = dx * -1 ;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2) {
                    dy = dy * -1 ;
                }
                */

                float TOUCH_SCALE_FACTOR_X = 180.0f / 32000;
                float TOUCH_SCALE_FACTOR_Y = 90.0f / 32000;
                mRenderer.mAngleX += dx * TOUCH_SCALE_FACTOR_X;
                mRenderer.mAngleY += dy * TOUCH_SCALE_FACTOR_Y;
                mRenderer.mAngleY = Math.min(mRenderer.mAngleY, (float) Math.PI - 0.000001f);
                mRenderer.mAngleY = Math.max(mRenderer.mAngleY, 0.0000001f);
                float r = 1 / mScaleFactor;
                sg.camera.setPosition(r * (float) Math.sin(mRenderer.mAngleY) * (float) Math.cos(mRenderer.mAngleX),
                        r * (float) Math.cos(mRenderer.mAngleY),
                        r * (float) Math.sin(mRenderer.mAngleY) * (float) Math.sin(mRenderer.mAngleX));
                requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

    public void modelDone(STLModel model) {
        String vertex = STLUtil.assetToString(getContext(), "standard.vert");
        String fragment = STLUtil.assetToString(getContext(), "standard.frag");
        model.setShader(vertex, fragment);
        model.parent = sg.camera;
        sg.camera.children.add(model);
        requestRender();
    }

}
