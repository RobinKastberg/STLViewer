package org.kastberg.stlviewer;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by kastberg on 10/31/2014.
 */
public class STLTexture extends STLNode {
    static final String TAG = "STLTexture";
    int[] fb = new int[1];
    int[] depthRb = new int[1];
    int[] renderTex = new int[1];
    int[] texMax = new int[1];

    STLTexture() {
        super(TAG);
    }
    @Override
    public void pre() {
        super.pre();
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        if(GLES20.glIsTexture(renderTex[0]))
        {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTex[0]);
            return;
        }

        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, texMax, 0);
        int texW = texMax[0];
        int texH = texMax[0];

        final float squareCoords[] = {
                -1f, 1f, 0.0f,   // top left
                -1f, -1f, 0.0f,   // bottom left
                1f, -1f, 0.0f,   // bottom right
                -1f, 1f, 0.0f,   // top left
                1f, -1f, 0.0f,   // bottom right
                1f, 1f, 0.0f}; // top right
        ByteBuffer bb = ByteBuffer.allocateDirect(4 * squareCoords.length).order(ByteOrder.nativeOrder());
        FloatBuffer vertex = bb.asFloatBuffer().put(squareCoords);
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
        float[] max_aniso = new float[1];
        GLES20.glGetFloatv(GLES11Ext.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, max_aniso, 0);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES11Ext.GL_TEXTURE_MAX_ANISOTROPY_EXT,
                max_aniso[0]);
        //Buffer texBuffer = ByteBuffer.allocateDirect(texW * texH * 2);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, texW, texH, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, null);


// create render buffer and bind 16-bit depth buffer
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthRb[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, texW, texH);


        GLES20.glViewport(0, 0, texW, texH);
        float[] mMVPMatrix = new float[16];
        float[] mProjMatrix = new float[16];
        float[] mVMatrix = new float[16];

        Matrix.orthoM(mProjMatrix, 0, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f);
        Matrix.setLookAtM(mVMatrix, 0, 0, 1.0f, 0.0f, 0f, 0.0f, 0.0f, 0f, 0.0f, 1.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fb[0]);

// specify texture as color attachment
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, renderTex[0], 0);

// attach render buffer as depth buffer
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthRb[0]);

        GLES20.glUseProgram(program);
        GLES20.glClearColor(.0f, .0f, .0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        int mPositionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        //GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program, "uMVPMatrix"), 1, false, mMVPMatrix, 0);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                0, vertex);


        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        GLES20.glDisableVertexAttribArray(mPositionHandle);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTex[0]);

        //GLES20.glUniform1i(GLES20.glGetUniformLocation(renderShader, "texture"), 0);
    }

}
