package org.kastberg.stlviewer;

import android.content.Context;
import android.util.Log;

import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by kastberg on 11/5/2014.
 */
public class STLUtil {
    public static final String TAG = "STLUtil";

    public static final String assetToString(Context ctx, String fname) {
        try {
            //Log.e(TAG, "KEKE"+CharStreams.toString(new InputStreamReader(ctx.getAssets().open(fname), "ISO-8859-1"))+"HEHU");
            return "" + CharStreams.toString(new InputStreamReader(ctx.getAssets().open(fname), "UTF-8"));
        } catch (IOException e) {
            Log.e(TAG, "assetToString(" + fname + "): " + e.toString());
            return "";
        }
    }
}
