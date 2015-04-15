package com.nicholasdavies.bitalinosensorapplication;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * Created by Nick Davies on 12/04/2015.
 */

/**
 * Utility class with cool static methods (inspired by Tesla)
 */
public class Utilities {

    /**
     * Get the base IP for the service
     *
     * @param ctx Context to use for the shared preferences
     * @return IP from preferences
     */
    public static String getIP(Context ctx) {
        /**
         * Preferences object from the android abstraction layer
         */
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        // Return the host ip from the preferences
        return prefs.getString("host_ip", "");
    }

    // ***
    // WHEN YOU WANT TO GET THE URL USE Utilities.getURL(getApplicationContext());
    // *&**

    /**
     * Get the entire URL for the service
     *
     * @param ctx Context to use for the shared preferences
     * @return Entire URL
     */
    public static String getURL(Context ctx) {
        return "http://" + getIP(ctx) + "/scripts/";
    }
}
