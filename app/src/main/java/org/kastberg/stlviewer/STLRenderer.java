package org.kastberg.stlviewer;

import android.app.Activity;
import android.content.Intent;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class STLRenderer implements GLSurfaceView.Renderer {
    private STLSurfaceView glSurfaceView;
    private static final String TAG = "STLRenderer";

    public STLRenderer(STLSurfaceView glSurfaceView) {

        this.glSurfaceView = glSurfaceView;
    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        Log.e(TAG, GLES20.glGetShaderInfoLog(shader));

        return shader;
    }

    public void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
    public volatile float mAngle;

    private FloatBuffer vertexBuffer;
    private FloatBuffer normalBuffer;
    int mProgram;
    int vertexCount;
    float[] mVMatrix = new float[16];
    float[] mProjMatrix = new float[16];

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        Intent in = ((Activity)glSurfaceView.getContext()).getIntent();
        InputStream is = null;
        if(in.getAction().equals("android.intent.action.VIEW"))
            try {
                is = glSurfaceView.getContext().getContentResolver().openInputStream(in.getData());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        else
            is = glSurfaceView.getResources().openRawResource(R.raw.boobpoop);
        STLModel model = STLLoader.fromInputStream(is);
        model.center();
        model.scale();
        vertexBuffer = model.vertexBuffer();
        Log.e(TAG,vertexBuffer.toString());
        normalBuffer = model.normalBuffer();
        vertexCount = model.len;
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                glSurfaceView.getResources().getString(R.string.vertex_shader));
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                glSurfaceView.getResources().getString(R.string.fragment_shader));

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);
        checkGlError("glLinkProgram");
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        // create a projection matrix from device screen geometry
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 1, 100);
    }
    final int COORDS_PER_VERTEX = 3;
    private int vertexStride = Float.SIZE * COORDS_PER_VERTEX;
    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_DITHER);
        GLES20.glUseProgram(mProgram);
        // get handle to vertex shader's vPosition member
        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        checkGlError("glGetAttribLocation");
        int mNormalHandle = GLES20.glGetAttribLocation(mProgram, "vNormal");
        checkGlError("glGetAttribLocation");
        int muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        int muMVMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVMatrix");
        float[] mMVPMatrix = new float[16];
        float[] mMMatrix = new float[16];
        float[] mMVMatrix = new float[16];
        Matrix.setIdentityM(mMMatrix,0);
        Matrix.rotateM(mMMatrix,0,mAngle,-1.0f,0.0f,0.0f);

        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -2f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        Matrix.multiplyMM(mMVMatrix, 0, mVMatrix, 0, mMMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVMatrix, 0);
        GLES20.glUniformMatrix4fv(muMVMatrixHandle, 1, false, mMVMatrix, 0);
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        checkGlError("glEnableVertexAttribArray");
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        checkGlError("glEnableVertexAttribArray");
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        checkGlError("glEnableVertexAttribArray");
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                0, vertexBuffer);
        checkGlError("glVertexAttribPointer");
        GLES20.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                0, normalBuffer);
        checkGlError("glVertexAttribPointer");
        // get handle to fragment shader's vColor member
        int mLightHandle = GLES20.glGetUniformLocation(mProgram, "uLightPos");
        float[] light = new float[]{0f, 0f, -1f};

        // Set color for drawing the triangle
        GLES20.glUniform3fv(mLightHandle, 1, light, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        checkGlError("glDrawArrays");
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mNormalHandle);
    }
}
