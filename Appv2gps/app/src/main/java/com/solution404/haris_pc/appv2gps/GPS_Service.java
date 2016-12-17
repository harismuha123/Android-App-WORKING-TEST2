package com.solution404.haris_pc.appv2gps;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;


public class GPS_Service extends Service {

    private static final String TAG = "org.eclipse.paho.android.service.MqttService";
    private static final String LOGTAG = "org.eclipse.paho.android.service.MqttService";

    String clientId = "telefon";
    private MqttAndroidClient client;



    private LocationListener listener;
    private LocationManager locationManager;
    private MqttMessage message;

    @Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException (Thread thread, Throwable e)
            {
                handleUncaughtException (thread, e);
            }
        });

        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://solution404.io:1884", clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {


                @SuppressLint("LongLogTag")
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(LOGTAG, "onSuccess");
                    Toast.makeText(getApplicationContext(), "connected", Toast.LENGTH_SHORT).show();
                    message = new MqttMessage("Hello, I am Android Mqtt Client.".getBytes());
                    message.setQos(1);
                    message.setRetained(false);

                    try {
                            client.publish("messages", message);
                        Log.i(LOGTAG, "Message published");

                        client.disconnect();
                        Log.i(LOGTAG, "client disconnected");
                    } catch (MqttPersistenceException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (MqttException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }

                @SuppressLint("LongLogTag")
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getApplicationContext(), "connection failed", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();

        }




        listener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                Toast.makeText(getApplicationContext(),"updated location" , Toast.LENGTH_SHORT).show();
                Intent i = new Intent("location_update");
                i.putExtra("coordinates",location.getLongitude()+" "+location.getLatitude());
                sendBroadcast(i);
                Toast.makeText(getApplicationContext(),String.valueOf(String.valueOf(location.getLatitude())+","+String.valueOf(location.getLongitude()) ), Toast.LENGTH_SHORT).show();


                try {
                    IMqttToken token = client.connect();
                    token.setActionCallback(new IMqttActionListener() {

                        @SuppressLint("LongLogTag")
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            // We are connected
                            Log.d(TAG, "onSuccess");
                            message = new MqttMessage(String.valueOf(String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude())).getBytes());
                            message.setQos(1);
                            message.setRetained(false);

                            try {
                                Toast.makeText(getApplicationContext(), String.valueOf(message), Toast.LENGTH_SHORT);
                                    client.publish("location/583048e227ea4d2c35a18e46", message);

                                Log.i(LOGTAG, "Message published");

                                //client.disconnect();
                                Log.i(LOGTAG, "client disconnected");
                            } catch (MqttException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } finally {
                                Toast.makeText(getApplicationContext(), "ne zafrkavaj", Toast.LENGTH_SHORT);
                            }

                        }

                        @SuppressLint("LongLogTag")
                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            // Something went wrong e.g. connection timeout or firewall problems
                            Log.d(TAG, "onFailure");

                        }
                    });
                } catch (MqttException e) {
                    e.printStackTrace();

                }

            }


            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                //Toast.makeText(getApplicationContext(),"change status" , Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onProviderEnabled(String provider) {
                //Toast.makeText(getApplicationContext(),"provider enabled" , Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderDisabled(String provider) {
                //Toast.makeText(getApplicationContext(),"provider disabled" , Toast.LENGTH_SHORT).show();

            }
        };
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //noinspection MissingPermission
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, listener);
        Toast.makeText(getApplicationContext(),"Proba" , Toast.LENGTH_SHORT).show();

    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null){
            locationManager.removeUpdates(listener);
        }
    }

    public void handleUncaughtException (Thread thread, Throwable e)
    {
        e.printStackTrace(); // not all Android versions will print the stack trace automatically

        Intent intent = new Intent ();
        intent.setAction ("com.mydomain.SEND_LOG"); // see step 5.
        intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
        startActivity (intent);

        System.exit(1); // kill off the crashed app
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
