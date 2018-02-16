package com.orboid.orboid;


import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.UUID;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.view.View.OnClickListener;
import android.widget.Button;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.vidyo.VidyoClient.Connector.Connector;
import com.vidyo.VidyoClient.Connector.VidyoConnector;
import com.vidyo.VidyoClient.Device.VidyoDevice;
import com.vidyo.VidyoClient.Device.VidyoLocalCamera;
import com.vidyo.VidyoClient.Device.VidyoLocalMicrophone;
import com.vidyo.VidyoClient.Device.VidyoRemoteCamera;
import com.vidyo.VidyoClient.Endpoint.VidyoParticipant;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Robot extends AppCompatActivity implements VidyoConnector.IConnect,
        VidyoConnector.IRegisterLocalCameraEventListener,
        VidyoConnector.IRegisterLocalMicrophoneEventListener,
        VidyoConnector.IRegisterRemoteCameraEventListener {
    Button reconnect;
    Thread t;
    String macAddress = "68:C4:4D:2A:AB:E3";
    BluetoothDevice mmDevice;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket mmSocket = null;
    private OutputStream mmStream = null;
    String sending_str="NZZ";
    int i,latest;
    private DatabaseReference myRef;

    private VidyoConnector vc;
    private FrameLayout videoFrame;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot);
        i=0;
        latest=0;
        macAddress = android.provider.Settings.Secure.getString(this.getContentResolver(), "bluetooth_address");
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        CheckingBluetoothDevices();

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        try {
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmStream = mmSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Connector.SetApplicationUIContext(this);
        Connector.Initialize();
        videoFrame = (FrameLayout)findViewById(R.id.videoFrame);

                myRef = FirebaseDatabase.getInstance().getReference();
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot topSnapshot) {
                        for (DataSnapshot snapshot : topSnapshot.getChildren()) {
                            i++;
                            for (DataSnapshot inner_snapshot : snapshot.getChildren()) {
                                sending_str = inner_snapshot.getValue(String.class);
                            }
                            if (i == (latest + 1))
                                break;
                        }
                        Log.d("LOG", "Value is: " + sending_str);
                        try {
                            sendData();
                        } catch (IOException e) {
                            System.out.println("******** Sending Data Failed ********");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w("LOG", "Failed to read value.", error.toException());
                    }
                });
        Start(this.findViewById(android.R.id.content));

        Connect(this.findViewById(android.R.id.content));
    }


    void CheckingBluetoothDevices(){
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("HC-05"))
                {
                    mmDevice = device;
                    break;
                }
            }
        }
    }
    void  sendData() throws  IOException{
        latest=(latest+1)%5;
        System.out.println("Bluetooth ="+sending_str);
        byte[] msgBuffer = sending_str.getBytes();
        mmStream.write(msgBuffer);
    }
    public void Start(View v){
        vc = new VidyoConnector(null,VidyoConnector.VidyoConnectorViewStyle.VIDYO_CONNECTORVIEWSTYLE_Tiles,16,"","",0);
        RegisterForVidyoEvents();
        vc.SelectDefaultCamera();
        vc.CycleCamera();
    }

    public void Connect(View v){
        String token = "cHJvdmlzaW9uAHVzZXIxQDNmZmM4Yi52aWR5by5pbwA2MzY4MDQ1NjE5MQAANGVkZWIwMDMzNzMyMGQ5MmE3MDQyZDljMjI2MzM1Njc4ZWYxM2E3OTQ4MjczMWQ5MjMwZmRkNDdkZDg3ZWFmZjhkMDQ0NzlhZDM0ODUzYTg4NWNiNzIwNDg1MTg3OGU4   ";
        boolean s = vc.Connect("prod.vidyo.io",token,"DemoUser","room1",this);

    }

//    public void Disconnect(View v){
//
//        vc.Disconnect();
//    }


    public void OnSuccess() {

    }

    public void RegisterForVidyoEvents() {
        /* Register for camera, microphone, and speaker events */
        vc.RegisterLocalCameraEventListener(this);
        vc.RegisterLocalMicrophoneEventListener(this);
        vc.RegisterRemoteCameraEventListener(this);

    }

    public void OnFailure(VidyoConnector.VidyoConnectorFailReason vidyoConnectorFailReason) {

    }

    public void OnDisconnected(VidyoConnector.VidyoConnectorDisconnectReason vidyoConnectorDisconnectReason) {

    }


    public void OnLocalCameraAdded(VidyoLocalCamera vidyoLocalCamera) {
        vc.SelectDefaultCamera();

    }

    public void OnLocalCameraRemoved(VidyoLocalCamera vidyoLocalCamera) {

    }

    public void OnLocalCameraSelected(VidyoLocalCamera vidyoLocalCamera) {
        vc.AssignViewToLocalCamera(videoFrame, vidyoLocalCamera, true, true);
        vc.ShowViewAt(videoFrame, 0, 0, videoFrame.getWidth(), videoFrame.getHeight());


    }

    public void OnLocalCameraStateUpdated(VidyoLocalCamera vidyoLocalCamera, VidyoDevice.VidyoDeviceState vidyoDeviceState) {

    }

    public void OnLocalMicrophoneAdded(VidyoLocalMicrophone vidyoLocalMicrophone) {

    }

    public void OnLocalMicrophoneRemoved(VidyoLocalMicrophone vidyoLocalMicrophone) {

    }

    public void OnLocalMicrophoneSelected(VidyoLocalMicrophone vidyoLocalMicrophone) {

    }

    public void OnLocalMicrophoneStateUpdated(VidyoLocalMicrophone vidyoLocalMicrophone, VidyoDevice.VidyoDeviceState vidyoDeviceState) {

    }

    @Override
    public void OnRemoteCameraAdded(final VidyoRemoteCamera vidyoRemoteCamera, VidyoParticipant vidyoParticipant) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                vc.AssignViewToRemoteCamera(videoFrame, vidyoRemoteCamera, true, false);
                vc.ShowViewAt(videoFrame, 0, 0, videoFrame.getWidth(), videoFrame.getHeight());
            }
        });

    }

    @Override
    public void OnRemoteCameraRemoved(VidyoRemoteCamera vidyoRemoteCamera, VidyoParticipant vidyoParticipant) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                vc.HideView(videoFrame);
            }
        });

    }

    @Override
    public void OnRemoteCameraStateUpdated(VidyoRemoteCamera vidyoRemoteCamera, VidyoParticipant vidyoParticipant, VidyoDevice.VidyoDeviceState vidyoDeviceState) {

    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Exit")
                .setMessage("Do you want to Disconnect?")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(Robot.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    }
                }).setNegativeButton("no", null).show();
    }
}

/*
#include <Servo.h>

Servo vertical, horizontal;

char bit1;
int bit2, bit3;
int val2 , val3, t;
int leftMotorR = 4;
int leftMotorF = 5;
int rightMotorF = 6;
int rightMotorR = 7;
void setup()
{
  Serial.begin(9600);

  pinMode(leftMotorF, OUTPUT);
  pinMode(leftMotorR, OUTPUT);
  pinMode(rightMotorF, OUTPUT);
  pinMode(rightMotorR, OUTPUT);

  vertical.attach(9);
  vertical.write(90);
  horizontal.attach(10);
  horizontal.write(90);

}

void loop()
{

  if (Serial.available() > 0)
  {
    bit1 = Serial.read();

    if (bit1 == 'F') {
      digitalWrite(leftMotorF, HIGH);
      digitalWrite(rightMotorF, HIGH);
      delay(1000);
      digitalWrite(leftMotorF, LOW);
      digitalWrite(rightMotorF, LOW);
    }
    if (bit1 == 'B') {
      digitalWrite(leftMotorR, HIGH);
      digitalWrite(rightMotorR, HIGH);
      delay(1000);
      digitalWrite(leftMotorR, LOW);
      digitalWrite(rightMotorR, LOW);
    }
    if (bit1 == 'L') {
      digitalWrite(leftMotorR, HIGH);
      digitalWrite(rightMotorF, HIGH);
      delay(1000);
      digitalWrite(leftMotorR, LOW);
      digitalWrite(rightMotorF, LOW);
    }
    if (bit1 == 'R') {
      digitalWrite(leftMotorF, HIGH);
      digitalWrite(rightMotorR, HIGH);
      delay(1000);
      digitalWrite(leftMotorF, LOW);
      digitalWrite(rightMotorR, LOW);
    }
     bit2 = Serial.read();
     vertical.write(bit2);
     Serial.println(bit2);

     bit3 = Serial.read();
     vertical.write(bit3);
     Serial.println(bit2);
    }
  }
 */
