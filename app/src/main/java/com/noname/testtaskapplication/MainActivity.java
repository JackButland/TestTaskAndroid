package com.noname.testtaskapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {
    public static String TAG = "MainActivity";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnNative = findViewById(R.id.btnNative);
        Button btnDex = findViewById(R.id.btnDex);
        btnNative.setOnClickListener((view)->{
            Log.w(TAG,"start retrieving native fingerprint");
            String androidSecureId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            String androidGsfId = getGSFID(this);
            String androidBuildFingerprint = Build.FINGERPRINT;
            String fp = fingerprintFromJNI(androidSecureId,androidGsfId,androidBuildFingerprint);
            Log.w(TAG,"fingerprint: "+fp);
            Toast.makeText(getApplicationContext(),"fingerprint: "+fp,Toast.LENGTH_LONG).show();
        });
        btnDex.setOnClickListener((view)->{
            Log.w(TAG,"start retrieving dex fingerprint");
            try {
                String fp = fingerprintFromDex(this);
                Log.w(TAG,"fingerprint: "+fp);
                Toast.makeText(getApplicationContext(), "fingerprint: "+fp, Toast.LENGTH_LONG).show();
            }
            catch(Exception ex){
                Log.e(TAG,"Error making fingerprint from dex", ex);
                Toast.makeText(getApplicationContext(), "Error making fingerprint from dex", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String fingerprintFromJNI(String androidSecureID, String androidGsfID, String buildFingerprint);

    public static String fingerprintFromDex(Context context) throws Exception {
        InputStream is = context.getResources().getAssets().open("AndroidTestTaskDex_Dex.dex");
        File dexCacheFile = new File(context.getFilesDir(), "AndroidTestTaskDex_Dex.dex");
        IOUtils.copy(is, new FileOutputStream(dexCacheFile));
        DexClassLoader dexClassLoader = new DexClassLoader(dexCacheFile.getAbsolutePath(),
                context.getCacheDir().getPath(), context.getCacheDir().getPath(),
                context.getClass().getClassLoader());
        Class klass = dexClassLoader.loadClass("com.noname.dexfunc.Utils");
        Method method = klass.getDeclaredMethod("fingerprintFromDex", Context.class);
        String result = (String) method.invoke(null,context);
        return result;
    }

    public static String getGSFID(Context context) {
        try {
            Uri sUri = Uri.parse("content://com.google.android.gsf.gservices");
            Cursor query = context.getContentResolver().query(sUri, null, null, new String[] { "android_id" }, null);
            if (query == null) {
                return "Not found";
            }
            if (!query.moveToFirst() || query.getColumnCount() < 2) {
                query.close();
                return "Not found";
            }
            final String toHexString = Long.toHexString(Long.parseLong(query.getString(1)));
            query.close();
            return toHexString.toUpperCase().trim();
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }

}
