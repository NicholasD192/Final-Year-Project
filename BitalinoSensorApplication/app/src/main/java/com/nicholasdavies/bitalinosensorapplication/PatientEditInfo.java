package com.nicholasdavies.bitalinosensorapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Nick Davies on 24/11/2014.
 */

public class PatientEditInfo extends Activity {
    Button bUploadEdit, bBack;

    TextView firstName, lastName, dateOfBirth, patientSymptoms;

    boolean firstNameCheck, lastNameCheck, dateCheck;


    InputStream isr = null;
    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
    int patientID = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_editinfo);


        firstName = (EditText) findViewById(R.id.firstName);

        lastName = (EditText) findViewById(R.id.lastName);

        dateOfBirth = (EditText) findViewById(R.id.dateOfBirth);

        patientSymptoms = (EditText) findViewById(R.id.symptoms);

        bUploadEdit = (Button) findViewById(R.id.btnUploadEdit);

        bBack = (Button) findViewById(R.id.btnBack);

        Intent patientIdIntent = getIntent();
        patientID = patientIdIntent.getIntExtra("PatientID", 0);

        getData();

        firstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (firstName.getText().toString().length() == 0) {
                    firstName.setError("First Name is required!");
                    firstNameCheck = true;
                } else
                    firstNameCheck = false;


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        lastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (lastName.getText().toString().length() == 0) {
                    lastName.setError("First Name is required!");
                    lastNameCheck = true;
                } else
                    lastNameCheck = false;


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        dateOfBirth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (dateOfBirth.getText().toString().length() == 0) {
                    dateOfBirth.setError("First Name is required!");
                    dateCheck = true;
                } else
                    dateCheck = false;


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

//        bBack.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View arg0) {
//
//                Intent openStartingPoint = new Intent(getApplicationContext(), PatientNames.class);
//                startActivity(openStartingPoint);
//
//
//            }
//        });

        bUploadEdit.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {

//                if (!firstNameCheck || !lastNameCheck || !dateCheck) {

                String firstname = "" + firstName.getText().toString();
                String lastname = "" + lastName.getText().toString();
                String dateofbirth = "" + dateOfBirth.getText().toString();
                String symptoms = "" + patientSymptoms.getText().toString();

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);


                nameValuePairs.add(new BasicNameValuePair("patientID", String.valueOf(patientID)));
                nameValuePairs.add(new BasicNameValuePair("firstname", firstname));
                nameValuePairs.add(new BasicNameValuePair("lastname", lastname));
                nameValuePairs.add(new BasicNameValuePair("dateofbirth", dateofbirth));
                nameValuePairs.add(new BasicNameValuePair("symptoms", symptoms));

                //Actually connecting to the server
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    String baseURL = Utilities.getURL(getApplicationContext());
                    HttpPost httppost = new HttpPost(baseURL + "editpatient.php"); //YOUR PHP SCRIPT ADDRESS
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    isr = entity.getContent();

                    String msg = "Data Edited Successfully";
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();

                    Intent patientIdIntent = getIntent();
                    int PatientID = patientIdIntent.getIntExtra("PatientID", 0);
                    Intent openStartingPoint = new Intent(getApplicationContext(), PatientInfo.class);
                    openStartingPoint.putExtra("PatientID", PatientID);
                    startActivity(openStartingPoint);

                } catch (ClientProtocolException e) {
                    Log.e("ClientProtocal", "Log_tag");
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e("Log_tag", "IOException");
                    e.printStackTrace();
                }
//                } else
//                {
//                    Toast.makeText(getApplicationContext(), "Invalid Input", Toast.LENGTH_LONG).show();
//                }


            }
        });


    }


    public void getData() {
        String result = "";
        InputStream isr = null;


        nameValuePairs.add(new BasicNameValuePair("patientID", String.valueOf(patientID)));

        try {
            HttpClient httpclient = new DefaultHttpClient();
            String baseURL = Utilities.getURL(getApplicationContext());
            HttpPost httppost = new HttpPost(baseURL + "getpatientinfo.php"); //YOUR PHP SCRIPT ADDRESS
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            isr = entity.getContent();

        } catch (ClientProtocolException e) {
            Log.e("ClientProtocal", "Log_tag");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("Log_tag", "IOException");
            e.printStackTrace();
        }


        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(isr, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            isr.close();

            result = sb.toString();
        } catch (Exception e) {
            Log.e("log_tag", "Error  converting result " + e.toString());
        }

        //parse json data
        try {

            JSONArray jArray = new JSONArray(result);

            for (int i = 0; i < jArray.length(); i++) {
                JSONObject json = jArray.getJSONObject(i);
                firstName.setText(json.getString("First Names"));
                lastName.setText(json.getString("Last Names"));
                dateOfBirth.setText(json.getString("Date Of Birth"));
                patientSymptoms.setText(json.getString("Symptoms"));


            }

        } catch (Exception e) {
            // TODO: handle exception
            Log.e("log_tag", "Error Parsing Data " + e.toString());
        }

    }


}

