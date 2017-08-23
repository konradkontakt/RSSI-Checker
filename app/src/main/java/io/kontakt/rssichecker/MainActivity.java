package io.kontakt.rssichecker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.filter.ibeacon.IBeaconFilter;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.EddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleEddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.ble.spec.EddystoneFrameType;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static String API_KEY = "Your API KEY";

    private ProximityManager proximityManager;

    String TAG = "XYZ";

    public int rssi;

    //set unique id for the eddystone beacon you will be looking for
    private static String uniqueIdEddystone = "wsuT";

    //set unique id for the beacon broadcasting iBeacon that you will be looking for
    private static String uniqueIdIBeacon = "wsuT";

    public device device;

    private UUID uuid = UUID.fromString("6473724b-fde8-4cd8-bac1-3db4e4271033");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissionAndStart();
        KontaktSDK.initialize(API_KEY);
        if (KontaktSDK.isInitialized())
            Log.d(TAG, "SDK initialised");
        Button startButton = (Button) findViewById(R.id.button);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScanning();
            }
        });
        Button stopButton = (Button) findViewById(R.id.button2);
        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopScanning();
            }
        });
        this.proximityManager = ProximityManagerFactory.create(this);
        this.proximityManager.setIBeaconListener(iBeaconListener);
        this.proximityManager.setEddystoneListener(eddystoneListener);
        this.proximityManager.configuration()
                .activityCheckConfiguration(ActivityCheckConfiguration.DEFAULT)
                .scanPeriod(ScanPeriod.RANGING)
                .scanMode(ScanMode.BALANCED)
                .eddystoneFrameTypes(EnumSet.of(EddystoneFrameType.TLM))
                .deviceUpdateCallbackInterval(300);
    }
/*  //Apply filter for your UUID, then to the Listeners go in only beacons which passes this filter
    private void setFilters(){
        IBeaconFilter customIBeaconFilter = new IBeaconFilter() {
            @Override
            public boolean apply(IBeaconDevice iBeaconDevice) {
                // So here we set the max distance from a beacon to 1m
                return iBeaconDevice.getProximityUUID() == uuid;
            }
        };

        proximityManager.filters().iBeaconFilter(customIBeaconFilter);
    }
*/

/*    // Creating Safe and Danger zones
    public void declareZones(){
        if (rssi != 0 && rssi > -77) {
            device.isInSafeZone();
        }
        if (rssi != 0 && rssi < -80){
            device.isInDangerZone();
        }
    }
*/

    private void checkPermissionAndStart() {
        int checkSelfPermissionResult = ContextCompat.checkSelfPermission(this, Arrays.toString(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE}));
        if (PackageManager.PERMISSION_GRANTED == checkSelfPermissionResult) {
            //already granted
            Log.d(TAG,"Permission already granted");
        }
        else {
            //request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            Log.d(TAG,"Permission request called");
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (100 == requestCode) {
                Log.d(TAG,"Permission granted");
            }
        } else
        {
            Log.d(TAG,"Permission not granted");
        }
    }

    private final IBeaconListener iBeaconListener = new SimpleIBeaconListener() {
        @Override
        public void onIBeaconDiscovered(IBeaconDevice ibeacon, IBeaconRegion region) {
            super.onIBeaconDiscovered(ibeacon, region);
            if (uniqueIdIBeacon.equals(ibeacon.getUniqueId())) {
                Log.d("RSSI value for beacon ", ibeacon.getUniqueId() + ": " + ibeacon.getRssi());
            }
        }

        @Override
        public void onIBeaconsUpdated(List<IBeaconDevice> ibeacons, IBeaconRegion region) {
            super.onIBeaconsUpdated(ibeacons, region);
            for (IBeaconDevice ibeacon : ibeacons) {
                if (uniqueIdIBeacon.equals(ibeacon.getUniqueId())) {
                    Log.d("RSSI update for beacon ", ibeacon.getUniqueId() + ": " + ibeacon.getRssi());
                }
            }
        }

        @Override
        public void onIBeaconLost(IBeaconDevice ibeacon, IBeaconRegion region) {
            super.onIBeaconLost(ibeacon, region);
            if (uniqueIdIBeacon.equals(ibeacon.getUniqueId())) {
                Log.d("Beacon Lost", ibeacon.getUniqueId());
            }
        }
    };
    // receiving the temp by Eddystone TLM packet
    private final EddystoneListener eddystoneListener = new SimpleEddystoneListener() {
        @Override
        public void onEddystoneDiscovered(IEddystoneDevice eddystone, IEddystoneNamespace namespace) {
            super.onEddystoneDiscovered(eddystone, namespace);
            if (uniqueIdEddystone.equals(eddystone.getUniqueId())) {
                Log.d("CPU temp", String.valueOf(eddystone.getTelemetry().getTemperature()));
            }
        }

        @Override
        public void onEddystonesUpdated(List<IEddystoneDevice> eddystones, IEddystoneNamespace namespace) {
            super.onEddystonesUpdated(eddystones, namespace);

        }

        @Override
        public void onEddystoneLost(IEddystoneDevice eddystone, IEddystoneNamespace namespace) {
            super.onEddystoneLost(eddystone, namespace);
        }
    };

    private void startScanning(){
        proximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                proximityManager.startScanning();
            }
        });
    }
    private void stopScanning() {
        proximityManager.disconnect();
    }
}
