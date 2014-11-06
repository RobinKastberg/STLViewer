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
    private float[] modelViewProjectionMatrix = new float[16];
    float[] modelViewMatrix = new float[16];
    float[] inverseModelViewMatrix = new float[16];
    float[] modelMatrix = new float[16];
    private String vertex_shader;
    private String fragment_shader;

    STLNode() {
        Matrix.setIdentityM(modelViewProjectionMatrix, 0);
        Matrix.setIdentityM(modelMatrix, 0);
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

    public void pre(STLSceneGraph sg) {
        if (parent != null) {
            Matrix.multiplyMM(modelViewProjectionMatrix, 0, parent.modelViewProjectionMatrix, 0, modelMatrix, 0);
        } else {
            modelViewProjectionMatrix = modelMatrix;
        }
    }

    public void in(STLSceneGraph sg) {
        if (program == -1) {
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertex_shader);
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragment_shader);
            program = GLES20.glCreateProgram();
            GLES20.glAttachShader(program, vertexShader);
            GLES20.glAttachShader(program, fragmentShader);
            GLES20.glLinkProgram(program);
            Log.e("SHADER", GLES20.glGetProgramInfoLog(program));
            sg.shaders.add(program);
        }

        GLES20.glUseProgram(program);

        setMat4("modelViewProjectionMatrix", modelViewProjectionMatrix);
        setMat4("modelMatrix", modelMatrix);

        Matrix.multiplyMM(modelViewMatrix, 0, sg.camera.viewMatrix, 0, modelMatrix, 0);
        setMat4("modelViewMatrix", modelViewMatrix);
        Matrix.invertM(inverseModelViewMatrix, 0, modelViewMatrix, 0);
        setMat4("inverseModelViewMatrix", inverseModelViewMatrix);
        setMat4Trans("normalMatrix", inverseModelViewMatrix);
    }

    public void post(STLSceneGraph sg) {
    }

    public void setShader(String vertex, String fragment) {
        this.vertex_shader = vertex;
        this.fragment_shader = fragment;
    }

    public void setFloat(String name, float f) {
        GLES20.glUniform1f(GLES20.glGetUniformLocation(program, name), f);
    }

    public void setInt(String name, int i) {
        GLES20.glUniform1i(GLES20.glGetUniformLocation(program, name), i);
    }

    public void setVec4(String name, float[] vec4) {
        GLES20.glUniform4fv(GLES20.glGetUniformLocation(program, name), 1, vec4, 0);
    }

    public void setVec3(String name, float[] vec3) {
        GLES20.glUniform3fv(GLES20.glGetUniformLocation(program, name), 1, vec3, 0);
    }

    public void setMat4(String name, float[] mat4) {
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program, name), 1, false, mat4, 0);
    }

    public void setMat4Trans(String name, float[] mat4) {
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program, name), 1, true, mat4, 0);
    }
}
