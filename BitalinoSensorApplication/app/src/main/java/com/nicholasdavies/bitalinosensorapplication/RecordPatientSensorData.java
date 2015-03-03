package com.nicholasdavies.bitalinosensorapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by Nick Davies on 03/03/2015.
 */
public class RecordPatientSensorData extends Activity {


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_patient_sensor_data);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        int patientID = bundle.getInt("PatientID");
        int sensorType = bundle.getInt("sensorType");


        TextView textView = (TextView)findViewById(R.id.textView);
        textView.setText("Patient ID ="+patientID+"Sensor Type = "+sensorType);


    }

}
