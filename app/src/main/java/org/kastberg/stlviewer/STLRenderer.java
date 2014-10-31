package org.kastberg.stlviewer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class STLRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "STLRenderer";
    public volatile float mAngleX = 0.0f;
    public volatile float mAngleY = 30.0f;
    private final float[] mVMatrix = new float[16];
    private final float[] mProjMatrix = new float[16];
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
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        w = width;
        h = height;
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glViewport(0, 0, w, h);
        float ratio = (float) w / h;
        //Matrix.setIdentityM(mProjMatrix,0);
        //Matrix.orthoM(mProjMatrix,0, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f);
        // create a projection matrix from device screen geometry

        float zNear = 0.1f;
        float zFar = 3.0f;
        float fH = (float) Math.tan(90.0 / 360 * Math.PI) * zNear;
        float fW = fH * ratio;
        Matrix.frustumM(mProjMatrix, 0, -fW, fW, -fH, fH, zNear, zFar);
        //Matrix.perspectiveM(mProjMatrix,0,90.0f,ratio, 0.1f, 3.0f);
        float[] mMVPMatrix = new float[16];
        float[] mMMatrix = new float[16];
        float[] mMVMatrix = new float[16];
        Matrix.setIdentityM(mMMatrix, 0);
        Matrix.rotateM(mMMatrix, 0, mAngleY, -1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mMMatrix, 0, mAngleX, 0.0f, -1.0f, 0.0f);

        Matrix.setLookAtM(mVMatrix, 0, 0, 0.5f, -1.5f, 0f, 0.0f, 0.0f, 0f, 1.0f, 0.0f);

        Matrix.multiplyMM(mMVMatrix, 0, mVMatrix, 0, mMMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVMatrix, 0);

        glSurfaceView.camera.matrix = mMVPMatrix;

        // get handle to fragment shader's vColor member
        int mLightHandle = GLES20.glGetUniformLocation(glSurfaceView.camera.program, "uLightPos");
        float[] light = new float[]{0f, 0f, 0f, 1f};
        GLES20.glUniform3fv(mLightHandle, 1, light, 0);


        glSurfaceView.sg.render();
    }
}
