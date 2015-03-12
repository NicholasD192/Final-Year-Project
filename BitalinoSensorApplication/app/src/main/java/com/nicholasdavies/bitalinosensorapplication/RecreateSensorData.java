package com.nicholasdavies.bitalinosensorapplication;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.io.Files;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewDataInterface;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.nicholasdavies.bitalinosensorapplication.BITalinoDevice;
import com.nicholasdavies.bitalinosensorapplication.BITalinoFrame;

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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import retrofit.RestAdapter;
import retrofit.client.Response;


public class RecreateSensorData extends Activity {

    private static final String TAG = "LiveInfo";
    private static final boolean UPLOAD = false;
    ArrayList<String> patientNames;
    ListView mainList;
    ArrayAdapter adapter;
    Button bCancel;
    Button bRestart;
    File root = android.os.Environment.getExternalStorageDirectory();

    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);


    private class GraphViewWrapper implements GraphViewDataInterface {

        private int mX = 0;
        private int mY = 0;

        public GraphViewWrapper(int x, int y) {
            System.out.println("X: " + x + "| Y: " + y);
            mX = x;
            mY = y;
        }

        @Override
        public double getX() {
            return mX;
        }

        @Override
        public double getY() {
            return mY;
        }

        public void setX(int value) {
            mX = value;
        }

        public void setY(int value) {
            mY = value;
        }
    }

    /*
     * http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html
     * #createRfcommSocketToServiceRecord(java.util.UUID)
     *
     * "Hint: If you are connecting to a Bluetooth serial board then try using the
     * well-known SPP UUID 00001101-0000-1000-8000-00805F9B34FB. However if you
     * are connecting to an Android peer then please generate your own unique
     * UUID."
     */
    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean testInitiated = false;

    private int xVal = 0;

    private GraphViewSeries liveGraph;

    private ArrayList<GraphViewWrapper> fileBuffer;
    int SensordataID;
    String sensorData;
    String patientID = "";
    TextView tvTitle;
    File dir = new File(root.getAbsolutePath() + "/Temp");
    String outputDir = root.getAbsolutePath() + "/Temp/";
    String outputFile = "temp.txt";
    InputStream isr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recreate_sensor_data);
        StrictMode.enableDefaults(); //STRICT MODE ENABLED
        liveGraph = new GraphViewSeries(new GraphView.GraphViewData[]{});
        bCancel = (Button) findViewById(R.id.btnCancel);
        bRestart = (Button) findViewById(R.id.btnRestart);
        tvTitle = (TextView) findViewById(R.id.title);
        Intent patientIdIntent = getIntent();
        SensordataID = patientIdIntent.getIntExtra("SensordataID", 0);


        downloadRawSensorData();
        saveRawDataToFile();
        writeDataToGraph();

        final TestAsyncTask MySyncTask = new TestAsyncTask();
        bCancel.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {

                MySyncTask.cancel(true);
                Intent openStartingPoint = new Intent(getApplicationContext(), PatientNames.class);
                startActivity(openStartingPoint);

            }

        });

        bRestart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                MySyncTask.cancel(true);
                TestAsyncTask MySyncTask = new TestAsyncTask();
                MySyncTask.execute();
                Toast.makeText(getApplicationContext(), "Connecting to the Bitalino Device", Toast.LENGTH_LONG).show();


            }
        });

        GraphView graphView = new LineGraphView(this, "Patient Data");
        graphView.setScrollable(true);
        graphView.addSeries(liveGraph);
        graphView.setDisableTouch(false);
        graphView.setManualMaxY(true);
        graphView.setManualMinY(true);
        graphView.setManualYMaxBound(1000);
        graphView.setManualYMinBound(0);

        LinearLayout layout = (LinearLayout) findViewById(R.id.live_graph);
        layout.addView(graphView);

        // if (!testInitiated)
        MySyncTask.execute();
    }


    void downloadRawSensorData() {
        String result = "";
        InputStream isr = null;

        nameValuePairs.add(new BasicNameValuePair("sensordataID", String.valueOf(SensordataID)));

        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://178.62.115.123/scripts/getsensordata.php"); //YOUR PHP SCRIPT ADDRESS
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
                sensorData = "" + json.getString("SensorFileName");
                patientID = "" + json.getString("PatientID");
            }


        } catch (Exception e) {
            // TODO: handle exception
            Log.e("log_tag", "Error Parsing Data " + e.toString());
        }

    }

    void saveRawDataToFile() {
        if (!dir.isDirectory()) {
            dir.mkdir();
        }

        try {
            File tempFile = new File(outputDir + outputFile);
            //Deletes file first before creating a new one;
            tempFile.delete();
            tempFile.createNewFile();
            FileOutputStream stream = new FileOutputStream(tempFile);
            try {
                stream.write(sensorData.getBytes());
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }


    public String readSensorFile() {


        File file = new File(outputDir + outputFile);
        StringBuilder readData = new StringBuilder();
        String data = "";

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                readData.append(line);
                readData.append('\n');
            }
            br.close();
        } catch (IOException e) {

        }

        //Con
        data = readData.toString();


        return data;

    }


    /**
     * @author Nick Davies
     */
    void writeDataToGraph() {
        File file = new File(outputDir + outputFile);
        String data = "";


        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            if (fileBuffer == null)
                fileBuffer = new ArrayList<GraphViewWrapper>();

            try {
                while ((line = br.readLine()) != null) {

                    int yVal = Integer.parseInt(line);

                    if (yVal > 0 && yVal < 1000)
                        fileBuffer.add(new GraphViewWrapper(++xVal, yVal));

                }
                br.close();
            } catch (IOException e) {

            }
        } catch (FileNotFoundException e) {

        }

    }

    private class TestAsyncTask extends AsyncTask<Void, String, Void> {

        private final int MAX_BEFORE_SCROLL = 20;
        /**
         * Buffer to hold the currently shown values, won't exceed maxBeforeScroll in length
         */
        private ArrayList<GraphViewWrapper> graphViewWrapperList = new ArrayList<GraphViewWrapper>();


        @Override
        protected Void doInBackground(Void... paramses) {


            while (true) {
                // Check if our buffer is empty and stop if it is
                if (fileBuffer.size() <= 0)
                    break;

                // Get the top of the buffer left to process
                GraphViewWrapper temp = fileBuffer.get(0);
                // Then remove it so we no longer add it on
                fileBuffer.remove(0);

                int yVal = (int) temp.getY();

                // If we've already hit the 20 limit, need to pop first, shift all others back by 1
                if (graphViewWrapperList.size() == MAX_BEFORE_SCROLL) {
                    // Pop the first one off
                    graphViewWrapperList.remove(0);
                }

                // Add one to the end, X value is irrelevant here
                graphViewWrapperList.add(new GraphViewWrapper(graphViewWrapperList.size(), (int) temp.getY()));

                // Loop over all of them, from 1 to X, set the X value of each to I so we're always drawing a graph
                for (int i = 0; i < graphViewWrapperList.size(); i++) {
                    if (graphViewWrapperList.get(i).getX() != i)
                        graphViewWrapperList.get(i).setX(i);
                }

                publishProgress(null);

                // Attempt the sleep for 1 second
                try {
                    Thread.sleep(50, 0);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

            }

            return null;
        }


        @Override
        protected void onProgressUpdate(String... values) {

            // Actually load the data onto the graph
            liveGraph.resetData(graphViewWrapperList.toArray(new GraphViewWrapper[0]));
        }

        @Override
        protected void onCancelled() {

        }

    }

}