package com.zebra.jamesswinton.beaconproximitypoc;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeaconManagerWrapper {

    // Beacon Management Variables
    private final Context mContext;
    private BeaconManager mBeaconManager;
    private final BeaconConsumer mBeaconConsumer;
    private final MonitorNotifier mBeaconMonitorNotifier;
    private final RangeNotifier mBeaconRangeNotifier;

    // Region Holder (Boolean value indicates if region has been entered this charge cycle)
    public Map<Region, Boolean> mBeaconRegions = new HashMap<>();

    public BeaconManagerWrapper(Context context,
                                BeaconConsumer beaconConsumer,
                                MonitorNotifier monitorNotifier,
                                RangeNotifier rangeNotifier) {
        mContext = context;
        mBeaconConsumer = beaconConsumer;
        mBeaconMonitorNotifier = monitorNotifier;
        mBeaconRangeNotifier = rangeNotifier;
    }

    /**
     * Initialisation
     */

    public void init() {
        // Get Singleton Instance
        mBeaconManager = BeaconManager.getInstanceForApplication(mContext.getApplicationContext());

        // Clear All Existing Regions From Disk
        mBeaconManager.setRegionStatePersistenceEnabled(false);

        // Set Rate
        int intervalBetweenScans = App.mConfig.getScanParameters().getIntervalBetweenScans();
        int scanDuration = App.mConfig.getScanParameters().getScanDuration();
        mBeaconManager.setBackgroundBetweenScanPeriod(Math.max(intervalBetweenScans, 0));
        mBeaconManager.setBackgroundScanPeriod(Math.min(scanDuration, 1100));

        // Set Beacon Layouts
        mBeaconManager.getBeaconParsers().clear();
        for (String beaconLayout : App.mConfig.getScanParameters().getBeaconLayouts()) {
            mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(beaconLayout));
        }

        // Bind to BeaconConsumer
        mBeaconManager.bind(mBeaconConsumer);
    }

    public void deInit() {
        // Unbind BeaconConsumer
        mBeaconManager.unbind(mBeaconConsumer);

        // Stop Monitoring
        try {
            stopMonitoringForBeaconsInAllRegions();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Scanning
     */

    public void startMonitoringForBeaconsInAllRegions() throws RemoteException {
        // Reset Notifiers
        mBeaconManager.removeAllMonitorNotifiers();

        // Add New Notifier
        mBeaconManager.addMonitorNotifier(mBeaconMonitorNotifier);
        mBeaconManager.addRangeNotifier(mBeaconRangeNotifier);

        // Build Regions from Config
        for (Config.Beacon beacon : App.mConfig.getBeacons()) {
            String regionId = String.valueOf(App.mConfig.getBeacons().indexOf(beacon));
            String regionBtMac = String.valueOf(beacon.getBeaconMac());
            mBeaconRegions.put(new Region(regionId, regionBtMac), false);
        }

        // Start Monitoring Region
        for (Region region : mBeaconRegions.keySet()) {
            mBeaconManager.startMonitoringBeaconsInRegion(region);
            Log.i(this.getClass().getName(), "Started Monitoring region: "
                    + region.getUniqueId() + " | MAC: + " + region.getBluetoothAddress());
        }
    }

    public void stopMonitoringForBeaconsInAllRegions() throws RemoteException {
        for (Region beaconRegion : mBeaconRegions.keySet()) {
            mBeaconManager.stopMonitoringBeaconsInRegion(beaconRegion);
            mBeaconManager.stopRangingBeaconsInRegion(beaconRegion);
        }
    }

    public void startRangingBeaconsInRegion(Region region) throws RemoteException {
        mBeaconManager.startRangingBeaconsInRegion(region);
    }

    /**
     * Region Monitoring
     */

    public void declareRegionEntered(Region region) {
        mBeaconRegions.put(region, true);
    }

    public void declareRegionExit(Region region) {
        mBeaconRegions.put(region, false);
    }

    public void resetAllRegionStates() {
        for (Region region : mBeaconRegions.keySet()) {
            declareRegionExit(region);
        }
    }

}
