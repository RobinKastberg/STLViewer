package org.kastberg.stlviewer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class STLRenderer implements GLSurfaceView.Renderer {
    private STLSurfaceView glSurfaceView;
    private static final String TAG = "STLRenderer";

    public STLRenderer(STLSurfaceView glSurfaceView) {
        this.glSurfaceView = glSurfaceView;

        vertexShaderCode   = glSurfaceView.getResources().getString(R.string.vertex_shader);
        fragmentShaderCode = glSurfaceView.getResources().getString(R.string.fragment_shader);
    }

    public void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
    public volatile float mAngle;
    private String vertexShaderCode;

    private String fragmentShaderCode;
    private FloatBuffer vertexBuffer;
    private FloatBuffer normalBuffer;
    int mProgram;
    int vertexCount;
    float[] mVMatrix = new float[16];
    float[] mProjMatrix = new float[16];

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        InputStream boobpoop = glSurfaceView.getResources().openRawResource(R.raw.duck);
        byte[] header = new byte[80];
        byte[] triangles = new byte[4];
        try {
            boobpoop.read(header);
            boobpoop.read(triangles);
        } catch(IOException e) {
            Log.e("Boobpoop","you are bad and should feel bad.");
        }
        Log.e("stlviewer",new String(header));
        ByteBuffer wrapped = ByteBuffer.wrap(triangles);
        wrapped = wrapped.order(ByteOrder.LITTLE_ENDIAN);
        int numOfTriangles = (int)(((long)wrapped.getInt()&0xffffffff));
        Log.e("numofTraingles",""+numOfTriangles);
        vertexCount = numOfTriangles*3;

        vertexBuffer = ByteBuffer.allocateDirect(numOfTriangles*3*3*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        normalBuffer = ByteBuffer.allocateDirect(numOfTriangles*3*3*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for(int i=0;i<numOfTriangles;i++)
        {
            byte[] triangle = new byte[50];
            try {
                boobpoop.read(triangle);
            } catch(IOException e) {
                Log.e("Boobpoop","you are bad and should feel bad.");
            }
            FloatBuffer normal = ByteBuffer.wrap(triangle,0,12).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
            FloatBuffer tri = ByteBuffer.wrap(triangle,12,4*3*3).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
            short attr = ByteBuffer.wrap(triangle,48,2).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(0);
            vertexBuffer.put(tri);
            normalBuffer.put(normal);normal.position(0);
            normalBuffer.put(normal);normal.position(0);
            normalBuffer.put(normal);normal.position(0);
        }

        vertexBuffer.position(0);
        Log.e("stlviewer vertex",vertexBuffer.toString());
        Log.e("stlviewer normal",vertexBuffer.toString());
        normalBuffer.position(0);

        int vertexShader = STLSurfaceView.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = STLSurfaceView.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

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
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 1, 10);
    }
    final int COORDS_PER_VERTEX = 3;
    private int vertexStride = Float.SIZE * COORDS_PER_VERTEX;
    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
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
        Matrix.translateM(mMMatrix,0,-1.0f,0f,0f);
        Matrix.rotateM(mMMatrix,0,mAngle,-1.0f,0.0f,0.0f);
        Matrix.scaleM(mMMatrix,0,0.1f,-0.1f,0.1f);

        //Matrix.translateM(mMMatrix,0,0.0f,-150f,-100.0f);
        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

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
        float[] light = new float[]{0f, 1f, 0f};

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
