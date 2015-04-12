package com.nicholasdavies.bitalinosensorapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

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
        final String Filename = "current_IP";
        File file = new File(Filename);
        if(!file.exists())
        {
            try {
                file.createNewFile();
            } catch (IOException e) {

            }
            // write code for saving data to the file
        }

        try{
            FileInputStream fin = openFileInput(Filename);
            int c;
            String temp="";
            while( (c = fin.read()) != -1){
                temp = temp + Character.toString((char)c);
            }
            rootIP.setText(temp);
            Toast.makeText(getBaseContext(),"file read",
                    Toast.LENGTH_SHORT).show();

        }catch(Exception e){

        }




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
                try {
                    FileOutputStream fOut = openFileOutput(Filename,MODE_WORLD_READABLE);
                    fOut.write(data.getBytes());
                    fOut.close();
                    Toast.makeText(getBaseContext(),"file saved",
                            Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }





            }
        });


    }




}
