package com.noname.dexfunc;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Base64;

public class Utils {
	public static String fingerprintFromDex(Context context) throws NoSuchAlgorithmException, InvalidKeyException {
		String androidSecureId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String androidGsfId = getGSFID(context);
        String androidBuildFingerprint = Build.FINGERPRINT;
        Mac mac = Mac.getInstance("HMACSHA1");
        String key = "random init key";
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(),"HMACSHA1");
        mac.init(keySpec);
        mac.update(androidSecureId.getBytes());
        mac.update(androidGsfId.getBytes());
        mac.update(androidBuildFingerprint.getBytes());
        return Base64.encodeToString(mac.doFinal(),2);
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
