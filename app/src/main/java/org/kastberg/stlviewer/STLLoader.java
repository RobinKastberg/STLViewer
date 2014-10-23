package org.kastberg.stlviewer;

import android.content.Context;
import android.util.Log;

import java.io.InputStream;

/**
 * Created by kastberg on 10/22/2014.
 */
public class STLLoader {
    private static final String TAG = "STLLoader";
    public static STLModel fromInputStream(Context ctx, InputStream is)
    {
        try {
            return new STLLoaderTask(ctx).execute(is).get();
        } catch(Exception e) {
            Log.e(TAG, "I SUCK COCKS: "+e);
            return null;
        }
    }
}
