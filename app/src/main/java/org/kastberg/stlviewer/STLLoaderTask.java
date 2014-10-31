package org.kastberg.stlviewer;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class STLLoaderTask extends AsyncTask<InputStream, Integer, STLModel> {
    private final String TAG = "STLLoaderTask";
    private final Context context;

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
        } catch (IOException e) {
            Log.e(TAG, "you are bad and should feel bad.");
        }
        Log.e("stlviewer", new String(header));
        ByteBuffer wrapped = ByteBuffer.wrap(triangles);
        wrapped = wrapped.order(ByteOrder.LITTLE_ENDIAN);
        int numOfTriangles = (int) (((long) wrapped.getInt()));
        STLModel model = new STLModel(numOfTriangles * 3);
        byte[] triangle = new byte[50 * numOfTriangles];
        try {
            is.read(triangle);
        } catch (IOException e) {
            Log.e(TAG, "you are bad and should feel bad.");
        }
        ByteBuffer ary = ByteBuffer.wrap(triangle);
        for (int i = 0; i < numOfTriangles; i++) {
            //Log.e(TAG,normal.toString());
            ary.limit(50 * i + 12);
            ary.position(50 * i);
            model.normalByte.put(ary);
            ary.position(50 * i);
            model.normalByte.put(ary);
            ary.position(50 * i);
            model.normalByte.put(ary);

            ary.limit(50 * i + 48);
            ary.position(50 * i + 12);
            model.vertexByte.put(ary);


            if (i % 100 == 0)
                publishProgress(10000 * (int) ((float) i) / numOfTriangles);
        }
        model.normal.position(0);
        model.vertex.position(0);
        return model;
    }

    protected void onPreExecute() {
        /*
        progressBar = new ProgressDialog(context);
        progressBar.setCancelable(true);
        progressBar.setMessage("Loading");
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();*/
        ((Activity) context).setProgressBarVisibility(true);
    }

    @Override
    protected void onPostExecute(STLModel model) {
        //((Activity)context).setProgressBarVisibility(false);
        ((Activity) context).setProgress(10000);
        ((STLSurfaceView) ((Activity) context).findViewById(R.id.surfaceView)).modelDone(model);
        Log.e(TAG, "onPostExecute");
        //((Activity)context).setProgress(1);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        ((Activity) context).setProgress(values[0]);
    }
}
