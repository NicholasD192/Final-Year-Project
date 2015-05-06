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


/**
 * Connects to Bitalino Device, Records and displays sensor Data.
 *
 * @author Nick Davies
 */


public class RecordPatientSensorData extends Activity {

    private static final String TAG = "LiveInfo";
    private static final boolean UPLOAD = false;
    Button bCancel, bUpload, bStart;
    File root = android.os.Environment.getExternalStorageDirectory();

    File dir = new File(root.getAbsolutePath() + "/Temp");
    String outputDir = root.getAbsolutePath() + "/Temp/";
    String outputFile = "temp.txt";
    InputStream isr = null;
    int sampleRate;



    private class GraphViewWrapper implements GraphViewDataInterface {
        /**
         * Implements a wrapper to enclose the graphView (Couldn't get Included one working)
         */

        private int mX = 0;
        private int mY = 0;

        public GraphViewWrapper(int x, int y) {
            //System.out.println("X: " + x + "| Y: " + y);
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
    int xBuff=0;

    private GraphViewSeries liveGraph;
    TextView txtSensorType, txtSampleRate;

    private ArrayList<GraphViewWrapper> graphViewWrapperList;
    StringBuilder buffData = new StringBuilder();
    GraphView graphView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /**  On create links Activity to XML Layout and defines button and text fields etc */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_patient_sensor_data);
        StrictMode.enableDefaults(); //STRICT MODE ENABLED
        liveGraph = new GraphViewSeries(new GraphView.GraphViewData[]{});
        bCancel = (Button) findViewById(R.id.btnCancel);
        bUpload = (Button) findViewById(R.id.btnUpload);
        bStart = (Button) findViewById(R.id.btnStart);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        final int patientID = bundle.getInt("PatientID");
        sampleRate = bundle.getInt("sampleRate");
        final int sensorType = bundle.getInt("sensorType");

        txtSensorType = (TextView) findViewById(R.id.txtSensorType);
        txtSampleRate = (TextView) findViewById(R.id.txtSampleRate);
        txtSensorType.setText("Sensor Type: " + returnSensorType(sensorType));
        txtSampleRate.setText("Sample Rate: " + sampleRate + "Hz");

        Calendar c = Calendar.getInstance();
        final String Notes = "";


        /**  Sets up the axis and various properties of the graphView Package */
        graphView = new LineGraphView(this, "Patient Data");
        graphView.setScrollable(true);
        graphView.addSeries(liveGraph);
        graphView.setManualYAxis(true);
        graphView.setManualYAxisBounds(1000,0);


        LinearLayout layout = (LinearLayout) findViewById(R.id.live_graph);
        layout.addView(graphView);

        //Currently can't upload data at this rate
        if(sampleRate == 1000)
        {
            bUpload.setVisibility(View.GONE);
        }





        /**  This will store the date the sensor information has been taken */
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        final String dateFormatted = date.format(c.getTime());

        graphViewWrapperList = new ArrayList<GraphViewWrapper>();
        final TestAsyncTask MySyncTask = new TestAsyncTask();
        bCancel.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                /**  Cancels MySyncTask that will close the connection with the bitalino and tell it to stop */


                MySyncTask.cancel(true);
                Intent openStartingPoint = new Intent("com.nicholasdavies.bitalinosensorapplication.MAIN");
                startActivity(openStartingPoint);

            }

        });
        bStart.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                /**  Cancels MySyncTask that will close the connection with the bitalino and tell it to stop */

                Toast.makeText(getApplicationContext(), "Connecting to the Bitalino Device", Toast.LENGTH_LONG).show();


                /**  Begin MySyncTask */

                if (!testInitiated)
                    MySyncTask.execute();

            }

        });

        bUpload.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {

                /**  Will stop Bitalino Sending Data and upload data stored*/

                MySyncTask.cancel(true);

                /**  File that temporarily stores sensor data before upload */
                String sensorData = readSensorFile();


                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);

                nameValuePairs.add(new BasicNameValuePair("patientID", Integer.toString(patientID)));
                nameValuePairs.add(new BasicNameValuePair("date", dateFormatted));
                nameValuePairs.add(new BasicNameValuePair("sensorfilename", sensorData));
                nameValuePairs.add(new BasicNameValuePair("sensortype", Integer.toString(sensorType)));
                nameValuePairs.add(new BasicNameValuePair("samplerate", Integer.toString(sampleRate)));

                /**  Connecting to server */
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    /**  Calls PhP Script */
                    String baseURL = Utilities.getURL(getApplicationContext());
                    HttpPost httppost = new HttpPost(baseURL + "createnewsensordata.php"); //YOUR PHP SCRIPT ADDRESS
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    isr = entity.getContent();

                    String msg = "Data Entered Successfully Returning to Main Menu";
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            /**  Closes the Activity once the toast has finished displaying */
                            try {
                                Thread.sleep(3500);
                                RecordPatientSensorData.this.finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    Intent openStartingPoint = new Intent("com.nicholasdavies.bitalinosensorapplication.MAIN");
                    startActivity(openStartingPoint);

                } catch (ClientProtocolException e) {
                    Log.e("ClientProtocal", "Log_tag");
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e("Log_tag", "IOException");
                    e.printStackTrace();
                }

            }

        });

    }

    /**
     * Builds a string out of the temp file so it can be uploaded
     */
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
     * Returns the sensorType from an integer to be displayed
     */
    public String returnSensorType(int sensorType){
        if  (sensorType == 1) {
            return "EMG";
        }
        if (sensorType == 2) {
            return "ECG";
        }
        if (sensorType == 3) {
            return "EDA";
        }
        return null;

    }



    /**
     * Main Async Task that connects to bitalino device
     */
    private class TestAsyncTask extends AsyncTask<Void, String, Void> {
        ArrayAdapter<String> adapter;
        private BluetoothDevice dev = null;
        private BluetoothSocket sock = null;
        private InputStream is = null;
        private OutputStream os = null;
        private BITalinoDevice bitalino;
        private PrintWriter out;


        @Override
        protected Void doInBackground(Void... paramses) {
            try {
                /**  Bitalino Mac Address*/
                final String remoteDevice = Utilities.getMac(getApplicationContext());

                final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
                dev = btAdapter.getRemoteDevice(remoteDevice);

                Log.d(TAG, "Stopping Bluetooth discovery.");
                btAdapter.cancelDiscovery();

                sock = dev.createRfcommSocketToServiceRecord(MY_UUID);
                sock.connect();
                testInitiated = true;

                bitalino = new BITalinoDevice(sampleRate, new int[]{0});

                bitalino.open(sock.getInputStream(), sock.getOutputStream());

                bitalino.start();

                /**  Read intill Task is stopped*/
                int counter = 0;
                while (!isCancelled()) {
                    final int numberOfSamplesToRead = 1;


                    BITalinoFrame[] frames = bitalino.read(numberOfSamplesToRead);


                    /**  This passes sensor value to PublishProgess*/
                    for (BITalinoFrame frame : frames)

                        publishProgress(Integer.toString(frame.getAnalog(0)));


                    counter++;
                }

            } catch (Exception e) {
                Log.e(TAG, "There was an error.", e);
            }

            return null;
        }

        @Override
        /**  Creates Temp File to Store Sensor Data, This is done as the MySyncTask is created*/
        protected void onPreExecute() {
            graphView.setManualYAxis(false);
            if (!dir.isDirectory()) {
                dir.mkdir();
            }

            try {
                File tempFile = new File(outputDir + outputFile);
                //Deletes file first before creating a new one;
                tempFile.delete();
                tempFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        protected void onProgressUpdate(String... values) {
            /**  Outputs Sensor Data to file*/

            if (values.length > 0) {
                try {
                    //Sets up a buffer so its not writing 1000 times a second to external memory

                        if (sampleRate == 10 || sampleRate == 100) {
                            buffData.append(values[0] + "\n");
                            if (xBuff == 10) {
                                try {
                                    out = new PrintWriter(new BufferedWriter(new FileWriter(outputDir + outputFile, true)));
                                    out.println(buffData.toString());
                                    out.flush();
                                } catch (IOException e) {

                                }
                                buffData.setLength(0);
                                xBuff = 0;
                            }
                        }


                    int yVal = Integer.parseInt(values[0]);
                    /**  Updates Graph with new Sensor Values, Currently the graph will only show 20 on screen */
                    if (yVal < 1000 && yVal > 0) {
                        if (graphViewWrapperList.size() > onScreenData())
                            graphViewWrapperList.remove(0);
                        graphViewWrapperList.add(new GraphViewWrapper(++xVal, yVal));
                        liveGraph.resetData(graphViewWrapperList.toArray(new GraphViewWrapper[0]));
                    }
                } catch (NumberFormatException e) {

                }
                if (sampleRate == 10 || sampleRate == 100) {
                    xBuff++;
                }
            }
        }

        @Override
        protected void onCancelled() {
            /**  This is called when the MySyncTask is called, closes connection with the bitalino device. */
            try {

                bitalino.stop();
                publishProgress("BITalino is stopped");

                sock.close();
                publishProgress("And we're done! :-)");
            } catch (Exception e) {
                Log.e(TAG, "There was an error.", e);
            }
        }

        protected int onScreenData(){

            if (sampleRate == 10) {
                return 50;
            }
            if (sampleRate == 100) {
                return 500;
            }
            if (sampleRate == 1000) {
                return 5000;
            }
            else

            return 100;
        }

    }

}