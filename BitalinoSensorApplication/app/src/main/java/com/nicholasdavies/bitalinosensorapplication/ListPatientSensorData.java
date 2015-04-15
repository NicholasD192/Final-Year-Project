package com.nicholasdavies.bitalinosensorapplication;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListPatientSensorData extends Activity implements Serializable {
    /**
     * Called when the activity is first created.
     */

    private static int lastArbitraryData = 0;
    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);

    private class ListObject {
        private ListObject(Integer arbitraryDataA, String arbitraryDataB) {
            this.arbitraryDataA = arbitraryDataA;
            this.arbitraryDataB = arbitraryDataB;
        }

        private Integer arbitraryDataA = 0;
        private String arbitraryDataB = "";

        @Override
        public String toString() {
            return arbitraryDataB;
        }

        public int getArbitraryDataA() {
            return arbitraryDataA;
        }

        public String getArbitraryDataB() {
            return arbitraryDataB;
        }

        public ListObject() {
            arbitraryDataA = ++ListPatientSensorData.lastArbitraryData;
        }
    }

    List<ListObject> patientnames;
    TextView error;
    ListView resultView;

    Button bBack;
    int x = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_sensor_data_list);
        StrictMode.enableDefaults(); //STRICT MODE ENABLE
        patientnames = new ArrayList<ListObject>();
        ListView resultView = (ListView) findViewById(R.id.patient_Names);

        // Button bBack = (Button) findViewById(R.id.btnBack);

        getData();
        onClickList();
        if (patientnames.size() == 0) {
            int intsensorDataID = 0;
            String s = "No Sensor Data Found ";
            patientnames.add(new ListObject(intsensorDataID, s));
        }

        ArrayAdapter<ListObject> myadapter = new ArrayAdapter<ListObject>(this, R.layout.patient_list_item, R.id.label, patientnames);
        resultView.setAdapter(myadapter);
    }


    public void getData() {
        String result = "";
        InputStream isr = null;
        //Actually connecting to the server
        Intent patientIdIntent = getIntent();
        int PatientID = patientIdIntent.getIntExtra("PatientID", 0);
        nameValuePairs.add(new BasicNameValuePair("patientID", String.valueOf(PatientID)));
        try {

            HttpClient httpclient = new DefaultHttpClient();
            String baseURL = Utilities.getURL(getApplicationContext());
            HttpPost httppost = new HttpPost(baseURL + "getpatientsensordatalist.php"); //YOUR PHP SCRIPT ADDRESS
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            isr = entity.getContent();
        } catch (Exception e) {
            Log.e("log_tag", "Error in http connection " + e.toString());
            error.setText("Couldn't connect to database");
        }
        //convert response to string
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
            String s = "";
            String sensorTypeString = "";
            String sensorType = "";
            String sensorDataID = "";
            int intsensorDataID;
            int id;

            JSONArray jArray = new JSONArray(result);

            for (int i = 0; i < jArray.length(); i++) {
                JSONObject json = jArray.getJSONObject(i);
                sensorType = "" + json.getString("SensorType");


                id = Integer.parseInt(sensorType);

                if (id == 1) {
                    sensorTypeString = "EMG";
                }
                if (id == 2) {
                    sensorTypeString = "ECG";
                }
                if (id == 3) {
                    sensorTypeString = "EDA";
                }


                s = "Date:" + json.getString("Date") + " Sensor Type: " + sensorTypeString + "\n";

                sensorDataID = json.getString("SensorDataID");
                intsensorDataID = Integer.parseInt(sensorDataID);


                patientnames.add(new ListObject(intsensorDataID, s));


            }

            error.setText(s);

        } catch (Exception e) {
            // TODO: handle exception
            Log.e("log_tag", "Error Parsing Data " + e.toString());
        }

    }


    private void onClickList() {
        final ListView resultView = (ListView) findViewById(R.id.patient_Names);
        resultView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent SensorIDintent = new Intent(getApplicationContext(), RecreateSensorData.class);

                ListObject item = (ListObject) resultView.getItemAtPosition(i);

                SensorIDintent.putExtra("SensordataID", item.getArbitraryDataA());


                startActivity(SensorIDintent);


            }
        });


    }
}

