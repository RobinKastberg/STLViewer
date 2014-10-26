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

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                glSurfaceView.getResources().getString(R.string.goban_vertex_shader));
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                glSurfaceView.getResources().getString(R.string.goban_fragment_shader));

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);
        Log.e(TAG,GLES20.glGetString(GLES20.GL_EXTENSIONS));
        checkGlError("glLinkProgram");
        GLES20.glEnable(GLES20.GL_DITHER);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }
    public void loadModel(STLModel model)
    {
        model.center();
        //model.scale();
        vertexBuffer = model.vertex;
        Log.e(TAG,vertexBuffer.toString());
        normalBuffer = model.normal;
        vertexCount = model.len;
        isLoaded = true;
    }
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        // create a projection matrix from device screen geometry
        //Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 0.1f, 3);
        Matrix.perspectiveM(mProjMatrix,0,90.0f,ratio, 0.1f, 3.0f);
    }
    final int COORDS_PER_VERTEX = 3;
    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            if(isLoaded) {
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
            Matrix.setIdentityM(mMMatrix, 0);
            Matrix.rotateM(mMMatrix, 0, mAngleY, -1.0f, 0.0f, 0.0f);
            Matrix.rotateM(mMMatrix, 0, mAngleX, 0.0f, -1.0f, 0.0f);

            Matrix.setLookAtM(mVMatrix, 0, 0, 0.5f, -1.5f, 0f, 0.0f, 0.0f, 0f, 1.0f, 0.0f);

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
            float[] light = new float[]{0f, 0f, 0f,1f};
            float[] eye = new float[4];

            float[] lol = new float[16];
            Matrix.invertM(lol,0,mMVMatrix,0);
            Matrix.multiplyMV(eye,0,lol,0,light,0);
            // Set color for drawing the triangle
            GLES20.glUniform3fv(mLightHandle, 1, eye, 0);
            // Draw the triangle
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
            checkGlError("glDrawArrays");
            // Disable vertex array
            GLES20.glDisableVertexAttribArray(mPositionHandle);
            GLES20.glDisableVertexAttribArray(mNormalHandle);
        }
    }
}
