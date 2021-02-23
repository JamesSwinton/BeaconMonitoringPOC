package com.zebra.jamesswinton.beaconproximitypoc;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.databinding.DataBindingUtil;

import com.zebra.jamesswinton.beaconproximitypoc.databinding.LayoutPromotionBinding;
import com.zebra.jamesswinton.beaconproximitypoc.utilities.NotificationHelper;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class BeaconMonitoringService extends Service implements BeaconConsumer, MonitorNotifier, RangeNotifier {

    // Beacon
    private BeaconManagerWrapper mBeaconManager;

    // Promotions
    private PromotionDisplayManager mPromotionDisplayManager;

    @Override
    public void onCreate() {
        super.onCreate();

        // Init Beacon Manager
        mBeaconManager = new BeaconManagerWrapper(this,
                this, this, this);
        mBeaconManager.init();

        // Init Promotion Display Manager
        mPromotionDisplayManager = new PromotionDisplayManager(this);

        // Register Broadcast Receiver
        registerReceiver(mChargeStateBroadcastReceiver,
                new IntentFilter(Intent.ACTION_POWER_CONNECTED));

        // StartUp
        startForeground(NotificationHelper.NOTIFICATION_ID,
                NotificationHelper.createNotification(this));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String intentAction = intent.getAction();
            if (intentAction == null) {
                return START_STICKY;
            } else {
                switch (intentAction) {
                    case NotificationHelper.ACTION_STOP_SERVICE: {
                        stopSelf(startId);
                        return START_NOT_STICKY;
                    }
                    default: {
                        return START_NOT_STICKY;
                    }
                }
            }
        } else {
            return START_NOT_STICKY;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBeaconManager.deInit();
        unregisterReceiver(mChargeStateBroadcastReceiver);
    }

    /********************
     * Beacon Callbacks *
     ********************/

    @Override
    public void onBeaconServiceConnect() {
        try {
            mBeaconManager.startMonitoringForBeaconsInAllRegions();
        } catch (RemoteException e) {
            e.printStackTrace();
            Toast.makeText(this, "Could not start monitoring for beacons",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void didEnterRegion(Region region) {
        Log.i(this.getClass().getName(), "Region Entered: " + region.getUniqueId() +
                " | MAC: " + region.getBluetoothAddress());

        // Query Config for Promotion Details based on Beacon MAC
        for (Config.Beacon configBeacon : App.mConfig.getBeacons()) {
            // Check if we detect a beacon that we've registered in our config
            if (configBeacon.getBeaconMac().matches(region.getBluetoothAddress())) {
                // Check if we should perform our own ranging, or fire promotion as soon as we see beacon
                if (App.mConfig.getScanParameters().getPerformBeaconRanging() || App.mConfig.getScanParameters().getPerformBeaconRssiRanging() ) {
                    // Start Ranging on Beacons
                    try {
                        mBeaconManager.startRangingBeaconsInRegion(region);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Trigger Promotion
                    displayPromotion(configBeacon, region);
                }
            }
        }
    }

    @Override
    public void didExitRegion(Region region) {
        Log.i(this.getClass().getName(), "Region Exited: " + region.getUniqueId() +
                " | MAC: " + region.getBluetoothAddress());
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {
        // Not Supported
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

        int rssi;
        // Loop Beacons Within Range
        for (Beacon beacon: beacons) {
            if (App.mConfig.getScanParameters().getPerformBeaconRanging()) {
                // Verify Distance against Configuration
                if (beacon.getDistance() < App.mConfig.getScanParameters().getMetersToTriggerPromotion()) {
                    Log.i(this.getClass().getName(), "Check against distance:" + beacon.getDistance() + " rssi:" + beacon.getRssi() + ", checking if unique");
                    // We're within range, so lets find the beacon details in our config
                    for (Config.Beacon configBeacon : App.mConfig.getBeacons()) {
                        if (configBeacon.getBeaconMac().matches(region.getBluetoothAddress())) {
                            displayPromotion(configBeacon, region);
                        }
                    }
                }
            }
            if (App.mConfig.getScanParameters().getPerformBeaconRssiRanging()) {
                // Verify Distance against RSSI threshold
                rssi = beacon.getRssi();
                Log.i(this.getClass().getName(), "MAC:"+ beacon.getBluetoothAddress() + " distance:" + beacon.getDistance() + " rssi:" + rssi);
                if (rssi > App.mConfig.getScanParameters().getThresholdRssiToTriggerPromotions()) {
                    // We're within range, so lets find the beacon details in our config
                    for (Config.Beacon configBeacon : App.mConfig.getBeacons()) {
                        if (configBeacon.getBeaconMac().matches(region.getBluetoothAddress())) {
//                            Log.i(this.getClass().getName(), configBeacon.getBeaconName() + " : " + configBeacon.getBeaconMac()  + " - Distance:" + beacon.getDistance() + " rssi:" + rssi);
                            displayPromotion(configBeacon, region);
                        }
                    }
                }
            }
        }
    }

    private void displayPromotion(Config.Beacon configBeacon, Region region) {

        // Verify we haven't been within range of this beacon this shopping trip
        if (!mBeaconManager.mBeaconRegions.get(region)) {

//        if (!App.mConfig.getScanParameters().getLimitPromotions() ||
//                (App.mConfig.getScanParameters().getLimitPromotions()
//                        && !mBeaconManager.mBeaconRegions.get(region))) {

            Log.i(this.getClass().getName(), "Region is unique. Displaying promotion");
            // Store Entered Region
            mBeaconManager.declareRegionEntered(region);

            // Display Promotion
            mPromotionDisplayManager.showAlertDialogPromotion(configBeacon.getPromotionTitle(),
                    configBeacon.getPromotionMessage(), configBeacon.getPromotionImage(),
                    configBeacon.getPromotionSplash(), configBeacon.getPromotionTimeout());

            // Play Sound
            if (configBeacon.getTTSEnabled()) {
                mPromotionDisplayManager.playTtsPromotion(configBeacon.getTTS());
            }

            if (!App.mConfig.getScanParameters().getLimitPromotions() ) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBeaconManager.declareRegionExit(region);
                    }
                }, (App.mConfig.getScanParameters().getLimitPromotionsTimeout() * 1000)); //timeout given is in seconds
            }
        }
    }

    /************************
     * Monitor Charge State *
     ************************/

    private final BroadcastReceiver mChargeStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            if (intentAction != null && intentAction.equals(Intent.ACTION_POWER_CONNECTED)) {
                mBeaconManager.resetAllRegionStates();
            }
        }
    };

    /***************
     * Unsupported *
     ***************/

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("This feature is not supported");
    }
}
