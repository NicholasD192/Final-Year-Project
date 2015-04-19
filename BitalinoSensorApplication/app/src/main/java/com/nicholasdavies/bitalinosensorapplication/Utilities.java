package com.nicholasdavies.bitalinosensorapplication;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * Class Containing methods to retrieve the server IP
 *
 * @author Nick Davies
 */
public class Utilities {

    /** This method is called when only the IP fo the server is wanted */
    public static String getIP(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        // Return the host ip from the shared preferences
        return prefs.getString("host_ip", "");
    }


    /** This method is called when the IP and scripts directory are required*/
    public static String getURL(Context ctx) {
        return "http://" + getIP(ctx) + "/scripts/";
    }
}
