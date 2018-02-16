package com.orboid.orboid;

import com.orboid.orboid.inputmanagercompat.InputManagerCompat;
import com.orboid.orboid.inputmanagercompat.InputManagerCompat.InputDeviceListener;

import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.vidyo.VidyoClient.Connector.Connector;
import com.vidyo.VidyoClient.Connector.VidyoConnector;
import com.vidyo.VidyoClient.Device.VidyoDevice;
import com.vidyo.VidyoClient.Device.VidyoLocalCamera;
import com.vidyo.VidyoClient.Device.VidyoLocalMicrophone;
import com.vidyo.VidyoClient.Device.VidyoRemoteCamera;
import com.vidyo.VidyoClient.Endpoint.VidyoLogRecord;
import com.vidyo.VidyoClient.Endpoint.VidyoParticipant;
import com.vidyo.VidyoClient.VidyoNetworkInterface;

import java.io.IOException;
import java.util.logging.Logger;




import static com.orboid.orboid.R.layout.activity_user;

public class user extends AppCompatActivity implements
        SensorEventListener,
        InputManager.InputDeviceListener,
        InputDeviceListener,
        VidyoConnector.IConnect,
        VidyoConnector.IRegisterLogEventListener,
        VidyoConnector.IRegisterNetworkInterfaceEventListener,
        VidyoConnector.IRegisterLocalCameraEventListener,
        VidyoConnector.IRegisterLocalMicrophoneEventListener,
        VidyoConnector.IRegisterRemoteCameraEventListener{


enum VIDYO_CONNECTOR_STATE {
    VC_CONNECTED,
    VC_DISCONNECTED,
    VC_DISCONNECTED_UNEXPECTED,
    VC_CONNECTION_FAILURE
}

    private VIDYO_CONNECTOR_STATE mVidyoConnectorState = VIDYO_CONNECTOR_STATE.VC_DISCONNECTED;
    private boolean mVidyoClientInitialized = false;
    private VidyoConnector mVidyoConnector = null;
    private ToggleButton mToggleConnectButton;
    private ProgressBar mConnectionSpinner;
    private LinearLayout mControlsLayout;
    private LinearLayout mToolbarLayout;
    private EditText mHost;
    private EditText mDisplayName;
    private EditText mToken;
    private EditText mResourceId;
    private TextView mToolbarStatus;
    private TextView mClientVersion;
    private FrameLayout mVideoFrame;
    private FrameLayout mVideoFrame2;
    private FrameLayout mToggleToolbarFrame;
    private boolean mHideConfig = false;
    private boolean mAutoJoin = false;
    private boolean mAllowReconnect = true;
    private boolean mEnableDebug = false;
    private String mReturnURL = null;
    private String mExperimentalOptions = null;
    private user mSelf;

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 1000;
    private int mScreenDensity;
    private MediaProjectionManager mProjectionManager;
    private static final int DISPLAY_WIDTH = 720;
    private static final int DISPLAY_HEIGHT = 1280;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionCallback mMediaProjectionCallback;
    private ToggleButton mToggleButton;
    private MediaRecorder mMediaRecorder;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_PERMISSIONS = 10;
    String blu_str="N";

    private InputManagerCompat mInputManager;
    private InputDevice mInputDevice;

    private float[] mGravity;
    private float[] mGeomagnetic;
    private float azimut,roll;
    Thread t;
    private DatabaseReference First,Second,Third,Fourth,Fifth;


    private  String strX="Z" ,strY="Z";
    private int initial_y,initial_x;
    private boolean firstX,firstY;

    /*
     *  Operating System Events
     */
static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
        }

@Override
protected void onCreate(Bundle savedInstanceState) {
        System.out.println("onCreate");
        super.onCreate(savedInstanceState);
    this.firstX=true;
    this.firstY=true;

    mInputManager = InputManagerCompat.Factory.getInputManager(getApplicationContext());
    mInputManager.registerInputDeviceListener(this, null);
    findController();
    SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    Sensor mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_GAME);
    First = FirebaseDatabase.getInstance().getReference("First");
    Second = FirebaseDatabase.getInstance().getReference("Second");
    Third = FirebaseDatabase.getInstance().getReference("Third");
    Fourth = FirebaseDatabase.getInstance().getReference("Fourth");
    Fifth = FirebaseDatabase.getInstance().getReference("Fifth");
        setContentView(R.layout.activity_user);
    t=new Thread(){
        @Override
        public void run() {

            while(!isInterrupted())
            {
                try
                {
                    Thread.sleep(500);
                    First.removeValue();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            First.push().setValue(getData());
                        }
                    });
                    Thread.sleep(500);
                    Second.removeValue();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Second.push().setValue(getData());
                        }
                    });
                    Thread.sleep(500);
                    Third.removeValue();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Third.push().setValue(getData());
                        }
                    });
                    Thread.sleep(500);
                    Fourth.removeValue();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Fourth.push().setValue(getData());
                        }
                    });
                    Thread.sleep(500);
                    Fifth.removeValue();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Fifth.push().setValue(getData());
                        }
                    });

                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    // Initialize the member variables
        mToggleConnectButton = (ToggleButton) findViewById(R.id.toggleConnectButton);
        mControlsLayout = (LinearLayout) findViewById(R.id.controlsLayout);
        mToolbarLayout = (LinearLayout) findViewById(R.id.toolbarLayout);
        mVideoFrame = (FrameLayout) findViewById(R.id.videoFrame);
        mVideoFrame2 = (FrameLayout) findViewById(R.id.videoFrame2);
        mToggleToolbarFrame = (FrameLayout) findViewById(R.id.toggleToolbarFrame);
        mHost = (EditText) findViewById(R.id.hostTextBox);
        mDisplayName = (EditText) findViewById(R.id.displayNameTextBox);
        mToken = (EditText) findViewById(R.id.tokenTextBox);
        mResourceId = (EditText) findViewById(R.id.resourceIdTextBox);
        mToolbarStatus = (TextView) findViewById(R.id.toolbarStatusText);
        mConnectionSpinner = (ProgressBar) findViewById(R.id.connectionSpinner);
        mSelf = this;
        t.start();
        // Suppress keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Initialize the VidyoClient
        Connector.SetApplicationUIContext(this);
        mVidyoClientInitialized = Connector.Initialize();


        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;

        mMediaRecorder = new MediaRecorder();

        mProjectionManager = (MediaProjectionManager) getSystemService
        (Context.MEDIA_PROJECTION_SERVICE);

    mToggleButton = (ToggleButton) findViewById(R.id.toggle);
        mToggleButton.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
        if (ContextCompat.checkSelfPermission(user.this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE) + ContextCompat
        .checkSelfPermission(user.this,
        Manifest.permission.RECORD_AUDIO)
        != PackageManager.PERMISSION_GRANTED) {
        if (ActivityCompat.shouldShowRequestPermissionRationale
        (user.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
        ActivityCompat.shouldShowRequestPermissionRationale
        (user.this, Manifest.permission.RECORD_AUDIO)) {
        mToggleButton.setChecked(false);
        Snackbar.make(findViewById(android.R.id.content), R.string.label_permissions,
        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
        new View.OnClickListener() {
@Override
public void onClick(View v) {
        ActivityCompat.requestPermissions(user.this,
        new String[]{Manifest.permission
        .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
        REQUEST_PERMISSIONS);
        }
        }).show();
        } else {
        ActivityCompat.requestPermissions(user.this,
        new String[]{Manifest.permission
        .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
        REQUEST_PERMISSIONS);
        }
        } else {
        onToggleScreenShare(v);
        }
        }
        });

        }

@Override
protected void onNewIntent(Intent intent) {
        System.out.println("onNewIntent");
        super.onNewIntent(intent);

        // New intent was received so set it to use in onStart()
        setIntent(intent);
        }

@Override
protected void onStart() {
        System.out.println("onStart");
        super.onStart();
        // If the app was launched by a different app, then get any parameters; otherwise use default settings
        Intent intent = getIntent();
        mHost.setText(intent.hasExtra("host") ? intent.getStringExtra("host") : "prod.vidyo.io");
        mToken.setText(intent.hasExtra("token") ? intent.getStringExtra("token") : "cHJvdmlzaW9uAHVzZXIxQDNmZmM4Yi52aWR5by5pbwA2MzY4MDQ1NjE5MQAANGVkZWIwMDMzNzMyMGQ5MmE3MDQyZDljMjI2MzM1Njc4ZWYxM2E3OTQ4MjczMWQ5MjMwZmRkNDdkZDg3ZWFmZjhkMDQ0NzlhZDM0ODUzYTg4NWNiNzIwNDg1MTg3OGU4");
        mDisplayName.setText(intent.hasExtra("displayName") ? intent.getStringExtra("displayName") : "John");
        mResourceId.setText(intent.hasExtra("resourceId") ? intent.getStringExtra("resourceId") : "room1");
        mReturnURL = intent.hasExtra("returnURL") ? intent.getStringExtra("returnURL") : null;
        mHideConfig = intent.getBooleanExtra("hideConfig", false);
        mAutoJoin = intent.getBooleanExtra("autoJoin", false);
        mAllowReconnect = intent.getBooleanExtra("allowReconnect", true);
        mEnableDebug = intent.getBooleanExtra("enableDebug", false);
        mExperimentalOptions = intent.hasExtra("experimentalOptions") ? intent.getStringExtra("experimentalOptions") : null;

        System.out.println("onStart: hideConfig = " + mHideConfig + ", autoJoin = " + mAutoJoin + ", allowReconnect = " + mAllowReconnect + ", enableDebug = " + mEnableDebug);

        // Enable toggle connect button
        mToggleConnectButton.setEnabled(true);

        // Hide the controls if hideConfig enabled
        if (mHideConfig) {
        mControlsLayout.setVisibility(View.GONE);
        }
        }

public void RegisterForVidyoEvents() {
        /* Register for camera, microphone, and speaker events */
        mVidyoConnector.RegisterLocalCameraEventListener(this);
        mVidyoConnector.RegisterLocalMicrophoneEventListener(this);
        mVidyoConnector.RegisterRemoteCameraEventListener(this);

        }

@Override
protected void onResume() {
        System.out.println("onResume");
        super.onResume();

        ViewTreeObserver viewTreeObserver = mVideoFrame.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
@Override
public void onGlobalLayout() {
        mVideoFrame.getViewTreeObserver().removeOnGlobalLayoutListener(this);

        // If the vidyo connector was not previously successfully constructed then construct it

        if (mVidyoConnector == null) {

        if (mVidyoClientInitialized) {

        try {
        mVidyoConnector = new VidyoConnector(null,
        VidyoConnector.VidyoConnectorViewStyle.VIDYO_CONNECTORVIEWSTYLE_Default,
        15,
        "info@VidyoClient info@VidyoConnector warning",
        "",
        0);

        RegisterForVidyoEvents();
        mVidyoConnector.SelectDefaultCamera();
        mVidyoConnector.CycleCamera();
        // Set the client version in the toolbar
        mClientVersion.setText(mVidyoConnector.GetVersion());

        // If enableDebug is configured then enable debugging
        if (mEnableDebug) {
        mVidyoConnector.EnableDebug(7776, "warning info@VidyoClient info@VidyoConnector");
        }

        // Set experimental options if any exist
        if (mExperimentalOptions != null) {
        Connector.SetExperimentalOptions(mExperimentalOptions);
        }

        // Set initial position
        RefreshUI();

        // Register for network interface callbacks
        if (!mVidyoConnector.RegisterNetworkInterfaceEventListener(mSelf)) {
        System.out.println("VidyoConnector RegisterNetworkInterfaceEventListener failed");
        }

        // Register for log callbacks
        if (!mVidyoConnector.RegisterLogEventListener(mSelf, "info@VidyoClient info@VidyoConnector warning")) {
        System.out.println("VidyoConnector RegisterLogEventListener failed");
        }
        }
        catch (Exception e) {
        System.out.println("VidyoConnector Construction failed");
        System.out.println(e.getMessage());
        }
        } else {
        System.out.println("ERROR: VidyoClientInitialize failed - not constructing VidyoConnector ...");
        }

        Log.d("Onresume","onResume: mVidyoConnectorConstructed => " + (mVidyoConnector != null ? "success" : "failed"));
        }

        // If configured to auto-join, then simulate a click of the toggle connect button
        if (mAutoJoin && (mVidyoConnector != null)) {
        mToggleConnectButton.performClick();
        }
        }
        });
        }
        }

@Override
protected void onPause() {
        System.out.println("onPause");
        super.onPause();
        }

@Override
protected void onRestart() {
        System.out.println("onRestart");
        super.onRestart();
        if (mVidyoConnector != null) {
        mVidyoConnector.SetMode(VidyoConnector.VidyoConnectorMode.VIDYO_CONNECTORMODE_Foreground);
        }
        }

@Override
protected void onStop() {
        System.out.println("onStop");
        if (mVidyoConnector != null) {
        mVidyoConnector.SetMode(VidyoConnector.VidyoConnectorMode.VIDYO_CONNECTORMODE_Background);
        }
        super.onStop();
        }

@Override
protected void onDestroy() {
        System.out.println("onDestroy");

        // Release device resources
        mVidyoConnector.Disable();
        mVidyoConnector = null;

        // Uninitialize the VidyoClient library
        Connector.Uninitialize();

        super.onDestroy();
        }

@Override
public void OnLocalCameraAdded(VidyoLocalCamera vidyoLocalCamera) {
        mVidyoConnector.SelectDefaultCamera();
        mVidyoConnector.CycleCamera();
        }

@Override
public void OnLocalCameraRemoved(VidyoLocalCamera vidyoLocalCamera) {

        }

@Override
public void OnLocalCameraSelected(VidyoLocalCamera vidyoLocalCamera) {

        mVidyoConnector.AssignViewToLocalCamera(mVideoFrame, vidyoLocalCamera, true, true);
        mVidyoConnector.ShowViewAt(mVideoFrame, 0, 0, mVideoFrame.getWidth(), mVideoFrame.getHeight());
        mVidyoConnector.AssignViewToLocalCamera(mVideoFrame2, vidyoLocalCamera, true, true);
        mVidyoConnector.ShowViewAt(mVideoFrame2, 0, 0, mVideoFrame2.getWidth(), mVideoFrame2.getHeight());
        }

@Override
public void OnLocalCameraStateUpdated(VidyoLocalCamera vidyoLocalCamera, VidyoDevice.VidyoDeviceState vidyoDeviceState) {

        }

@Override
public void OnLocalMicrophoneAdded(VidyoLocalMicrophone vidyoLocalMicrophone) {

        }

@Override
public void OnLocalMicrophoneRemoved(VidyoLocalMicrophone vidyoLocalMicrophone) {

        }

@Override
public void OnLocalMicrophoneSelected(VidyoLocalMicrophone vidyoLocalMicrophone) {

        }

@Override
public void OnLocalMicrophoneStateUpdated(VidyoLocalMicrophone vidyoLocalMicrophone, VidyoDevice.VidyoDeviceState vidyoDeviceState) {

        }



@Override
public void OnRemoteCameraAdded(final VidyoRemoteCamera vidyoRemoteCamera, VidyoParticipant vidyoParticipant) {

        runOnUiThread(new Runnable() {
@Override
public void run() {
        mVidyoConnector.AssignViewToRemoteCamera(mVideoFrame, vidyoRemoteCamera, true, false);
        mVidyoConnector.ShowViewAt(mVideoFrame, 0, 0, mVideoFrame.getWidth(), mVideoFrame.getHeight());

        mVidyoConnector.AssignViewToRemoteCamera(mVideoFrame2, vidyoRemoteCamera, true, false);
        mVidyoConnector.ShowViewAt(mVideoFrame2, 0, 0, mVideoFrame2.getWidth(), mVideoFrame2.getHeight());
        }
        });

        }

@Override
public void OnRemoteCameraRemoved(VidyoRemoteCamera vidyoRemoteCamera, VidyoParticipant vidyoParticipant) {
        runOnUiThread(new Runnable() {
@Override
public void run() {
        mVidyoConnector.HideView(mVideoFrame);
        mVidyoConnector.HideView(mVideoFrame2);
        }
        });

        }

@Override
public void OnRemoteCameraStateUpdated(VidyoRemoteCamera vidyoRemoteCamera, VidyoParticipant vidyoParticipant, VidyoDevice.VidyoDeviceState vidyoDeviceState) {

        }

// The device interface orientation has changed
@Override
public void onConfigurationChanged(Configuration newConfig) {
        System.out.println("onConfigurationChanged");
        super.onConfigurationChanged(newConfig);

        }

@Override
public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
        }

@Override
public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
        }

    /*
     * Private Utility Functions
     */

// Refresh the UI
private void RefreshUI() {
        // Refresh the rendering of the video
        mVidyoConnector.ShowViewAt(mVideoFrame, 0, 0, mVideoFrame.getWidth(), mVideoFrame.getHeight());
        mVidyoConnector.ShowViewAt(mVideoFrame2, 0, 0, mVideoFrame2.getWidth(), mVideoFrame2.getHeight());
        System.out.println("VidyoConnectorShowViewAt: x = 0, y = 0, w = " + mVideoFrame.getWidth() + ", h = " + mVideoFrame.getHeight());
        }

// The state of the VidyoConnector connection changed, reconfigure the UI.
// If connected, dismiss the controls layout
private void ConnectorStateUpdated(VIDYO_CONNECTOR_STATE state, final String statusText) {
        System.out.println("ConnectorStateUpdated, state = " + state.toString());

        mVidyoConnectorState = state;

        // Execute this code on the main thread since it is updating the UI layout

        runOnUiThread(new Runnable() {
@Override
public void run() {

        // Update the toggle connect button to either start call or end call image
        mToggleConnectButton.setChecked(mVidyoConnectorState == VIDYO_CONNECTOR_STATE.VC_CONNECTED);

        // Set the status text in the toolbar
        mToolbarStatus.setText(statusText);

        if (mVidyoConnectorState == VIDYO_CONNECTOR_STATE.VC_CONNECTED) {
        // Enable the toggle toolbar control
        mToggleToolbarFrame.setVisibility(View.VISIBLE);

        if (!mHideConfig) {
        // Update the view to hide the controls
        mControlsLayout.setVisibility(View.GONE);
        }
        } else {
        // VidyoConnector is disconnected

        // Disable the toggle toolbar control and display toolbar in case it is hidden
        mToggleToolbarFrame.setVisibility(View.GONE);
        mToolbarLayout.setVisibility(View.VISIBLE);

        // If a return URL was provided as an input parameter, then return to that application
        if (mReturnURL != null) {
        // Provide a callstate of either 0 or 1, depending on whether the call was successful
        Intent returnApp = getPackageManager().getLaunchIntentForPackage(mReturnURL);
        returnApp.putExtra("callstate", (mVidyoConnectorState == VIDYO_CONNECTOR_STATE.VC_DISCONNECTED) ? 1 : 0);
        startActivity(returnApp);
        }

        // If the allow-reconnect flag is set to false and a normal (non-failure) disconnect occurred,
        // then disable the toggle connect button, in order to prevent reconnection.
        if (!mAllowReconnect && (mVidyoConnectorState == VIDYO_CONNECTOR_STATE.VC_DISCONNECTED)) {
        mToggleConnectButton.setEnabled(false);
        mToolbarStatus.setText("Call ended");
        }

        if (!mHideConfig ) {
        // Update the view to display the controls
        mControlsLayout.setVisibility(View.VISIBLE);
        }
        }

        // Hide the spinner animation
        mConnectionSpinner.setVisibility(View.INVISIBLE);
        }
        });
        }

    /*
     * Button Event Callbacks
     */

// The Connect button was pressed.
// If not in a call, attempt to connect to the backend service.
// If in a call, disconnect.
public void ToggleConnectButtonPressed(View v) {
        if (mToggleConnectButton.isChecked()) {
        // Abort the Connect call if resourceId is invalid. It cannot contain empty spaces or "@".
        if (mResourceId.getText().toString().contains(" ") || mResourceId.getText().toString().contains("@")) {
        mToolbarStatus.setText("Invalid Resource ID");
        mToggleConnectButton.setChecked(false);
        } else {
        mToolbarStatus.setText("Connecting...");

        // Display the spinner animation
        mConnectionSpinner.setVisibility(View.VISIBLE);

final boolean status = mVidyoConnector.Connect(
        mHost.getText().toString(),
        mToken.getText().toString(),
        mDisplayName.getText().toString(),
        mResourceId.getText().toString(),
        this);
        if (!status) {
        // Hide the spinner animation
        mConnectionSpinner.setVisibility(View.INVISIBLE);

        ConnectorStateUpdated(VIDYO_CONNECTOR_STATE.VC_CONNECTION_FAILURE, "Connection failed");
        }
        System.out.println("VidyoConnectorConnect status = " + status);
        }
        } else {
        // The button just switched to the callStart image: The user is either connected to a resource
        // or is in the process of connecting to a resource; call VidyoConnectorDisconnect to either disconnect
        // or abort the connection attempt.
        // Change the button back to the callEnd image because do not want to assume that the Disconnect
        // call will actually end the call. Need to wait for the callback to be received
        // before swapping to the callStart image.
        mToggleConnectButton.setChecked(true);

        mToolbarStatus.setText("Disconnecting...");

        mVidyoConnector.Disconnect();
        }
        }

// Toggle the microphone privacy
public void MicrophonePrivacyButtonPressed(View v) {
        mVidyoConnector.SetMicrophonePrivacy(((ToggleButton) v).isChecked());
        }

// Toggle the camera privacy
public void CameraPrivacyButtonPressed(View v) {
        mVidyoConnector.SetCameraPrivacy(((ToggleButton) v).isChecked());
        }

// Handle the camera swap button being pressed. Cycle the camera.
public void CameraSwapButtonPressed(View v) {
        mVidyoConnector.CycleCamera();
        }

// Toggle visibility of the toolbar
public void ToggleToolbarVisibility(View v) {
        if (mVidyoConnectorState == VIDYO_CONNECTOR_STATE.VC_CONNECTED) {
        if (mToolbarLayout.getVisibility() == View.VISIBLE) {
        mToolbarLayout.setVisibility(View.INVISIBLE);
        } else {
        mToolbarLayout.setVisibility(View.VISIBLE);
        }
        }
        }

    /*
     *  Connector Events
     */

// Handle successful connection.
@Override
public void OnSuccess() {
        System.out.println("OnSuccess: successfully connected.");
        ConnectorStateUpdated(VIDYO_CONNECTOR_STATE.VC_CONNECTED, "Connected");
        }

// Handle attempted connection failure.
@Override
public void OnFailure(VidyoConnector.VidyoConnectorFailReason reason) {
        System.out.println("OnFailure: connection attempt failed, reason = " + reason.toString());

        // Update UI to reflect connection failed
        ConnectorStateUpdated(VIDYO_CONNECTOR_STATE.VC_CONNECTION_FAILURE, "Connection failed");
        }

// Handle an existing session being disconnected.
@Override
public void OnDisconnected(VidyoConnector.VidyoConnectorDisconnectReason reason) {
        if (reason == VidyoConnector.VidyoConnectorDisconnectReason.VIDYO_CONNECTORDISCONNECTREASON_Disconnected) {
        System.out.println("OnDisconnected: successfully disconnected, reason = " + reason.toString());
        ConnectorStateUpdated(VIDYO_CONNECTOR_STATE.VC_DISCONNECTED, "Disconnected");
        } else {
        System.out.println("OnDisconnected: unexpected disconnection, reason = " + reason.toString());
        ConnectorStateUpdated(VIDYO_CONNECTOR_STATE.VC_DISCONNECTED_UNEXPECTED, "Unexpected disconnection");
        }
        }

// Handle a message being logged.
@Override
public void OnLog(VidyoLogRecord logRecord) {
        System.out.println(logRecord.message);
        }

@Override
public void OnNetworkInterfaceAdded(VidyoNetworkInterface vidyoNetworkInterface) {
        System.out.println("OnNetworkInterfaceAdded: name=" + vidyoNetworkInterface.GetName() + " address=" + vidyoNetworkInterface.GetAddress() + " type=" + vidyoNetworkInterface.GetType() + " family=" + vidyoNetworkInterface.GetFamily());
        }

@Override
public void OnNetworkInterfaceRemoved(VidyoNetworkInterface vidyoNetworkInterface) {
        System.out.println("OnNetworkInterfaceRemoved: name=" + vidyoNetworkInterface.GetName() + " address=" + vidyoNetworkInterface.GetAddress() + " type=" + vidyoNetworkInterface.GetType() + " family=" + vidyoNetworkInterface.GetFamily());
        }

@Override
public void OnNetworkInterfaceSelected(VidyoNetworkInterface vidyoNetworkInterface, VidyoNetworkInterface.VidyoNetworkInterfaceTransportType vidyoNetworkInterfaceTransportType) {
        System.out.println("OnNetworkInterfaceSelected: name=" + vidyoNetworkInterface.GetName() + " address=" + vidyoNetworkInterface.GetAddress() + " type=" + vidyoNetworkInterface.GetType() + " family=" + vidyoNetworkInterface.GetFamily());
        }

@Override
public void OnNetworkInterfaceStateUpdated(VidyoNetworkInterface vidyoNetworkInterface, VidyoNetworkInterface.VidyoNetworkInterfaceState vidyoNetworkInterfaceState) {
        System.out.println("OnNetworkInterfaceStateUpdated: name=" + vidyoNetworkInterface.GetName() + " address=" + vidyoNetworkInterface.GetAddress() + " type=" + vidyoNetworkInterface.GetType() + " family=" + vidyoNetworkInterface.GetFamily() + " state=" + vidyoNetworkInterfaceState);
        }

@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE) {
        Log.e(TAG, "Unknown request code: " + requestCode);
        return;
        }
        if (resultCode != RESULT_OK) {
        Toast.makeText(this,
        "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
        mToggleButton.setChecked(false);
        return;
        }
        mMediaProjectionCallback = new MediaProjectionCallback();
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        mMediaProjection.registerCallback(mMediaProjectionCallback, null);
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
        }

public void onToggleScreenShare(View view) {
        if (((ToggleButton) view).isChecked()) {
        initRecorder();
        shareScreen();
        } else {
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        Log.v(TAG, "Stopping Recording");
        stopScreenSharing();
        }
        }

private void shareScreen() {
        if (mMediaProjection == null) {
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
        return;
        }
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
        }

private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay("MainActivity",
        DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
        mMediaRecorder.getSurface(), null /*Callbacks*/, null
                /*Handler*/);
        }

private void initRecorder() {
        try {
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setOutputFile(Environment
        .getExternalStoragePublicDirectory(Environment
        .DIRECTORY_DOWNLOADS) + "/video.mp4");
        mMediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
        mMediaRecorder.setVideoFrameRate(30);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int orientation = ORIENTATIONS.get(rotation + 90);
        mMediaRecorder.setOrientationHint(orientation);
        mMediaRecorder.prepare();
        } catch (IOException e) {
        e.printStackTrace();
        }
        }

private class MediaProjectionCallback extends MediaProjection.Callback {
    @Override
    public void onStop() {
        if (mToggleButton.isChecked()) {
            mToggleButton.setChecked(false);
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            Log.v(TAG, "Recording Stopped");
        }
        mMediaProjection = null;
        stopScreenSharing();
    }
}

    private void stopScreenSharing() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        //mMediaRecorder.release(); //If used: mMediaRecorder object cannot
        // be reused again
        destroyMediaProjection();
    }

    public void onDestroy2() {
        super.onDestroy();
        destroyMediaProjection();
    }

    private void destroyMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mMediaProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.i(TAG, "MediaProjection Stopped");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if ((grantResults.length > 0) && (grantResults[0] +
                        grantResults[1]) == PackageManager.PERMISSION_GRANTED) {
                    onToggleScreenShare(mToggleButton);
                } else {
                    mToggleButton.setChecked(false);
                    Snackbar.make(findViewById(android.R.id.content), R.string.label_permissions,
                            Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                    startActivity(intent);
                                }
                            }).show();
                }
                return;
            }
        }
    }

//*****************************remote controller
    void findController() {
        int[] deviceIds = mInputManager.getInputDeviceIds();
        for (int deviceId : deviceIds) {
            InputDevice dev = mInputManager.getInputDevice(deviceId);
            int sources = dev.getSources();
            if (((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) ||
                    ((sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)) {
                mInputDevice = InputDevice.getDevice(deviceId);
            }
        }
    }
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
            final int historySize = event.getHistorySize();
            for (int i = 0; i < historySize; i++) {
                processJoystickInput(event, i);
            }

            processJoystickInput(event, -1);

        return true;
    }


    private void processJoystickInput(MotionEvent event, int historyPos) {
        if (null == mInputDevice) {
            mInputDevice = event.getDevice();
        }
        float x = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_X, historyPos);
        if (x == 0) {
            x = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_HAT_X, historyPos);
        }
        if (x == 0) {
            x = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_Z, historyPos);
        }

        float y = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_Y, historyPos);
        if (y == 0) {
            y = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_HAT_Y, historyPos);
        }
        if (y == 0) {
            y = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_RZ, historyPos);
        }

        if(x==1 && y==0)
            blu_str="R";
        else if(x==(-1) && y==0)
            blu_str="L";
        else if(x==0 && y==1)
            blu_str="B";
        else if(x==0 && y==(-1))
            blu_str="F";
        else
            blu_str="N";
    }

    private static float getCenteredAxis(MotionEvent event, InputDevice device,
                                         int axis, int historyPos) {
        final InputDevice.MotionRange range = device.getMotionRange(axis, event.getSource());
        if (range != null) {
            final float flat = range.getFlat();
            final float value = historyPos < 0 ? event.getAxisValue(axis)
                    : event.getHistoricalAxisValue(axis, historyPos);

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            // A joystick at rest does not always report an absolute position of
            // (0,0).
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];

            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {

                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimut = orientation[0];
//              pitch = orientation[1];
                roll = orientation[2];
                int y = (int) Math.toDegrees(azimut);
                int x = (int) Math.toDegrees(roll);
                //vertical
                if(x<0)
                    x+=360;
                if(this.firstX){
                    this.initial_x=x;
                    this.firstX=false;
                }
                if(this.initial_x>269){
                    if(x<90)
                        x+=360;
                }
                else if(this.initial_x<90){
                    if(x>270)
                        x-=360;
                }
                if(this.initial_x-x>0 && this.initial_x-x<91)
                    x=90-(this.initial_x-x);
                else if(this.initial_x-x<0 && this.initial_x-x>-91)
                    x=90-(this.initial_x-x);
                else
                    x=90;

                //horizontal
                if(y>0)
                    y-=360;
                y*=(-1);
                if(this.firstY){
                    this.initial_y=y;
                    this.firstY=false;
                }
                if(this.initial_y>269){
                    if(y<90)
                        y+=360;
                }
                else if(this.initial_y<90){
                    if(y>270)
                        y-=360;
                }
                if(this.initial_y-y>0 && this.initial_y-y<91)
                    y=90-(this.initial_y-y);
                else if(this.initial_y-y<0 && this.initial_y-y>-91)
                    y=90-(this.initial_y-y);
                else
                    y=90;
                x/=5;
                y/=5;
                x+=35;
                y+=35;
                strX = Character.toString((char)x);
                strY = Character.toString((char)y);

            }
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    String getData()
    {
        String s="";

        //s=s+x.getText().toString()+"|"+y.getText().toString()+"|"+z.getText().toString()+"|"+ss;
        s=s+blu_str+strX+strY;
        System.out.println("************String :"+s);
        return s;
    }


    @Override
    public void onInputDeviceAdded(int deviceId) {
        mInputDevice = InputDevice.getDevice(deviceId);
    }

    @Override
    public void onInputDeviceRemoved(int deviceId) {
        Toast.makeText(getApplicationContext(), "JoyStick Diconnected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInputDeviceChanged(int deviceId) {
        mInputDevice = InputDevice.getDevice(deviceId);
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Exit")
                .setMessage("Do you want to Disconnect Call?")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(user.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    }
                }).setNegativeButton("no", null).show();
    }
}


