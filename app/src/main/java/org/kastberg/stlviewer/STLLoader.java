package org.kastberg.stlviewer;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by kastberg on 10/22/2014.
 */
public class STLLoader {
    private static final String TAG = "STLLoader";
    public static STLModel fromInputStream(InputStream is)
    {
        byte[] header = new byte[80];
        byte[] triangles = new byte[4];
        try {
            is.read(header);
            is.read(triangles);
        } catch(IOException e) {
            Log.e(TAG, "you are bad and should feel bad.");
        }
        Log.e("stlviewer",new String(header));
        ByteBuffer wrapped = ByteBuffer.wrap(triangles);
        wrapped = wrapped.order(ByteOrder.LITTLE_ENDIAN);
        int numOfTriangles = (int)(((long)wrapped.getInt()&0xffffffff));
        STLModel model = new STLModel(numOfTriangles*3);
        for(int i=0;i<numOfTriangles;i++)
        {
            byte[] triangle = new byte[50];
            try {
                is.read(triangle);
            } catch(IOException e) {
                Log.e(TAG,"you are bad and should feel bad.");
            }
            FloatBuffer normal = ByteBuffer.wrap(triangle,0,12).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
            FloatBuffer tri = ByteBuffer.wrap(triangle, 12, 4 * 3 * 3).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
            //Log.e(TAG,normal.toString());
            for(int j=0;j<3;j++) {
                tri.get(model.vertex,3*3*i+j*3,3);
                normal.get(model.normal,3*3*i+j*3,3);
                normal.position(0);
                short attr = ByteBuffer.wrap(triangle, 48, 2).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(0);
            }
        }

        return model;
    }
}
