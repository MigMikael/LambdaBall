package com.example.mig.lambdaball;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ShowDataActivity extends AppCompatActivity {

    private static final String TAG = "ShowDataActivity";
    Button exitButton, groundButton, headButton;
    TextView txtArduino, txtConnectStatus;
    TextView txtGvalue, txtAvalue, txtMvalue, txtBvalue, txtHead;
    Handler bluetoothIn;

    final int handlerState = 0;                        //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;

    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address;

    String value = "";

    int countOverHead = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_data);

        txtConnectStatus = (TextView) findViewById(R.id.txtConnectStatus);
        txtArduino = (TextView) findViewById(R.id.txtArduino);
        exitButton = (Button) findViewById(R.id.buttonExit);
        txtGvalue = (TextView) findViewById(R.id.txtGvalue);
        txtAvalue = (TextView) findViewById(R.id.txtAvalue);
        txtMvalue = (TextView) findViewById(R.id.txtMvalue);
        txtBvalue = (TextView) findViewById(R.id.txtBvalue);

        groundButton = (Button) findViewById(R.id.buttonGround);
        headButton = (Button) findViewById(R.id.buttonHead);
        txtHead = (TextView) findViewById(R.id.txtHead);

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {                                             //if message is what we want
                    String readMessage = (String) msg.obj;

                    for (int i = 0; i < readMessage.length(); i++) {
                        if (readMessage.charAt(i) == '{') {
                            value = "";
                            Log.d(TAG, "... Start Package ...");
                        } else if (readMessage.charAt(i) == '}') {
                            updateView(value);
                            Log.d(TAG, value);
                            Log.d(TAG, "... end Package ...");
                        } else {
                            value += readMessage.charAt(i);
                        }
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    btSocket.close();
                    Log.d(TAG, "...close Socket ok...");
                } catch (IOException e2) {
                    //insert code to deal with this
                }
            }
        });

        headButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtHead.setText(txtBvalue.getText());
                countOverHead = 0;
            }
        });
    }
    static double count = 0;

    public void updateView(String value) {
        int indexG = 0;
        int indexA = value.indexOf("A");
        int indexM = value.indexOf("M");
        int indexB = value.indexOf("B");

        txtGvalue.setText(value.substring(indexG, indexA));
        txtAvalue.setText(value.substring(indexA, indexM));
        txtMvalue.setText(value.substring(indexM, indexB));
        txtBvalue.setText(value.substring(indexB, value.length()));

        String a[] = txtAvalue.getText().toString().split(",");
        double a1 = Double.parseDouble(a[1]);
        a1 = Math.abs(a1);
        double a2 = Double.parseDouble(a[2]);
        a2 = Math.abs(a2);
        double a3 = Double.parseDouble(a[3]);
        a3 = Math.abs(a3);

        Log.d(TAG, "a1 = "+ String.valueOf(a1) + " a2 = " + String.valueOf(a2) + " a3 = " + String.valueOf(a3));

        double headHeight = Double.parseDouble(txtHead.getText().toString().replace("B,", ""));
        double ballHeight = Double.parseDouble(txtBvalue.getText().toString().replace("B,", ""));
        if(ballHeight > headHeight && a1 < 0.8&& a2 < 0.8&& a3 < 0.8){
            count ++;
        }else if(count > 0){
            countOverHead ++;
            count = 0;
        }
        txtArduino.setText(String.valueOf(countOverHead));
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - try connect...");

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        btAdapter.cancelDiscovery();

        // Establish the Bluetooth socket connection.
        Log.d(TAG, "...Connecting...");
        try {
            btSocket.connect();
            Log.d(TAG, "....Connection ok...");
            txtConnectStatus.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                //insert code to deal with this
            }
        }
        Log.d(TAG, "...Create Socket...");

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        try {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
            Log.d(TAG, "...close Socket ok...");
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    private void checkBTState() {

        if (btAdapter == null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
    }
}
