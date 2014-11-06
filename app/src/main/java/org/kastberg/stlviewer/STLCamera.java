package org.kastberg.stlviewer;

import android.opengl.GLES20;
import android.opengl.Matrix;

/**
 * Created by kastberg on 11/5/2014.
 */
public class STLCamera extends STLNode {
    public float[] viewMatrix = new float[16];
    public float[] projectionMatrix = new float[16];
    public float[] position = new float[3];
    public float[] lookAt = new float[3];

    STLCamera() {
        super();
        Matrix.setIdentityM(viewMatrix, 0);
        Matrix.setIdentityM(projectionMatrix, 0);
    }

    public void setPosition(float x, float y, float z) {
        position[0] = x;
        position[1] = y;
        position[2] = z;
        Matrix.setLookAtM(viewMatrix, 0, position[0], position[1], position[2], lookAt[0], lookAt[1], lookAt[2], 0.0f, 1.0f, 0.0f);
    }

    public void lookAt(float x, float y, float z) {
        lookAt[0] = x;
        lookAt[1] = y;
        lookAt[2] = z;
        Matrix.setLookAtM(viewMatrix, 0, position[0], position[1], position[2], lookAt[0], lookAt[1], lookAt[2], 0.0f, 1.0f, 0.0f);
    }

    public void setProjection(int w, int h, float fovy) {
        float ratio = (float) w / h;
        float zNear = 0.1f;
        float zFar = 100.0f;
        float fH = (float) Math.tan(fovy / 360 * Math.PI) * zNear;
        float fW = fH * ratio;
        Matrix.frustumM(projectionMatrix, 0, -fW, fW, -fH, fH, zNear, zFar);
    }

    @Override
    public void pre(STLSceneGraph sg) {
        Matrix.multiplyMM(modelMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        super.pre(sg);
        for (int prog : sg.shaders) {
            GLES20.glUseProgram(prog);
            GLES20.glUniform3fv(GLES20.glGetUniformLocation(prog, "eye"), 1, position, 0);
            GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(prog, "viewMatrix"), 1, false, viewMatrix, 0);
            GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(prog, "projectionMatrix"), 1, false, projectionMatrix, 0);
        }
    }

    @Override
    public void in(STLSceneGraph sg) {
    }
}
