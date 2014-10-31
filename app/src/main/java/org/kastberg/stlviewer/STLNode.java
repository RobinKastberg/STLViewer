package org.kastberg.stlviewer;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

public class STLNode {
    private static final String TAG = "STLNode";
    public STLNode parent = null;
    public final List<STLNode> children = new LinkedList<STLNode>();
    public int program = -1;
    private float[] worldMatrix = new float[16];
    float[] matrix = new float[16];
    private String vertex_shader;
    private String fragment_shader;

    STLNode() {
        Matrix.setIdentityM(worldMatrix, 0);
        Matrix.setIdentityM(matrix, 0);
    }

    STLNode(STLNode parent) {
        this();
        this.parent = parent;
    }

    private static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        Log.e("SHADER", GLES20.glGetShaderInfoLog(shader));

        return shader;
    }

    public void pre() {
        if (parent != null) {
            Matrix.multiplyMM(worldMatrix, 0, parent.worldMatrix, 0, matrix, 0);
        } else {
            worldMatrix = matrix;
        }
    }

    public void in() {
        //Log.e("STLNode",""+this);
        if (program == -1) {
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertex_shader);
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragment_shader);
            program = GLES20.glCreateProgram();
            GLES20.glAttachShader(program, vertexShader);
            GLES20.glAttachShader(program, fragmentShader);
            GLES20.glLinkProgram(program);
            Log.e("SHADER", GLES20.glGetProgramInfoLog(program));
        }
        GLES20.glUseProgram(program);
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program, "uMVPMatrix"), 1, false, worldMatrix, 0);
    }

    public void post() {
    }

    public void setShader(String vertex, String fragment) {
        this.vertex_shader = vertex;
        this.fragment_shader = fragment;

    }
}
