package org.kastberg.stlviewer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
* Created by kastberg on 10/23/2014.
*/
public class STLLoaderTask extends AsyncTask<InputStream, Integer, STLModel> {
    private final String TAG = "STLLoaderTask";
    private Context context;

    public STLLoaderTask(Context context) {
        this.context = context;
    }
    protected STLModel doInBackground(InputStream... iss) {
        InputStream is = iss[0];
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
                tri.get(model.vertex, 3 * 3 * i + j * 3, 3);
                normal.get(model.normal,3*3*i+j*3,3);
                normal.position(0);
                short attr = ByteBuffer.wrap(triangle, 48, 2).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(0);
            }
            if(i%100==0)
                publishProgress(10000*(int)((float)i)/numOfTriangles);
        }
        return model;
    }

    ProgressDialog progressBar;
    protected void onPreExecute() {
        /*
        progressBar = new ProgressDialog(context);
        progressBar.setCancelable(true);
        progressBar.setMessage("Loading");
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();*/
        ((Activity)context).setProgressBarVisibility(true);
    }
    @Override
    protected void onPostExecute(STLModel models) {
        //((Activity)context).setProgressBarVisibility(false);
        ((Activity)context).setProgress(10000);
        ((STLSurfaceView)((Activity)context).findViewById(R.id.surfaceView)).modelDone(models);
        Log.e(TAG,"onPostExecute");
        //((Activity)context).setProgress(1);
    }
    @Override
    protected void onProgressUpdate(Integer... values) {
        ((Activity)context).setProgress(values[0]);
    }
}
