package com.nicholasdavies.bitalinosensorapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Activity where user can select custom server IP (Server must be configured with correct scripts)
 *
 * @author Nick Davies
 */

public class Settings extends Activity {

    Button bUpdate, bBack;
    EditText rootIP, macAddress;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        macAddress = (EditText) findViewById(R.id.editMac);
        rootIP = (EditText) findViewById(R.id.editIP);
        bUpdate = (Button) findViewById(R.id.btnUpdate);


        rootIP.setText(Utilities.getIP(getApplicationContext()));
        /** Uses android own Shared preferences to store the IP Address */

        macAddress.setText(Utilities.getMac(getApplicationContext()));
        /** Uses android own Shared preferences to store the IP Address */

        bUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputIP;
                String inputMac;

                inputIP = rootIP.getText().toString();

                inputMac = macAddress.getText().toString();

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                prefs.edit().putString("host_ip", inputIP).apply();
                prefs.edit().putString("mac_address", inputMac).apply();


            }
        });


    }


}
