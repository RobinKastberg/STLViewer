package org.kastberg.stlviewer;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class STLModel extends STLNode {
    private static final String TAG = "STLModel";
    public final FloatBuffer vertex;
    public final FloatBuffer normal;
    public final ByteBuffer vertexByte;
    public final ByteBuffer normalByte;
    private final int len;

    STLModel(int numberOfVertices) {
        super();
        len = numberOfVertices;
        vertexByte = ByteBuffer.allocateDirect(4 * 3 * len).order(ByteOrder.nativeOrder());
        normalByte = ByteBuffer.allocateDirect(4 * 3 * len).order(ByteOrder.nativeOrder());
        vertex = vertexByte.asFloatBuffer();
        normal = normalByte.asFloatBuffer();
    }

    public void center() {
        float[] middle = new float[3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < len; j++) {
                middle[i] += vertex.get(3 * j + i);
            }
            middle[i] /= len;
        }
        Log.d(TAG, "Middle=(" + middle[0] + "," + middle[1] + "," + middle[2] + ")");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < len; j++) {
                vertex.put(3 * j + i, vertex.get(3 * j + i) - middle[i]);
            }
        }
    }

    public void scale(float scale) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < len; j++) {
                vertex.put(3 * j + i, vertex.get(3 * j + i) * scale);
            }
        }
    }

    public void scale() {
        double max = 0.0f;
        for (int j = 0; j < len; j++) {
            double l2 = Math.sqrt(vertex.get(3 * j) * vertex.get(3 * j) + vertex.get(3 * j + 1) * vertex.get(3 * j + 1) + vertex.get(3 * j + 2) * vertex.get(3 * j + 2));
            max = (l2 > max) ? l2 : max;
        }
        Log.d(TAG, "Scaling=" + 1.0 / max);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < len; j++) {
                vertex.put(3 * j + i, vertex.get(3 * j + i) / (float) max);
            }
        }
    }

    @Override
    public void in() {
        super.in();
        int mPositionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        int mNormalHandle = GLES20.glGetAttribLocation(program, "vNormal");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                0, vertex);
        GLES20.glVertexAttribPointer(mNormalHandle, 3,
                GLES20.GL_FLOAT, false,
                0, normal);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, len);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mNormalHandle);
    }
}
