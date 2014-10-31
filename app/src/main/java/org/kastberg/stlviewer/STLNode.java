package org.kastberg.stlviewer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
* Created by kastberg on 10/31/2014.
*/
public class STLNode {
    public STLNode parent = null;
    public List<STLNode> children = new LinkedList<STLNode>();
    float[] worldMatrix = new float[16];
    float[] matrix = new float[16];
    public int program;
    private static final String TAG = "STLNode";
    STLNode() {
        Matrix.setIdentityM(worldMatrix, 0);
        Matrix.setIdentityM(matrix, 0);
    }
    STLNode(STLNode parent) {
        this();
        this.parent = parent;
    }
    public void pre()  {
        if(parent != null) {
            Matrix.multiplyMM(worldMatrix, 0, parent.worldMatrix, 0, matrix, 0);
        } else {
            worldMatrix = matrix;
        }
    }
    public void in() {
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program, "uMVPMatrix"), 1, false, worldMatrix, 0);
    }
    public void post() {}

    public static int loadShader(int type, String shaderCode)
    {
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        Log.e(TAG, GLES20.glGetShaderInfoLog(shader));

        return shader;
    }
    public void setShader(String vertex, String fragment) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertex);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragment);
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
    }
}
