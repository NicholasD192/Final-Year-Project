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
 * Created by Nick Davies on 12/04/2015.
 */
public class Settings extends Activity {

    Button bUpdate, bBack;
    EditText rootIP;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);



        rootIP = (EditText) findViewById(R.id.editIP);
        bUpdate = (Button)  findViewById(R.id.btnUpdate);
        bBack = (Button) findViewById(R.id.btnBack);



        rootIP.setText(Utilities.getIP(getApplicationContext()));

        bBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openStartingPoint = new Intent(getApplicationContext(), Main.class);
                startActivity(openStartingPoint);
            }
        });

        bUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String data;
                data = rootIP.getText().toString();
                /*try {
                    FileOutputStream fOut = openFileOutput(Filename,MODE_WORLD_READABLE);
                    fOut.write(data.getBytes());
                    fOut.close();
                    Toast.makeText(getBaseContext(),"file saved",
                            Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }*/

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                prefs.edit().putString("host_ip", data).apply();







            }
        });


    }




}
