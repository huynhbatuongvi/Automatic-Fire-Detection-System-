package com.example.doan2;

import android.content.Context;
import android.content.SharedPreferences;

public class DataManager {
    private static final String PREFERENCE_NAME = "DevicePrefs";
    private static final String PREF_NAME = "AppPreferences";

    public static void saveDeviceName(Context context, String deviceKey, String deviceName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(deviceKey, deviceName);
        editor.apply();
    }

    public static String getDeviceName(Context context, String deviceKey, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(deviceKey, defaultValue);
    }

    public static void saveSwitchState(Context context, String switchKey, boolean switchState) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(switchKey, switchState);
        editor.apply();
    }

    public static boolean getSwitchState(Context context, String switchKey, boolean defaultState) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(switchKey, defaultState);
    }

    public static void saveBatteryPercentage(Context context, float batteryPercentage) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putFloat("BatteryPercentage", batteryPercentage);
        editor.apply();
    }

    public static float getBatteryPercentage(Context context, float defaultPercentage) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getFloat("BatteryPercentage", defaultPercentage);
    }
}
