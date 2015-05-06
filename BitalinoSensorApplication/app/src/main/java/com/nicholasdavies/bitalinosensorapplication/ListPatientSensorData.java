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

/**
 * This Activity Displays all sensor data associated with a single patient
 *
 * @author Nick Davies
 */
public class ListPatientSensorData extends Activity implements Serializable {


    private static int lastArbitraryData = 0;
    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);

    /**
     * Custom List Object used to store a Sensor Information in a list as well as the Unique ID for that Input
     */
    private class ListObject {
        /**
         * Default Constructor
         */
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

    List<ListObject> sensordata;
    TextView error;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_sensor_data_list);
        StrictMode.enableDefaults();
        sensordata = new ArrayList<ListObject>();
        ListView resultView = (ListView) findViewById(R.id.patient_Names);


        getData();
        onClickList();
        if (sensordata.size() == 0) {
            int intsensorDataID = 0;
            String s = "No Sensor Data Found ";
            sensordata.add(new ListObject(intsensorDataID, s));
        }

        ArrayAdapter<ListObject> myadapter = new ArrayAdapter<ListObject>(this, R.layout.patient_list_item, R.id.label, sensordata);
        resultView.setAdapter(myadapter);
    }

    /**
     * Gets Patient Sensor Information from server
     */
    public void getData() {
        String result = "";
        InputStream isr = null;
        /** Extracts PatientID from last activity and passes this into the PhP script */
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
        /** Converts Response to a string using StringBuilder */
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
            /** Displays EMG, ECG or EDA depending on SensorType */
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

                /** Constructs List entry */
                s = "Date:" + json.getString("Date") + " | Sensor Type: " + sensorTypeString + " | Sample Rate: " + json.getString("SampleRate") + "Hz" + "\n";

                sensorDataID = json.getString("SensorDataID");
                intsensorDataID = Integer.parseInt(sensorDataID);

                /** Uses the same List Object used in Patient */
                sensordata.add(new ListObject(intsensorDataID, s));


            }

            error.setText(s);

        } catch (Exception e) {
            // TODO: handle exception
            Log.e("log_tag", "Error Parsing Data " + e.toString());
        }

    }


    private void onClickList() {
        /** When a user selects a specific data recording, a new activity is called where the sensorID is passed to */
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

