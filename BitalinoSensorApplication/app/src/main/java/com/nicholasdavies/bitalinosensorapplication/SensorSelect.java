
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
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.Toast;
/**
 * Activity where user selects sensor and patient they wish to record data to
 *
 * @author Nick Davies
 */




public class SensorSelect extends Activity implements Serializable {

    private static int lastArbitraryData = 0;


    /** Uses the same custom list object as found in PatientNames.java */

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
            arbitraryDataA = ++SensorSelect.lastArbitraryData;
        }
    }

    List<ListObject> patientnames;
    TextView error;

    boolean noSensor, noSample;
    int sensorType, sampleRate;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_select);
        StrictMode.enableDefaults(); //STRICT MODE ENABLE
        patientnames = new ArrayList<ListObject>();
        ListView resultView = (ListView) findViewById(R.id.patient_Names);
        EditText search = (EditText) findViewById(R.id.search);
        setupDropDown();

        getData();
        onClickList();

        final ArrayAdapter<ListObject> myadapter = new ArrayAdapter<ListObject>(this, R.layout.patient_list_item, R.id.label, patientnames);
        resultView.setAdapter(myadapter);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                myadapter.getFilter().filter(charSequence);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    public void setupDropDown() {
        /** Dropdown menu where user can select Sensors */
        Spinner sensorDropDown = (Spinner) findViewById(R.id.spinner1);
        String[] dropdownsensoritems = new String[]{"Choose Sensor", "EMG", "ECG", "EDA"};

        ArrayAdapter<String> dropDownAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dropdownsensoritems);
        sensorDropDown.setAdapter(dropDownAdapter);
        sensorDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                /** Validation */
                if (i == 0) {
                    noSensor = true;
                } else {
                    noSensor = false;
                    sensorType = i;

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        /** Dropdown menu where user can select Sensor Sample Rate */
        String[] sampleRateArray = new String[]{"Choose Rate", "10", "100", "1000"};
        Spinner sampleDropDown = (Spinner) findViewById(R.id.spinner2);
        ArrayAdapter<String> sampleRateAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sampleRateArray);
        sampleDropDown.setAdapter(sampleRateAdapter);
        sampleDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                /** Validation */
                if (i == 0) {
                    noSample = true;
                } else {
                    noSample = false;
                    if (i==1)
                    {
                        sampleRate = 10;
                    }
                    if (i==2)
                    {
                        sampleRate = 100;

                    }
                    if (i==3)
                    {
                        sampleRate = 1000;
                    }


                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }


    public void getData() {
        String result = "";
        InputStream isr = null;
        /** Connecting to server */
        try {
            HttpClient httpclient = new DefaultHttpClient();
            String baseURL = Utilities.getURL(getApplicationContext());
            HttpPost httppost = new HttpPost(baseURL + "getpatientnames.php"); //YOUR PHP SCRIPT ADDRESS
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            isr = entity.getContent();
        } catch (Exception e) {
            Log.e("log_tag", "Error in http connection " + e.toString());
            Intent openStartingPoint = new Intent("com.nicholasdavies.bitalinosensorapplication.MAIN");
            startActivity(openStartingPoint);
            Toast.makeText(getApplicationContext(), "Network Error - Check IP Settings", Toast.LENGTH_LONG).show();
        }
        /** Converting response to string */
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

        try {
            String s = "";
            String sid = "";
            Integer id = 0;
            JSONArray jArray = new JSONArray(result);

            for (int i = 0; i < jArray.length(); i++) {
                JSONObject json = jArray.getJSONObject(i);
                s = "" + json.getString("First Names") + " " + json.getString("Last Names") + "\n";
                sid = "" + json.getString("PatientID");
                id = Integer.parseInt(sid);

                patientnames.add(new ListObject(id, s));

            }

            error.setText(s);

        } catch (Exception e) {
            // TODO: handle exception
            Log.e("log_tag", "Error Parsing Data " + e.toString());
        }

    }


    public void onClickList() {
        /** Once a user selects a patient, Validation takes place to make sure the user has bluetooth turned on and has selected a sensor */
        final BluetoothAdapter BluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
        final ListView resultView = (ListView) findViewById(R.id.patient_Names);
        resultView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (noSensor) {

                    Toast.makeText(adapterView.getContext(), "You have not selected a sensor", Toast.LENGTH_SHORT).show();

                }
                if (noSample) {

                    Toast.makeText(adapterView.getContext(), "You have not selected a Sample Rate", Toast.LENGTH_SHORT).show();

                }
                if (!BluetoothAdapter.isEnabled()) {
                    Toast.makeText(adapterView.getContext(), "Bluetooth must be enabled to connect to Bitalino Device", Toast.LENGTH_SHORT).show();
                }
                if (BluetoothAdapter.isEnabled() && (!noSensor) && (!noSample)) {
                    ListObject item = (ListObject) resultView.getItemAtPosition(i);
                    Intent intent = new Intent(getApplicationContext(), RecordPatientSensorData.class);
                    Bundle extras = new Bundle();


                    extras.putInt("sampleRate", sampleRate);
                    extras.putInt("sensorType", sensorType);
                    extras.putInt("PatientID", item.getArbitraryDataA());
                    intent.putExtras(extras);
                    startActivity(intent);

                }

            }
        });


    }
}


