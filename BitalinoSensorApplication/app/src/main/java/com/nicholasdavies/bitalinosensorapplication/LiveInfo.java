package com.nicholasdavies.bitalinosensorapplication;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewDataInterface;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.nicholasdavies.bitalinosensorapplication.BITalinoDevice;
import com.nicholasdavies.bitalinosensorapplication.BITalinoFrame;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit.RestAdapter;
import retrofit.client.Response;


public class LiveInfo extends Activity {

    private static final String TAG = "LiveInfo";
    private static final boolean UPLOAD = false;
    ArrayList<String> patientNames;
    ListView mainList;
    ArrayAdapter adapter;
    Button bCancel;


    private class GraphViewWrapper implements GraphViewDataInterface{

        private int mX = 0;
        private int mY = 0;

        public GraphViewWrapper(int x, int y)
        {
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

    private ArrayList<GraphViewWrapper> graphViewWrapperList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_info);
        StrictMode.enableDefaults(); //STRICT MODE ENABLED
        liveGraph = new GraphViewSeries(new GraphView.GraphViewData[] {});
        bCancel = (Button) findViewById(R.id.btnCancel);

        graphViewWrapperList = new ArrayList<GraphViewWrapper>();

        final TestAsyncTask MySyncTask = new TestAsyncTask();
        bCancel.setOnClickListener(new View.OnClickListener(){

            public void onClick(View arg0) {

                MySyncTask.cancel(true);
                Intent openStartingPoint = new Intent("com.nicholasdavies.bitalinosensorapplication.MAIN");
                startActivity(openStartingPoint);

            }
        });

        GraphView graphView = new LineGraphView(this,"Patient Data");
        graphView.setScrollable(true);
        graphView.addSeries(liveGraph);
        graphView.setDisableTouch(false);
        graphView.setManualMaxY(true);
        graphView.setManualMinY(true);
        graphView.setManualYMaxBound(1000);
        graphView.setManualYMinBound(0);

        LinearLayout layout = (LinearLayout) findViewById(R.id.live_graph);
        layout.addView(graphView);

        if (!testInitiated)
             MySyncTask.execute();
    }




    private class TestAsyncTask extends AsyncTask<Void, String, Void>{
        ArrayAdapter<String> adapter;
        private BluetoothDevice dev = null;
        private BluetoothSocket sock = null;
        private InputStream is = null;
        private OutputStream os = null;
        private BITalinoDevice bitalino;



        // ArrayAdapter<String> adapter = new ArrayAdapter<String>()

        @Override
        protected Void doInBackground(Void... paramses) {
            try {
                // Let's get the remote Bluetooth device
                final String remoteDevice = "98:D3:31:B1:83:A4";

                final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
                dev = btAdapter.getRemoteDevice(remoteDevice);

                Log.d(TAG, "Stopping Bluetooth discovery.");
                btAdapter.cancelDiscovery();

                sock = dev.createRfcommSocketToServiceRecord(MY_UUID);
                sock.connect();
                testInitiated = true;

                bitalino = new BITalinoDevice(10, new int[]{0});
                publishProgress("Connecting to BITalino [" + remoteDevice + "]..");
                bitalino.open(sock.getInputStream(), sock.getOutputStream());
                publishProgress("Connected.");

                // get BITalino version
                publishProgress("Version: " + bitalino.version());
               // publishProgress("Analogue Value" + );

                // start acquisition on predefined analog channels
                bitalino.start();

                // read until task is stopped
                int counter = 0;
                while (!isCancelled()) {
                    final int numberOfSamplesToRead = 1;

                    BITalinoFrame[] frames = bitalino.read(numberOfSamplesToRead);

                    if (UPLOAD) {
                        // prepare reading for upload
                        BITalinoReading reading = new BITalinoReading();
                        reading.setTimestamp(System.currentTimeMillis());
                        reading.setFrames(frames);
                        // instantiate reading service client
                        RestAdapter restAdapter = new RestAdapter.Builder()
                                .setEndpoint("http://server_ip:8080/bitalino")
                                .build();
                        ReadingService service = restAdapter.create(ReadingService.class);
                        // upload reading
                        Response response = service.uploadReading(reading);
                        assert response.getStatus() == 200;
                    }

                    // present data in screen
                    for (BITalinoFrame frame : frames)
                        //publishProgress(frame.toString());
                        publishProgress(Integer.toString(frame.getAnalog(0)));





                    counter++;
                }

                // trigger digital outputs
                // int[] digital = { 1, 1, 1, 1 };
                // device.trigger(digital);
            } catch (Exception e) {
                Log.e(TAG, "There was an error.", e);
            }

            return null;
        }

        @Override
        protected void onPreExecute() {


    }

        @Override
        protected void onProgressUpdate(String... values) {

            //liveGraph.appendData/new GraphView.GraphViewData(xVal++, Integer.parseInt(values[0])));
            if(values.length > 0) {
                try{
                    int yVal = Integer.parseInt(values[0]);

                    if(yVal < 1000 && yVal > 0) {
                        if(graphViewWrapperList.size() > 20)
                            graphViewWrapperList.remove(0);
                        graphViewWrapperList.add(new GraphViewWrapper(++xVal, yVal));

                        liveGraph.resetData(graphViewWrapperList.toArray(new GraphViewWrapper[0]));
                    }
                }
                catch(NumberFormatException e)
                {

                }
            }
        }

        @Override
        protected void onCancelled() {
            // stop acquisition and close bluetooth connection
            try {
                bitalino.stop();
                publishProgress("BITalino is stopped");

                sock.close();
                publishProgress("And we're done! :-)");
            } catch (Exception e) {
                Log.e(TAG, "There was an error.", e);
            }
        }

    }

}