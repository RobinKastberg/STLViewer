package org.kastberg.stlviewer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class STLRenderer implements GLSurfaceView.Renderer {
    private STLSurfaceView glSurfaceView;
    public STLModel model = null;
    private static final String TAG = "STLRenderer";

    public STLRenderer(STLSurfaceView glSurfaceView) {

        this.glSurfaceView = glSurfaceView;
    }


    public volatile float mAngleX = 0.0f;
    public volatile float mAngleY = 30.0f;

    private FloatBuffer vertexBuffer;
    private FloatBuffer normalBuffer;
    int mProgram;
    int vertexCount;
    float[] mVMatrix = new float[16];
    float[] mProjMatrix = new float[16];
    boolean isLoaded = false;
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        GLES20.glEnable(GLES20.GL_DITHER);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        // create a projection matrix from device screen geometry
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 0.1f, 3);
        //Matrix.perspectiveM(mProjMatrix,0,90.0f,ratio, 0.1f, 3.0f);
    }
    final int COORDS_PER_VERTEX = 3;
    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            if(isLoaded) {
            GLES20.glUseProgram(mProgram);
            // get handle to vertex shader's vPosition member
            int muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            int muMVMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVMatrix");
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

            int mLightHandle = GLES20.glGetUniformLocation(mProgram, "uLightPos");
            float[] light = new float[]{0f, 0f, 0f,1f};
            GLES20.glUniform3fv(mLightHandle, 1, light, 0);


            glSurfaceView.sg.render();
        }
    }
}
