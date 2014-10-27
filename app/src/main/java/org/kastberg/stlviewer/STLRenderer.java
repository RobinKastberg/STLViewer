package org.kastberg.stlviewer;

import android.opengl.ETC1;
import android.opengl.ETC1Util;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

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
    int textureShader;
    int renderShader;
    int vertexCount;
    float[] mVMatrix = new float[16];
    float[] mProjMatrix = new float[16];
    boolean isLoaded = false;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                glSurfaceView.getResources().getString(R.string.texture_vertex_shader));
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                glSurfaceView.getResources().getString(R.string.texture_fragment_shader));
        int vertexShader2 = loadShader(GLES20.GL_VERTEX_SHADER,
                glSurfaceView.getResources().getString(R.string.goban_vertex_shader));
        int fragmentShader2 = loadShader(GLES20.GL_FRAGMENT_SHADER,
                glSurfaceView.getResources().getString(R.string.goban_fragment_shader));
        textureShader = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(textureShader, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(textureShader, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(textureShader);
        renderShader = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(renderShader, vertexShader2);   // add the vertex shader to program
        GLES20.glAttachShader(renderShader, fragmentShader2); // add the fragment shader to program
        GLES20.glLinkProgram(renderShader);
        Log.e(TAG,GLES20.glGetString(GLES20.GL_EXTENSIONS));
        checkGlError("glLinkProgram");
        GLES20.glEnable(GLES20.GL_DITHER);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }
    int[] fb;
    int[] depthRb;
    int[] renderTex;
    public void renderTextures() {
        fb = new int[1];
        depthRb = new int[1];
        renderTex = new int[1];
        int[] fbi = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_MAX_RENDERBUFFER_SIZE,fbi,0);
        int fbMax = fbi[0];
        int texW = 512;
        int texH = 512;
        GLES20.glGenFramebuffers(1, fb, 0);
        GLES20.glGenRenderbuffers(1, depthRb, 0); // the depth buffer
        GLES20.glGenTextures(1, renderTex, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTex[0]);


// parameters - we have to make sure we clamp the textures to the edges
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLES20.glHint(GLES20.GL_GENERATE_MIPMAP_HINT, GLES20.GL_NICEST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES11Ext.GL_TEXTURE_MAX_ANISOTROPY_EXT,
                4);
        //Buffer texBuffer = ByteBuffer.allocateDirect(texW * texH * 2);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, texW, texH, 0, GLES20.GL_RGB,GLES20.GL_UNSIGNED_BYTE, null);



// create render buffer and bind 16-bit depth buffer
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthRb[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, texW, texH);



        GLES20.glViewport(0, 0, texW, texH);
        float[] mMVPMatrix = new float[16];
        Matrix.orthoM(mProjMatrix,0, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f);
        Matrix.setLookAtM(mVMatrix, 0, 0, 1.0f, 0.0f, 0f, 0.0f, 0.0f, 0f, 0.0f, 1.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fb[0]);

// specify texture as color attachment
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, renderTex[0], 0);

// attach render buffer as depth buffer
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthRb[0]);

        int mPositionHandle = GLES20.glGetAttribLocation(textureShader, "vPosition");
        int muMVPMatrixHandle = GLES20.glGetUniformLocation(textureShader, "uMVPMatrix");

        GLES20.glUseProgram(textureShader);
        GLES20.glClearColor(.0f, .0f, .0f, 1.0f);
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);


        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        checkGlError("glEnableVertexAttribArray");

        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                0, vertexBuffer);


        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        GLES20.glDisableVertexAttribArray(mPositionHandle);


        //GLES20.glReadPixels(0, 0, texW, texH, GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5, texBuffer);
        //Buffer doneTex = ByteBuffer.allocateDirect(ETC1.getEncodedDataSize(texW, texH));
        //ETC1.encodeImage(texBuffer, texW, texH, 2, 2*texH, doneTex);
        //GLES20.glCompressedTexImage2D(GLES20.GL_TEXTURE_2D, 0, ETC1.ETC1_RGB8_OES, texW, texH, 0, ETC1.getEncodedDataSize(texW, texH), doneTex);
        checkGlError("glCompressedTexImage2D");

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
    int w;
    int h;
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        w = width;
        h = height;
    }
    final int COORDS_PER_VERTEX = 3;
    boolean textureReady = false;
    @Override
    public void onDrawFrame(GL10 unused) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            if(isLoaded) {
                if(!textureReady) {
                    renderTextures();
                    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
                    textureReady = true;
                }
            GLES20.glViewport(0, 0,w, h);
            float ratio = (float) w / h;
            //Matrix.setIdentityM(mProjMatrix,0);
            //Matrix.orthoM(mProjMatrix,0, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f);
            // create a projection matrix from device screen geometry
            Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 0.1f, 3);
            //Matrix.perspectiveM(mProjMatrix,0,90.0f,ratio, 0.1f, 3.0f);
            GLES20.glUseProgram(renderShader);
            // get handle to vertex shader's vPosition member
            int mPositionHandle = GLES20.glGetAttribLocation(renderShader, "vPosition");
            int mNormalHandle = GLES20.glGetAttribLocation(renderShader, "vNormal");
            int muMVPMatrixHandle = GLES20.glGetUniformLocation(renderShader, "uMVPMatrix");
            int muMVMatrixHandle = GLES20.glGetUniformLocation(renderShader, "uMVMatrix");
            float[] mMVPMatrix = new float[16];
            float[] mMMatrix = new float[16];
            float[] mMVMatrix = new float[16];
            Matrix.setIdentityM(mMMatrix, 0);
            Matrix.rotateM(mMMatrix, 0, mAngleY, -1.0f, 0.0f, 0.0f);
            Matrix.rotateM(mMMatrix, 0, mAngleX, 0.0f, -1.0f, 0.0f);
            //Matrix.setLookAtM(mVMatrix, 0, 0, 1.0f, 0.0f, 0f, 0.0f, 0.0f, 0f, 0.0f, 1.0f);
            Matrix.setLookAtM(mVMatrix, 0, 0, 0.5f, -1.0f, 0f, 0.0f, 0.0f, 0f, 1.0f, 0.0f);


            Matrix.multiplyMM(mMVMatrix, 0, mVMatrix, 0, mMMatrix, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVMatrix, 0);
            GLES20.glUniformMatrix4fv(muMVMatrixHandle, 1, false, mMVMatrix, 0);
            GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
            checkGlError("glEnableVertexAttribArray");
            // Enable a handle to the triangle vertices

            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glEnableVertexAttribArray(mNormalHandle);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTex[0]);

            GLES20.glUniform1i(GLES20.glGetUniformLocation(renderShader, "texture"), 0);


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
            int mLightHandle = GLES20.glGetUniformLocation(renderShader, "uLightPos");
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
