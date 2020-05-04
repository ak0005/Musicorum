package com.RunTimeTerror.Musicorum.utils;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class Util {
    public static final String CURRENT_VOLUME_KEY = "current_volume";

    public static int getPreferenceValue(String str, int i, Context context) {
        if (str == null || context == null) {
            return -1;
        }
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(str, i);
    }

    public static void saveInPreferences(Context context, String str, int i) {
        if (context != null && str != null) {
            Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
            edit.putInt(str, i);
            edit.commit();
        }
    }


}

