package org.kastberg.stlviewer;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by kastberg on 10/22/2014.
 */
public class STLModel {
    public float[] vertex;
    public float[] normal;
    int len;
    private static final String TAG = "STLModel";
    STLModel(int numberOfVertices) {
        vertex = new float[3*numberOfVertices];
        normal = new float[3*numberOfVertices];
        len = numberOfVertices;
    }

    public void center() {
        float[] middle = new float[3];
        for(int i=0;i<3;i++) {
            for(int j = 0;j<len;j++) {
                middle[i] += vertex[3*j+i];
            }
            middle[i] /= len;
        }
        Log.d(TAG, "Middle=(" +middle[0]+","+middle[1]+","+middle[2]+")");
        for(int i=0;i<3;i++) {
            for(int j = 0;j<len;j++) {
                vertex[3*j+i] -= middle[i];
            }
        }
    }

    public void scale(float scale) {
        for(int i=0;i<3;i++) {
            for(int j = 0;j<len;j++) {
                vertex[3*j+i] *= scale;
            }
        }
    }
    public void scale() {
        double max = 0.0f;
        for(int j = 0;j<len;j++) {
            double l2 = Math.sqrt(vertex[3*j]*vertex[3*j]+vertex[3*j+1]*vertex[3*j+1]+vertex[3*j+2]*vertex[3*j+2]);
            max = (l2 > max) ? l2 : max;
        }
        Log.d(TAG, "Scaling=" + 1.0 / max);
        for(int i=0;i<3;i++) {
            for(int j = 0;j<len;j++) {
                vertex[3*j+i] /= max;
            }
        }
    }
    public FloatBuffer vertexBuffer() {
        FloatBuffer buf = ByteBuffer.allocateDirect(4*3*len).order(ByteOrder.nativeOrder()).asFloatBuffer();
        buf.put(vertex);
        buf.position(0);
        return buf;
    }
    public FloatBuffer normalBuffer() {
        FloatBuffer buf = ByteBuffer.allocateDirect(4*3*len).order(ByteOrder.nativeOrder()).asFloatBuffer();
        buf.put(normal);
        buf.position(0);
        return buf;
    }
}
