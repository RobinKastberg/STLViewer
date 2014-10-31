package org.kastberg.stlviewer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

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
    final STLNode camera;
    private float mPreviousX;
    private float mPreviousY;
    public STLSurfaceView(Context context, AttributeSet set) {
        super(context, set);
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
            is = getResources().openRawResource(R.raw.stone);

        mRenderer = new STLRenderer(this);
        setDebugFlags(DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS);
        setRenderer(mRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);

        new STLLoaderTask(this.getContext()).execute(is);
        sg = new STLSceneGraph();
        camera = new STLNode(null);
        sg.children.add(camera);

    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = mPreviousX - x;
                float dy = y - mPreviousY;
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

                float TOUCH_SCALE_FACTOR_X = 180.0f / 320;
                float TOUCH_SCALE_FACTOR_Y = 90.0f / 320;
                mRenderer.mAngleX += dx * TOUCH_SCALE_FACTOR_X;
                mRenderer.mAngleY += dy * TOUCH_SCALE_FACTOR_Y;
                mRenderer.mAngleY = Math.min(mRenderer.mAngleY, 90.0f);
                mRenderer.mAngleY = Math.max(mRenderer.mAngleY, -90.0f);
                requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

    public void modelDone(STLModel model) {
        model.setShader(getContext().getResources().getString(R.string.vertex_shader), getContext().getResources().getString(R.string.fragment_shader));
        model.parent = camera;
        camera.children.add(model);
        requestRender();
    }
}
