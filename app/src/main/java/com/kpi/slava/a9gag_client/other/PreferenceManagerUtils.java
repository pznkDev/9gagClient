package com.kpi.slava.a9gag_client.other;


import android.content.Context;
import android.preference.PreferenceManager;

public class PreferenceManagerUtils {

    public static final String LOGIN_VK = "vk";

    public static boolean isLoginVk(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(LOGIN_VK, false);
    }

    public static void setLoginVk(Context context, boolean flag) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(LOGIN_VK, flag)
                .apply();
    }

}
