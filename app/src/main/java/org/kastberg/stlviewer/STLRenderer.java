package org.kastberg.stlviewer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class STLRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "STLRenderer";
    public volatile float mAngleX = 0.0f;
    public volatile float mAngleY = 0.0f;
    private int w;
    private int h;
    private final STLSurfaceView glSurfaceView;

    public STLRenderer(STLSurfaceView glSurfaceView) {
        this.glSurfaceView = glSurfaceView;
        w = glSurfaceView.getWidth();
        h = glSurfaceView.getHeight();
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glEnable(GLES20.GL_DITHER);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        Log.d(TAG, GLES20.glGetString(GLES20.GL_EXTENSIONS));
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        w = width;
        h = height;
        glSurfaceView.sg.camera.setProjection(w, h, 90.0f);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glViewport(0, 0, w, h);
        glSurfaceView.sg.render();
    }
}
